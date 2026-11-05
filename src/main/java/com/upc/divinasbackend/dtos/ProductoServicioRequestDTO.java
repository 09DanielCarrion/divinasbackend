package com.upc.divinasbackend.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductoServicioRequestDTO(
        @Size(max = 255, message = "La imagen no puede pasar de 255 caracteres.")
        String imagen,

        @Size(max = 80, message = "El proveedor no puede pasar de 80 caracteres.")
        String proveedor,

        @Size(max = 50, message = "La marca no puede pasar de 50 caracteres.")
        String marca,

        @NotBlank(message = "La descripción es obligatoria.")
        @Size(max = 150, message = "La descripción no puede pasar de 150 caracteres.")
        String descripcion,

        @NotBlank(message = "La unidad de medida es obligatoria.")
        @Size(max = 20, message = "La unidad de medida no puede pasar de 20 caracteres.")
        String unidadMedida,

        @NotNull(message = "El precio al contado es obligatorio.")
        @DecimalMin(value = "0", inclusive = false, message = "El precio al contado debe ser mayor a 0.")
        @Digits(integer = 10, fraction = 2, message = "El precio admite hasta 2 decimales.")
        BigDecimal precioContado,

        @NotNull(message = "El precio de lista (crédito) es obligatorio.")
        @DecimalMin(value = "0", inclusive = false, message = "El precio de lista debe ser mayor a 0.")
        @Digits(integer = 10, fraction = 2, message = "El precio admite hasta 2 decimales.")
        BigDecimal precioCredito,

        @Pattern(regexp = "^(Al contado|En meses)$", message = "La forma de venta debe ser 'Al contado' o 'En meses'.")
        String formaVenta
) {
}
