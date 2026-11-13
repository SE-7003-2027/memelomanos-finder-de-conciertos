package com.memelomanos.finderconciertos.integracion.superboletos;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

@Component
public class SuperboletosProveedor {

    private static final String URL_BUSQUEDA =
            "https://www.superboletos.com/busqueda?query=";

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
