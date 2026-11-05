package com.upc.divinasbackend.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ListadoPagoDTO(
        Integer idCliente,
        String nombreCliente,
        Integer fechaCorte,
        LocalDate fechaPago,
        BigDecimal totalInteresesAcumulados,
        BigDecimal totalAPagar,
        List<ItemListadoPagoDTO> detalle
) {
}
