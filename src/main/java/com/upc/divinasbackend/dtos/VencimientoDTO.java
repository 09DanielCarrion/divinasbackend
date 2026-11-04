package com.upc.divinasbackend.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record VencimientoDTO(
        Integer idCompra,
        Integer idCliente,
        String nombreCliente,
        int numeroCuota,
        LocalDate fechaVencimiento,
        BigDecimal montoCuota
) {
}
