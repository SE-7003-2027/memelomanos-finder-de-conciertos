package com.memelomanos.finderconciertos.exception;

// Se lanza cuando se busca un concierto por id y no existe.
// Mas adelante, un @ControllerAdvice puede mapear esto a un 404 Not Found.
public class ConciertoNoEncontradoException extends RuntimeException {

    public ConciertoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
