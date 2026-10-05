package com.memelomanos.finderconciertos.repository;

import com.memelomanos.finderconciertos.model.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByCorreo(String correo);

    Optional<Usuario> findByCorreo(String correo);

    @Query("""
            select distinct usuario
            from Usuario usuario
            join usuario.favoritos concierto
            where concierto.id = :conciertoId
            """)
    List<Usuario> findUsuariosConFavorito(
            @Param("conciertoId") Long conciertoId);
}