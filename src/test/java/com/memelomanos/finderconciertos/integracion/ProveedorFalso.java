package com.memelomanos.finderconciertos.integracion;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Implementación en memoria de {@link ProveedorConciertos} solo para pruebas.
 * Permite probar filtros y el recorrido polimórfico de proveedores sin depender
 * de APIs externas ni credenciales.
 */
public class ProveedorFalso implements ProveedorConciertos {

    private static final String NOMBRE_POR_DEFECTO = "PROVEEDOR_FALSO";

    private final String nombre;
    private final List<ConciertoExterno> conciertos;
    private final boolean habilitado;

    public ProveedorFalso(List<ConciertoExterno> conciertos) {
        this(NOMBRE_POR_DEFECTO, conciertos, true);
    }

    public ProveedorFalso(String nombre, List<ConciertoExterno> conciertos) {
        this(nombre, conciertos, true);
    }

    public ProveedorFalso(String nombre, List<ConciertoExterno> conciertos, boolean habilitado) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del proveedor no puede estar vacío");
        }
        this.nombre = nombre.strip();
        this.conciertos = conciertos == null ? List.of() : List.copyOf(conciertos);
        this.habilitado = habilitado;
    }

    public static ProveedorFalso deshabilitado(String nombre) {
        return new ProveedorFalso(nombre, List.of(), false);
    }

    @Override
    public List<ConciertoExterno> buscar(CriteriosBusqueda criterios) {
        if (!habilitado) {
            return List.of();
        }

        CriteriosBusqueda filtros = criterios == null ? CriteriosBusqueda.vacio() : criterios;
        return conciertos.stream()
                .filter(Objects::nonNull)
                .filter(concierto -> coincideTexto(concierto.artista(), filtros.artista()))
                .filter(concierto -> coincideTexto(concierto.ciudad(), filtros.ciudad()))
                .filter(concierto -> cumpleDesde(concierto.fecha(), filtros.fechaDesde()))
                .filter(concierto -> cumpleHasta(concierto.fecha(), filtros.fechaHasta()))
                .toList();
    }

    @Override
    public String nombre() {
        return nombre;
    }

    private static boolean coincideTexto(String valor, String filtro) {
        if (filtro == null || filtro.isBlank()) {
            return true;
        }
        if (valor == null) {
            return false;
        }
        return valor.toLowerCase(Locale.ROOT).contains(filtro.strip().toLowerCase(Locale.ROOT));
    }

    private static boolean cumpleDesde(LocalDate fecha, LocalDate fechaDesde) {
        if (fechaDesde == null) {
            return true;
        }
        return fecha != null && !fecha.isBefore(fechaDesde);
    }

    private static boolean cumpleHasta(LocalDate fecha, LocalDate fechaHasta) {
        if (fechaHasta == null) {
            return true;
        }
        return fecha != null && !fecha.isAfter(fechaHasta);
    }
}
