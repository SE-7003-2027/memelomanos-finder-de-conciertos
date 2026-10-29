package com.memelomanos.finderconciertos.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuracion web de la aplicacion:
 * 1. Agrega el prefijo /api/v1 a todos nuestros controllers para coincidir con openapi.yml.
 * 2. Configura CORS para permitir la comunicacion con el frontend en Angular.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    public static final String PREFIJO_API = "/api/v1";

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(PREFIJO_API,
                HandlerTypePredicate.forBasePackage("com.memelomanos.finderconciertos.controller"));
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                // Permite el puerto por defecto de desarrollo de Angular (4200), se puede cambiar si es necesario
                .allowedOrigins("http://localhost:4200", "http://127.0.0.1:4200")
                // Metodos HTTP permitidos
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                // Encabezados permitidos (se ocupan para JWT y JSON)
                .allowedHeaders("Authorization", "Content-Type", "Accept", "X-Requested-With", "Origin")
                // Encabezados que Angular podra leer en la respuesta
                .exposedHeaders("Authorization")
                // Permite credenciales (cookies o headers de autenticacion)
                .allowCredentials(true)
                // Cache de preflight (OPTIONS) por 1 hora
                .maxAge(3600);
    }
}
