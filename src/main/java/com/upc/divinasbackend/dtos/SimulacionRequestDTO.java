package com.upc.divinasbackend.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SimulacionRequestDTO(
        LocalDate fechaCompra,

        @NotNull(message = "El monto es obligatorio.")
        @DecimalMin(value = "0", inclusive = false, message = "El monto debe ser mayor a 0.")
        @Digits(integer = 10, fraction = 2, message = "El monto admite hasta 2 decimales.")
        BigDecimal monto,

        @NotBlank(message = "La forma de pago es obligatoria.")
        @Pattern(regexp = "^(Unico pago|En meses)$", message = "La forma de pago debe ser 'Unico pago' o 'En meses'.")
        String formaPago,

        @Min(value = 1, message = "El número de meses debe ser al menos 1.")
        Integer numMeses
) {
}
