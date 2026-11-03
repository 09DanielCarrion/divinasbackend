package com.upc.divinasbackend.exceptions;

// Se responde con HTTP 404
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
