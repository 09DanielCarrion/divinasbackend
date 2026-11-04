package com.upc.divinasbackend.controllers;

import com.upc.divinasbackend.dtos.ResumenTiendaDTO;
import com.upc.divinasbackend.dtos.VencimientoDTO;
import com.upc.divinasbackend.exceptions.RecursoNoEncontradoException;
import com.upc.divinasbackend.serviceinterfaces.ClienteService;
import com.upc.divinasbackend.serviceinterfaces.CuotaService;
import com.upc.divinasbackend.serviceinterfaces.ProductoServicioService;
import com.upc.divinasbackend.serviceinterfaces.TiendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tiendas/{idTienda}/resumen")
public class ResumenController {

    @Autowired
    private TiendaService tS;

    @Autowired
    private ClienteService cS;

    @Autowired
    private ProductoServicioService pS;

    @Autowired
    private CuotaService cuS;

    @GetMapping
    public ResumenTiendaDTO obtener(@PathVariable("idTienda") int idTienda) {
        if (tS.listId(idTienda) == null) {
            throw new RecursoNoEncontradoException("La tienda no existe.");
        }
        List<VencimientoDTO> vencimientos = cuS.proximosVencimientos(idTienda).stream()
                .map(c -> new VencimientoDTO(c.getCompra().getIdCompra(),
                        c.getCompra().getCliente().getIdCliente(),
                        c.getCompra().getCliente().getNombreCliente(),
                        c.getNumeroCuota(), c.getFechaVencimiento(), c.getMontoCuota()))
                .toList();
        return new ResumenTiendaDTO(cS.countActivos(idTienda), pS.countActivos(idTienda),
                cuS.comprasEnCurso(idTienda), vencimientos);
    }
}
