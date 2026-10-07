package com.memelomanos.finderconciertos.service;

import com.memelomanos.finderconciertos.exception.ConciertoNoEncontradoException;
import com.memelomanos.finderconciertos.model.Concierto;
import com.memelomanos.finderconciertos.model.ConciertoRequest;
import com.memelomanos.finderconciertos.repository.ConciertoRepository;
import com.memelomanos.finderconciertos.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConciertoServiceTest {

    @Mock
    private ConciertoRepository conciertoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ConciertoService conciertoService;

    @Test
    void buscar_SinFiltros_RetornaTodos() {
        Concierto c1 = new Concierto();
        c1.setArtista("Artista 1");
        
        when(conciertoRepository.findAll()).thenReturn(List.of(c1));

        List<Concierto> resultado = conciertoService.buscar(null, null);

        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
        verify(conciertoRepository, times(1)).findAll();
    }

    @Test
    void buscarPorId_LanzaConciertoNoEncontradoException() {
        when(conciertoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ConciertoNoEncontradoException.class, () -> {
            conciertoService.buscarPorId(99L);
        });
    }

    @Test
    void crear_Exito() {
        ConciertoRequest request = new ConciertoRequest();
        request.setArtista("Coldplay");
        request.setCiudad("Mexico");
        
        Concierto conciertoGuardado = new Concierto();
        conciertoGuardado.setArtista("Coldplay");
        conciertoGuardado.setCiudad("Mexico");

        when(conciertoRepository.save(any(Concierto.class))).thenReturn(conciertoGuardado);

        Concierto resultado = conciertoService.crear(request);

        assertNotNull(resultado);
        assertEquals("Coldplay", resultado.getArtista());
        verify(conciertoRepository, times(1)).save(any(Concierto.class));
    }
}
