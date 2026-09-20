package com.memelomanos.finderconciertos.repository;

import com.memelomanos.finderconciertos.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository de Usuario. JpaRepository nos da los métodos básicos (save, findById, etc.).
 * Aquí agregamos las búsquedas por correo.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
    * Revisa si el correo ya existe en la base de datos.
    */
    boolean existsByCorreo(String correo);

    /**
    * Busca a un usuario por su correo en la base de datos.
    */
    Optional<Usuario> findByCorreo(String correo);
}