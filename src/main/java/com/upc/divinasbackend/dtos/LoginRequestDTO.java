package com.upc.divinasbackend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequestDTO(
        @NotBlank(message = "El tipo de usuario es obligatorio.")
        @Pattern(regexp = "^(SISTEMA|ADMIN|CLIENTE)$", message = "El tipo debe ser SISTEMA, ADMIN o CLIENTE.")
        String tipo,


        Integer idTienda,

        @NotBlank(message = "El usuario es obligatorio.")
        String usuario,

        @NotBlank(message = "La contraseña es obligatoria.")
        String password
) {
}
