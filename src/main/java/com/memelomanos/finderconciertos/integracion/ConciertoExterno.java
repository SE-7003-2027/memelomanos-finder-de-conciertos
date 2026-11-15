package com.memelomanos.finderconciertos.integracion;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ConciertoExterno(
        String fuente,
        String idExterno,
        String artista,
        LocalDate fecha,
        String ciudad,
        String lugar,
        String urlCompra,
        String imagenUrl,
        BigDecimal latitud,
        BigDecimal longitud,
        BigDecimal precioMinimo
) {}
