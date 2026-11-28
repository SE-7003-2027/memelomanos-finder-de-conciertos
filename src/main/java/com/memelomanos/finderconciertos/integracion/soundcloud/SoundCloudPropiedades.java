package com.memelomanos.finderconciertos.integracion.soundcloud;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuracion de SoundCloud (seccion "soundcloud" de application.yml).
 * El client id y el secret salen de variables de entorno y pueden
 * quedar vacios: en ese caso no se usa el Plan A de ADR-005, solo oEmbed.
 *
 * @param duracionCacheNoEncontrados cuanto se recuerda que un artista no
 *        tiene perfil, para no repetir las consultas en cada visita
 */
@ConfigurationProperties(prefix = "soundcloud")
public record SoundCloudPropiedades(
        String clientId,
        String clientSecret,
        @DefaultValue("https://api.soundcloud.com") String apiUrl,
        @DefaultValue("https://secure.soundcloud.com") String authUrl,
        @DefaultValue("https://soundcloud.com/oembed") String oembedUrl,
        @DefaultValue("5") int maxPistas,
        @DefaultValue("24h") Duration duracionCache,
        @DefaultValue("6h") Duration duracionCacheNoEncontrados,
        @DefaultValue("5s") Duration timeout) {

    public boolean tieneCredenciales() {
        return clientId != null && !clientId.isBlank()
                && clientSecret != null && !clientSecret.isBlank();
    }

    // El record genera un toString con todos los campos; lo cambiamos
    // para que el secret nunca termine en un log.
    @Override
    public String toString() {
        return "SoundCloudPropiedades[apiUrl=" + apiUrl
                + ", credenciales=" + (tieneCredenciales() ? "definidas" : "vacias") + "]";
    }
}
