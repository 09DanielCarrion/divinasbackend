package com.upc.divinasbackend.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PagoCompraDTO(
        Integer idCompra,
        String descripcionProducto,
        LocalDate fechaCompra,
        BigDecimal monto,
        String formaPago,
        int cuotasPagadas,
        int cuotasTotales,
        BigDecimal cuotaAPagar,
        LocalDate fechaProximoPago,
        BigDecimal saldoPendiente
) {
}
