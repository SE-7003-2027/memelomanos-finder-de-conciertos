package com.memelomanos.finderconciertos.integracion.soundcloud;

import java.util.List;

/**
 * Lo que regresa GET /artistas/{nombre}/soundcloud (schema
 * "ArtistaSoundCloud" en openapi.yml). Solo lleva datos publicos del
 * artista: nada de tokens ni credenciales.
 *
 * @param fuente "api" si salio de la API con credenciales (Plan A) u
 *               "oembed" si salio del endpoint publico (Plan B)
 */
public record ArtistaSoundCloud(
        String artista,
        String perfilUrl,
        String avatarUrl,
        String widgetUrl,
        List<PistaSoundCloud> pistas,
        String fuente) {

    static final String WIDGET_URL = "https://w.soundcloud.com/player/";

    public record PistaSoundCloud(String titulo, String url, long duracionMs) {
    }
}
