package com.memelomanos.finderconciertos.service;

import com.memelomanos.finderconciertos.integracion.ConciertoExterno;
import com.memelomanos.finderconciertos.integracion.ticketmaster.TicketmasterProveedor;
import com.memelomanos.finderconciertos.model.Concierto;
import com.memelomanos.finderconciertos.repository.ConciertoRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class SincronizacionService {

    private final ConciertoRepository conciertos;
    private final TicketmasterProveedor proveedor;

    public SincronizacionService(ConciertoRepository conciertos, TicketmasterProveedor proveedor) {
        this.conciertos = conciertos;
        this.proveedor = proveedor;
    }

    @Scheduled(cron = "${sincronizacion.ticketmaster.cron:-}", zone = "UTC")
    @Transactional
    public int sincronizar() {
        List<Concierto> eventos = proveedor.buscar(null, null, null, null).stream()
                .map(this::convertirEvento)
                .toList();

        return guardar(eventos);
    }

    private Concierto convertirEvento(ConciertoExterno evento) {
        Concierto concierto = new Concierto(
                evento.artista(), evento.fecha(), evento.ciudad(), evento.lugar()
        );
        concierto.setFuente(evento.fuente());
        concierto.setIdExterno(evento.idExterno());
        concierto.setLatitud(evento.latitud());
        concierto.setLongitud(evento.longitud());
        return concierto;
    }

    @Transactional
    public int guardar(List<Concierto> eventos) {
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

            conciertos.save(concierto);
            guardados++;
        }

        return guardados;
    }
}
