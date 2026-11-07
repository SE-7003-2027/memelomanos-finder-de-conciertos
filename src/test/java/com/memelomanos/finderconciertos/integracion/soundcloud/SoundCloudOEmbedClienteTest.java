package com.memelomanos.finderconciertos.integracion.soundcloud;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@DisplayName("Cliente oEmbed de SoundCloud (Plan B)")
class SoundCloudOEmbedClienteTest {

    private static final String PERFIL = "https://soundcloud.com/queen";

    // Respuesta de ejemplo con la forma que regresa https://soundcloud.com/oembed
    private static final String OEMBED = """
            {"version": 1.0, "type": "rich", "provider_name": "SoundCloud",
             "title": "Queen", "author_name": "Queen",
             "author_url": "https://soundcloud.com/queen",
             "thumbnail_url": "https://i1.sndcdn.com/avatars-queen.jpg",
             "html": "<iframe width=\\"100%\\" height=\\"400\\" scrolling=\\"no\\" frameborder=\\"no\\" src=\\"https://w.soundcloud.com/player/?visual=true&amp;url=https%3A%2F%2Fapi.soundcloud.com%2Fusers%2F1&amp;show_artwork=true\\"></iframe>"}
            """;

    private MockRestServiceServer servidor;
    private SoundCloudOEmbedCliente cliente;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        servidor = MockRestServiceServer.bindTo(builder).build();
        SoundCloudPropiedades propiedades = new SoundCloudPropiedades(
                null, null, "https://api.soundcloud.com", "https://secure.soundcloud.com",
                "https://soundcloud.com/oembed", 5, Duration.ofHours(24), Duration.ofSeconds(5));
        cliente = new SoundCloudOEmbedCliente(builder.build(), propiedades);
    }

    @Test
    @DisplayName("Regresa la URL del reproductor sin el HTML del iframe")
    void obtenerReproductor_RegresaWidget() {
        servidor.expect(requestTo(startsWith("https://soundcloud.com/oembed?format=json&url=")))
                .andRespond(withSuccess(OEMBED, MediaType.APPLICATION_JSON));

        Optional<ArtistaSoundCloud> resultado = cliente.obtenerReproductor("Queen", PERFIL);

        assertThat(resultado).isPresent();
        ArtistaSoundCloud artista = resultado.get();
        assertThat(artista.widgetUrl()).isEqualTo(
                "https://w.soundcloud.com/player/?visual=true&url=https%3A%2F%2Fapi.soundcloud.com%2Fusers%2F1&show_artwork=true");
        assertThat(artista.perfilUrl()).isEqualTo(PERFIL);
        assertThat(artista.avatarUrl()).isEqualTo("https://i1.sndcdn.com/avatars-queen.jpg");
        assertThat(artista.pistas()).isEmpty();
        assertThat(artista.fuente()).isEqualTo("oembed");
    }

    @Test
    @DisplayName("Lanza RestClientException si SoundCloud no conoce la URL")
    void obtenerReproductor_UrlDesconocida_Lanza() {
        servidor.expect(requestTo(startsWith("https://soundcloud.com/oembed")))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> cliente.obtenerReproductor("Queen", PERFIL))
                .isInstanceOf(RestClientException.class);
    }

    @Test
    @DisplayName("Ignora un iframe que no apunta al reproductor oficial")
    void extraerWidgetUrl_RechazaOtrosDominios() {
        assertThat(SoundCloudOEmbedCliente.extraerWidgetUrl(
                "<iframe src=\"https://sitio-malicioso.com/player\"></iframe>")).isEmpty();
        assertThat(SoundCloudOEmbedCliente.extraerWidgetUrl(null)).isEmpty();
    }
}
