package com.upc.divinasbackend.controllers;

import com.upc.divinasbackend.dtos.ListadoPagoDTO;
import com.upc.divinasbackend.entities.Cliente;
import com.upc.divinasbackend.exceptions.RecursoNoEncontradoException;
import com.upc.divinasbackend.serviceinterfaces.ClienteService;
import com.upc.divinasbackend.serviceinterfaces.ListadoPagoService;
import com.upc.divinasbackend.serviceinterfaces.TiendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tiendas/{idTienda}")
public class ListadoPagoController {

    @Autowired
    private ListadoPagoService lS;

    @Autowired
    private ClienteService cS;

    @Autowired
    private TiendaService tS;

    @GetMapping("/clientes/{idCliente}/listado-pago")
    public ListadoPagoDTO delCliente(@PathVariable("idTienda") int idTienda,
                                     @PathVariable("idCliente") int idCliente,
                                     @RequestParam(name = "fechaPago", required = false)
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaPago) {
        Cliente cliente = cS.listId(idCliente);
        if (cliente == null || !cliente.getTienda().getIdTienda().equals(idTienda)) {
            throw new RecursoNoEncontradoException("El cliente no existe en esta tienda.");
        }
        return lS.generar(cliente, fechaPago);
    }

    @GetMapping("/listado-pago")
    public List<ListadoPagoDTO> deLaTienda(@PathVariable("idTienda") int idTienda,
                                           @RequestParam(name = "fechaPago", required = false)
                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaPago) {
        if (tS.listId(idTienda) == null) {
            throw new RecursoNoEncontradoException("La tienda no existe.");
        }
        return lS.generarTienda(idTienda, fechaPago);
    }
}
