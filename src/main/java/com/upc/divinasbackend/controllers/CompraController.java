package com.upc.divinasbackend.controllers;

import com.upc.divinasbackend.dtos.SimulacionRequestDTO;
import com.upc.divinasbackend.dtos.SimulacionResponseDTO;
import com.upc.divinasbackend.entities.Cliente;
import com.upc.divinasbackend.exceptions.RecursoNoEncontradoException;
import com.upc.divinasbackend.finanzas.PlanDePagos;
import com.upc.divinasbackend.serviceinterfaces.ClienteService;
import com.upc.divinasbackend.serviceinterfaces.CompraService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/tiendas/{idTienda}/clientes/{idCliente}/compras")
public class CompraController {

    @Autowired
    private CompraService coS;

    @Autowired
    private ClienteService cS;

    // Calcula el plan de pagos pero no guarda nada en la base de datos, es solo para pruebas y ya
    @PostMapping("/simular")
    public SimulacionResponseDTO simular(@PathVariable("idTienda") int idTienda,
                                         @PathVariable("idCliente") int idCliente,
                                         @Valid @RequestBody SimulacionRequestDTO dto) {
        Cliente cliente = buscarClienteDeTienda(idTienda, idCliente);
        LocalDate fechaCompra = dto.fechaCompra() != null ? dto.fechaCompra() : LocalDate.now();
        PlanDePagos plan = coS.simular(cliente, fechaCompra, dto.monto(), dto.formaPago(), dto.numMeses());
        return aRespuesta(cliente, fechaCompra, dto, plan);
    }

    private Cliente buscarClienteDeTienda(int idTienda, int idCliente) {
        Cliente cliente = cS.listId(idCliente);
        if (cliente == null || !cliente.getTienda().getIdTienda().equals(idTienda)) {
            throw new RecursoNoEncontradoException("El cliente no existe en esta tienda.");
        }
        return cliente;
    }

    private SimulacionResponseDTO aRespuesta(Cliente cliente, LocalDate fechaCompra,
                                             SimulacionRequestDTO dto, PlanDePagos p) {
        return new SimulacionResponseDTO(cliente.getIdCliente(), fechaCompra, dto.monto(), dto.formaPago(),
                dto.numMeses(), p.fechaFinGracia(), p.diasGracia(), tasa(p.tasaEfectivaAnual()),
                tasa(p.tasaEfectivaMensual()), p.capitalCapitalizado(), p.interesGracia(), p.cuotaFija(),
                p.cuotas(), p.totalCuotas(), p.totalIntereses(), tasa(p.tir()), tasa(p.tcea()));
    }

    private BigDecimal tasa(BigDecimal t) {
        return t == null ? null : t.setScale(10, RoundingMode.HALF_UP);
    }
}
