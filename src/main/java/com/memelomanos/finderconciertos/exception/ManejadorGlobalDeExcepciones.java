package com.memelomanos.finderconciertos.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.stream.Collectors;

/**
 * Traduce las excepciones del dominio a los codigos HTTP que promete
 * el contrato. Sin esto, cualquier excepcion llega al cliente como un
 * 500 generico, aunque sea un error del cliente (correo repetido,
 * id que no existe, body incompleto).
 */
@RestControllerAdvice
public class ManejadorGlobalDeExcepciones {

    @ExceptionHandler({UsuarioNoEncontradoException.class, ConciertoNoEncontradoException.class})
    public ResponseEntity<ErrorRespuesta> manejarNoEncontrado(RuntimeException ex) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(CorreoDuplicadoException.class)
    public ResponseEntity<ErrorRespuesta> manejarCorreoDuplicado(CorreoDuplicadoException ex) {
        return construir(HttpStatus.CONFLICT, ex.getMessage());
    }

    // Se dispara cuando un body con @Valid no cumple las reglas
    // (por ejemplo, correo vacio o con formato invalido).
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> manejarValidacion(MethodArgumentNotValidException ex) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return construir(HttpStatus.BAD_REQUEST, detalle);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorRespuesta> manejarParametroFaltante(MissingServletRequestParameterException ex) {
        return construir(HttpStatus.BAD_REQUEST, "Falta el parametro '" + ex.getParameterName() + "'");
    }

    // JSON mal formado o tipos incorrectos (por ejemplo "usuarioId": "abc").
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> manejarBodyIlegible(HttpMessageNotReadableException ex) {
        return construir(HttpStatus.BAD_REQUEST, "El body de la peticion no es un JSON valido");
    }

    private ResponseEntity<ErrorRespuesta> construir(HttpStatus status, String mensaje) {
        return ResponseEntity.status(status)
                .body(ErrorRespuesta.de(status.value(), status.getReasonPhrase(), mensaje));
    }
}
