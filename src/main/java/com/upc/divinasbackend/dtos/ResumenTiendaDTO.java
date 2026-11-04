package com.upc.divinasbackend.dtos;

import java.util.List;

public record ResumenTiendaDTO(
        long totalClientesActivos,
        long totalServiciosRegistrados,
        long comprasEnCurso,
        List<VencimientoDTO> proximosVencimientos
) {
}
