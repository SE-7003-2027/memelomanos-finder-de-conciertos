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
import com.memelomanos.finderconciertos.integracion.soundcloud.ArtistaSoundCloud.Origen;
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

// Los mocks de Mockito regresan Optional.empty() por defecto, asi que un
// metodo sin "when" equivale a "SoundCloud no encontro nada".
@DisplayName("Servicio de Artistas (SoundCloud)")
class ArtistaServiceTest {

    private static final String PERFIL = "https://soundcloud.com/queen";
    private static final String BAD_BUNNY_JUNTO = "https://soundcloud.com/badbunny";
    private static final String BAD_BUNNY_GUIONES = "https://soundcloud.com/bad-bunny";

    private final ArtistaSoundCloud manual = new ArtistaSoundCloud(
            "Queen", PERFIL, null, "https://w.soundcloud.com/player/?url=manual", List.of(),
            "oembed", Origen.MANUAL);
    private final ArtistaSoundCloud desdeApi = new ArtistaSoundCloud(
            "Queen", PERFIL, null, "https://w.soundcloud.com/player/?url=api", List.of(),
            "api", Origen.API);
    private final ArtistaSoundCloud automatico = new ArtistaSoundCloud(
            "Bad Bunny", BAD_BUNNY_GUIONES, null, "https://w.soundcloud.com/player/?url=auto", List.of(),
            "oembed", Origen.AUTOMATICO);

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
                null, null, null, null, null, 5, Duration.ofHours(24), Duration.ofHours(6),
                Duration.ofSeconds(5));
        servicio = new ArtistaService(api, oEmbed, conciertoRepository, propiedades, reloj);
    }

    private void conPerfilGuardado() {
        Concierto concierto = new Concierto("Queen", LocalDate.of(2026, 11, 20), "CDMX", "Foro Sol");
        concierto.setSoundcloudUrl(PERFIL);
        when(conciertoRepository.findFirstByArtistaIgnoreCaseAndSoundcloudUrlIsNotNull("Queen"))
                .thenReturn(Optional.of(concierto));
    }

    // Orden de prioridad

    @Test
    @DisplayName("La URL guardada a mano gana aunque haya credenciales")
    void obtener_ConUrlManual_GanaSobreLaApi() {
        when(api.estaConfigurado()).thenReturn(true);
        conPerfilGuardado();
        when(oEmbed.obtenerReproductor("Queen", PERFIL)).thenReturn(Optional.of(manual));

        assertThat(servicio.obtenerSoundCloud(" Queen ")).isEqualTo(manual);
        verify(api, never()).buscarArtista(anyString());
        verify(oEmbed, never()).buscarPerfil(anyString(), anyString());
    }

    @Test
    @DisplayName("Sin URL manual usa la API cuando hay credenciales (Plan A)")
    void obtener_ConCredenciales_UsaApi() {
        when(api.estaConfigurado()).thenReturn(true);
        when(api.buscarArtista("Queen")).thenReturn(Optional.of(desdeApi));

        assertThat(servicio.obtenerSoundCloud("Queen")).isEqualTo(desdeApi);
        verify(oEmbed, never()).buscarPerfil(anyString(), anyString());
    }

    @Test
    @DisplayName("Si la URL manual falla, sigue con la API en lugar de responder 500")
    void obtener_UrlManualFalla_UsaApi() {
        when(api.estaConfigurado()).thenReturn(true);
        conPerfilGuardado();
        when(oEmbed.obtenerReproductor("Queen", PERFIL)).thenThrow(new RestClientException("404 Not Found"));
        when(api.buscarArtista("Queen")).thenReturn(Optional.of(desdeApi));

        assertThat(servicio.obtenerSoundCloud("Queen")).isEqualTo(desdeApi);
    }

    @Test
    @DisplayName("Si la API falla, sigue con las URLs probables (Plan C)")
    void obtener_ApiFalla_UsaPlanC() {
        when(api.estaConfigurado()).thenReturn(true);
        when(api.buscarArtista("Bad Bunny")).thenThrow(new RestClientException("429 Too Many Requests"));
        when(oEmbed.buscarPerfil("Bad Bunny", BAD_BUNNY_GUIONES)).thenReturn(Optional.of(automatico));

        assertThat(servicio.obtenerSoundCloud("Bad Bunny")).isEqualTo(automatico);
    }

    // Plan C

    @Test
    @DisplayName("Sin credenciales prueba las URLs probables y se detiene en la primera que coincide")
    void obtener_SinCredenciales_UsaPlanC() {
        when(api.estaConfigurado()).thenReturn(false);
        when(oEmbed.buscarPerfil("Bad Bunny", BAD_BUNNY_GUIONES)).thenReturn(Optional.of(automatico));

        assertThat(servicio.obtenerSoundCloud("Bad Bunny")).isEqualTo(automatico);
        verify(oEmbed).buscarPerfil("Bad Bunny", BAD_BUNNY_JUNTO);
        verify(oEmbed).buscarPerfil("Bad Bunny", BAD_BUNNY_GUIONES);
        verify(oEmbed, times(2)).buscarPerfil(anyString(), anyString());
        verify(api, never()).buscarArtista(anyString());
    }

    @Test
    @DisplayName("Si ninguna candidata coincide responde 404 (ArtistaSinSoundCloudException)")
    void obtener_SinPerfil_Lanza404() {
        when(api.estaConfigurado()).thenReturn(false);

        assertThatThrownBy(() -> servicio.obtenerSoundCloud("Nadie"))
                .isInstanceOf(ArtistaSinSoundCloudException.class)
                .hasMessage("No encontramos a Nadie en SoundCloud");
        // "nadie" y "nadieofficial": el nombre de una palabra no genera guiones.
        verify(oEmbed, times(2)).buscarPerfil(anyString(), anyString());
    }

    @Test
    @DisplayName("Si SoundCloud falla en el Plan C no prueba las demas candidatas y responde 404")
    void obtener_PlanCFalla_SeDetieneYLanza404() {
        when(api.estaConfigurado()).thenReturn(false);
        when(oEmbed.buscarPerfil("Bad Bunny", BAD_BUNNY_JUNTO))
                .thenThrow(new RestClientException("Connection timed out"));

        assertThatThrownBy(() -> servicio.obtenerSoundCloud("Bad Bunny"))
                .isInstanceOf(ArtistaSinSoundCloudException.class);
        verify(oEmbed, times(1)).buscarPerfil(anyString(), anyString());
    }

    // Cache

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
    @DisplayName("Recuerda a los artistas no encontrados por 6 horas")
    void obtener_NoEncontrado_UsaCacheHastaQueVence() {
        when(api.estaConfigurado()).thenReturn(false);

        assertThatThrownBy(() -> servicio.obtenerSoundCloud("Nadie"))
                .isInstanceOf(ArtistaSinSoundCloudException.class);
        reloj.avanzar(Duration.ofHours(5));
        assertThatThrownBy(() -> servicio.obtenerSoundCloud("nadie"))
                .isInstanceOf(ArtistaSinSoundCloudException.class)
                .hasMessage("No encontramos a nadie en SoundCloud");
        verify(oEmbed, times(2)).buscarPerfil(anyString(), anyString());

        reloj.avanzar(Duration.ofHours(2));
        assertThatThrownBy(() -> servicio.obtenerSoundCloud("Nadie"))
                .isInstanceOf(ArtistaSinSoundCloudException.class);
        verify(oEmbed, times(4)).buscarPerfil(anyString(), anyString());
    }

    @Test
    @DisplayName("No guarda el 'no encontrado' si SoundCloud fallo")
    void obtener_SoundCloudFalla_NoGuardaEnCache() {
        when(api.estaConfigurado()).thenReturn(false);
        when(oEmbed.buscarPerfil(anyString(), anyString()))
                .thenThrow(new RestClientException("Connection timed out"));

        assertThatThrownBy(() -> servicio.obtenerSoundCloud("Queen"))
                .isInstanceOf(ArtistaSinSoundCloudException.class);
        assertThatThrownBy(() -> servicio.obtenerSoundCloud("Queen"))
                .isInstanceOf(ArtistaSinSoundCloudException.class);
        verify(oEmbed, times(2)).buscarPerfil(anyString(), anyString());
    }

    @Test
    @DisplayName("Capturar la URL a mano reemplaza un 'no encontrado' guardado en cache")
    void obtener_UrlManualNueva_IgnoraLaCacheAnterior() {
        when(api.estaConfigurado()).thenReturn(false);
        assertThatThrownBy(() -> servicio.obtenerSoundCloud("Queen"))
                .isInstanceOf(ArtistaSinSoundCloudException.class);

        conPerfilGuardado();
        when(oEmbed.obtenerReproductor("Queen", PERFIL)).thenReturn(Optional.of(manual));

        assertThat(servicio.obtenerSoundCloud("Queen")).isEqualTo(manual);
    }

    @Test
    @DisplayName("Rechaza un nombre vacio")
    void obtener_NombreVacio_Lanza400() {
        assertThatThrownBy(() -> servicio.obtenerSoundCloud("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
