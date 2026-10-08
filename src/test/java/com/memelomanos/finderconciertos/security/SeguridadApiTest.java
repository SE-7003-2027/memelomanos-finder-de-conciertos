package com.memelomanos.finderconciertos.security;

import com.memelomanos.finderconciertos.controller.UsuarioController;
import com.memelomanos.finderconciertos.controller.ConciertoController;
import com.memelomanos.finderconciertos.service.UsuarioService;
import com.memelomanos.finderconciertos.service.ConciertoService;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// TODO: Cuando se agregue Spring Security, descomentar estos imports:
// import org.springframework.security.test.context.support.WithMockUser;
// import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@WebMvcTest({UsuarioController.class, ConciertoController.class})
@DisplayName("Seguridad y Permisos (Integraciones OAuth2)")
public class SeguridadApiTest {

    @Autowired
    private MockMvc mockMvc;

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
        mockMvc.perform(post("/api/v1/usuarios/registro")
               .contentType("application/json")
               .content("{\"nombre\":\"Test\",\"correo\":\"test@test.com\"}"))
               .andExpect(status().isCreated());
    }

    /* TODO: DESCOMENTAR CUANDO TENGAS SPRING SECURITY Y LAS DEPENDENCIAS DE TESTING*/
    // Nota: esta parte está comentada porque al no tener integrado Spring Security va a chicotear
    /*
    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Usuario con rol básico no puede borrar conciertos (403 Forbidden)")
    void usuarioNormal_NoPuedeBorrarConciertos() throws Exception {
        // Asumiendo que solo administradores pueden borrar conciertos del sistema
        mockMvc.perform(delete("/api/v1/conciertos/10"))
               .andExpect(status().isForbidden());
    }
    */

    /*
    @Test
    @DisplayName("Debe autenticarse correctamente usando un Token JWT (Ej: Spotify / Google APIs)")
    void simulacion_LoginConJwtToken() throws Exception {
        // Al conectar con Spotify o Google, tu frontend te mandará un Token JWT (Bearer Token).
        // Así simulamos que la petición HTTP trae un token válido:
        mockMvc.perform(get("/api/v1/usuarios/1/favoritos")
               .with(jwt().jwt(builder -> builder.claim("email", "usuario@gmail.com"))))
               .andExpect(status().isOk());
    }
    */
}
