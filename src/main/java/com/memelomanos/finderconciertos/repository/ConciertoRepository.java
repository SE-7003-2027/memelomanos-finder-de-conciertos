package com.memelomanos.finderconciertos.repository;

import com.memelomanos.finderconciertos.model.Concierto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository de Concierto. JpaRepository ya nos da los metodos
 * basicos (save, findById, findAll, etc), aqui solo agregamos el
 * metodo que necesitamos para buscar por artista.
 */
public interface ConciertoRepository extends JpaRepository<Concierto, Long> {

    // Spring Data genera la consulta solo con el nombre del metodo.
    // ContainingIgnoreCase hace que busque coincidencias parciales
    // sin importar mayusculas/minusculas (para que "queen" encuentre
    // "Queen" tambien).
    List<Concierto> findByArtistaContainingIgnoreCase(String artista);
}
