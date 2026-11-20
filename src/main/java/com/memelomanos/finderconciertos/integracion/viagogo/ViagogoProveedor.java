package com.memelomanos.finderconciertos.integracion.viagogo;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

@Component
public class ViagogoProveedor {

    private static final String URL_BUSQUEDA =
            "https://www.viagogo.com.mx/secure/Search?q=";

    public String generarUrlBusqueda(String artista) {
        if (artista == null || artista.isBlank()) {
            throw new IllegalArgumentException("El nombre del artista no puede estar vacío");
        }

        String artistaCodificado = URLEncoder.encode(
                artista.trim(),
                StandardCharsets.UTF_8
        );

        return URL_BUSQUEDA + artistaCodificado;
    }
}
