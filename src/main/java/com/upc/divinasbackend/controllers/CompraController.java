package com.upc.divinasbackend.controllers;

import com.upc.divinasbackend.dtos.CompraRequestDTO;
import com.upc.divinasbackend.dtos.CompraResponseDTO;
import com.upc.divinasbackend.dtos.CuotaDetalleDTO;
import com.upc.divinasbackend.dtos.SimulacionRequestDTO;
import com.upc.divinasbackend.dtos.SimulacionResponseDTO;
import com.upc.divinasbackend.entities.Cliente;
import com.upc.divinasbackend.entities.Compra;
import com.upc.divinasbackend.entities.Cuota;
import com.upc.divinasbackend.entities.ProductoServicio;
import com.upc.divinasbackend.exceptions.RecursoNoEncontradoException;
import com.upc.divinasbackend.finanzas.FilaCuota;
import com.upc.divinasbackend.finanzas.PlanDePagos;
import com.upc.divinasbackend.serviceinterfaces.ClienteService;
import com.upc.divinasbackend.serviceinterfaces.CompraService;
import com.upc.divinasbackend.serviceinterfaces.ProductoServicioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/tiendas/{idTienda}/clientes/{idCliente}/compras")
public class CompraController {

    @Autowired
    private CompraService coS;

    @Autowired
    private ClienteService cS;

    @Autowired
    private ProductoServicioService pS;

    // Calcula el plan de pagos sin guardar nada en la base
    @PostMapping("/simular")
    public SimulacionResponseDTO simular(@PathVariable("idTienda") int idTienda,
                                         @PathVariable("idCliente") int idCliente,
                                         @Valid @RequestBody SimulacionRequestDTO dto) {
        Cliente cliente = buscarClienteDeTienda(idTienda, idCliente);
        LocalDate fechaCompra = dto.fechaCompra() != null ? dto.fechaCompra() : LocalDate.now();
        PlanDePagos plan = coS.simular(cliente, fechaCompra, dto.monto(), dto.formaPago(), dto.numMeses());
        return aSimulacion(cliente, fechaCompra, dto, plan);
    }

    // Registra la compra y guarda sus cuotas
    @PostMapping
    public ResponseEntity<CompraResponseDTO> registrar(@PathVariable("idTienda") int idTienda,
                                                       @PathVariable("idCliente") int idCliente,
                                                       @Valid @RequestBody CompraRequestDTO dto) {
        Cliente cliente = buscarClienteDeTienda(idTienda, idCliente);
        ProductoServicio producto = pS.listId(dto.idProductoServicio());
        if (producto == null || !producto.getTienda().getIdTienda().equals(idTienda)) {
            throw new RecursoNoEncontradoException("El producto o servicio no existe en esta tienda.");
        }
        LocalDate fechaCompra = dto.fechaCompra() != null ? dto.fechaCompra() : LocalDate.now();
        Compra compra = coS.registrar(cliente, producto, fechaCompra, dto.formaPago(), dto.numMeses());
        return ResponseEntity.status(HttpStatus.CREATED).body(aRespuesta(compra, cliente));
    }

    @GetMapping
    public List<CompraResponseDTO> listar(@PathVariable("idTienda") int idTienda,
                                          @PathVariable("idCliente") int idCliente) {
        Cliente cliente = buscarClienteDeTienda(idTienda, idCliente);
        return coS.listByCliente(idCliente).stream().map(c -> aRespuesta(c, cliente)).toList();
    }

    @GetMapping("/{idCompra}")
    public CompraResponseDTO obtener(@PathVariable("idTienda") int idTienda,
                                     @PathVariable("idCliente") int idCliente,
                                     @PathVariable("idCompra") int idCompra) {
        Cliente cliente = buscarClienteDeTienda(idTienda, idCliente);
        Compra compra = coS.listId(idCompra);
        if (compra == null || !compra.getCliente().getIdCliente().equals(idCliente)) {
            throw new RecursoNoEncontradoException("La compra no existe para este cliente.");
        }
        return aRespuesta(compra, cliente);
    }

    private Cliente buscarClienteDeTienda(int idTienda, int idCliente) {
        Cliente cliente = cS.listId(idCliente);
        if (cliente == null || !cliente.getTienda().getIdTienda().equals(idTienda)) {
            throw new RecursoNoEncontradoException("El cliente no existe en esta tienda.");
        }
        return cliente;
    }

    private SimulacionResponseDTO aSimulacion(Cliente cliente, LocalDate fechaCompra,
                                              SimulacionRequestDTO dto, PlanDePagos p) {
        return new SimulacionResponseDTO(cliente.getIdCliente(), fechaCompra, dto.monto(), dto.formaPago(),
                dto.numMeses(), p.fechaFinGracia(), p.diasGracia(), tasa(p.tasaEfectivaAnual()),
                tasa(p.tasaEfectivaMensual()), p.capitalCapitalizado(), p.interesGracia(), p.cuotaFija(),
                p.cuotas(), p.totalCuotas(), p.totalIntereses(), tasa(p.tir()), tasa(p.tcea()));
    }

    // Une las cuotas guardadas con el desglose calculado (interés, amortización, saldo)
    private CompraResponseDTO aRespuesta(Compra compra, Cliente cliente) {
        PlanDePagos p = coS.planDe(compra, cliente);
        List<Cuota> guardadas = coS.cuotasDe(compra.getIdCompra());
        List<CuotaDetalleDTO> cuotas = new ArrayList<>();
        for (int i = 0; i < guardadas.size(); i++) {
            Cuota c = guardadas.get(i);
            FilaCuota f = p.cuotas().get(i);
            cuotas.add(new CuotaDetalleDTO(c.getIdCuota(), c.getNumeroCuota(), c.getFechaVencimiento(),
                    f.saldoInicial(), c.getMontoCuota(), f.interes(), f.amortizacion(), f.saldoFinal(),
                    c.getEstado(), c.getFechaPagoReal()));
        }
        ProductoServicio prod = compra.getProductoServicio();
        return new CompraResponseDTO(compra.getIdCompra(), cliente.getIdCliente(), prod.getIdProductoServicio(),
                prod.getDescripcion(), compra.getFechaCompra(), compra.getMonto(), compra.getFormaPago(),
                compra.getNumMeses(), p.fechaFinGracia(), p.diasGracia(), tasa(p.tasaEfectivaAnual()),
                tasa(p.tasaEfectivaMensual()), p.capitalCapitalizado(), p.interesGracia(), p.cuotaFija(),
                cuotas, p.totalCuotas(), p.totalIntereses(), tasa(p.tir()), tasa(p.tcea()));
    }

    // Las tasas se muestran con 10 decimales
    private BigDecimal tasa(BigDecimal t) {
        return t == null ? null : t.setScale(10, RoundingMode.HALF_UP);
    }
}
