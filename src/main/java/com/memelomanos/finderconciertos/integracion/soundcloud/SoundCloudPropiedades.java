package com.memelomanos.finderconciertos.integracion.soundcloud;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Configuracion de SoundCloud (seccion "soundcloud" de application.yml, ADR-005). */
@ConfigurationProperties(prefix = "soundcloud")
public record SoundCloudPropiedades(
        @DefaultValue("https://soundcloud.com/oembed") String oembedUrl,
        @DefaultValue("24h") Duration duracionCache,
        @DefaultValue("5s") Duration timeout) {
}
