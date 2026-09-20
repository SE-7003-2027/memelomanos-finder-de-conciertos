package com.memelomanos.finderconciertos.exception;

// Se lanza cuando alguien intenta registrarse con un correo que ya existe.
// Mas adelante, un @ControllerAdvice puede mapear esto a un 400 Bad Request.
public class CorreoDuplicadoException extends RuntimeException {

    public CorreoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
