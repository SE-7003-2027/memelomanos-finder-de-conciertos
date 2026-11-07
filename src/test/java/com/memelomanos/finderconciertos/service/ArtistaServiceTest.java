package com.memelomanos.finderconciertos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.memelomanos.finderconciertos.RelojDePrueba;
import com.memelomanos.finderconciertos.exception.ArtistaSinSoundCloudException;
import com.memelomanos.finderconciertos.integracion.soundcloud.ArtistaSoundCloud;
import com.memelomanos.finderconciertos.integracion.soundcloud.SoundCloudCliente;
import com.memelomanos.finderconciertos.integracion.soundcloud.SoundCloudOEmbedCliente;
import com.memelomanos.finderconciertos.integracion.soundcloud.SoundCloudPropiedades;
import com.memelomanos.finderconciertos.model.Concierto;
import com.memelomanos.finderconciertos.repository.ConciertoRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;

@DisplayName("Servicio de Artistas (SoundCloud)")
class ArtistaServiceTest {

    private static final String PERFIL = "https://soundcloud.com/queen";

    private final ArtistaSoundCloud desdeApi = new ArtistaSoundCloud(
            "Queen", PERFIL, null, "https://w.soundcloud.com/player/?url=api", List.of(), "api");
    private final ArtistaSoundCloud desdeOEmbed = new ArtistaSoundCloud(
            "Queen", PERFIL, null, "https://w.soundcloud.com/player/?url=oembed", List.of(), "oembed");

    private SoundCloudCliente api;
    private SoundCloudOEmbedCliente oEmbed;
    private ConciertoRepository conciertoRepository;
    private RelojDePrueba reloj;
    private ArtistaService servicio;

    @BeforeEach
    void setUp() {
        api = mock(SoundCloudCliente.class);
        oEmbed = mock(SoundCloudOEmbedCliente.class);
        conciertoRepository = mock(ConciertoRepository.class);
        reloj = new RelojDePrueba(Instant.parse("2026-11-07T12:00:00Z"));
        SoundCloudPropiedades propiedades = new SoundCloudPropiedades(
                null, null, null, null, null, 5, Duration.ofHours(24), Duration.ofSeconds(5));
        servicio = new ArtistaService(api, oEmbed, conciertoRepository, propiedades, reloj);
    }

    private void conPerfilGuardado() {
        Concierto concierto = new Concierto("Queen", LocalDate.of(2026, 11, 20), "CDMX", "Foro Sol");
        concierto.setSoundcloudUrl(PERFIL);
        when(conciertoRepository.findFirstByArtistaIgnoreCaseAndSoundcloudUrlIsNotNull("Queen"))
                .thenReturn(Optional.of(concierto));
    }

    @Test
    @DisplayName("Usa la API cuando hay credenciales (Plan A)")
    void obtener_ConCredenciales_UsaApi() {
        when(api.estaConfigurado()).thenReturn(true);
        when(api.buscarArtista("Queen")).thenReturn(Optional.of(desdeApi));

        assertThat(servicio.obtenerSoundCloud(" Queen ")).isEqualTo(desdeApi);
        verify(oEmbed, never()).obtenerReproductor(anyString(), anyString());
    }

    @Test
    @DisplayName("Sin credenciales usa oEmbed con la URL guardada (Plan B)")
    void obtener_SinCredenciales_UsaOEmbed() {
        when(api.estaConfigurado()).thenReturn(false);
        conPerfilGuardado();
        when(oEmbed.obtenerReproductor("Queen", PERFIL)).thenReturn(Optional.of(desdeOEmbed));

        assertThat(servicio.obtenerSoundCloud("Queen")).isEqualTo(desdeOEmbed);
        verify(api, never()).buscarArtista(anyString());
    }

    @Test
    @DisplayName("Si la API falla, cae al Plan B en lugar de responder 500")
    void obtener_ApiFalla_UsaOEmbed() {
        when(api.estaConfigurado()).thenReturn(true);
        when(api.buscarArtista("Queen")).thenThrow(new RestClientException("429 Too Many Requests"));
        conPerfilGuardado();
        when(oEmbed.obtenerReproductor("Queen", PERFIL)).thenReturn(Optional.of(desdeOEmbed));

        assertThat(servicio.obtenerSoundCloud("Queen")).isEqualTo(desdeOEmbed);
    }

    @Test
    @DisplayName("Lanza ArtistaSinSoundCloudException si no hay perfil")
    void obtener_SinPerfil_Lanza404() {
        when(api.estaConfigurado()).thenReturn(false);
        when(conciertoRepository.findFirstByArtistaIgnoreCaseAndSoundcloudUrlIsNotNull("Nadie"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.obtenerSoundCloud("Nadie"))
                .isInstanceOf(ArtistaSinSoundCloudException.class)
                .hasMessage("No encontramos a Nadie en SoundCloud");
    }

    @Test
    @DisplayName("Si oEmbed falla tambien responde 404, no 500")
    void obtener_OEmbedFalla_Lanza404() {
        when(api.estaConfigurado()).thenReturn(false);
        conPerfilGuardado();
        when(oEmbed.obtenerReproductor("Queen", PERFIL))
                .thenThrow(new RestClientException("Connection timed out"));

        assertThatThrownBy(() -> servicio.obtenerSoundCloud("Queen"))
                .isInstanceOf(ArtistaSinSoundCloudException.class);
    }

    @Test
    @DisplayName("Guarda la respuesta en cache por 24 horas")
    void obtener_UsaCacheHastaQueVence() {
        when(api.estaConfigurado()).thenReturn(true);
        when(api.buscarArtista(anyString())).thenReturn(Optional.of(desdeApi));

        servicio.obtenerSoundCloud("Queen");
        servicio.obtenerSoundCloud("QUEEN");
        reloj.avanzar(Duration.ofHours(23));
        servicio.obtenerSoundCloud("queen");
        verify(api, times(1)).buscarArtista(anyString());

        reloj.avanzar(Duration.ofHours(2));
        servicio.obtenerSoundCloud("Queen");
        verify(api, times(2)).buscarArtista(anyString());
    }

    @Test
    @DisplayName("Rechaza un nombre vacio")
    void obtener_NombreVacio_Lanza400() {
        assertThatThrownBy(() -> servicio.obtenerSoundCloud("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
