package com.memelomanos.finderconciertos.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException(String message) {
        super(message);
    }
}
