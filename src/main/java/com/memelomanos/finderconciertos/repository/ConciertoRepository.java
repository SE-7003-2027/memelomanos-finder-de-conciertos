package com.memelomanos.finderconciertos.repository;

import com.memelomanos.finderconciertos.model.Concierto;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ConciertoRepository extends
        JpaRepository<Concierto, Long>,
        JpaSpecificationExecutor<Concierto> {

    List<Concierto> findByArtistaContainingIgnoreCase(String artista);

    List<Concierto> findByCiudadContainingIgnoreCase(String ciudad);

    List<Concierto> findByArtistaContainingIgnoreCaseAndCiudadContainingIgnoreCase(
            String artista, String ciudad);

    // Perfil de SoundCloud capturado a mano para un artista (ADR-005).
    Optional<Concierto> findFirstByArtistaIgnoreCaseAndSoundcloudUrlIsNotNull(String artista);

    Optional<Concierto> findByFuenteAndIdExterno(String fuente, String idExterno);

    List<Concierto> findByFuenteAndDisponibleTrueAndFechaGreaterThanEqual(String fuente, LocalDate fecha);
}
