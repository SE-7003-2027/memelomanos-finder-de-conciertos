package com.memelomanos.finderconciertos.service;

import com.memelomanos.finderconciertos.exception.CorreoDuplicadoException;
import com.memelomanos.finderconciertos.exception.UsuarioNoEncontradoException;
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

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ConciertoRepository conciertoRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void registrarUsuario_Exito() {
        when(usuarioRepository.existsByCorreo(anyString())).thenReturn(false);
        
        Usuario usuarioMock = new Usuario("Juan", "juan@test.com");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioMock);

        Usuario resultado = usuarioService.registrarUsuario("Juan", "juan@test.com");

        assertNotNull(resultado);
        assertEquals("Juan", resultado.getNombre());
        assertEquals("juan@test.com", resultado.getCorreo());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void registrarUsuario_LanzaCorreoDuplicadoException() {
        when(usuarioRepository.existsByCorreo("juan@test.com")).thenReturn(true);

        assertThrows(CorreoDuplicadoException.class, () -> {
            usuarioService.registrarUsuario("Juan", "juan@test.com");
        });

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void buscarPerfilPorCorreo_Exito() {
        Usuario usuarioMock = new Usuario("Maria", "maria@test.com");
        when(usuarioRepository.findByCorreo("maria@test.com")).thenReturn(Optional.of(usuarioMock));

        Usuario resultado = usuarioService.buscarPerfilPorCorreo("maria@test.com");

        assertEquals("Maria", resultado.getNombre());
    }

    @Test
    void buscarPerfilPorCorreo_LanzaUsuarioNoEncontradoException() {
        when(usuarioRepository.findByCorreo("noexiste@test.com")).thenReturn(Optional.empty());

        assertThrows(UsuarioNoEncontradoException.class, () -> {
            usuarioService.buscarPerfilPorCorreo("noexiste@test.com");
        });
    }
}
