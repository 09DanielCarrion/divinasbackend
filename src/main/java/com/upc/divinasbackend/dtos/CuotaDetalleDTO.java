package com.upc.divinasbackend.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CuotaDetalleDTO(
        Integer idCuota,
        int numero,
        LocalDate fechaVencimiento,
        BigDecimal saldoInicial,
        BigDecimal cuota,
        BigDecimal interes,
        BigDecimal amortizacion,
        BigDecimal saldoFinal,
        String estado,
        LocalDate fechaPagoReal
) {
}
