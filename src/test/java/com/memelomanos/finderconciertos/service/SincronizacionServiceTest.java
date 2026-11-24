package com.memelomanos.finderconciertos.service;

import com.memelomanos.finderconciertos.integracion.ticketmaster.TicketmasterProveedor;
import com.memelomanos.finderconciertos.model.Concierto;
import com.memelomanos.finderconciertos.model.OpcionCompra;
import com.memelomanos.finderconciertos.model.Usuario;
import com.memelomanos.finderconciertos.repository.ConciertoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SincronizacionServiceTest {

    private final PlatformTransactionManager gestorTransacciones = mock(PlatformTransactionManager.class);
    private final TransactionStatus estadoTransaccion = mock(TransactionStatus.class);

    @BeforeEach
    void prepararTransaccion() {
        when(gestorTransacciones.getTransaction(any())).thenReturn(estadoTransaccion);
    }

    @Test
    void actualizaElMismoConciertoSusOpcionesYConservaElFavorito() {
        ConciertoRepository repositorio = mock(ConciertoRepository.class);
        SincronizacionService servicio = new SincronizacionService(
                repositorio, mock(AgregadorConciertosService.class), gestorTransacciones
        );

        when(repositorio.findByFuenteAndIdExterno("TICKETMASTER", "abc123"))
                .thenReturn(Optional.empty());
        when(repositorio.findByFecha(LocalDate.of(2027, 3, 20))).thenReturn(List.of());
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
        assertThat(guardado.getUrlCompra()).isEqualTo("https://example.com/boletos");
        assertThat(guardado.getImagenUrl()).isEqualTo("https://example.com/imagen.jpg");
        assertThat(guardado.getPrecioMinimo()).isEqualByComparingTo("850.00");
        assertThat(guardado.getOpcionesCompra()).hasSize(1);

        Usuario usuario = new Usuario("Ricardo", "ricardo@example.com");
        usuario.agregarFavorito(guardado);
        when(repositorio.findByFuenteAndIdExterno("TICKETMASTER", "abc123"))
                .thenReturn(Optional.of(guardado));

        Concierto nuevosDatos = eventoEn("Recinto actualizado");
        nuevosDatos.setUrlCompra("https://example.com/boletos-actualizados");
        nuevosDatos.setImagenUrl("https://example.com/imagen-actualizada.jpg");
        nuevosDatos.setPrecioMinimo(new BigDecimal("900.00"));
        nuevosDatos.agregarOpcionCompra(new OpcionCompra(
                "VIAGOGO", "https://viagogo.example/boletos", new BigDecimal("1150.00"), true
        ));
        assertThat(servicio.guardar(List.of(nuevosDatos))).isEqualTo(1);

        verify(repositorio, times(2)).save(captura.capture());
        Concierto actualizado = captura.getValue();
        assertThat(actualizado).isSameAs(guardado);
        assertThat(actualizado.getId()).isEqualTo(20L);
        assertThat(actualizado.getUrlCompra()).isEqualTo(nuevosDatos.getUrlCompra());
        assertThat(actualizado.getImagenUrl()).isEqualTo(nuevosDatos.getImagenUrl());
        assertThat(actualizado.getPrecioMinimo()).isEqualByComparingTo("900.00");
        assertThat(actualizado.getOpcionesCompra())
                .extracting(OpcionCompra::getFuente, OpcionCompra::isEsReventa)
                .containsExactly(
                        tuple("TICKETMASTER", false),
                        tuple("VIAGOGO", true)
                );
        assertThat(usuario.getFavoritos()).singleElement()
                .satisfies(favorito -> {
                    assertThat(favorito.getId()).isEqualTo(20L);
                    assertThat(favorito.getLugar()).isEqualTo("Recinto actualizado");
                });
    }

    @Test
    void reutilizaConciertoExistenteCuandoCoincidePorArtistaFechaYCiudad() {
        ConciertoRepository repositorio = mock(ConciertoRepository.class);
        SincronizacionService servicio = new SincronizacionService(
                repositorio, mock(AgregadorConciertosService.class), gestorTransacciones
        );

        Concierto existenteDeViagogo = new Concierto(
                "cafe tacvba", LocalDate.of(2027, 3, 20), "cdmx", "Auditorio"
        );
        ReflectionTestUtils.setField(existenteDeViagogo, "id", 35L);
        existenteDeViagogo.setFuente("VIAGOGO");
        existenteDeViagogo.setIdExterno("vg-555");

        when(repositorio.findByFuenteAndIdExterno("TICKETMASTER", "abc123"))
                .thenReturn(Optional.empty());
        when(repositorio.findByFecha(LocalDate.of(2027, 3, 20)))
                .thenReturn(List.of(existenteDeViagogo));

        Concierto desdeAgregador = eventoEn("Auditorio Nacional");
        desdeAgregador.agregarOpcionCompra(new OpcionCompra(
                "VIAGOGO", "https://viagogo.example/boletos", new BigDecimal("1100.00"), true
        ));

        assertThat(servicio.guardar(List.of(desdeAgregador))).isEqualTo(1);

        ArgumentCaptor<Concierto> captura = ArgumentCaptor.forClass(Concierto.class);
        verify(repositorio).save(captura.capture());
        Concierto actualizado = captura.getValue();
        assertThat(actualizado).isSameAs(existenteDeViagogo);
        assertThat(actualizado.getId()).isEqualTo(35L);
        assertThat(actualizado.getOpcionesCompra()).hasSize(2);
    }

    @Test
    void ignoraEventosSinIdentificacionExterna() {
        ConciertoRepository repositorio = mock(ConciertoRepository.class);
        SincronizacionService servicio = new SincronizacionService(
                repositorio, mock(AgregadorConciertosService.class), gestorTransacciones
        );
        Concierto sinId = eventoEn("Recinto");
        sinId.setIdExterno(" ");
        Concierto sinFuente = eventoEn("Recinto");
        sinFuente.setFuente(null);

        assertThat(servicio.guardar(List.of(sinId, sinFuente))).isZero();
        verifyNoInteractions(repositorio);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void consultaElAgregadorAntesDeAbrirLaTransaccionYGuardaTodosLosDatos(boolean cancelado) {
        ConciertoRepository repositorio = mock(ConciertoRepository.class);
        AgregadorConciertosService agregador = mock(AgregadorConciertosService.class);
        SincronizacionService servicio =
                new SincronizacionService(repositorio, agregador, gestorTransacciones);

        Concierto deduplicado = eventoEn("Recinto");
        deduplicado.setDisponible(!cancelado);
        deduplicado.agregarOpcionCompra(new OpcionCompra(
                "VIAGOGO", "https://viagogo.example/boletos", new BigDecimal("1200.00"), true
        ));
        when(agregador.descargarCatalogos()).thenReturn(
                new AgregadorConciertosService.CatalogoAgregado(List.of(deduplicado), Map.of())
        );
        when(repositorio.findByFuenteAndIdExterno("TICKETMASTER", "abc123"))
                .thenReturn(Optional.empty());
        when(repositorio.findByFecha(LocalDate.of(2027, 3, 20))).thenReturn(List.of());

        assertThat(servicio.sincronizar()).isEqualTo(1);

        var orden = inOrder(agregador, gestorTransacciones, repositorio);
        orden.verify(agregador).descargarCatalogos();
        orden.verify(gestorTransacciones).getTransaction(any());
        orden.verify(repositorio).findByFuenteAndIdExterno("TICKETMASTER", "abc123");
        ArgumentCaptor<Concierto> captura = ArgumentCaptor.forClass(Concierto.class);
        orden.verify(repositorio).save(captura.capture());
        orden.verify(gestorTransacciones).commit(estadoTransaccion);
        Concierto guardado = captura.getValue();
        assertThat(guardado.isDisponible()).isEqualTo(!cancelado);
        assertThat(guardado.getLatitud()).isEqualByComparingTo("19.4241");
        assertThat(guardado.getLongitud()).isEqualByComparingTo("-99.1949");
        assertThat(guardado.getUrlCompra()).isEqualTo("https://example.com/boletos");
        assertThat(guardado.getImagenUrl()).isEqualTo("https://example.com/imagen.jpg");
        assertThat(guardado.getPrecioMinimo()).isEqualByComparingTo("850.00");
        assertThat(guardado.getOpcionesCompra())
                .extracting(OpcionCompra::getFuente, OpcionCompra::getUrl, OpcionCompra::isEsReventa)
                .containsExactly(
                        tuple("TICKETMASTER", "https://example.com/boletos", false),
                        tuple("VIAGOGO", "https://viagogo.example/boletos", true)
                );
    }

    @ParameterizedTest
    @CsvSource({"true, false", "false, false", "true, true"})
    void soloMarcaAusentesConUnCatalogoCompletoYNoVacio(boolean completo, boolean vacio) {
        ConciertoRepository repositorio = mock(ConciertoRepository.class);
        AgregadorConciertosService agregador = mock(AgregadorConciertosService.class);
        SincronizacionService servicio =
                new SincronizacionService(repositorio, agregador, gestorTransacciones);
        Concierto ausente = eventoEn("Recinto");
        ReflectionTestUtils.setField(ausente, "id", 20L);
        Usuario usuario = new Usuario("Ricardo", "ricardo@example.com");
        usuario.agregarFavorito(ausente);

        Concierto recibido = new Concierto("Otro artista", ausente.getFecha(), "CDMX", "Recinto");
        recibido.setFuente(TicketmasterProveedor.FUENTE);
        recibido.setIdExterno("otro-id");

        List<Concierto> eventos = vacio ? List.of() : List.of(recibido);
        Map<String, Set<String>> fuentesCompletas = (completo && !vacio)
                ? Map.of(TicketmasterProveedor.FUENTE, Set.of("otro-id"))
                : Map.of();
        when(agregador.descargarCatalogos()).thenReturn(
                new AgregadorConciertosService.CatalogoAgregado(eventos, fuentesCompletas)
        );
        when(repositorio.findByFuenteAndIdExterno(TicketmasterProveedor.FUENTE, "otro-id"))
                .thenReturn(Optional.empty());
        when(repositorio.findByFecha(ausente.getFecha())).thenReturn(List.of());
        when(repositorio.findByFuenteAndDisponibleTrueAndFechaGreaterThanEqual(
                eq(TicketmasterProveedor.FUENTE), any(LocalDate.class)))
                .thenReturn(List.of(ausente));

        assertThat(servicio.sincronizar()).isEqualTo(eventos.size());

        assertThat(ausente.isDisponible()).isEqualTo(!completo || vacio);
        if (vacio) {
            verifyNoInteractions(repositorio);
        }
        assertThat(usuario.getFavoritos()).containsExactly(ausente);
        verify(repositorio, never()).delete(any(Concierto.class));
    }

    @Test
    void revierteLaTransaccionSiFallaElGuardado() {
        ConciertoRepository repositorio = mock(ConciertoRepository.class);
        SincronizacionService servicio = new SincronizacionService(
                repositorio, mock(AgregadorConciertosService.class), gestorTransacciones
        );
        when(repositorio.findByFuenteAndIdExterno("TICKETMASTER", "abc123"))
                .thenReturn(Optional.empty());
        when(repositorio.findByFecha(LocalDate.of(2027, 3, 20))).thenReturn(List.of());
        when(repositorio.save(any(Concierto.class)))
                .thenThrow(new IllegalStateException("Fallo simulado de guardado"));

        assertThatThrownBy(() -> servicio.guardar(List.of(eventoEn("Recinto"))))
                .isInstanceOf(IllegalStateException.class);

        verify(gestorTransacciones).rollback(estadoTransaccion);
        verify(gestorTransacciones, never()).commit(any());
    }

    private static Concierto eventoEn(String lugar) {
        Concierto evento = new Concierto("Café Tacvba", LocalDate.of(2027, 3, 20), "CDMX", lugar);
        evento.setFuente("TICKETMASTER");
        evento.setIdExterno("abc123");
        evento.setLatitud(new BigDecimal("19.4241"));
        evento.setLongitud(new BigDecimal("-99.1949"));
        evento.setUrlCompra("https://example.com/boletos");
        evento.setImagenUrl("https://example.com/imagen.jpg");
        evento.setPrecioMinimo(new BigDecimal("850.00"));
        evento.agregarOpcionCompra(new OpcionCompra(
                "TICKETMASTER", "https://example.com/boletos", new BigDecimal("850.00"), false
        ));
        return evento;
    }
}
