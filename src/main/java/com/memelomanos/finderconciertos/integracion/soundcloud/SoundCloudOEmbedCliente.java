package com.memelomanos.finderconciertos.integracion.soundcloud;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.memelomanos.finderconciertos.integracion.soundcloud.ArtistaSoundCloud.Origen;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Usa el endpoint publico oEmbed de SoundCloud, que no requiere
 * credenciales, para obtener el reproductor embebible de un perfil. Sirve
 * para la URL capturada a mano y para las URLs deducidas del nombre
 * (Plan C de ADR-005).
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
     * Reproductor de una URL capturada a mano. No revisa el nombre: una
     * persona ya confirmo que es el artista correcto.
     *
     * @return vacio si la respuesta no trae un reproductor de SoundCloud valido
     * @throws RestClientException si SoundCloud no responde o no conoce la URL (404)
     */
    public Optional<ArtistaSoundCloud> obtenerReproductor(String nombreArtista, String perfilUrl) {
        return Optional.ofNullable(consultar(perfilUrl))
                .flatMap(respuesta -> armar(nombreArtista, perfilUrl, respuesta, Origen.MANUAL));
    }

    /**
     * Reproductor de una URL deducida del nombre. Solo se acepta si el
     * nombre del perfil coincide con el del artista, para no mostrar a
     * otro usuario que ocupa esa URL.
     *
     * @return vacio si la URL no existe (404) o es de alguien con otro nombre
     * @throws RestClientException si SoundCloud no responde o falla por otra razon
     */
    public Optional<ArtistaSoundCloud> buscarPerfil(String nombreArtista, String perfilUrl) {
        RespuestaOEmbed respuesta;
        try {
            respuesta = consultar(perfilUrl);
        } catch (HttpClientErrorException.NotFound ex) {
            return Optional.empty();
        }
        if (respuesta == null || !NombresSoundCloud.coinciden(nombreArtista, respuesta.authorName())) {
            return Optional.empty();
        }
        return armar(nombreArtista, perfilUrl, respuesta, Origen.AUTOMATICO);
    }

    private RespuestaOEmbed consultar(String perfilUrl) {
        return restClient.get()
                .uri(propiedades.oembedUrl() + "?format=json&url={url}", perfilUrl)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(RespuestaOEmbed.class);
    }

    private Optional<ArtistaSoundCloud> armar(String nombreArtista, String perfilUrl,
                                              RespuestaOEmbed respuesta, Origen origen) {
        return extraerWidgetUrl(respuesta.html()).map(widgetUrl -> new ArtistaSoundCloud(
                nombreArtista,
                respuesta.authorUrl() != null ? respuesta.authorUrl() : perfilUrl,
                respuesta.thumbnailUrl(),
                widgetUrl,
                List.of(),
                "oembed",
                origen));
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
            @JsonProperty("author_name") String authorName,
            @JsonProperty("author_url") String authorUrl,
            @JsonProperty("thumbnail_url") String thumbnailUrl) {
    }
}
