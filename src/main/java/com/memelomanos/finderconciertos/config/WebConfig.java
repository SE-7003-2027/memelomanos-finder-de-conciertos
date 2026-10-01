package com.memelomanos.finderconciertos.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Agrega el prefijo /api/v1 a todos nuestros controllers, para que
 * coincidan con el "servers.url" del contrato (openapi.yml).
 *
 * Se limita al paquete controller a proposito: si se aplicara a todos
 * los @RestController, tambien moveria los endpoints internos de
 * springdoc (/v3/api-docs) y Swagger UI dejaria de funcionar.
 * Tenerlo en un solo lugar evita repetir "/api/v1" en cada controller
 * y facilita sacar una /api/v2 en el futuro sin romper la v1.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    public static final String PREFIJO_API = "/api/v1";

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(PREFIJO_API,
                HandlerTypePredicate.forBasePackage("com.memelomanos.finderconciertos.controller"));
    }
}
