package com.upc.divinasbackend.serviceinterfaces;

import com.upc.divinasbackend.dtos.ListadoPagoDTO;
import com.upc.divinasbackend.entities.Cliente;

import java.time.LocalDate;
import java.util.List;

public interface ListadoPagoService {
    public ListadoPagoDTO generar(Cliente cliente, LocalDate fechaPago);
    public List<ListadoPagoDTO> generarTienda(int idTienda, LocalDate fechaPago);
}
