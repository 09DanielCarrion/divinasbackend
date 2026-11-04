package com.upc.divinasbackend.dtos;

import java.math.BigDecimal;
import java.util.List;

public record PagosClienteDTO(
        Integer idCliente,
        String nombreCliente,
        BigDecimal saldoPendienteTotal,
        int comprasEnCurso,
        List<PagoCompraDTO> compras
) {
}
