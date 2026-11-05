package com.upc.divinasbackend.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ItemListadoPagoDTO(
        Integer idCompra,
        String descripcionProducto,
        LocalDate fechaCompra,
        BigDecimal montoCompra,
        String formaPago,
        int numeroCuota,
        int cuotasTotales,
        int diasGracia,
        BigDecimal interesGracia,
        BigDecimal interesCuota,
        BigDecimal montoCuota
) {
}
