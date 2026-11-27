package com.memelomanos.finderconciertos.service;

import org.junit.jupiter.api.DisplayName;

import com.memelomanos.finderconciertos.RelojDePrueba;
import com.memelomanos.finderconciertos.exception.ConciertoNoEncontradoException;
import com.memelomanos.finderconciertos.model.Concierto;
import com.memelomanos.finderconciertos.model.ConciertoRequest;
import com.memelomanos.finderconciertos.repository.ConciertoRepository;
import com.memelomanos.finderconciertos.repository.UsuarioRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Servicio de Conciertos (Lógica de Negocio)")
public class ConciertoServiceTest {

    @Mock
    private ConciertoRepository conciertoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private final LocalDate hoy = LocalDate.of(2026, 11, 25);
    private ConciertoService conciertoService;

    @BeforeEach
    void configurarServicio() {
        var reloj = new RelojDePrueba(Instant.parse("2026-11-25T12:00:00Z"));
        conciertoService = new ConciertoService(conciertoRepository, usuarioRepository, reloj);
    }

    @Test
    void buscar_sinFechaInicialFiltraDesdeHoy() {
        comprobarFechaInicial(null, hoy);
    }

    @Test
    void buscar_respetaUnaFechaInicialAnteriorAHoy() {
        LocalDate ayer = hoy.minusDays(1);
        comprobarFechaInicial(ayer, ayer);
    }

    private void comprobarFechaInicial(LocalDate solicitada, LocalDate esperada) {
        @SuppressWarnings("unchecked")
        Root<Concierto> concierto = mock(Root.class);
        @SuppressWarnings("unchecked")
        Path<LocalDate> fecha = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<Boolean> disponible = mock(Path.class);
        CriteriaBuilder constructor = mock(CriteriaBuilder.class);
        when(concierto.<LocalDate>get("fecha")).thenReturn(fecha);
        when(concierto.<Boolean>get("disponible")).thenReturn(disponible);
        when(conciertoRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenAnswer(consulta -> {
                    Specification<Concierto> filtro = consulta.getArgument(0);
                    filtro.toPredicate(concierto, null, constructor);
                    return List.of();
                });

        conciertoService.buscar(null, null, solicitada, null);

        verify(constructor).greaterThanOrEqualTo(fecha, esperada);
        verify(constructor).isTrue(disponible);
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
