package com.memelomanos.finderconciertos.exception;

// Se lanza cuando se busca un concierto por id y no existe.
// ManejadorGlobalDeExcepciones lo convierte en un 404 Not Found.
public class ConciertoNoEncontradoException extends RuntimeException {

    public ConciertoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
