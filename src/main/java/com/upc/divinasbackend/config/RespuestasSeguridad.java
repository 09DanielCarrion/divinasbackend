package com.upc.divinasbackend.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.nio.charset.StandardCharsets;

final class RespuestasSeguridad {

    private RespuestasSeguridad() {
    }

    static AuthenticationEntryPoint noAutenticado() {
        return (request, response, ex) -> escribir(response, HttpStatus.UNAUTHORIZED, "NO_AUTENTICADO",
                "Debes iniciar sesión para acceder.");
    }

    static AccessDeniedHandler sinPermiso() {
        return (request, response, ex) -> escribir(response, HttpStatus.FORBIDDEN, "SIN_PERMISO",
                "No tienes permiso para realizar esta operación.");
    }

    private static void escribir(HttpServletResponse response, HttpStatus status, String codigo, String mensaje)
            throws java.io.IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"codigo\":\"" + codigo + "\",\"mensaje\":\"" + mensaje + "\",\"detalles\":{}}");
    }
}
