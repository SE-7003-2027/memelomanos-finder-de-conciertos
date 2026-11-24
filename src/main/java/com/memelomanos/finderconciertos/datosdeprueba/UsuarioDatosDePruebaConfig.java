package com.memelomanos.finderconciertos.datosdeprueba;

import com.memelomanos.finderconciertos.model.RolUsuario;
import com.memelomanos.finderconciertos.model.Usuario;
import com.memelomanos.finderconciertos.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Esta clase sirve para cargar usuarios de prueba al iniciar la aplicacion
 * para poder probar el endpoint GET /usuarios/perfil y los roles (ADMIN y USUARIO)
 * sin la base vacia.
 */
// TODO(#95): borrar estos usuarios cuando el registro de usuarios esté estable
@Configuration
@Profile("dev")
public class UsuarioDatosDePruebaConfig {

    @Bean
    CommandLineRunner cargarUsuariosDePrueba(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String defaultHashedPassword = passwordEncoder.encode("password123");

            // Usuario administrador de prueba en perfil dev.
            // Si ya existía en la base local de una versión previa, asegura su rol ADMIN.
            usuarioRepository.findByCorreo("flanconsalsa67@gmail.com")
                    .ifPresentOrElse(usuario -> {
                        if (usuario.getRol() != RolUsuario.ADMIN) {
                            usuario.setRol(RolUsuario.ADMIN);
                            usuarioRepository.save(usuario);
                        }
                    }, () -> usuarioRepository.save(new Usuario(
                            "Marilu Putowsky",
                            "flanconsalsa67@gmail.com",
                            defaultHashedPassword,
                            RolUsuario.ADMIN
                    )));

            // Usuario estándar de prueba (rol USUARIO).
            if (!usuarioRepository.existsByCorreo("pollosasados99@gmail.com")) {
                usuarioRepository.save(new Usuario(
                        "Eduardo Jimenez",
                        "pollosasados99@gmail.com",
                        defaultHashedPassword,
                        RolUsuario.USUARIO
                ));
            }
        };
    }
}
