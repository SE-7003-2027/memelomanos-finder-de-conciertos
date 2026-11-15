package com.memelomanos.finderconciertos.service;

import com.memelomanos.finderconciertos.model.Concierto;
import com.memelomanos.finderconciertos.model.Usuario;
import com.memelomanos.finderconciertos.repository.ConciertoRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SincronizacionServiceTest {

    @Test
    void actualizaElMismoConciertoYConservaElFavorito() {
        ConciertoRepository repositorio = mock(ConciertoRepository.class);
        SincronizacionService servicio = new SincronizacionService(repositorio);

        when(repositorio.findByFuenteAndIdExterno("TICKETMASTER", "abc123"))
                .thenReturn(Optional.empty());
        when(repositorio.save(any(Concierto.class))).thenAnswer(invocacion -> {
            Concierto concierto = invocacion.getArgument(0);
            if (concierto.getId() == null) {
                ReflectionTestUtils.setField(concierto, "id", 20L);
            }
            return concierto;
        });

        assertThat(servicio.guardar(List.of(eventoEn("Recinto original")))).isEqualTo(1);
        ArgumentCaptor<Concierto> captura = ArgumentCaptor.forClass(Concierto.class);
        verify(repositorio).save(captura.capture());
        Concierto guardado = captura.getValue();
        assertThat(guardado.getLugar()).isEqualTo("Recinto original");
        assertThat(guardado.getLatitud()).isEqualByComparingTo("19.4241");
        assertThat(guardado.getLongitud()).isEqualByComparingTo("-99.1949");

        Usuario usuario = new Usuario("Ricardo", "ricardo@example.com");
        usuario.agregarFavorito(guardado);
        when(repositorio.findByFuenteAndIdExterno("TICKETMASTER", "abc123"))
                .thenReturn(Optional.of(guardado));

        assertThat(servicio.guardar(List.of(eventoEn("Recinto actualizado")))).isEqualTo(1);

        verify(repositorio, times(2)).save(captura.capture());
        Concierto actualizado = captura.getValue();
        assertThat(actualizado).isSameAs(guardado);
        assertThat(actualizado.getId()).isEqualTo(20L);
        assertThat(usuario.getFavoritos()).singleElement()
                .satisfies(favorito -> {
                    assertThat(favorito.getId()).isEqualTo(20L);
                    assertThat(favorito.getLugar()).isEqualTo("Recinto actualizado");
                });
    }

    @Test
    void ignoraEventosSinIdentificacionExterna() {
        ConciertoRepository repositorio = mock(ConciertoRepository.class);
        SincronizacionService servicio = new SincronizacionService(repositorio);
        Concierto sinId = eventoEn("Recinto");
        sinId.setIdExterno(" ");
        Concierto sinFuente = eventoEn("Recinto");
        sinFuente.setFuente(null);

        assertThat(servicio.guardar(List.of(sinId, sinFuente))).isZero();
        verifyNoInteractions(repositorio);
    }

    private static Concierto eventoEn(String lugar) {
        Concierto evento = new Concierto("Café Tacvba", LocalDate.of(2027, 3, 20), "CDMX", lugar);
        evento.setFuente("TICKETMASTER");
        evento.setIdExterno("abc123");
        evento.setLatitud(new BigDecimal("19.4241"));
        evento.setLongitud(new BigDecimal("-99.1949"));
        return evento;
    }
}
