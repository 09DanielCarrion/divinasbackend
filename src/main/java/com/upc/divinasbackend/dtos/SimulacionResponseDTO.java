package com.upc.divinasbackend.dtos;

import com.upc.divinasbackend.finanzas.FilaCuota;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record SimulacionResponseDTO(
        Integer idCliente,
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
        List<FilaCuota> cuotas,
        BigDecimal totalCuotas,
        BigDecimal totalIntereses,
        BigDecimal tir,
        BigDecimal tcea
) {
}
