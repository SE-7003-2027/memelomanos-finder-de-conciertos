package com.memelomanos.finderconciertos.service;

import com.memelomanos.finderconciertos.integracion.ConciertoExterno;
import com.memelomanos.finderconciertos.integracion.ticketmaster.TicketmasterProveedor;
import com.memelomanos.finderconciertos.model.Concierto;
import com.memelomanos.finderconciertos.repository.ConciertoRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SincronizacionService {

    private final ConciertoRepository conciertos;
    private final TicketmasterProveedor proveedor;
    private final TransactionTemplate transaccion;

    public SincronizacionService(
            ConciertoRepository conciertos,
            TicketmasterProveedor proveedor,
            PlatformTransactionManager gestorTransacciones
    ) {
        this.conciertos = conciertos;
        this.proveedor = proveedor;
        this.transaccion = new TransactionTemplate(gestorTransacciones);
    }

    @Scheduled(cron = "${sincronizacion.ticketmaster.cron:-}", zone = "UTC")
    public synchronized int sincronizar() {
        var catalogo = proveedor.descargarCatalogo();
        if (catalogo.eventos().isEmpty()) {
            // Un catálogo vacío no confirma el retiro de los eventos guardados.
            return 0;
        }

        List<Concierto> eventos = catalogo.eventos().stream()
                .map(evento -> convertirEvento(evento, catalogo.cancelados()))
                .toList();

        return transaccion.execute(estado -> {
            int guardados = guardarEnTransaccion(eventos);
            if (catalogo.completo()) {
                marcarAusentes(eventos);
            }
            return guardados;
        });
    }

    private void marcarAusentes(List<Concierto> eventos) {
        var recibidos = eventos.stream().map(Concierto::getIdExterno).collect(Collectors.toSet());
        LocalDate hoy = LocalDate.now(ZoneId.of("America/Mexico_City"));
        for (Concierto concierto : conciertos.findByFuenteAndDisponibleTrueAndFechaGreaterThanEqual(
                TicketmasterProveedor.FUENTE, hoy)) {
            if (!recibidos.contains(concierto.getIdExterno())) {
                concierto.setDisponible(false);
            }
        }
    }

    private Concierto convertirEvento(ConciertoExterno evento, Set<String> cancelados) {
        Concierto concierto = new Concierto(
                evento.artista(), evento.fecha(), evento.ciudad(), evento.lugar()
        );
        concierto.setFuente(evento.fuente());
        concierto.setIdExterno(evento.idExterno());
        concierto.setDisponible(evento.idExterno() == null || !cancelados.contains(evento.idExterno()));
        concierto.setLatitud(evento.latitud());
        concierto.setLongitud(evento.longitud());
        concierto.setUrlCompra(evento.urlCompra());
        concierto.setImagenUrl(evento.imagenUrl());
        concierto.setPrecioMinimo(evento.precioMinimo());
        return concierto;
    }

    public synchronized int guardar(List<Concierto> eventos) {
        // Abre la transacción después de consultar la API, incluso en llamadas de esta clase.
        // El bloqueo evita que dos sincronizaciones creen a la vez el mismo evento en esta instancia.
        return transaccion.execute(estado -> guardarEnTransaccion(eventos));
    }

    private int guardarEnTransaccion(List<Concierto> eventos) {
        int guardados = 0;

        for (Concierto evento : eventos) {
            if (!StringUtils.hasText(evento.getFuente()) || !StringUtils.hasText(evento.getIdExterno())) {
                continue;
            }

            Concierto concierto = conciertos.findByFuenteAndIdExterno(evento.getFuente(), evento.getIdExterno())
                    .orElseGet(Concierto::new);

            concierto.setFuente(evento.getFuente());
            concierto.setIdExterno(evento.getIdExterno());
            concierto.setArtista(evento.getArtista());
            concierto.setFecha(evento.getFecha());
            concierto.setCiudad(evento.getCiudad());
            concierto.setLugar(evento.getLugar());
            concierto.setLatitud(evento.getLatitud());
            concierto.setLongitud(evento.getLongitud());
            concierto.setUrlCompra(evento.getUrlCompra());
            concierto.setImagenUrl(evento.getImagenUrl());
            concierto.setPrecioMinimo(evento.getPrecioMinimo());
            concierto.setDisponible(evento.isDisponible());

            conciertos.save(concierto);
            guardados++;
        }

        return guardados;
    }
}
