package com.memelomanos.finderconciertos.exception;

// Se lanza cuando se busca un usuario por correo y no existe.
// ManejadorGlobalDeExcepciones lo convierte en un 404 Not Found.
public class UsuarioNoEncontradoException extends RuntimeException {

    public UsuarioNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
