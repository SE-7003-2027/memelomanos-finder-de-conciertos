package com.memelomanos.finderconciertos.exception;

// Se lanza cuando alguien intenta registrarse con un correo que ya existe.
// ManejadorGlobalDeExcepciones lo convierte en un 409 Conflict.
public class CorreoDuplicadoException extends RuntimeException {

    public CorreoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
