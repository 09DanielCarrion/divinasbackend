package com.upc.divinasbackend.dtos;

import java.math.BigDecimal;

public record ClienteResponseDTO(
        Integer idCliente,
        Integer idTienda,
        String nombreCliente,
        String tipoTasa,
        BigDecimal tasaInteres,
        String capitalizacion,
        String moneda,
        BigDecimal limiteMaxCredito,
        Integer maxMesesCredito,
        Integer fechaCorte,
        Integer fechaPago,
        String usuarioCliente,
        Boolean estado
) {
}
