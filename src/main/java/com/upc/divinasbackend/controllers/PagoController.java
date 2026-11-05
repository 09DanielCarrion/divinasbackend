package com.upc.divinasbackend.controllers;

import com.upc.divinasbackend.dtos.PagoCompraDTO;
import com.upc.divinasbackend.dtos.PagosClienteDTO;
import com.upc.divinasbackend.entities.Cliente;
import com.upc.divinasbackend.entities.Compra;
import com.upc.divinasbackend.entities.Cuota;
import com.upc.divinasbackend.exceptions.RecursoNoEncontradoException;
import com.upc.divinasbackend.serviceinterfaces.ClienteService;
import com.upc.divinasbackend.serviceinterfaces.CuotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tiendas/{idTienda}/clientes/{idCliente}/pagos")
public class PagoController {

    @Autowired
    private CuotaService cuS;

    @Autowired
    private ClienteService cS;

    @GetMapping
    public PagosClienteDTO listar(@PathVariable("idTienda") int idTienda,
                                  @PathVariable("idCliente") int idCliente) {
        Cliente cliente = cS.listId(idCliente);
        if (cliente == null || !cliente.getTienda().getIdTienda().equals(idTienda)) {
            throw new RecursoNoEncontradoException("El cliente no existe en esta tienda.");
        }

        Map<Integer, List<Cuota>> porCompra = new LinkedHashMap<>();
        for (Cuota cuota : cuS.listByCliente(idCliente)) {
            porCompra.computeIfAbsent(cuota.getCompra().getIdCompra(), k -> new ArrayList<>()).add(cuota);
        }

        List<PagoCompraDTO> compras = new ArrayList<>();
        BigDecimal saldoTotal = BigDecimal.ZERO.setScale(2);
        int enCurso = 0;
        for (List<Cuota> cuotas : porCompra.values()) {
            PagoCompraDTO pago = aPago(cuotas);
            compras.add(pago);
            saldoTotal = saldoTotal.add(pago.saldoPendiente());
            if (pago.saldoPendiente().signum() > 0) {
                enCurso++;
            }
        }
        BigDecimal disponible = cliente.getLimiteMaxCredito().subtract(saldoTotal).max(BigDecimal.ZERO);
        return new PagosClienteDTO(idCliente, cliente.getNombreCliente(), cliente.getLimiteMaxCredito(), disponible,
                saldoTotal, enCurso, compras);
    }

    private PagoCompraDTO aPago(List<Cuota> cuotas) {
        Compra compra = cuotas.get(0).getCompra();
        List<Cuota> pendientes = cuotas.stream().filter(c -> "Pendiente".equals(c.getEstado())).toList();
        BigDecimal saldo = pendientes.stream()
                .map(Cuota::getMontoCuota)
                .reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
        Cuota siguiente = pendientes.isEmpty() ? null : pendientes.get(0);
        return new PagoCompraDTO(compra.getIdCompra(), compra.getProductoServicio().getDescripcion(),
                compra.getFechaCompra(), compra.getMonto(), compra.getFormaPago(),
                cuotas.size() - pendientes.size(), cuotas.size(),
                siguiente == null ? null : siguiente.getMontoCuota(),
                siguiente == null ? null : siguiente.getFechaVencimiento(), saldo);
    }
}
