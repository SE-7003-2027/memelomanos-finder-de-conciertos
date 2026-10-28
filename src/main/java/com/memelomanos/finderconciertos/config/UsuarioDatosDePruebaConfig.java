package com.memelomanos.finderconciertos.config;

import com.memelomanos.finderconciertos.model.Usuario;
import com.memelomanos.finderconciertos.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Esta clase sirve para cargar usuarios de prueba al iniciar la aplicacion
 * para poder probar el endpoint GET /usuarios/perfil sin la base vacia.
 */
@Configuration
public class UsuarioDatosDePruebaConfig {

    @Bean
    CommandLineRunner cargarUsuariosDePrueba(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String defaultHashedPassword = passwordEncoder.encode("password123");

            // Evita duplicar los usuarios en cada arranque.
            if (!usuarioRepository.existsByCorreo("flanconsalsa67@gmail.com")) {
                usuarioRepository.save(new Usuario("Marilu Putowsky", "flanconsalsa67@gmail.com", defaultHashedPassword));
            }
            if (!usuarioRepository.existsByCorreo("pollosasados99@gmail.com")) {
                usuarioRepository.save(new Usuario("Eduardo Jimenez", "pollosasados99@gmail.com", defaultHashedPassword));
            }
        };
    }
}
