package com.memelomanos.finderconciertos.integracion;

import java.time.LocalDate;

/**
 * Filtros opcionales para consultar conciertos en un proveedor externo.
 * Cualquier campo puede ser {@code null} cuando no se aplica ese criterio.
 */
public record CriteriosBusqueda(
        String artista,
        String ciudad,
        LocalDate fechaDesde,
        LocalDate fechaHasta
) {

    public CriteriosBusqueda {
        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            throw new IllegalArgumentException(
                    "fechaDesde no puede ser posterior a fechaHasta"
            );
        }
    }

    public static CriteriosBusqueda vacio() {
        return new CriteriosBusqueda(null, null, null, null);
    }
}
