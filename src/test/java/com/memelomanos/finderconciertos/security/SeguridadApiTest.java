package com.memelomanos.finderconciertos.security;

import com.memelomanos.finderconciertos.controller.ConciertoController;
import com.memelomanos.finderconciertos.controller.SincronizacionController;
import com.memelomanos.finderconciertos.controller.UsuarioController;
import com.memelomanos.finderconciertos.integracion.superboletos.SuperboletosProveedor;
import com.memelomanos.finderconciertos.integracion.viagogo.ViagogoProveedor;
import com.memelomanos.finderconciertos.model.Concierto;
import com.memelomanos.finderconciertos.model.ConciertoRequest;
import com.memelomanos.finderconciertos.model.LoginResponse;
import com.memelomanos.finderconciertos.model.RolUsuario;
import com.memelomanos.finderconciertos.model.Usuario;
import com.memelomanos.finderconciertos.service.ConciertoService;
import com.memelomanos.finderconciertos.service.SincronizacionService;
import com.memelomanos.finderconciertos.service.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({UsuarioController.class, ConciertoController.class, SincronizacionController.class})
@Import({ConfiguracionSeguridad.class, JwtAuthenticationFilter.class, JwtService.class})
@ActiveProfiles("dev")
@DisplayName("Seguridad y Permisos (JWT y Roles)")
public class SeguridadApiTest {

    private static final String CONCIERTO_JSON = """
            {
              "artista": "Queen",
              "fecha": "2027-02-20",
              "ciudad": "CDMX",
              "lugar": "Auditorio Nacional"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private ConciertoService conciertoService;

    @MockBean
    private SincronizacionService sincronizacionService;

    @MockBean
    private SuperboletosProveedor superboletosProveedor;

    @MockBean
    private ViagogoProveedor viagogoProveedor;

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
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("USUARIO"));
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
    @DisplayName("La consulta de conciertos debe ser pública (GET /conciertos y GET /conciertos/{id})")
    void conciertos_EsPublico() throws Exception {
        Concierto concierto = new Concierto("Queen", LocalDate.of(2027, 2, 20), "CDMX", "Auditorio Nacional");
        when(conciertoService.buscarPorId(1L)).thenReturn(concierto);

        mockMvc.perform(get("/api/v1/conciertos"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/conciertos/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("La búsqueda en Viagogo debe ser pública (GET /conciertos/{id}/viagogo)")
    void viagogo_EsPublico() throws Exception {
        Concierto concierto = new Concierto();
        concierto.setArtista("Coldplay");
        when(conciertoService.buscarPorId(1L)).thenReturn(concierto);
        when(viagogoProveedor.generarUrlBusqueda("Coldplay"))
                .thenReturn("https://www.viagogo.com.mx/secure/Search?q=Coldplay");

        mockMvc.perform(get("/api/v1/conciertos/1/viagogo"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Perfil y favoritos funcionan con token JWT de rol USUARIO")
    void simulacion_LoginConJwtToken() throws Exception {
        String tokenUsuario = jwtService.generarToken("usuario@gmail.com", 1L, RolUsuario.USUARIO);
        Usuario usuario = new Usuario("Usuario", "usuario@gmail.com");
        when(usuarioService.buscarPerfilPorCorreo("usuario@gmail.com")).thenReturn(usuario);

        assertThat(jwtService.extraerRol(tokenUsuario)).isEqualTo(RolUsuario.USUARIO);

        mockMvc.perform(get("/api/v1/usuarios/1/favoritos")
                .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/usuarios/perfil")
                .param("correo", "usuario@gmail.com")
                .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("USUARIO"));
    }

    @Test
    @DisplayName("POST /conciertos: 401 sin token, 403 con USUARIO y 201 con ADMIN")
    void crearConcierto_SoloAdmin() throws Exception {
        String tokenUsuario = jwtService.generarToken("usuario@gmail.com", 2L, RolUsuario.USUARIO);
        String tokenAdmin = jwtService.generarToken("admin@gmail.com", 1L, RolUsuario.ADMIN);
        Concierto creado = new Concierto("Queen", LocalDate.of(2027, 2, 20), "CDMX", "Auditorio Nacional");
        when(conciertoService.crear(any(ConciertoRequest.class))).thenReturn(creado);

        assertThat(jwtService.extraerRol(tokenAdmin)).isEqualTo(RolUsuario.ADMIN);

        mockMvc.perform(post("/api/v1/conciertos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CONCIERTO_JSON))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/conciertos")
                .header("Authorization", "Bearer " + tokenUsuario)
                .contentType(MediaType.APPLICATION_JSON)
                .content(CONCIERTO_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.mensaje").value("No tienes permisos para realizar esta acción"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());

        mockMvc.perform(post("/api/v1/conciertos")
                .header("Authorization", "Bearer " + tokenAdmin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(CONCIERTO_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.artista").value("Queen"));
    }

    @Test
    @DisplayName("PUT /conciertos/{id}: 401 sin token, 403 con USUARIO y 200 con ADMIN")
    void actualizarConcierto_SoloAdmin() throws Exception {
        String tokenUsuario = jwtService.generarToken("usuario@gmail.com", 2L, RolUsuario.USUARIO);
        String tokenAdmin = jwtService.generarToken("admin@gmail.com", 1L, RolUsuario.ADMIN);
        Concierto actualizado = new Concierto("Queen", LocalDate.of(2027, 2, 20), "CDMX", "Auditorio Nacional");
        when(conciertoService.actualizar(eq(1L), any(ConciertoRequest.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/conciertos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CONCIERTO_JSON))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/v1/conciertos/1")
                .header("Authorization", "Bearer " + tokenUsuario)
                .contentType(MediaType.APPLICATION_JSON)
                .content(CONCIERTO_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.mensaje").isNotEmpty());

        mockMvc.perform(put("/api/v1/conciertos/1")
                .header("Authorization", "Bearer " + tokenAdmin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(CONCIERTO_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artista").value("Queen"));
    }

    @Test
    @DisplayName("DELETE /conciertos/{id}: 401 sin token, 403 con USUARIO y 204 con ADMIN")
    void eliminarConcierto_SoloAdmin() throws Exception {
        String tokenUsuario = jwtService.generarToken("usuario@gmail.com", 2L, RolUsuario.USUARIO);
        String tokenAdmin = jwtService.generarToken("admin@gmail.com", 1L, RolUsuario.ADMIN);
        doNothing().when(conciertoService).eliminar(anyLong());

        mockMvc.perform(delete("/api/v1/conciertos/1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/v1/conciertos/1")
                .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.mensaje").isNotEmpty());

        mockMvc.perform(delete("/api/v1/conciertos/1")
                .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /admin/sincronizar: 401 sin token, 403 con USUARIO y 200 con ADMIN")
    void sincronizarAdmin_SoloAdmin() throws Exception {
        String tokenUsuario = jwtService.generarToken("usuario@gmail.com", 2L, RolUsuario.USUARIO);
        String tokenAdmin = jwtService.generarToken("admin@gmail.com", 1L, RolUsuario.ADMIN);
        when(sincronizacionService.sincronizar()).thenReturn(5);

        mockMvc.perform(post("/api/v1/admin/sincronizar"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/admin/sincronizar")
                .header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.mensaje").value("No tienes permisos para realizar esta acción"));

        mockMvc.perform(post("/api/v1/admin/sincronizar")
                .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.procesados").value(5));
    }
}