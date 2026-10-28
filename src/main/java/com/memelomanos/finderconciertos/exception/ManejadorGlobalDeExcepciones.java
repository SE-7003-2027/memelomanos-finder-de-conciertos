package com.memelomanos.finderconciertos.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import jakarta.validation.ConstraintViolationException;
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

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorRespuesta> manejarViolacionDeRestriccion(
            ConstraintViolationException ex) {
        String detalle = ex.getConstraintViolations().stream()
                .map(violacion -> violacion.getMessage())
                .collect(Collectors.joining(", "));

        return construir(HttpStatus.BAD_REQUEST, detalle);
    }

    // Rango de fechas incorrecto
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorRespuesta> manejarArgumentoInvalido(
            IllegalArgumentException ex) {
        return construir(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Parámetro con formato o tipo incorrecto, como una fechas invalidas
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorRespuesta> manejarTipoParametroIncorrecto(
            MethodArgumentTypeMismatchException ex) {
        return construir(
                HttpStatus.BAD_REQUEST,
                "El parámetro '" + ex.getName()
                        + "' debe ser una fecha válida con formato YYYY-MM-DD");
    }

    private ResponseEntity<ErrorRespuesta> construir(HttpStatus status, String mensaje) {
        return ResponseEntity.status(status)
                .body(ErrorRespuesta.de(status.value(), status.getReasonPhrase(), mensaje));
    }
}