package com.upc.divinasbackend.controllers;

import com.upc.divinasbackend.dtos.OperacionResponseDTO;
import com.upc.divinasbackend.entities.Operacion;
import com.upc.divinasbackend.exceptions.RecursoNoEncontradoException;
import com.upc.divinasbackend.serviceinterfaces.OperacionService;
import com.upc.divinasbackend.serviceinterfaces.TiendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OperacionController {

    private static final int LIMITE_MAXIMO = 500;

    @Autowired
    private OperacionService oS;

    @Autowired
    private TiendaService tS;

    @GetMapping("/api/tiendas/{idTienda}/operaciones")
    public List<OperacionResponseDTO> deLaTienda(@PathVariable("idTienda") int idTienda,
                                                 @RequestParam(name = "limite", defaultValue = "100") int limite) {
        if (tS.listId(idTienda) == null) {
            throw new RecursoNoEncontradoException("La tienda no existe.");
        }
        return oS.listar(idTienda, acotar(limite)).stream().map(this::aRespuesta).toList();
    }

    @GetMapping("/api/operaciones")
    public List<OperacionResponseDTO> todas(@RequestParam(name = "idTienda", required = false) Integer idTienda,
                                            @RequestParam(name = "limite", defaultValue = "100") int limite) {
        return oS.listar(idTienda, acotar(limite)).stream().map(this::aRespuesta).toList();
    }

    private int acotar(int limite) {
        return Math.max(1, Math.min(limite, LIMITE_MAXIMO));
    }

    private OperacionResponseDTO aRespuesta(Operacion o) {
        return new OperacionResponseDTO(o.getIdOperacion(), o.getFechaHora(), o.getUsuario(), o.getRol(),
                o.getIdTienda(), o.getAccion(), o.getMetodo(), o.getRuta(), o.getCodigoHttp());
    }
}
