package com.memelomanos.finderconciertos.integracion.soundcloud;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    /** URL del reproductor embebible de SoundCloud para un perfil o una pista. */
    static String widgetPara(String urlSoundCloud) {
        return WIDGET_URL + "?url=" + URLEncoder.encode(urlSoundCloud, StandardCharsets.UTF_8)
                + "&show_comments=false&visual=false";
    }

    public record PistaSoundCloud(String titulo, String url, long duracionMs) {
    }
}
