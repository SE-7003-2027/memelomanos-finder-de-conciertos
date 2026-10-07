package com.memelomanos.finderconciertos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    void registrarUsuario_DebeRetornarCreated() throws Exception {
        RegistroRequest request = new RegistroRequest();
        request.setNombre("Test User");
        request.setCorreo("test@correo.com");

        when(usuarioService.registrarUsuario(anyString(), anyString())).thenReturn(usuarioMock);

        mockMvc.perform(post("/usuarios/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Test User"))
                .andExpect(jsonPath("$.correo").value("test@correo.com"));
    }

    @Test
    void obtenerUsuario_DebeRetornarOk() throws Exception {
        when(usuarioService.buscarUsuarioPorId(1L)).thenReturn(usuarioMock);

        mockMvc.perform(get("/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Test User"));
    }

    @Test
    void eliminarUsuario_DebeRetornarNoContent() throws Exception {
        doNothing().when(usuarioService).eliminarUsuario(1L);

        mockMvc.perform(delete("/usuarios/1"))
                .andExpect(status().isNoContent());
        
        verify(usuarioService, times(1)).eliminarUsuario(1L);
    }
}
