package com.upc.divinasbackend.serviceinterfaces;

import com.upc.divinasbackend.entities.Cuota;

import java.time.LocalDate;
import java.util.List;

public interface CuotaService {
    public void insert(Cuota cuota);
    public void delete(int id);
    public Cuota listId(int id);
    public void update(Cuota cuota);

    // Marca la cuota como pagada; debe respetarse el orden de las cuotas
    public Cuota pagar(int idCompra, int numeroCuota, LocalDate fechaPago);

    // Cuotas de todas las compras de un cliente, ordenadas por compra y número
    public List<Cuota> listByCliente(int idCliente);

    // Las próximas cuotas pendientes de la tienda por fecha de vencimiento
    public List<Cuota> proximosVencimientos(int idTienda);

    // Cantidad de compras de la tienda con cuotas pendientes
    public long comprasEnCurso(int idTienda);
}
