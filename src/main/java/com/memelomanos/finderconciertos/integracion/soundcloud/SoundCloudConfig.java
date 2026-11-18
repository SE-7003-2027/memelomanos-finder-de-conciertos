package com.memelomanos.finderconciertos.integracion.soundcloud;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(SoundCloudPropiedades.class)
public class SoundCloudConfig {

    // Cliente HTTP compartido por SoundCloudCliente y SoundCloudOEmbedCliente.
    // El timeout evita que el detalle del concierto se quede esperando
    // si SoundCloud tarda en responder.
    @Bean
    RestClient soundCloudRestClient(SoundCloudPropiedades propiedades) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        int milisegundos = (int) propiedades.timeout().toMillis();
        fabrica.setConnectTimeout(milisegundos);
        fabrica.setReadTimeout(milisegundos);
        return RestClient.builder().requestFactory(fabrica).build();
    }
}
