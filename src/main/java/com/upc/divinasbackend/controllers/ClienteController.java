package com.upc.divinasbackend.controllers;

import com.upc.divinasbackend.dtos.ClienteRequestDTO;
import com.upc.divinasbackend.dtos.ClienteResponseDTO;
import com.upc.divinasbackend.dtos.ClienteUpdateDTO;
import com.upc.divinasbackend.entities.Cliente;
import com.upc.divinasbackend.entities.Tienda;
import com.upc.divinasbackend.exceptions.ConflictoException;
import com.upc.divinasbackend.exceptions.RecursoNoEncontradoException;
import com.upc.divinasbackend.serviceinterfaces.ClienteService;
import com.upc.divinasbackend.serviceinterfaces.TiendaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/tiendas/{idTienda}/clientes")
public class ClienteController {

    @Autowired
    private ClienteService cS;

    @Autowired
    private TiendaService tS;

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> crear(@PathVariable("idTienda") int idTienda,
                                                    @Valid @RequestBody ClienteRequestDTO dto) {
        Cliente cliente = new Cliente();
        cliente.setTienda(buscarTienda(idTienda));
        cliente.setNombreCliente(dto.nombreCliente());
        cliente.setTipoTasa(dto.tipoTasa());
        cliente.setTasaInteres(dto.tasaInteres());
        cliente.setCapitalizacion(dto.capitalizacion());
        cliente.setMoneda(dto.moneda());
        cliente.setLimiteMaxCredito(dto.limiteMaxCredito());
        cliente.setMaxMesesCredito(dto.maxMesesCredito());
        cliente.setFechaCorte(dto.fechaCorte());
        cliente.setFechaPago(dto.fechaPago());
        cliente.setUsuarioCliente(dto.usuarioCliente());
        cliente.setPasswordCliente(dto.passwordCliente());
        cS.insert(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(aRespuesta(cliente));
    }

    @GetMapping
    public List<ClienteResponseDTO> listar(@PathVariable("idTienda") int idTienda) {
        buscarTienda(idTienda);
        return cS.listByTienda(idTienda).stream().map(this::aRespuesta).toList();
    }

    @GetMapping("/{idCliente}")
    public ClienteResponseDTO obtener(@PathVariable("idTienda") int idTienda,
                                      @PathVariable("idCliente") int idCliente) {
        return aRespuesta(buscarClienteDeTienda(idTienda, idCliente));
    }

    @PutMapping("/{idCliente}")
    public ClienteResponseDTO actualizar(@PathVariable("idTienda") int idTienda,
                                         @PathVariable("idCliente") int idCliente,
                                         @Valid @RequestBody ClienteUpdateDTO dto) {
        Cliente cliente = buscarClienteDeTienda(idTienda, idCliente);
        validarCondicionesFinancierasSinCambio(cliente, dto);
        cliente.setNombreCliente(dto.nombreCliente());
        if (dto.moneda() != null) {
            cliente.setMoneda(dto.moneda());
        }
        cliente.setLimiteMaxCredito(dto.limiteMaxCredito());
        cliente.setMaxMesesCredito(dto.maxMesesCredito());
        cliente.setUsuarioCliente(dto.usuarioCliente());
        cS.update(cliente);
        return aRespuesta(cliente);
    }

    @DeleteMapping("/{idCliente}")
    public ResponseEntity<Void> darDeBaja(@PathVariable("idTienda") int idTienda,
                                          @PathVariable("idCliente") int idCliente) {
        buscarClienteDeTienda(idTienda, idCliente);
        cS.deactivate(idCliente);
        return ResponseEntity.noContent().build();
    }

    private void validarCondicionesFinancierasSinCambio(Cliente cliente, ClienteUpdateDTO dto) {
        String capitalizacionNueva = "Efectiva".equals(dto.tipoTasa()) ? null : dto.capitalizacion();
        boolean cambia = !Objects.equals(cliente.getTipoTasa(), dto.tipoTasa())
                || cliente.getTasaInteres().compareTo(dto.tasaInteres()) != 0
                || !Objects.equals(cliente.getCapitalizacion(), capitalizacionNueva)
                || !Objects.equals(cliente.getFechaCorte(), dto.fechaCorte())
                || !Objects.equals(cliente.getFechaPago(), dto.fechaPago());
        if (cambia) {
            throw new ConflictoException(
                    "La tasa, la capitalización y las fechas de corte y pago no se pueden modificar.");
        }
    }

    private Tienda buscarTienda(int idTienda) {
        Tienda tienda = tS.listId(idTienda);
        if (tienda == null) {
            throw new RecursoNoEncontradoException("La tienda no existe.");
        }
        return tienda;
    }

    private Cliente buscarClienteDeTienda(int idTienda, int idCliente) {
        Cliente cliente = cS.listId(idCliente);
        if (cliente == null || !cliente.getTienda().getIdTienda().equals(idTienda)) {
            throw new RecursoNoEncontradoException("El cliente no existe en esta tienda.");
        }
        return cliente;
    }

    private ClienteResponseDTO aRespuesta(Cliente c) {
        return new ClienteResponseDTO(c.getIdCliente(), c.getTienda().getIdTienda(), c.getNombreCliente(),
                c.getTipoTasa(), c.getTasaInteres(), c.getCapitalizacion(), c.getMoneda(),
                c.getLimiteMaxCredito(), c.getMaxMesesCredito(), c.getFechaCorte(), c.getFechaPago(),
                c.getUsuarioCliente(), c.getEstado());
    }
}
