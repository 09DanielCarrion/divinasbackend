package com.upc.divinasbackend.controllers;

import com.upc.divinasbackend.dtos.TiendaRequestDTO;
import com.upc.divinasbackend.dtos.TiendaResponseDTO;
import com.upc.divinasbackend.dtos.TiendaUpdateDTO;
import com.upc.divinasbackend.entities.Tienda;
import com.upc.divinasbackend.exceptions.RecursoNoEncontradoException;
import com.upc.divinasbackend.serviceinterfaces.TiendaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tiendas")
public class TiendaController {

    @Autowired
    private TiendaService tS;

    @PostMapping
    public ResponseEntity<TiendaResponseDTO> crear(@Valid @RequestBody TiendaRequestDTO dto) {
        Tienda tienda = new Tienda();
        tienda.setNombreTienda(dto.nombreTienda());
        tienda.setDireccion(dto.direccion());
        tienda.setTelefono(dto.telefono());
        tienda.setUsuarioAdmin(dto.usuarioAdmin());
        tienda.setPasswordAdmin(dto.passwordAdmin());
        tS.insert(tienda);
        return ResponseEntity.status(HttpStatus.CREATED).body(aRespuesta(tienda));
    }

    @GetMapping
    public List<TiendaResponseDTO> listar() {
        return tS.list().stream().map(this::aRespuesta).toList();
    }

    @GetMapping("/{id}")
    public TiendaResponseDTO obtener(@PathVariable("id") int id) {
        return aRespuesta(buscar(id));
    }

    @PutMapping("/{id}")
    public TiendaResponseDTO actualizar(@PathVariable("id") int id,
                                        @Valid @RequestBody TiendaUpdateDTO dto) {
        Tienda tienda = buscar(id);
        tienda.setNombreTienda(dto.nombreTienda());
        tienda.setDireccion(dto.direccion());
        tienda.setTelefono(dto.telefono());
        tienda.setUsuarioAdmin(dto.usuarioAdmin());
        tS.update(tienda);
        return aRespuesta(tienda);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable("id") int id) {
        tS.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    private Tienda buscar(int id) {
        Tienda tienda = tS.listId(id);
        if (tienda == null) {
            throw new RecursoNoEncontradoException("La tienda no existe.");
        }
        return tienda;
    }

    private TiendaResponseDTO aRespuesta(Tienda t) {
        return new TiendaResponseDTO(t.getIdTienda(), t.getNombreTienda(), t.getDireccion(),
                t.getTelefono(), t.getUsuarioAdmin(), t.getEstado());
    }
}
