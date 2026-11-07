package com.memelomanos.finderconciertos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.memelomanos.finderconciertos.RelojDePrueba;
import com.memelomanos.finderconciertos.exception.ArtistaSinSoundCloudException;
import com.memelomanos.finderconciertos.integracion.soundcloud.ArtistaSoundCloud;
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

    private final ArtistaSoundCloud desdeOEmbed = new ArtistaSoundCloud(
            "Queen", PERFIL, null, "https://w.soundcloud.com/player/?url=oembed", List.of(), "oembed");

    private SoundCloudOEmbedCliente oEmbed;
    private ConciertoRepository conciertoRepository;
    private RelojDePrueba reloj;
    private ArtistaService servicio;

    @BeforeEach
    void setUp() {
        oEmbed = mock(SoundCloudOEmbedCliente.class);
        conciertoRepository = mock(ConciertoRepository.class);
        reloj = new RelojDePrueba(Instant.parse("2026-11-07T12:00:00Z"));
        SoundCloudPropiedades propiedades = new SoundCloudPropiedades(
                null, Duration.ofHours(24), Duration.ofSeconds(5));
        servicio = new ArtistaService(oEmbed, conciertoRepository, propiedades, reloj);
    }

    private void conPerfilGuardado() {
        Concierto concierto = new Concierto("Queen", LocalDate.of(2026, 11, 20), "CDMX", "Foro Sol");
        concierto.setSoundcloudUrl(PERFIL);
        when(conciertoRepository.findFirstByArtistaIgnoreCaseAndSoundcloudUrlIsNotNull("Queen"))
                .thenReturn(Optional.of(concierto));
    }

    @Test
    @DisplayName("Usa oEmbed con la URL guardada en los conciertos")
    void obtener_ConUrlGuardada_UsaOEmbed() {
        conPerfilGuardado();
        when(oEmbed.obtenerReproductor("Queen", PERFIL)).thenReturn(Optional.of(desdeOEmbed));

        assertThat(servicio.obtenerSoundCloud(" Queen ")).isEqualTo(desdeOEmbed);
    }

    @Test
    @DisplayName("Lanza ArtistaSinSoundCloudException si no hay perfil")
    void obtener_SinPerfil_Lanza404() {
        when(conciertoRepository.findFirstByArtistaIgnoreCaseAndSoundcloudUrlIsNotNull("Nadie"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.obtenerSoundCloud("Nadie"))
                .isInstanceOf(ArtistaSinSoundCloudException.class)
                .hasMessage("No encontramos a Nadie en SoundCloud");
    }

    @Test
    @DisplayName("Si SoundCloud falla responde 404, no 500")
    void obtener_OEmbedFalla_Lanza404() {
        conPerfilGuardado();
        when(oEmbed.obtenerReproductor("Queen", PERFIL))
                .thenThrow(new RestClientException("Connection timed out"));

        assertThatThrownBy(() -> servicio.obtenerSoundCloud("Queen"))
                .isInstanceOf(ArtistaSinSoundCloudException.class);
    }

    @Test
    @DisplayName("Guarda la respuesta en cache por 24 horas")
    void obtener_UsaCacheHastaQueVence() {
        Concierto concierto = new Concierto("Queen", LocalDate.of(2026, 11, 20), "CDMX", "Foro Sol");
        concierto.setSoundcloudUrl(PERFIL);
        when(conciertoRepository.findFirstByArtistaIgnoreCaseAndSoundcloudUrlIsNotNull(anyString()))
                .thenReturn(Optional.of(concierto));
        when(oEmbed.obtenerReproductor(anyString(), anyString())).thenReturn(Optional.of(desdeOEmbed));

        servicio.obtenerSoundCloud("Queen");
        servicio.obtenerSoundCloud("QUEEN");
        reloj.avanzar(Duration.ofHours(23));
        servicio.obtenerSoundCloud("queen");
        verify(oEmbed, times(1)).obtenerReproductor(anyString(), anyString());

        reloj.avanzar(Duration.ofHours(2));
        servicio.obtenerSoundCloud("Queen");
        verify(oEmbed, times(2)).obtenerReproductor(anyString(), anyString());
    }

    @Test
    @DisplayName("Rechaza un nombre vacio")
    void obtener_NombreVacio_Lanza400() {
        assertThatThrownBy(() -> servicio.obtenerSoundCloud("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
