package com.memelomanos.finderconciertos.integracion.soundcloud;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Plan B de ADR-005: usa el endpoint publico oEmbed de SoundCloud, que
 * no requiere credenciales, para obtener el reproductor embebible de un
 * perfil cuya URL ya conocemos.
 */
@Component
public class SoundCloudOEmbedCliente {

    private static final Pattern SRC_DEL_IFRAME = Pattern.compile("src=\"([^\"]+)\"");

    private final RestClient restClient;
    private final SoundCloudPropiedades propiedades;

    public SoundCloudOEmbedCliente(RestClient restClient, SoundCloudPropiedades propiedades) {
        this.restClient = restClient;
        this.propiedades = propiedades;
    }

    /**
     * Regresa el reproductor del perfil, o vacio si la respuesta no trae
     * un reproductor de SoundCloud valido.
     *
     * @throws RestClientException si SoundCloud no responde o no conoce la URL (404)
     */
    public Optional<ArtistaSoundCloud> obtenerReproductor(String nombreArtista, String perfilUrl) {
        RespuestaOEmbed respuesta = restClient.get()
                .uri(propiedades.oembedUrl() + "?format=json&url={url}", perfilUrl)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(RespuestaOEmbed.class);

        if (respuesta == null) {
            return Optional.empty();
        }
        return extraerWidgetUrl(respuesta.html()).map(widgetUrl -> new ArtistaSoundCloud(
                nombreArtista,
                respuesta.authorUrl() != null ? respuesta.authorUrl() : perfilUrl,
                respuesta.thumbnailUrl(),
                widgetUrl,
                List.of(),
                "oembed"));
    }

    // oEmbed regresa el HTML completo del iframe. La interfaz no inserta
    // HTML de terceros (evita XSS): solo recibe la URL del reproductor, y
    // solo si apunta al widget oficial de SoundCloud.
    static Optional<String> extraerWidgetUrl(String html) {
        if (html == null) {
            return Optional.empty();
        }
        Matcher coincidencia = SRC_DEL_IFRAME.matcher(html);
        if (!coincidencia.find()) {
            return Optional.empty();
        }
        String src = coincidencia.group(1).replace("&amp;", "&");
        return src.startsWith(ArtistaSoundCloud.WIDGET_URL) ? Optional.of(src) : Optional.empty();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RespuestaOEmbed(
            String html,
            @JsonProperty("author_url") String authorUrl,
            @JsonProperty("thumbnail_url") String thumbnailUrl) {
    }
}
