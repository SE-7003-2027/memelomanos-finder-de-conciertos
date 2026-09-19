package com.memelomanos.finderconciertos.service;

import com.memelomanos.finderconciertos.model.Concierto;
import com.memelomanos.finderconciertos.repository.ConciertoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Aqui va la logica de negocio de Concierto. Por ahora es simple
 * (solo delega al repository), pero si mas adelante necesitamos
 * validar algo o combinar datos de varias fuentes, va aqui y no en
 * el controller.
 */
@Service
public class ConciertoService {

    private final ConciertoRepository conciertoRepository;

    @Autowired
    public ConciertoService(ConciertoRepository conciertoRepository) {
        this.conciertoRepository = conciertoRepository;
    }

    public List<Concierto> buscarPorArtista(String artista) {
        return conciertoRepository.findByArtistaContainingIgnoreCase(artista);
    }
}
