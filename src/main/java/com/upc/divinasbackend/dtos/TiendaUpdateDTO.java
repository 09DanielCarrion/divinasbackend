package com.upc.divinasbackend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TiendaUpdateDTO(
        @NotBlank(message = "El nombre de la tienda es obligatorio.")
        @Size(max = 100, message = "El nombre no puede pasar de 100 caracteres.")
        String nombreTienda,

        @NotBlank(message = "La dirección es obligatoria.")
        @Size(max = 150, message = "La dirección no puede pasar de 150 caracteres.")
        String direccion,

        @NotBlank(message = "El teléfono es obligatorio.")
        @Pattern(regexp = "^[0-9]{9}$", message = "El teléfono debe tener exactamente 9 dígitos.")
        String telefono,

        @NotBlank(message = "El usuario administrador es obligatorio.")
        @Size(max = 30, message = "El usuario no puede pasar de 30 caracteres.")
        String usuarioAdmin
) {
}
