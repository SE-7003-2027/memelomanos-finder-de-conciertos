package com.memelomanos.finderconciertos.service;

import com.memelomanos.finderconciertos.integracion.ConciertoExterno;
import com.memelomanos.finderconciertos.integracion.CriteriosBusqueda;
import com.memelomanos.finderconciertos.integracion.ProveedorConciertos;
import com.memelomanos.finderconciertos.integracion.ProveedorFalso;
import com.memelomanos.finderconciertos.model.Concierto;
import com.memelomanos.finderconciertos.model.OpcionCompra;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Servicio Agregador de Conciertos")
class AgregadorConciertosServiceTest {

    private static final LocalDate FECHA_CONCIERTO = LocalDate.of(2027, 3, 20);

    @Test
    void deduplicaConciertoDeDosProveedoresNormalizandoAcentosYMayusculas() {
        ConciertoExterno enTicketmaster = new ConciertoExterno(
                "TICKETMASTER",
                "tm-001",
                "Café Tacvba",
                FECHA_CONCIERTO,
                "Ciudad de México",
                "Auditorio Nacional",
                "https://ticketmaster.example/cafe-tacvba",
                null,
                null,
                null,
                new BigDecimal("1100.00")
        );
        ConciertoExterno enViagogo = new ConciertoExterno(
                "VIAGOGO",
                "vg-999",
                "  CAFE   TACVBA ",
                FECHA_CONCIERTO,
                "ciudad de mexico",
                null,
                "https://viagogo.example/cafe-tacvba",
                "https://cdn.example/cafe-tacvba.jpg",
                new BigDecimal("19.4241"),
                new BigDecimal("-99.1949"),
                new BigDecimal("950.00")
        );

        var ticketmaster = new ProveedorFalso("TICKETMASTER", List.of(enTicketmaster));
        var viagogo = ProveedorFalso.deReventa("VIAGOGO", List.of(enViagogo));
        var agregador = new AgregadorConciertosService(
                List.of(ticketmaster, viagogo),
                Duration.ofSeconds(2)
        );

        List<Concierto> resultado = agregador.buscar(CriteriosBusqueda.vacio());
        agregador.cerrarEjecutor();

        assertThat(resultado).hasSize(1);
        Concierto unico = resultado.getFirst();
        assertThat(unico.getArtista()).isEqualTo("Café Tacvba");
        assertThat(unico.getLugar()).isEqualTo("Auditorio Nacional");
        assertThat(unico.getImagenUrl()).isEqualTo("https://cdn.example/cafe-tacvba.jpg");
        assertThat(unico.getLatitud()).isEqualByComparingTo("19.4241");
        assertThat(unico.getLongitud()).isEqualByComparingTo("-99.1949");
        assertThat(unico.getPrecioMinimo()).isEqualByComparingTo("950.00");

        assertThat(unico.getOpcionesCompra())
                .extracting(
                        OpcionCompra::getFuente,
                        OpcionCompra::getUrl,
                        OpcionCompra::getPrecioMinimo,
                        OpcionCompra::isEsReventa
                )
                .containsExactly(
                        tuple("TICKETMASTER", "https://ticketmaster.example/cafe-tacvba",
                                new BigDecimal("1100.00"), false),
                        tuple("VIAGOGO", "https://viagogo.example/cafe-tacvba",
                                new BigDecimal("950.00"), true)
                );
        assertThat(unico.getOpcionesCompra())
                .allSatisfy(opcion -> assertThat(opcion.getConcierto()).isSameAs(unico));
    }

    @Test
    void mantieneSeparadosConciertosConDistintaFechaCiudadOArtista() {
        ConciertoExterno zoeCdmx = evento("TICKETMASTER", "1", "Zoé", FECHA_CONCIERTO, "CDMX", "800.00");
        ConciertoExterno zoeGdl = evento("TICKETMASTER", "2", "Zoé", FECHA_CONCIERTO, "Guadalajara", "800.00");
        ConciertoExterno zoeOtraFecha = evento(
                "SUPERBOLETOS", "3", "Zoé", FECHA_CONCIERTO.plusDays(1), "CDMX", "850.00"
        );
        ConciertoExterno caifanesCdmx = evento("SUPERBOLETOS", "4", "Caifanes", FECHA_CONCIERTO, "CDMX", "900.00");

        var agregador = new AgregadorConciertosService(
                List.of(
                        new ProveedorFalso("TICKETMASTER", List.of(zoeCdmx, zoeGdl)),
                        new ProveedorFalso("SUPERBOLETOS", List.of(zoeOtraFecha, caifanesCdmx))
                ),
                Duration.ofSeconds(2)
        );

        List<Concierto> resultado = agregador.buscar(null);

        assertThat(resultado).hasSize(4);
        assertThat(resultado).allSatisfy(concierto ->
                assertThat(concierto.getOpcionesCompra()).hasSize(1)
        );
    }

    @Test
    void aislaProveedorQueFallaYDevuelveResultadosDeLosDemas() {
        ProveedorConciertos caido = new ProveedorConciertos() {
            @Override
            public List<ConciertoExterno> buscar(CriteriosBusqueda criterios) {
                throw new IllegalStateException("Servicio externo no disponible");
            }

            @Override
            public String nombre() {
                return "PROVEEDOR_CAIDO";
            }
        };

        ConciertoExterno valido = evento("TICKETMASTER", "tm-1", "Enjambre", FECHA_CONCIERTO, "CDMX", "750.00");
        var sano = new ProveedorFalso("TICKETMASTER", List.of(valido));

        var agregador = new AgregadorConciertosService(
                List.of(caido, sano),
                Duration.ofSeconds(2)
        );

        List<Concierto> resultado = agregador.buscar(CriteriosBusqueda.vacio());

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getArtista()).isEqualTo("Enjambre");
        assertThat(resultado.getFirst().getOpcionesCompra()).hasSize(1);
    }

    @Test
    void omiteProveedorQueExcedeTimeoutSinBloquearALosDemas() {
        CountDownLatch lentoLiberado = new CountDownLatch(1);
        ProveedorConciertos lento = new ProveedorConciertos() {
            @Override
            public List<ConciertoExterno> buscar(CriteriosBusqueda criterios) {
                try {
                    lentoLiberado.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                return List.of(evento("LENTO", "lento-1", "Artista Lento", FECHA_CONCIERTO, "CDMX", "500.00"));
            }

            @Override
            public String nombre() {
                return "PROVEEDOR_LENTO";
            }
        };

        ConciertoExterno rapidoEvento = evento("TICKETMASTER", "tm-2", "Siddhartha", FECHA_CONCIERTO, "CDMX", "650.00");
        var rapido = new ProveedorFalso("TICKETMASTER", List.of(rapidoEvento));

        var agregador = new AgregadorConciertosService(
                List.of(lento, rapido),
                Duration.ofMillis(120)
        );

        try {
            long inicio = System.nanoTime();
            List<Concierto> resultado = agregador.buscar(CriteriosBusqueda.vacio());
            long duracionMs = Duration.ofNanos(System.nanoTime() - inicio).toMillis();

            assertThat(resultado).hasSize(1);
            assertThat(resultado.getFirst().getArtista()).isEqualTo("Siddhartha");
            assertThat(duracionMs).isLessThan(1500);
        } finally {
            lentoLiberado.countDown();
            agregador.cerrarEjecutor();
        }
    }

    @Test
    void consultaLosProveedoresEnParalelo() {
        CountDownLatch ambosArrancaron = new CountDownLatch(2);
        AtomicBoolean ejecutaronEnParalelo = new AtomicBoolean(false);

        ProveedorConciertos primero = proveedorSincronizado(
                "TICKETMASTER", "tm-1", "Artista Uno", ambosArrancaron, ejecutaronEnParalelo
        );
        ProveedorConciertos segundo = proveedorSincronizado(
                "SUPERBOLETOS", "sb-1", "Artista Dos", ambosArrancaron, ejecutaronEnParalelo
        );

        var agregador = new AgregadorConciertosService(
                List.of(primero, segundo),
                Duration.ofSeconds(2)
        );

        List<Concierto> resultado = agregador.buscar(CriteriosBusqueda.vacio());
        agregador.cerrarEjecutor();

        assertThat(ejecutaronEnParalelo.get()).isTrue();
        assertThat(resultado).hasSize(2);
    }

    @Test
    void omiteProveedoresDeshabilitadosYSinProveedoresActivosDevuelveVacio() {
        ProveedorConciertos deshabilitado = mock(ProveedorConciertos.class);
        when(deshabilitado.estaHabilitado()).thenReturn(false);

        var agregador = new AgregadorConciertosService(
                List.of(deshabilitado, ProveedorFalso.deshabilitado("APAGADO")),
                Duration.ofSeconds(1)
        );

        assertThat(agregador.buscar(CriteriosBusqueda.vacio())).isEmpty();
        verify(deshabilitado, never()).buscar(any());

        var sinProveedores = new AgregadorConciertosService(null, Duration.ofSeconds(1));
        assertThat(sinProveedores.buscar(CriteriosBusqueda.vacio())).isEmpty();
    }

    @Test
    void manejaEventosIncompletosNulosYDuplicadosExactosDeOpcion() {
        ConciertoExterno sinDatosClaveUno = new ConciertoExterno(
                " ", "id-1", null, null, null, null, null, null, null, null, null
        );
        ConciertoExterno sinDatosClaveDos = new ConciertoExterno(
                "TICKETMASTER", null, "Artista Sin Fecha", null, "CDMX",
                null, "https://tm.example/sin-fecha", null, null, null, null
        );
        ConciertoExterno duplicadoCaro = new ConciertoExterno(
                "VIAGOGO", "vg-1", "Porter", FECHA_CONCIERTO, "Monterrey",
                null, "https://viagogo.example/porter", null, null, null, new BigDecimal("1400.00")
        );
        ConciertoExterno duplicadoBaratoMismaUrl = new ConciertoExterno(
                "VIAGOGO", "vg-2", "Porter", FECHA_CONCIERTO, "Monterrey",
                "Arena Monterrey", "https://viagogo.example/porter", null, null, null, new BigDecimal("1200.00")
        );
        ConciertoExterno mismoSinPrecio = new ConciertoExterno(
                "TICKETMASTER", "tm-3", "Porter", FECHA_CONCIERTO, "Monterrey",
                null, null, null, null, null, null
        );

        ProveedorConciertos proveedorConNulos = new ProveedorConciertos() {
            @Override
            public List<ConciertoExterno> buscar(CriteriosBusqueda criterios) {
                return Arrays.asList(
                        null,
                        sinDatosClaveUno,
                        sinDatosClaveDos,
                        duplicadoCaro,
                        duplicadoBaratoMismaUrl,
                        mismoSinPrecio
                );
            }

            @Override
            public String nombre() {
                return "MIXTO";
            }
        };
        ProveedorConciertos proveedorRespuestaNula = new ProveedorConciertos() {
            @Override
            public List<ConciertoExterno> buscar(CriteriosBusqueda criterios) {
                return null;
            }

            @Override
            public String nombre() {
                return "RESPUESTA_NULA";
            }
        };

        var agregador = new AgregadorConciertosService(
                List.of(proveedorConNulos, proveedorRespuestaNula),
                Duration.ofSeconds(2),
                Runnable::run
        );

        List<Concierto> resultado = agregador.buscar(CriteriosBusqueda.vacio());

        assertThat(resultado).hasSize(3);
        Concierto porter = resultado.get(2);
        assertThat(porter.getLugar()).isEqualTo("Arena Monterrey");
        assertThat(porter.getPrecioMinimo()).isEqualByComparingTo("1200.00");
        assertThat(porter.getOpcionesCompra())
                .extracting(OpcionCompra::getFuente, OpcionCompra::getPrecioMinimo, OpcionCompra::isEsReventa)
                .containsExactly(
                        tuple("VIAGOGO", new BigDecimal("1200.00"), true),
                        tuple("TICKETMASTER", null, false)
                );
    }

    @Test
    void rechazaTimeoutInvalidoYExponeUtilidadesDeComparacion() {
        assertThatThrownBy(() -> new AgregadorConciertosService(List.of(), null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AgregadorConciertosService(List.of(), Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AgregadorConciertosService(List.of(), Duration.ofMillis(-50)))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(AgregadorConciertosService.normalizarTexto(null)).isEmpty();
        assertThat(AgregadorConciertosService.normalizarTexto("  Café   Tacvba  ")).isEqualTo("cafe tacvba");

        Concierto uno = new Concierto("Café Tacvba", FECHA_CONCIERTO, "Ciudad de México", "Lugar A");
        Concierto dos = new Concierto("CAFE TACVBA", FECHA_CONCIERTO, "ciudad de mexico", "Lugar B");
        Concierto sinFecha = new Concierto("Café Tacvba", null, "Ciudad de México", "Lugar C");

        assertThat(AgregadorConciertosService.mismoConcierto(uno, dos)).isTrue();
        assertThat(AgregadorConciertosService.mismoConcierto(uno, sinFecha)).isFalse();
        assertThat(AgregadorConciertosService.mismoConcierto(null, dos)).isFalse();
    }

    @Test
    void descargaCatalogosPropagandoCanceladosFuentesCompletasYFallos() {
        ConciertoExterno vigente = evento("TICKETMASTER", "tm-ok", "Caifanes", FECHA_CONCIERTO, "CDMX", "800.00");
        ConciertoExterno cancelado = evento("TICKETMASTER", "tm-cancel", "Zoé", FECHA_CONCIERTO, "CDMX", "900.00");
        ConciertoExterno deOtro = evento("SUPERBOLETOS", "sb-1", "Enjambre", FECHA_CONCIERTO, "GDL", "700.00");

        ProveedorConciertos completoConCancelado = new ProveedorConciertos() {
            @Override
            public List<ConciertoExterno> buscar(CriteriosBusqueda criterios) {
                return List.of(vigente, cancelado);
            }

            @Override
            public CatalogoProveedor descargarCatalogoCompleto() {
                return new CatalogoProveedor(List.of(vigente, cancelado), Set.of("tm-cancel"), true);
            }

            @Override
            public String nombre() {
                return "TICKETMASTER";
            }
        };
        ProveedorConciertos incompletoPorDefecto = new ProveedorFalso("SUPERBOLETOS", List.of(deOtro));
        ProveedorConciertos conFallo = new ProveedorConciertos() {
            @Override
            public List<ConciertoExterno> buscar(CriteriosBusqueda criterios) {
                throw new IllegalStateException("Error de red");
            }

            @Override
            public String nombre() {
                return "CAIDO";
            }
        };

        var agregador = new AgregadorConciertosService(
                List.of(completoConCancelado, incompletoPorDefecto, conFallo),
                Duration.ofSeconds(2),
                Runnable::run
        );

        var catalogo = agregador.descargarCatalogos();

        assertThat(catalogo.conciertos())
                .extracting(Concierto::getIdExterno, Concierto::isDisponible)
                .containsExactly(
                        tuple("tm-ok", true),
                        tuple("tm-cancel", false),
                        tuple("sb-1", true)
                );
        assertThat(catalogo.idsPorFuenteCompleta())
                .containsOnlyKeys("TICKETMASTER")
                .hasEntrySatisfying("TICKETMASTER", ids ->
                        assertThat(ids).containsExactlyInAnyOrder("tm-ok", "tm-cancel"));

        var sinActivos = new AgregadorConciertosService(List.of(), Duration.ofSeconds(1));
        assertThat(sinActivos.descargarCatalogos().conciertos()).isEmpty();
        assertThat(sinActivos.descargarCatalogos().idsPorFuenteCompleta()).isEmpty();
    }

    private static ConciertoExterno evento(
            String fuente, String id, String artista, LocalDate fecha, String ciudad, String precio
    ) {
        return new ConciertoExterno(
                fuente,
                id,
                artista,
                fecha,
                ciudad,
                "Recinto",
                "https://example.com/" + id,
                "https://example.com/" + id + ".jpg",
                new BigDecimal("19.4241"),
                new BigDecimal("-99.1949"),
                precio == null ? null : new BigDecimal(precio)
        );
    }

    private static ProveedorConciertos proveedorSincronizado(
            String nombre,
            String id,
            String artista,
            CountDownLatch ambosArrancaron,
            AtomicBoolean ejecutaronEnParalelo
    ) {
        return new ProveedorConciertos() {
            @Override
            public List<ConciertoExterno> buscar(CriteriosBusqueda criterios) {
                ambosArrancaron.countDown();
                try {
                    if (ambosArrancaron.await(1, TimeUnit.SECONDS)) {
                        ejecutaronEnParalelo.set(true);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                return List.of(evento(nombre, id, artista, FECHA_CONCIERTO, "CDMX", "700.00"));
            }

            @Override
            public String nombre() {
                return nombre;
            }
        };
    }
}
