package com.memelomanos.finderconciertos.exception;

import java.time.LocalDateTime;

/**
 * Forma comun de todas las respuestas de error de la API (schema
 * "Error" en openapi.yml). Asi el cliente siempre sabe que esperar,
 * sin importar que endpoint fallo.
 */
public record ErrorRespuesta(int status, String error, String mensaje, LocalDateTime timestamp) {

    public static ErrorRespuesta de(int status, String error, String mensaje) {
        return new ErrorRespuesta(status, error, mensaje, LocalDateTime.now());
    }
}
