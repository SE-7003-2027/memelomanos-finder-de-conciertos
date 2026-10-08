package com.memelomanos.finderconciertos.controller;

import org.junit.jupiter.api.DisplayName;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.memelomanos.finderconciertos.model.Concierto;
import com.memelomanos.finderconciertos.model.ConciertoRequest;
import com.memelomanos.finderconciertos.service.ConciertoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConciertoController.class)
@DisplayName("Controlador de Conciertos (Capa Web)")
public class ConciertoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConciertoService conciertoService;

    @Autowired
    private ObjectMapper objectMapper;

    private Concierto conciertoMock;

    @BeforeEach
    void setUp() {
        conciertoMock = new Concierto();
        conciertoMock.setArtista("Coldplay");
        conciertoMock.setCiudad("Mexico");
    }

    @Test
    void buscar_DebeRetornarOk() throws Exception {
        when(conciertoService.buscar(null, null)).thenReturn(List.of(conciertoMock));

        mockMvc.perform(get("/api/v1/conciertos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].artista").value("Coldplay"));
    }

    @Test
    void buscarPorId_DebeRetornarOk() throws Exception {
        when(conciertoService.buscarPorId(1L)).thenReturn(conciertoMock);

        mockMvc.perform(get("/api/v1/conciertos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artista").value("Coldplay"));
    }

    @Test
    void eliminar_DebeRetornarNoContent() throws Exception {
        doNothing().when(conciertoService).eliminar(1L);

        mockMvc.perform(delete("/api/v1/conciertos/1"))
                .andExpect(status().isNoContent());

        verify(conciertoService, times(1)).eliminar(1L);
    }
}
