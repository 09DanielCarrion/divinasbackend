package com.upc.divinasbackend.ServiceImplements;

import com.upc.divinasbackend.entities.Cuota;
import com.upc.divinasbackend.repositories.CuotaRepository;
import com.upc.divinasbackend.repositories.TiendaRepository;
import com.upc.divinasbackend.serviceinterfaces.CuotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CuotaServiceImplement implements CuotaService {
    @Autowired
    private CuotaRepository cR;

    @Override
    public void insert(Cuota nombre) {
        cR.save(nombre);

    }

    @Override
    public void delete(int id) {
        cR.deleteById(id);

    }

    @Override
    public Cuota listId(int id) {
        return cR.findById(id).orElse(null);
    }

    @Override
    public void update(Cuota nombre) {
        cR.save(nombre);
    }
}
