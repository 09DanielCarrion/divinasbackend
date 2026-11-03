package com.upc.divinasbackend.exceptions;

import java.util.Map;

// Formato único de error para todo el backend
public record ErrorResponse(String codigo, String mensaje, Map<String, String> detalles) {
}
