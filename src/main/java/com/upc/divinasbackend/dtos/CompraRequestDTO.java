package com.upc.divinasbackend.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record CompraRequestDTO(
        @NotNull(message = "El producto o servicio es obligatorio.")
        Integer idProductoServicio,

        LocalDate fechaCompra,

        @NotBlank(message = "La forma de pago es obligatoria.")
        @Pattern(regexp = "^(Unico pago|En meses)$", message = "La forma de pago debe ser 'Unico pago' o 'En meses'.")
        String formaPago,

        @Min(value = 1, message = "El número de meses debe ser al menos 1.")
        Integer numMeses
) {
}
