package com.upc.divinasbackend.finanzas;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PlanDePagos(
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
) {}