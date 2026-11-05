package com.upc.divinasbackend.ServiceImplements;

import com.upc.divinasbackend.entities.Cliente;
import com.upc.divinasbackend.entities.Compra;
import com.upc.divinasbackend.entities.Cuota;
import com.upc.divinasbackend.entities.ProductoServicio;
import com.upc.divinasbackend.finanzas.FilaCuota;
import com.upc.divinasbackend.exceptions.SolicitudInvalidaException;
import com.upc.divinasbackend.finanzas.CalculadoraFinanciera;
import com.upc.divinasbackend.finanzas.PlanDePagos;
import com.upc.divinasbackend.repositories.CompraRepository;
import com.upc.divinasbackend.repositories.CuotaRepository;
import com.upc.divinasbackend.serviceinterfaces.CompraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CompraServiceImplement implements CompraService {
    @Autowired
    private CompraRepository cR;

    @Autowired
    private CuotaRepository cuR;

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
        BigDecimal deuda = cuR.saldoPendienteDelCliente(cliente.getIdCliente());
        BigDecimal disponible = cliente.getLimiteMaxCredito().subtract(deuda);
        if (monto.compareTo(disponible) > 0) {
            throw new SolicitudInvalidaException("El monto supera el crédito disponible del cliente: S/ "
                    + disponible.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP) + ".");
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
    
    @Override
    @Transactional
    public Compra registrar(Cliente cliente, ProductoServicio producto, LocalDate fechaCompra,
                            String formaPago, Integer numMeses) {
        if (!Boolean.TRUE.equals(producto.getEstado())) {
            throw new SolicitudInvalidaException("El producto o servicio está dado de baja.");
        }
        if ("Al contado".equals(producto.getFormaVenta()) && "En meses".equals(formaPago)) {
            throw new SolicitudInvalidaException("Este producto o servicio solo se vende en un solo pago.");
        }
        // El monto financiado es el precio de lista (crédito) del producto
        BigDecimal monto = producto.getPrecioCredito();
        PlanDePagos plan = simular(cliente, fechaCompra, monto, formaPago, numMeses);

        Compra compra = new Compra();
        compra.setCliente(cliente);
        compra.setProductoServicio(producto);
        compra.setFechaCompra(fechaCompra);
        compra.setMonto(monto);
        compra.setFormaPago(formaPago);
        compra.setNumMeses("Unico pago".equals(formaPago) ? 1 : numMeses);
        cR.save(compra);

        List<Cuota> cuotas = new ArrayList<>();
        for (FilaCuota fila : plan.cuotas()) {
            Cuota cuota = new Cuota();
            cuota.setCompra(compra);
            cuota.setNumeroCuota(fila.numero());
            cuota.setFechaVencimiento(fila.fechaVencimiento());
            cuota.setMontoCuota(fila.cuota());
            cuota.setEstado("Pendiente");
            cuotas.add(cuota);
        }
        cuR.saveAll(cuotas);
        return compra;
    }

    @Override
    public List<Compra> listByCliente(int idCliente) {
        return cR.findByCliente_IdCliente(idCliente);
    }

    @Override
    public List<Cuota> cuotasDe(int idCompra) {
        return cuR.findByCompra_IdCompraOrderByNumeroCuota(idCompra);
    }

    @Override
    public PlanDePagos planDe(Compra compra, Cliente cliente) {
        return CalculadoraFinanciera.generarPlan(compra.getFechaCompra(), compra.getMonto(), compra.getFormaPago(),
                compra.getNumMeses(), cliente.getTipoTasa(), cliente.getTasaInteres(), cliente.getCapitalizacion(),
                cliente.getFechaCorte(), cliente.getFechaPago());
    }
}
