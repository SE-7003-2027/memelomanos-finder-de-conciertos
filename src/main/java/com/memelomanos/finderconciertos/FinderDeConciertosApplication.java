package com.memelomanos.finderconciertos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Clase principal: arranca la aplicacion de Spring Boot.
// No debe tener logica de negocio, solo el punto de entrada.
@SpringBootApplication
public class FinderDeConciertosApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinderDeConciertosApplication.class, args);
    }
}
