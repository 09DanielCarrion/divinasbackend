package com.upc.divinasbackend.serviceinterfaces;

import com.upc.divinasbackend.entities.Compra;
import com.upc.divinasbackend.entities.Cliente;
import com.upc.divinasbackend.finanzas.PlanDePagos;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface CompraService {
    public void insert(Compra compra);
    public void delete(int id);
    public Compra listId(int id);
    public void update(Compra compra);

    // Calcula el plan de pagos de una compra sin guardar nada
    public PlanDePagos simular(Cliente cliente, LocalDate fechaCompra, BigDecimal monto, String formaPago, Integer numMeses);
}
