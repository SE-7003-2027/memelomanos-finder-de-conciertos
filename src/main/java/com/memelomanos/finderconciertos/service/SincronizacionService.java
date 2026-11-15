package com.memelomanos.finderconciertos.service;

import com.memelomanos.finderconciertos.model.Concierto;
import com.memelomanos.finderconciertos.repository.ConciertoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class SincronizacionService {

    private final ConciertoRepository conciertos;

    public SincronizacionService(ConciertoRepository conciertos) {
        this.conciertos = conciertos;
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
