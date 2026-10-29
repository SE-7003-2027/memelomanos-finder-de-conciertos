package com.memelomanos.finderconciertos.repository;

import com.memelomanos.finderconciertos.model.Concierto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ConciertoRepository extends
        JpaRepository<Concierto, Long>,
        JpaSpecificationExecutor<Concierto> {

    List<Concierto> findByArtistaContainingIgnoreCase(String artista);

    List<Concierto> findByCiudadContainingIgnoreCase(String ciudad);

    List<Concierto> findByArtistaContainingIgnoreCaseAndCiudadContainingIgnoreCase(
            String artista, String ciudad);
}