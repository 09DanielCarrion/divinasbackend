package com.upc.divinasbackend.ServiceImplements;

import com.upc.divinasbackend.entities.Cliente;
import com.upc.divinasbackend.exceptions.ConflictoException;
import com.upc.divinasbackend.exceptions.RecursoNoEncontradoException;
import com.upc.divinasbackend.exceptions.SolicitudInvalidaException;
import com.upc.divinasbackend.repositories.ClienteRepository;
import com.upc.divinasbackend.serviceinterfaces.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteServiceImplement implements ClienteService {
    @Autowired
    private ClienteRepository cR;

    @Autowired
    private PasswordEncoder pE;

    @Override
    public void insert(Cliente cliente) {
        if (!Boolean.TRUE.equals(cliente.getTienda().getEstado())) {
            throw new SolicitudInvalidaException("La tienda está dada de baja.");
        }
        validarTasa(cliente);
        if (cR.existsByTienda_IdTiendaAndUsuarioCliente(cliente.getTienda().getIdTienda(), cliente.getUsuarioCliente())) {
            throw new ConflictoException("Ese usuario ya está en uso en esta tienda.");
        }
        if (cliente.getMoneda() == null || cliente.getMoneda().isBlank()) {
            cliente.setMoneda("Soles");
        }
        cliente.setPasswordCliente(pE.encode(cliente.getPasswordCliente()));
        cliente.setEstado(true);
        cR.save(cliente);

    }

    @Override
    public void delete(int id) {
        cR.deleteById(id);

    }

    @Override
    public Cliente listId(int id) {
        return cR.findById(id).orElse(null);
    }

    @Override
    public void update(Cliente cliente) {
        validarTasa(cliente);
        if (cR.existsByTienda_IdTiendaAndUsuarioClienteAndIdClienteNot(
                cliente.getTienda().getIdTienda(), cliente.getUsuarioCliente(), cliente.getIdCliente())) {
            throw new ConflictoException("Ese usuario ya está en uso en esta tienda.");
        }
        cR.save(cliente);
    }

    @Override
    public List<Cliente> listByTienda(int idTienda) {
        return cR.findByTienda_IdTienda(idTienda);
    }

    // Baja lógica: el cliente queda con estado = false
    @Override
    public void deactivate(int id) {
        Cliente cliente = cR.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El cliente no existe."));
        cliente.setEstado(false);
        cR.save(cliente);
    }

    // Si la tasa es Nominal, la capitalización es obligatoria; si es Efectiva, no aplica
    private void validarTasa(Cliente cliente) {
        if ("Nominal".equals(cliente.getTipoTasa())) {
            if (cliente.getCapitalizacion() == null || cliente.getCapitalizacion().isBlank()) {
                throw new SolicitudInvalidaException("La capitalización es obligatoria cuando la tasa es Nominal.");
            }
        } else {
            cliente.setCapitalizacion(null);
        }
    }

    @Override
    public long countActivos(int idTienda) {
        return cR.countByTienda_IdTiendaAndEstadoTrue(idTienda);
    }
}
