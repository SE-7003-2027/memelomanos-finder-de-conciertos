package com.memelomanos.finderconciertos.controller;

import org.junit.jupiter.api.DisplayName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.memelomanos.finderconciertos.model.FavoritoRequest;
import com.memelomanos.finderconciertos.model.RegistroRequest;
import com.memelomanos.finderconciertos.model.Usuario;
import com.memelomanos.finderconciertos.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Controlador de Usuarios (Capa Web)")
@WebMvcTest(UsuarioController.class)
public class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @Autowired
    private ObjectMapper objectMapper;

    private Usuario usuarioMock;

    @BeforeEach
    void setUp() {
        usuarioMock = new Usuario("Test User", "test@correo.com");
    }

    @Test
    @DisplayName("Debe retornar 201 Created al registrar usuario")
    void registrarUsuario_DebeRetornarCreated() throws Exception {
        RegistroRequest request = new RegistroRequest();
        request.setNombre("Test User");
        request.setCorreo("test@correo.com");

        when(usuarioService.registrarUsuario(anyString(), anyString())).thenReturn(usuarioMock);

        mockMvc.perform(post("/api/v1/usuarios/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Test User"))
                .andExpect(jsonPath("$.correo").value("test@correo.com"));
    }

    @Test
    @DisplayName("Debe retornar 200 OK al agregar un favorito")
    void agregarFavorito_DebeRetornarOk() throws Exception {
        FavoritoRequest request = new FavoritoRequest();
        request.setUsuarioId(1L);
        request.setConciertoId(10L);

        when(usuarioService.agregarFavorito(anyLong(), anyLong())).thenReturn(usuarioMock);

        mockMvc.perform(post("/api/v1/usuarios/favoritos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Test User"));
    }

    @Test
    @DisplayName("Debe retornar 204 No Content al eliminar usuario")
    void eliminarUsuario_DebeRetornarNoContent() throws Exception {
        doNothing().when(usuarioService).eliminarUsuario(1L);

        mockMvc.perform(delete("/api/v1/usuarios/1"))
                .andExpect(status().isNoContent());
        
        verify(usuarioService, times(1)).eliminarUsuario(1L);
    }

    @Test
    @DisplayName("Debe retornar 400 Bad Request si el correo tiene un formato inválido")
    void registrarUsuario_CorreoInvalido_RetornaBadRequest() throws Exception {
        RegistroRequest request = new RegistroRequest();
        request.setNombre("Juan Perez");
        request.setCorreo("correo fake 1234"); // Esto violará la etiqueta @Email del modelo

        mockMvc.perform(post("/api/v1/usuarios/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // Esperamos un 400

        // Comprobamos que Spring detuvo la petición y NUNCA llegó al servicio
        verify(usuarioService, never()).registrarUsuario(anyString(), anyString());
    }

    @Test
    @DisplayName("Debe retornar 400 Bad Request si faltan datos obligatorios (ej. Nombre)")
    void registrarUsuario_FaltaNombre_RetornaBadRequest() throws Exception {
        RegistroRequest request = new RegistroRequest();
        request.setNombre(""); // Esto violará la etiqueta @NotBlank
        request.setCorreo("juan@correo.com");

        mockMvc.perform(post("/api/v1/usuarios/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // Esperamos un 400

        // Comprobamos que Spring detuvo la petición y NUNCA llegó al servicio
        verify(usuarioService, never()).registrarUsuario(anyString(), anyString());
    }
}
