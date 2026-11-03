package com.upc.divinasbackend.ServiceImplements;

import com.upc.divinasbackend.entities.Tienda;
import com.upc.divinasbackend.exceptions.ConflictoException;
import com.upc.divinasbackend.exceptions.RecursoNoEncontradoException;
import com.upc.divinasbackend.repositories.TiendaRepository;
import com.upc.divinasbackend.serviceinterfaces.TiendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

public class TiendaServiceImplement implements TiendaService {

    @Autowired
    private TiendaRepository tR;

    @Autowired
    private PasswordEncoder pE;

    @Override
    public void insert(Tienda tienda) {
        if (tR.existsByNombreTienda(tienda.getNombreTienda())) {
            throw new ConflictoException("Ya existe una tienda con ese nombre.");
        }
        if (tR.findByUsuarioAdmin(tienda.getUsuarioAdmin()).isPresent()) {
            throw new ConflictoException("Ese usuario administrador ya está en uso.");
        }
        tienda.setPasswordAdmin(pE.encode(tienda.getPasswordAdmin()));
        tienda.setEstado(true);
        tR.save(tienda);
    }

    @Override
    public void delete(int id) {
        tR.deleteById(id);

    }

    @Override
    public Tienda listId(int id) {
        return tR.findById(id).orElse(null);
    }

    @Override
    public void update(Tienda tienda) {
        if (tR.existsByNombreTiendaAndIdTiendaNot(tienda.getNombreTienda(), tienda.getIdTienda())) {
            throw new ConflictoException("Ya existe otra tienda con ese nombre.");
        }
        if (tR.existsByUsuarioAdminAndIdTiendaNot(tienda.getUsuarioAdmin(), tienda.getIdTienda())) {
            throw new ConflictoException("Ese usuario administrador ya está en uso.");
        }
        tR.save(tienda);
    }

    @Override
    public List<Tienda> list() {
        return tR.findAll();
    }

    // Baja lógica: la tienda queda con estado = false, no se borra el registro
    @Override
    public void deactivate(int id) {
        Tienda tienda = tR.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("La tienda no existe."));
        tienda.setEstado(false);
        tR.save(tienda);
    }

}
