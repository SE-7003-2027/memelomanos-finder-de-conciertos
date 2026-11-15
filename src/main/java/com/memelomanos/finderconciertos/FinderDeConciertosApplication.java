package com.memelomanos.finderconciertos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// Clase principal: arranca la aplicacion de Spring Boot.
// No debe tener logica de negocio, solo el punto de entrada.
@SpringBootApplication
@EnableScheduling
public class FinderDeConciertosApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinderDeConciertosApplication.class, args);
    }
}
