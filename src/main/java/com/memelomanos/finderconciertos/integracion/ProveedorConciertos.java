package com.memelomanos.finderconciertos.integracion;

import java.util.List;

/**
 * Contrato común que implementa cada integración con un proveedor externo de
 * conciertos (ver ADR-006). Permite que el agregador recorra cualquier fuente
 * sin acoplarse a una API específica.
 */
public interface ProveedorConciertos {

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
}
