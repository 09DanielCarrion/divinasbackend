package com.upc.divinasbackend.serviceinterfaces;

import com.upc.divinasbackend.entities.Cliente;
import com.upc.divinasbackend.entities.Compra;
import com.upc.divinasbackend.entities.Cuota;
import com.upc.divinasbackend.entities.ProductoServicio;
import com.upc.divinasbackend.finanzas.PlanDePagos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface CompraService {
    public void insert(Compra compra);
    public void delete(int id);
    public Compra listId(int id);
    public void update(Compra compra);

    // Calcula el plan de pagos de una compra sin guardar nada
    public PlanDePagos simular(Cliente cliente, LocalDate fechaCompra, BigDecimal monto, String formaPago, Integer numMeses);

    // Registra la compra (monto = precio de lista (crédito) del producto) y guarda sus cuotas
    public Compra registrar(Cliente cliente, ProductoServicio producto, LocalDate fechaCompra, String formaPago, Integer numMeses);

    public List<Compra> listByCliente(int idCliente);

    public List<Cuota> cuotasDe(int idCompra);

    // Recalcula el plan de una compra ya registrada (la tasa del cliente no se puede modificar)
    public PlanDePagos planDe(Compra compra, Cliente cliente);
}
