package com.memelomanos.finderconciertos.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.memelomanos.finderconciertos.exception.ArtistaSinSoundCloudException;
import com.memelomanos.finderconciertos.integracion.soundcloud.ArtistaSoundCloud;
import com.memelomanos.finderconciertos.integracion.soundcloud.ArtistaSoundCloud.PistaSoundCloud;
import com.memelomanos.finderconciertos.security.ConfiguracionSeguridad;
import com.memelomanos.finderconciertos.security.JwtService;
import com.memelomanos.finderconciertos.service.ArtistaService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

// Sin @WithMockUser a proposito: el endpoint debe ser publico.
@WebMvcTest(ArtistaController.class)
@Import(ConfiguracionSeguridad.class)
@DisplayName("Controlador de Artistas (Capa Web)")
class ArtistaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArtistaService artistaService;

    @MockBean
    private JwtService jwtService;

    @Test
    @DisplayName("Regresa el perfil de SoundCloud sin pedir token")
    void obtenerSoundCloud_DebeRetornarOk() throws Exception {
        when(artistaService.obtenerSoundCloud("Bad Bunny")).thenReturn(new ArtistaSoundCloud(
                "Bad Bunny",
                "https://soundcloud.com/badbunny",
                "https://i1.sndcdn.com/avatar.jpg",
                "https://w.soundcloud.com/player/?url=https%3A%2F%2Fsoundcloud.com%2Fbadbunny",
                List.of(new PistaSoundCloud("Pista", "https://soundcloud.com/badbunny/pista", 180000)),
                "api"));

        mockMvc.perform(get("/api/v1/artistas/{nombre}/soundcloud", "Bad Bunny"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfilUrl").value("https://soundcloud.com/badbunny"))
                .andExpect(jsonPath("$.avatarUrl").value("https://i1.sndcdn.com/avatar.jpg"))
                .andExpect(jsonPath("$.pistas[0].titulo").value("Pista"))
                .andExpect(jsonPath("$.pistas[0].duracionMs").value(180000))
                .andExpect(jsonPath("$.fuente").value("api"));
    }

    @Test
    @DisplayName("Responde 404 con el formato de error comun si no hay perfil")
    void obtenerSoundCloud_SinPerfil_DebeRetornarNotFound() throws Exception {
        when(artistaService.obtenerSoundCloud("Nadie"))
                .thenThrow(new ArtistaSinSoundCloudException("No encontramos a Nadie en SoundCloud"));

        mockMvc.perform(get("/api/v1/artistas/Nadie/soundcloud"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.mensaje").value("No encontramos a Nadie en SoundCloud"));
    }
}
