package com.memelomanos.finderconciertos.security;

import com.memelomanos.finderconciertos.controller.UsuarioController;
import com.memelomanos.finderconciertos.controller.ConciertoController;
import com.memelomanos.finderconciertos.model.LoginResponse;
import com.memelomanos.finderconciertos.model.Usuario;
import com.memelomanos.finderconciertos.service.UsuarioService;
import com.memelomanos.finderconciertos.service.ConciertoService;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({UsuarioController.class, ConciertoController.class})
@Import({ConfiguracionSeguridad.class, JwtAuthenticationFilter.class, JwtService.class})
@DisplayName("Seguridad y Permisos (Integraciones OAuth2)")
public class SeguridadApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private ConciertoService conciertoService;

    @Test
    @DisplayName("Debe bloquear acceso anónimo a rutas privadas (401 Unauthorized)")
    void accesoAnonimo_Bloqueado() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/1/favoritos"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("El registro de usuarios debe ser público (No 401)")
    void registro_EsPublico() throws Exception {
        Usuario usuarioMock = new Usuario("Test", "test@test.com");
        when(usuarioService.registrarUsuario(anyString(), anyString(), anyString())).thenReturn(usuarioMock);

        mockMvc.perform(post("/api/v1/usuarios/registro")
               .contentType(MediaType.APPLICATION_JSON)
               .content("{\"nombre\":\"Test\",\"correo\":\"test@test.com\",\"password\":\"password123\"}"))
               .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("El login de usuarios debe ser público (No 401 por falta de token)")
    void login_EsPublico() throws Exception {
        when(usuarioService.login(anyString(), anyString())).thenReturn(new LoginResponse("token123"));

        mockMvc.perform(post("/api/v1/usuarios/login")
               .contentType(MediaType.APPLICATION_JSON)
               .content("{\"correo\":\"test@test.com\",\"password\":\"password123\"}"))
               .andExpect(status().isOk());
    }

    @Test
    @DisplayName("La consulta de conciertos debe ser pública (GET /conciertos)")
    void conciertos_EsPublico() throws Exception {
        mockMvc.perform(get("/api/v1/conciertos"))
               .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Debe autenticarse correctamente usando un Token JWT real en cabecera Authorization")
    void simulacion_LoginConJwtToken() throws Exception {
        String tokenValido = jwtService.generarToken("usuario@gmail.com", 1L);

        mockMvc.perform(get("/api/v1/usuarios/1/favoritos")
               .header("Authorization", "Bearer " + tokenValido))
               .andExpect(status().isOk());
    }
}
