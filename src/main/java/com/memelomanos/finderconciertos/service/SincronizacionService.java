package com.memelomanos.finderconciertos.service;

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
import java.util.Optional;
import java.util.Set;

@Service
public class SincronizacionService {

    private final ConciertoRepository conciertos;
    private final AgregadorConciertosService agregador;
    private final TransactionTemplate transaccion;

    public SincronizacionService(
            ConciertoRepository conciertos,
            AgregadorConciertosService agregador,
            PlatformTransactionManager gestorTransacciones
    ) {
        this.conciertos = conciertos;
        this.agregador = agregador;
        this.transaccion = new TransactionTemplate(gestorTransacciones);
    }

    @Scheduled(cron = "${sincronizacion.ticketmaster.cron:-}", zone = "UTC")
    public synchronized int sincronizar() {
        var catalogo = agregador.descargarCatalogos();
        if (catalogo.conciertos().isEmpty()) {
            // Un catálogo vacío no confirma el retiro de los eventos guardados.
            return 0;
        }

        return transaccion.execute(estado -> {
            int guardados = guardarEnTransaccion(catalogo.conciertos());
            catalogo.idsPorFuenteCompleta().forEach(this::marcarAusentes);
            return guardados;
        });
    }

    public synchronized int guardar(List<Concierto> eventos) {
        // Abre la transacción después de consultar las APIs externas, incluso en llamadas de esta clase.
        // El bloqueo evita que dos sincronizaciones creen a la vez el mismo evento en esta instancia.
        return transaccion.execute(estado -> guardarEnTransaccion(eventos));
    }

    private void marcarAusentes(String fuente, Set<String> recibidos) {
        if (!StringUtils.hasText(fuente) || recibidos == null || recibidos.isEmpty()) {
            return;
        }
        LocalDate hoy = LocalDate.now(ZoneId.of("America/Mexico_City"));
        for (Concierto concierto : conciertos.findByFuenteAndDisponibleTrueAndFechaGreaterThanEqual(
                fuente, hoy)) {
            if (!recibidos.contains(concierto.getIdExterno())) {
                concierto.setDisponible(false);
            }
        }
    }

    private int guardarEnTransaccion(List<Concierto> eventos) {
        int guardados = 0;

        for (Concierto evento : eventos) {
            if (!StringUtils.hasText(evento.getFuente()) || !StringUtils.hasText(evento.getIdExterno())) {
                continue;
            }

            Concierto concierto = buscarExistente(evento).orElseGet(Concierto::new);

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
            if (!evento.getOpcionesCompra().isEmpty()) {
                concierto.reemplazarOpcionesCompra(evento.getOpcionesCompra());
            }

            conciertos.save(concierto);
            guardados++;
        }

        return guardados;
    }

    private Optional<Concierto> buscarExistente(Concierto evento) {
        Optional<Concierto> porIdExterno = conciertos.findByFuenteAndIdExterno(
                evento.getFuente(), evento.getIdExterno()
        );
        if (porIdExterno.isPresent() || evento.getFecha() == null) {
            return porIdExterno;
        }

        return conciertos.findByFecha(evento.getFecha()).stream()
                .filter(existente -> AgregadorConciertosService.mismoConcierto(existente, evento))
                .findFirst();
    }
}
