package com.memelomanos.finderconciertos.exception;

// Se lanza cuando se busca un usuario por correo y no existe.
// Mas adelante, un @ControllerAdvice puede mapear esto a un 404 Not Found.
public class UsuarioNoEncontradoException extends RuntimeException {

    public UsuarioNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
