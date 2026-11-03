package com.upc.divinasbackend.exceptions;

// Se responde con HTTP 409 (por ejemplo, un nombre o usuario que ya existe)
public class ConflictoException extends RuntimeException {
    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
