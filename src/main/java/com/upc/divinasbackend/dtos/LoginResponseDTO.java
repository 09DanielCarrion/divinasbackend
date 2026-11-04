package com.upc.divinasbackend.dtos;

public record LoginResponseDTO(
        String token,
        String tipoToken,
        String rol,
        Integer idTienda,
        Integer idCliente,
        String nombre,
        long expiraEnSegundos
) {
}
