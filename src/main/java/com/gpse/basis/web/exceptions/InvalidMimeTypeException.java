package com.gpse.basis.web.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

/**
 * This exception is thrown when a request body has an invalid MIME Type.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidMimeTypeException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 69L;
}
