package com.memelomanos.finderconciertos.integracion.soundcloud;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.memelomanos.finderconciertos.RelojDePrueba;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

// Las respuestas de SoundCloud son de ejemplo: MockRestServiceServer
// contesta en lugar de la API real, asi que estas pruebas no salen a internet.
@DisplayName("Cliente de la API de SoundCloud (Plan A)")
class SoundCloudClienteTest {

    private static final String TOKEN_URL = "https://secure.soundcloud.com/oauth/token";
    private static final String USUARIOS_URL = "https://api.soundcloud.com/users";
    // RestClient codifica las variables de la URL, asi que los ":" del urn
    // pueden llegar como %3A. Aceptamos las dos formas.
    private static final String PISTAS_URL =
            "https://api.soundcloud.com/users/soundcloud:users:1/tracks";
    private static final String PISTAS_URL_CODIFICADA =
            "https://api.soundcloud.com/users/soundcloud%3Ausers%3A1/tracks";

    private static final String TOKEN = """
            {"access_token": "token-1", "refresh_token": "refresh-1", "expires_in": 3600}
            """;
    private static final String USUARIOS = """
            {"collection": [
              {"urn": "soundcloud:users:2", "username": "queen-tributo",
               "permalink_url": "https://soundcloud.com/queen-tributo"},
              {"urn": "soundcloud:users:1", "username": "queen", "full_name": "Queen",
               "permalink_url": "https://soundcloud.com/queen",
               "avatar_url": "https://i1.sndcdn.com/avatars-queen.jpg"}
            ]}
            """;
    private static final String PISTAS = """
            {"collection": [
              {"title": "Poco escuchada", "permalink_url": "https://soundcloud.com/queen/a",
               "duration": 1000, "playback_count": 10},
              {"title": "La mas escuchada", "permalink_url": "https://soundcloud.com/queen/b",
               "duration": 2000, "playback_count": 900},
              {"title": "Segunda", "permalink_url": "https://soundcloud.com/queen/c",
               "duration": 3000, "playback_count": 500}
            ]}
            """;

    private MockRestServiceServer servidor;
    private RelojDePrueba reloj;
    private SoundCloudCliente cliente;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        servidor = MockRestServiceServer.bindTo(builder).build();
        reloj = new RelojDePrueba(Instant.parse("2026-11-07T12:00:00Z"));
        SoundCloudPropiedades propiedades = new SoundCloudPropiedades(
                "id-de-prueba", "secreto-de-prueba",
                "https://api.soundcloud.com", "https://secure.soundcloud.com",
                "https://soundcloud.com/oembed", 2, Duration.ofHours(24), Duration.ofSeconds(5));
        cliente = new SoundCloudCliente(builder.build(), propiedades, reloj);
    }

    @Test
    @DisplayName("Pide el token con HTTP Basic y regresa las pistas mas escuchadas")
    void buscarArtista_RegresaPerfilYPistasOrdenadas() throws Exception {
        String basic = Base64.getEncoder().encodeToString(
                "id-de-prueba:secreto-de-prueba".getBytes(StandardCharsets.UTF_8));
        servidor.expect(once(), requestTo(TOKEN_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Basic " + basic))
                .andExpect(content().string("grant_type=client_credentials"))
                .andRespond(withSuccess(TOKEN, MediaType.APPLICATION_JSON));
        servidor.expect(requestTo(startsWith(USUARIOS_URL)))
                .andExpect(queryParam("q", "Queen"))
                .andExpect(header("Authorization", "OAuth token-1"))
                .andRespond(withSuccess(USUARIOS, MediaType.APPLICATION_JSON));
        servidor.expect(requestTo(anyOf(startsWith(PISTAS_URL), startsWith(PISTAS_URL_CODIFICADA))))
                .andRespond(withSuccess(PISTAS, MediaType.APPLICATION_JSON));

        Optional<ArtistaSoundCloud> resultado = cliente.buscarArtista("Queen");

        servidor.verify();
        assertThat(resultado).isPresent();
        ArtistaSoundCloud artista = resultado.get();
        // Prefiere la coincidencia exacta aunque no sea el primer resultado.
        assertThat(artista.perfilUrl()).isEqualTo("https://soundcloud.com/queen");
        assertThat(artista.avatarUrl()).isEqualTo("https://i1.sndcdn.com/avatars-queen.jpg");
        assertThat(artista.widgetUrl())
                .startsWith("https://w.soundcloud.com/player/?url=https%3A%2F%2Fsoundcloud.com%2Fqueen");
        assertThat(artista.fuente()).isEqualTo("api");
        // maxPistas = 2: solo las dos con mas reproducciones, en orden.
        assertThat(artista.pistas()).extracting(ArtistaSoundCloud.PistaSoundCloud::titulo)
                .containsExactly("La mas escuchada", "Segunda");
        assertThat(artista.pistas().get(0).duracionMs()).isEqualTo(2000);

        // El secret nunca forma parte de lo que se manda a la interfaz.
        String json = new ObjectMapper().writeValueAsString(artista);
        assertThat(json).doesNotContain("secreto-de-prueba").doesNotContain("token-1");
    }

    @Test
    @DisplayName("Reutiliza el token mientras no venza")
    void tokenVigente_ReutilizaElToken() {
        servidor.expect(once(), requestTo(TOKEN_URL))
                .andRespond(withSuccess(TOKEN, MediaType.APPLICATION_JSON));

        assertThat(cliente.tokenVigente()).isEqualTo("token-1");
        reloj.avanzar(Duration.ofMinutes(50));
        assertThat(cliente.tokenVigente()).isEqualTo("token-1");

        servidor.verify();
    }

    @Test
    @DisplayName("Renueva el token con el refresh token cuando vence")
    void tokenVigente_RenuevaCuandoVence() {
        servidor.expect(once(), requestTo(TOKEN_URL))
                .andRespond(withSuccess(TOKEN, MediaType.APPLICATION_JSON));
        servidor.expect(once(), requestTo(TOKEN_URL))
                .andExpect(content().string("grant_type=refresh_token&refresh_token=refresh-1"))
                .andRespond(withSuccess("""
                        {"access_token": "token-2", "expires_in": 3600}
                        """, MediaType.APPLICATION_JSON));

        cliente.tokenVigente();
        reloj.avanzar(Duration.ofMinutes(59));

        assertThat(cliente.tokenVigente()).isEqualTo("token-2");
        servidor.verify();
    }

    @Test
    @DisplayName("Regresa vacio si SoundCloud no encuentra al artista")
    void buscarArtista_SinResultados_RegresaVacio() {
        servidor.expect(requestTo(TOKEN_URL))
                .andRespond(withSuccess(TOKEN, MediaType.APPLICATION_JSON));
        servidor.expect(requestTo(startsWith(USUARIOS_URL)))
                .andRespond(withSuccess("{\"collection\": []}", MediaType.APPLICATION_JSON));

        assertThat(cliente.buscarArtista("Nadie")).isEmpty();
    }

    @Test
    @DisplayName("Lanza RestClientException si SoundCloud rechaza las credenciales")
    void buscarArtista_CredencialesInvalidas_Lanza() {
        servidor.expect(requestTo(TOKEN_URL))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> cliente.buscarArtista("Queen"))
                .isInstanceOf(RestClientException.class);
    }
}
