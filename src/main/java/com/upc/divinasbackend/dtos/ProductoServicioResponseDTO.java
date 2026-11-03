package com.upc.divinasbackend.dtos;

import java.math.BigDecimal;

public record ProductoServicioResponseDTO(
        Integer idProductoServicio,
        Integer idTienda,
        String imagen,
        String proveedor,
        String marca,
        String descripcion,
        String unidadMedida,
        BigDecimal precioContado,
        BigDecimal precioCredito,
        String formaVenta,
        Boolean estado
) {
}
