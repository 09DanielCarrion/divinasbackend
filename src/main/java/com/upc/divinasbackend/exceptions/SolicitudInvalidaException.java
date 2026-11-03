package com.upc.divinasbackend.exceptions;

// Se responde con HTTP 400 (regla de negocio que no se cumple)
public class SolicitudInvalidaException extends RuntimeException {
    public SolicitudInvalidaException(String mensaje) {
        super(mensaje);
    }
}
