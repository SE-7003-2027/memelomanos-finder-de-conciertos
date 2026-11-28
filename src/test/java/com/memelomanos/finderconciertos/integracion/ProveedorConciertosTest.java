package com.memelomanos.finderconciertos.integracion;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProveedorConciertosTest {

    private final ConciertoExterno cafeTacvbaCdmx = new ConciertoExterno(
            "PROVEEDOR_FALSO",
            "evt-001",
            "Café Tacvba",
            LocalDate.of(2027, 3, 20),
            "Ciudad de México",
            "Auditorio Nacional",
            "https://example.com/boletos/evt-001",
            "https://example.com/img/evt-001.jpg",
            new BigDecimal("19.4241"),
            new BigDecimal("-99.1949"),
            new BigDecimal("850.00")
    );

    private final ConciertoExterno zoeGuadalajara = new ConciertoExterno(
            "PROVEEDOR_FALSO",
            "evt-002",
            "Zoé",
            LocalDate.of(2027, 4, 15),
            "Guadalajara",
            "Auditorio Telmex",
            "https://example.com/boletos/evt-002",
            "https://example.com/img/evt-002.jpg",
            new BigDecimal("20.7336"),
            new BigDecimal("-103.3802"),
            null
    );

    private final ConciertoExterno cafeTacvbaMonterrey = new ConciertoExterno(
            "PROVEEDOR_FALSO",
            "evt-003",
            "Café Tacvba",
            LocalDate.of(2027, 5, 10),
            "Monterrey",
            "Arena Monterrey",
            "https://example.com/boletos/evt-003",
            null,
            new BigDecimal("25.6809"),
            new BigDecimal("-100.2837"),
            new BigDecimal("950.00")
    );

    @Test
    void devuelveNombreYTodosLosConciertosSinFiltros() {
        ProveedorConciertos proveedor = new ProveedorFalso(
                List.of(cafeTacvbaCdmx, zoeGuadalajara)
        );

        assertThat(proveedor.nombre()).isEqualTo("PROVEEDOR_FALSO");
        assertThat(proveedor.buscar(CriteriosBusqueda.vacio()))
                .containsExactly(cafeTacvbaCdmx, zoeGuadalajara);
        assertThat(proveedor.buscar(null))
                .containsExactly(cafeTacvbaCdmx, zoeGuadalajara);
    }

    @Test
    void filtraPorArtistaYCiudadIgnorandoMayusculas() {
        ProveedorConciertos proveedor = new ProveedorFalso(
                List.of(cafeTacvbaCdmx, zoeGuadalajara, cafeTacvbaMonterrey)
        );

        var criterios = new CriteriosBusqueda("tacvba", "méxico", null, null);

        assertThat(proveedor.buscar(criterios)).containsExactly(cafeTacvbaCdmx);
    }

    @Test
    void filtraPorRangoDeFechasInclusivo() {
        ProveedorConciertos proveedor = new ProveedorFalso(
                List.of(cafeTacvbaCdmx, zoeGuadalajara, cafeTacvbaMonterrey)
        );

        var criterios = new CriteriosBusqueda(
                null,
                null,
                LocalDate.of(2027, 3, 20),
                LocalDate.of(2027, 4, 15)
        );

        assertThat(proveedor.buscar(criterios))
                .containsExactly(cafeTacvbaCdmx, zoeGuadalajara);
    }

    @Test
    void permiteRecorrerMultiplesProveedoresPorLaInterfazComun() {
        ConciertoExterno conciertoOtroProveedor = new ConciertoExterno(
                "OTRO_PROVEEDOR",
                "otro-1",
                "Café Tacvba",
                LocalDate.of(2027, 6, 1),
                "Ciudad de México",
                "Palacio de los Deportes",
                "https://example.com/otro/1",
                null,
                null,
                null,
                null
        );

        List<ProveedorConciertos> proveedores = List.of(
                new ProveedorFalso("PROVEEDOR_A", List.of(cafeTacvbaCdmx, zoeGuadalajara)),
                new ProveedorFalso("PROVEEDOR_B", List.of(conciertoOtroProveedor)),
                ProveedorFalso.deshabilitado("PROVEEDOR_DESHABILITADO")
        );

        var criterios = new CriteriosBusqueda("Café Tacvba", "Ciudad de México", null, null);

        List<ConciertoExterno> resultados = proveedores.stream()
                .flatMap(proveedor -> proveedor.buscar(criterios).stream())
                .toList();

        assertThat(proveedores).extracting(ProveedorConciertos::nombre)
                .containsExactly("PROVEEDOR_A", "PROVEEDOR_B", "PROVEEDOR_DESHABILITADO");
        assertThat(resultados).containsExactly(cafeTacvbaCdmx, conciertoOtroProveedor);
    }

    @Test
    void criteriosBusquedaRechazaFechaDesdePosteriorAFechaHasta() {
        LocalDate desde = LocalDate.of(2027, 5, 10);
        LocalDate hasta = LocalDate.of(2027, 5, 1);

        assertThatThrownBy(() -> new CriteriosBusqueda("Zoé", "CDMX", desde, hasta))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fechaDesde");
    }

    @Test
    void criteriosBusquedaPermiteMismaFechaDesdeYHasta() {
        LocalDate fecha = LocalDate.of(2027, 4, 15);
        var criterios = new CriteriosBusqueda(null, null, fecha, fecha);

        ProveedorConciertos proveedor = new ProveedorFalso(
                List.of(cafeTacvbaCdmx, zoeGuadalajara, cafeTacvbaMonterrey)
        );

        assertThat(proveedor.buscar(criterios)).containsExactly(zoeGuadalajara);
    }

    @Test
    void conciertoExternoPermitePrecioMinimoNuloYConservaCampos() {
        assertThat(zoeGuadalajara.fuente()).isEqualTo("PROVEEDOR_FALSO");
        assertThat(zoeGuadalajara.idExterno()).isEqualTo("evt-002");
        assertThat(zoeGuadalajara.artista()).isEqualTo("Zoé");
        assertThat(zoeGuadalajara.precioMinimo()).isNull();
    }
}
