package com.upc.divinasbackend.ServiceImplements;

import com.upc.divinasbackend.entities.Cliente;
import com.upc.divinasbackend.entities.Compra;
import com.upc.divinasbackend.exceptions.SolicitudInvalidaException;
import com.upc.divinasbackend.finanzas.CalculadoraFinanciera;
import com.upc.divinasbackend.finanzas.PlanDePagos;
import com.upc.divinasbackend.repositories.CompraRepository;
import com.upc.divinasbackend.repositories.CuotaRepository;
import com.upc.divinasbackend.serviceinterfaces.CompraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class CompraServiceImplement implements CompraService {
    @Autowired
    private CompraRepository cR;

    @Override
    public void insert(Compra nombre) {
        cR.save(nombre);

    }

    @Override
    public void delete(int id) {
        cR.deleteById(id);

    }

    @Override
    public Compra listId(int id) {
        return cR.findById(id).orElse(null);
    }

    @Override
    public void update(Compra nombre) {
        cR.save(nombre);
    }

    @Override
    public PlanDePagos simular(Cliente cliente, LocalDate fechaCompra, BigDecimal monto, String formaPago, Integer numMeses) {
        if (!Boolean.TRUE.equals(cliente.getEstado())) {
            throw new SolicitudInvalidaException("El cliente está dado de baja.");
        }
        if (monto.compareTo(cliente.getLimiteMaxCredito()) > 0) {
            throw new SolicitudInvalidaException("El monto supera el límite de crédito del cliente.");
        }
        if ("En meses".equals(formaPago)) {
            if (numMeses == null) {
                throw new SolicitudInvalidaException("El número de meses es obligatorio cuando el pago es en meses.");
            }
            if (numMeses > cliente.getMaxMesesCredito()) {
                throw new SolicitudInvalidaException("El número de meses supera el máximo permitido para el cliente.");
            }
        }
        return CalculadoraFinanciera.generarPlan(fechaCompra, monto, formaPago, numMeses,
                cliente.getTipoTasa(), cliente.getTasaInteres(), cliente.getCapitalizacion(),
                cliente.getFechaCorte(), cliente.getFechaPago());
    }
}
