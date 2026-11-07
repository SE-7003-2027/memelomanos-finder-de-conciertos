package com.memelomanos.finderconciertos.integracion.soundcloud;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.memelomanos.finderconciertos.integracion.soundcloud.ArtistaSoundCloud.PistaSoundCloud;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Plan A de ADR-005: habla con la API de SoundCloud usando el flujo
 * Client Credentials. Solo funciona si estan definidos
 * SOUNDCLOUD_CLIENT_ID y SOUNDCLOUD_CLIENT_SECRET.
 */
@Component
public class SoundCloudCliente {

    // Pedimos un token nuevo un poco antes de que venza para no mandar
    // uno que expire a medio camino.
    private static final Duration MARGEN_TOKEN = Duration.ofSeconds(60);
    private static final long DURACION_TOKEN_POR_DEFECTO = 3600;
    private static final int PISTAS_A_REVISAR = 50;

    private final RestClient restClient;
    private final SoundCloudPropiedades propiedades;
    private final Clock reloj;

    // SoundCloud solo entrega 50 tokens cada 12 h por app, asi que el
    // token se guarda y se reutiliza hasta que vence. Se protege con
    // synchronized porque varias peticiones pueden llegar a la vez.
    private TokenGuardado token;

    @Autowired
    public SoundCloudCliente(RestClient restClient, SoundCloudPropiedades propiedades) {
        this(restClient, propiedades, Clock.systemUTC());
    }

    SoundCloudCliente(RestClient restClient, SoundCloudPropiedades propiedades, Clock reloj) {
        this.restClient = restClient;
        this.propiedades = propiedades;
        this.reloj = reloj;
    }

    public boolean estaConfigurado() {
        return propiedades.tieneCredenciales();
    }

    /**
     * Busca al artista y regresa su perfil con sus pistas mas escuchadas,
     * o vacio si SoundCloud no tiene a nadie con ese nombre.
     *
     * @throws RestClientException si SoundCloud no responde o rechaza la peticion
     */
    public Optional<ArtistaSoundCloud> buscarArtista(String nombre) {
        Perfiles perfiles = restClient.get()
                .uri(propiedades.apiUrl() + "/users?q={q}&limit=10&linked_partitioning=true", nombre)
                .header(HttpHeaders.AUTHORIZATION, "OAuth " + tokenVigente())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(Perfiles.class);

        return elegirPerfil(perfiles, nombre).map(perfil -> new ArtistaSoundCloud(
                perfil.nombreVisible(),
                perfil.permalinkUrl(),
                perfil.avatarUrl(),
                ArtistaSoundCloud.widgetPara(perfil.permalinkUrl()),
                pistasMasEscuchadas(perfil.urn()),
                "api"));
    }

    // Preferimos al usuario cuyo nombre coincide exacto; si no hay, el
    // primer resultado, que es el que SoundCloud considera mas relevante.
    private Optional<Perfil> elegirPerfil(Perfiles perfiles, String nombre) {
        if (perfiles == null || perfiles.collection() == null) {
            return Optional.empty();
        }
        List<Perfil> candidatos = perfiles.collection().stream()
                .filter(perfil -> perfil.urn() != null && perfil.permalinkUrl() != null)
                .toList();
        return candidatos.stream()
                .filter(perfil -> nombre.equalsIgnoreCase(perfil.username())
                        || nombre.equalsIgnoreCase(perfil.fullName()))
                .findFirst()
                .or(() -> candidatos.stream().findFirst());
    }

    // La API no ordena las pistas por popularidad, asi que pedimos las
    // primeras 50 y las ordenamos nosotros por numero de reproducciones.
    private List<PistaSoundCloud> pistasMasEscuchadas(String urnUsuario) {
        Pistas pistas = restClient.get()
                .uri(propiedades.apiUrl() + "/users/{urn}/tracks?access=playable&limit={limite}&linked_partitioning=true",
                        urnUsuario, PISTAS_A_REVISAR)
                .header(HttpHeaders.AUTHORIZATION, "OAuth " + tokenVigente())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(Pistas.class);

        if (pistas == null || pistas.collection() == null) {
            return List.of();
        }
        return pistas.collection().stream()
                .filter(pista -> pista.title() != null && pista.permalinkUrl() != null)
                .sorted(Comparator.comparingLong(Pista::reproducciones).reversed())
                .limit(propiedades.maxPistas())
                .map(pista -> new PistaSoundCloud(pista.title(), pista.permalinkUrl(),
                        Objects.requireNonNullElse(pista.duration(), 0L)))
                .toList();
    }

    synchronized String tokenVigente() {
        Instant ahora = reloj.instant();
        if (token != null && ahora.isBefore(token.venceEn())) {
            return token.valor();
        }

        RespuestaToken respuesta = null;
        // Renovar con el refresh token no gasta uno de los 50 tokens nuevos.
        if (token != null && token.refreshToken() != null) {
            try {
                respuesta = pedirToken("refresh_token", token.refreshToken());
            } catch (RestClientException ex) {
                respuesta = null;
            }
        }
        if (respuesta == null || respuesta.accessToken() == null) {
            respuesta = pedirToken("client_credentials", null);
        }
        if (respuesta == null || respuesta.accessToken() == null) {
            throw new RestClientException("SoundCloud no regreso un token de acceso");
        }

        long segundos = Objects.requireNonNullElse(respuesta.expiresIn(), DURACION_TOKEN_POR_DEFECTO);
        token = new TokenGuardado(respuesta.accessToken(), respuesta.refreshToken(),
                ahora.plusSeconds(segundos).minus(MARGEN_TOKEN));
        return token.valor();
    }

    // SoundCloud solo acepta el client id y el secret por HTTP Basic,
    // no en el cuerpo de la peticion.
    private RespuestaToken pedirToken(String tipo, String refreshToken) {
        MultiValueMap<String, String> formulario = new LinkedMultiValueMap<>();
        formulario.add("grant_type", tipo);
        if (refreshToken != null) {
            formulario.add("refresh_token", refreshToken);
        }
        return restClient.post()
                .uri(propiedades.authUrl() + "/oauth/token")
                .headers(encabezados -> encabezados.setBasicAuth(
                        propiedades.clientId(), propiedades.clientSecret()))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(formulario)
                .retrieve()
                .body(RespuestaToken.class);
    }

    private record TokenGuardado(String valor, String refreshToken, Instant venceEn) {
    }

    // Formas de las respuestas de SoundCloud. Solo se mapean los campos
    // que usamos; el resto se ignora.

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RespuestaToken(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("refresh_token") String refreshToken,
            @JsonProperty("expires_in") Long expiresIn) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Perfiles(List<Perfil> collection) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Perfil(
            String urn,
            String username,
            @JsonProperty("full_name") String fullName,
            @JsonProperty("permalink_url") String permalinkUrl,
            @JsonProperty("avatar_url") String avatarUrl) {

        String nombreVisible() {
            return fullName != null && !fullName.isBlank() ? fullName : username;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Pistas(List<Pista> collection) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Pista(
            String title,
            @JsonProperty("permalink_url") String permalinkUrl,
            Long duration,
            @JsonProperty("playback_count") Long playbackCount) {

        long reproducciones() {
            return playbackCount == null ? 0 : playbackCount;
        }
    }
}
