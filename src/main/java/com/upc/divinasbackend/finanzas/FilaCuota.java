package com.upc.divinasbackend.finanzas;

import java.math.BigDecimal;
import java.time.LocalDate;


public record FilaCuota(
        int numero,
        LocalDate fechaVencimiento,
        BigDecimal saldoInicial,
        BigDecimal cuota,
        BigDecimal interes,
        BigDecimal amortizacion,
        BigDecimal saldoFinal
) {}
