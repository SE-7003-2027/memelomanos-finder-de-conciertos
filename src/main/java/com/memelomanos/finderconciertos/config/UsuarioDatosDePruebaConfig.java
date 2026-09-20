package com.memelomanos.finderconciertos.config;

import com.memelomanos.finderconciertos.model.Usuario;
import com.memelomanos.finderconciertos.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Esta clase sirve para cargar usuarios de prueba al iniciar la aplicacion
 * para poder probar el endpoint GET /usuarios/perfil sin la base vacia.
 */
@Configuration
public class UsuarioDatosDePruebaConfig {

    @Bean
    CommandLineRunner cargarUsuariosDePrueba(UsuarioRepository usuarioRepository) {
        return args -> {
            usuarioRepository.save(new Usuario("Marilu Putowsky", "flanconsalsa67@gmail.com"));
            usuarioRepository.save(new Usuario("Eduardo Jimenez", "pollosasados99@gmail.com"));
        };
    }
}