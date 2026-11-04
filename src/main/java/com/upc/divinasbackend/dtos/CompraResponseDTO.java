package com.upc.divinasbackend.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CompraResponseDTO(
        Integer idCompra,
        Integer idCliente,
        Integer idProductoServicio,
        String descripcionProducto,
        LocalDate fechaCompra,
        BigDecimal monto,
        String formaPago,
        Integer numMeses,
        LocalDate fechaFinGracia,
        int diasGracia,
        BigDecimal tasaEfectivaAnual,
        BigDecimal tasaEfectivaMensual,
        BigDecimal capitalCapitalizado,
        BigDecimal interesGracia,
        BigDecimal cuotaFija,
        List<CuotaDetalleDTO> cuotas,
        BigDecimal totalCuotas,
        BigDecimal totalIntereses,
        BigDecimal tir,
        BigDecimal tcea
) {
}
