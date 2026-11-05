package com.upc.divinasbackend.dtos;

import java.time.LocalDateTime;

public record OperacionResponseDTO(
        Integer idOperacion,
        LocalDateTime fechaHora,
        String usuario,
        String rol,
        Integer idTienda,
        String accion,
        String metodo,
        String ruta,
        Integer codigoHttp
) {
}
