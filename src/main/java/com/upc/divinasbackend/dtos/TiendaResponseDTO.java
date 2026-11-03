package com.upc.divinasbackend.dtos;

public record TiendaResponseDTO(
        Integer idTienda,
        String nombreTienda,
        String direccion,
        String telefono,
        String usuarioAdmin,
        Boolean estado
) {
}
