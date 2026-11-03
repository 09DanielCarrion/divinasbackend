package com.upc.divinasbackend.dtos;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;


public record ClienteRequestDTO(
        @NotBlank(message = "El nombre del cliente es obligatorio.")
        @Size(max = 100, message = "El nombre no puede pasar de 100 caracteres.")
        String nombreCliente,

        @NotBlank(message = "El tipo de tasa es obligatorio.")
        @Pattern(regexp = "^(Nominal|Efectiva)$", message = "El tipo de tasa debe ser Nominal o Efectiva.")
        String tipoTasa,

        @NotNull(message = "La tasa de interés es obligatoria.")
        @DecimalMin(value = "0", inclusive = false, message = "La tasa de interés debe ser mayor a 0.")
        @DecimalMax(value = "200", message = "La tasa de interés no puede ser mayor a 200.")
        @Digits(integer = 3, fraction = 4, message = "La tasa admite hasta 4 decimales.")
        BigDecimal tasaInteres,

        @Pattern(regexp = "^(Diaria|Mensual|Bimestral|Trimestral|Semestral|Anual)$",
                message = "La capitalización debe ser Diaria, Mensual, Bimestral, Trimestral, Semestral o Anual.")
        String capitalizacion,

        @Pattern(regexp = "^Soles$", message = "La moneda debe ser Soles.")
        String moneda,

        @NotNull(message = "El límite máximo de crédito es obligatorio.")
        @DecimalMin(value = "0", inclusive = false, message = "El límite de crédito debe ser mayor a 0.")
        @Digits(integer = 10, fraction = 2, message = "El límite admite hasta 2 decimales.")
        BigDecimal limiteMaxCredito,

        @NotNull(message = "El máximo de meses de crédito es obligatorio.")
        @Min(value = 1, message = "El máximo de meses debe ser al menos 1.")
        Integer maxMesesCredito,

        @NotNull(message = "La fecha de corte es obligatoria.")
        @Min(value = 1, message = "La fecha de corte debe estar entre 1 y 31.")
        @Max(value = 31, message = "La fecha de corte debe estar entre 1 y 31.")
        Integer fechaCorte,

        @NotNull(message = "La fecha de pago es obligatoria.")
        @Min(value = 1, message = "La fecha de pago debe estar entre 1 y 31.")
        @Max(value = 31, message = "La fecha de pago debe estar entre 1 y 31.")
        Integer fechaPago,

        @NotBlank(message = "El usuario del cliente es obligatorio.")
        @Size(max = 30, message = "El usuario no puede pasar de 30 caracteres.")
        String usuarioCliente,

        @NotBlank(message = "La contraseña es obligatoria.")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres.")
        String passwordCliente
) {
}
