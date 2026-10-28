package com.memelomanos.finderconciertos.service;

import org.junit.jupiter.api.DisplayName;
import com.memelomanos.finderconciertos.exception.ConciertoNoEncontradoException;
import com.memelomanos.finderconciertos.exception.CorreoDuplicadoException;
import com.memelomanos.finderconciertos.exception.CredencialesInvalidasException;
import com.memelomanos.finderconciertos.exception.UsuarioNoEncontradoException;
import com.memelomanos.finderconciertos.model.Concierto;
import com.memelomanos.finderconciertos.model.LoginResponse;
import com.memelomanos.finderconciertos.model.Usuario;
import com.memelomanos.finderconciertos.repository.ConciertoRepository;
import com.memelomanos.finderconciertos.repository.UsuarioRepository;
import com.memelomanos.finderconciertos.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    @DisplayName("Debe registrar un usuario correctamente con password encriptado")
    void registrarUsuario_Exito() {
        when(usuarioRepository.existsByCorreo(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        
        Usuario usuarioMock = new Usuario("Juan", "juan@test.com", "hashedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioMock);

        Usuario resultado = usuarioService.registrarUsuario("Juan", "juan@test.com", "password123");

        assertNotNull(resultado);
        assertEquals("Juan", resultado.getNombre());
        verify(passwordEncoder, times(1)).encode("password123");
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debe fallar al registrar si el correo ya existe")
    void registrarUsuario_LanzaCorreoDuplicadoException() {
        when(usuarioRepository.existsByCorreo("juan@test.com")).thenReturn(true);

        assertThrows(CorreoDuplicadoException.class, () -> {
            usuarioService.registrarUsuario("Juan", "juan@test.com", "password123");
        });

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Login exitoso debe retornar token JWT")
    void login_Exito() {
        Usuario usuarioMock = new Usuario("Juan", "juan@test.com", "hashedPassword");
        when(usuarioRepository.findByCorreo("juan@test.com")).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(jwtService.generarToken(eq("juan@test.com"), any())).thenReturn("fake-jwt-token");

        LoginResponse response = usuarioService.login("juan@test.com", "password123");

        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());
    }

    @Test
    @DisplayName("Login con contraseña incorrecta debe lanzar CredencialesInvalidasException")
    void login_PasswordIncorrecto_LanzaExcepcion() {
        Usuario usuarioMock = new Usuario("Juan", "juan@test.com", "hashedPassword");
        when(usuarioRepository.findByCorreo("juan@test.com")).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        assertThrows(CredencialesInvalidasException.class, () -> {
            usuarioService.login("juan@test.com", "wrongPassword");
        });
    }

    @Test
    @DisplayName("Debe agregar un concierto a favoritos correctamente")
    void agregarFavorito_Exito() {
        Usuario usuarioMock = new Usuario("Juan", "juan@test.com");
        Concierto conciertoMock = new Concierto();
        conciertoMock.setArtista("Coldplay");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
        when(conciertoRepository.findById(10L)).thenReturn(Optional.of(conciertoMock));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioMock);

        Usuario resultado = usuarioService.agregarFavorito(1L, 10L);

        assertTrue(resultado.getFavoritos().contains(conciertoMock));
        verify(usuarioRepository, times(1)).save(usuarioMock);
    }

    @Test
    @DisplayName("Debe lanzar excepción si el concierto no existe al agregar a favoritos")
    void agregarFavorito_LanzaConciertoNoEncontrado() {
        Usuario usuarioMock = new Usuario("Juan", "juan@test.com");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
        when(conciertoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ConciertoNoEncontradoException.class, () -> {
            usuarioService.agregarFavorito(1L, 99L);
        });

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}
