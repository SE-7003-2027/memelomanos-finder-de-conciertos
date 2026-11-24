package com.memelomanos.finderconciertos.integracion;

import java.util.List;
import java.util.Set;

/**
 * Contrato común que implementa cada integración con un proveedor externo de
 * conciertos (ver ADR-006). Permite que el agregador recorra cualquier fuente
 * sin acoplarse a una API específica.
 */
public interface ProveedorConciertos {

    /**
     * Resultado de descargar el catálogo de un proveedor, incluyendo los IDs
     * marcados como cancelados y si la descarga se completó sin interrupciones.
     */
    record CatalogoProveedor(
            List<ConciertoExterno> eventos,
            Set<String> cancelados,
            boolean completo
    ) {
    }

    /**
     * Busca conciertos en el proveedor aplicando los criterios indicados.
     * Si el proveedor está deshabilitado, no tiene credenciales o falla al
     * responder, debe devolver una lista vacía (o los resultados parciales
     * obtenidos) sin interrumpir al resto del sistema.
     *
     * @param criterios filtros opcionales; {@code null} equivale a sin filtros
     * @return conciertos encontrados en el modelo común del proyecto
     */
    List<ConciertoExterno> buscar(CriteriosBusqueda criterios);

    /**
     * Nombre identificador del proveedor (por ejemplo, {@code "TICKETMASTER"}).
     *
     * @return identificador legible y único del proveedor
     */
    String nombre();

    /**
     * Indica si el proveedor está activo y listo para ser consultado.
     * Los proveedores deshabilitados o sin credenciales son omitidos por el agregador.
     *
     * @return {@code true} si el proveedor debe consultarse
     */
    default boolean estaHabilitado() {
        return true;
    }

    /**
     * Indica si las entradas ofrecidas por este proveedor corresponden a un
     * mercado de reventa (por ejemplo, Viagogo).
     *
     * @return {@code true} si el proveedor es de reventa
     */
    default boolean esReventa() {
        return false;
    }

    /**
     * Descarga el catálogo disponible del proveedor junto con metadatos de
     * cancelación y completitud para la sincronización periódica.
     *
     * @return catálogo del proveedor (incompleto por defecto si solo implementa {@link #buscar})
     */
    default CatalogoProveedor descargarCatalogoCompleto() {
        List<ConciertoExterno> eventos = buscar(CriteriosBusqueda.vacio());
        return new CatalogoProveedor(eventos == null ? List.of() : eventos, Set.of(), false);
    }
}
