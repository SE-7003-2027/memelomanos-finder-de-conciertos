package com.memelomanos.finderconciertos.service;

import org.junit.jupiter.api.DisplayName;
import com.memelomanos.finderconciertos.exception.ConciertoNoEncontradoException;
import com.memelomanos.finderconciertos.exception.CorreoDuplicadoException;
import com.memelomanos.finderconciertos.exception.UsuarioNoEncontradoException;
import com.memelomanos.finderconciertos.model.Concierto;
import com.memelomanos.finderconciertos.model.Usuario;
import com.memelomanos.finderconciertos.repository.ConciertoRepository;
import com.memelomanos.finderconciertos.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("Servicio de Usuarios (Lógica de Negocio)")
@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ConciertoRepository conciertoRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    @DisplayName("Debe registrar un usuario correctamente")
    void registrarUsuario_Exito() {
        when(usuarioRepository.existsByCorreo(anyString())).thenReturn(false);
        
        Usuario usuarioMock = new Usuario("Juan", "juan@test.com");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioMock);

        Usuario resultado = usuarioService.registrarUsuario("Juan", "juan@test.com");

        assertNotNull(resultado);
        assertEquals("Juan", resultado.getNombre());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debe fallar al registrar si el correo ya existe")
    void registrarUsuario_LanzaCorreoDuplicadoException() {
        when(usuarioRepository.existsByCorreo("juan@test.com")).thenReturn(true);

        assertThrows(CorreoDuplicadoException.class, () -> {
            usuarioService.registrarUsuario("Juan", "juan@test.com");
        });

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debe agregar un concierto a favoritos correctamente")
    void agregarFavorito_Exito() {
        Usuario usuarioMock = new Usuario("Juan", "juan@test.com");
        Concierto conciertoMock = new Concierto();
        conciertoMock.setArtista("Coldplay");

        // Simulamos que encontramos el usuario y el concierto en la BD
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
        when(conciertoRepository.findById(10L)).thenReturn(Optional.of(conciertoMock));
        
        // Simulamos el guardado
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioMock);

        Usuario resultado = usuarioService.agregarFavorito(1L, 10L);

        // Verificamos que el concierto se haya agregado a la lista
        assertTrue(resultado.getFavoritos().contains(conciertoMock));
        verify(usuarioRepository, times(1)).save(usuarioMock);
    }

    @Test
    @DisplayName("Debe lanzar excepción si el concierto no existe al agregar a favoritos")
    void agregarFavorito_LanzaConciertoNoEncontrado() {
        Usuario usuarioMock = new Usuario("Juan", "juan@test.com");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
        // Simulamos que el concierto NO existe
        when(conciertoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ConciertoNoEncontradoException.class, () -> {
            usuarioService.agregarFavorito(1L, 99L);
        });

        // Verificamos que NUNCA se intentó guardar nada
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}
