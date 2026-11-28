package com.memelomanos.finderconciertos.integracion.soundcloud;

import com.fasterxml.jackson.annotation.JsonValue;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

/**
 * Lo que regresa GET /artistas/{nombre}/soundcloud (schema
 * "ArtistaSoundCloud" en openapi.yml). Solo lleva datos publicos del
 * artista: nada de tokens ni credenciales.
 *
 * @param fuente "api" si salio de la API con credenciales u "oembed" si
 *               salio del endpoint publico
 * @param origen como se eligio el perfil (ver {@link Origen})
 */
public record ArtistaSoundCloud(
        String artista,
        String perfilUrl,
        String avatarUrl,
        String widgetUrl,
        List<PistaSoundCloud> pistas,
        String fuente,
        Origen origen) {

    static final String WIDGET_URL = "https://w.soundcloud.com/player/";

    /** URL del reproductor embebible de SoundCloud para un perfil o una pista. */
    static String widgetPara(String urlSoundCloud) {
        return WIDGET_URL + "?url=" + URLEncoder.encode(urlSoundCloud, StandardCharsets.UTF_8)
                + "&show_comments=false&visual=false";
    }

    public record PistaSoundCloud(String titulo, String url, long duracionMs) {
    }

    /**
     * De donde salio el perfil. La interfaz lo usa para ofrecer "¿No es
     * este artista?" cuando la coincidencia no la confirmo una persona.
     */
    public enum Origen {
        /** soundcloudUrl capturada a mano en un concierto. */
        MANUAL,
        /** Busqueda por nombre en la API con credenciales (Plan A). */
        API,
        /** URL deducida del nombre y confirmada con oEmbed (Plan C). */
        AUTOMATICO;

        @JsonValue
        public String valor() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
