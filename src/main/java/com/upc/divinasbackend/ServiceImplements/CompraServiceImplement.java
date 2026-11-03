package com.upc.divinasbackend.ServiceImplements;

import com.upc.divinasbackend.entities.Compra;
import com.upc.divinasbackend.repositories.CompraRepository;
import com.upc.divinasbackend.repositories.CuotaRepository;
import com.upc.divinasbackend.serviceinterfaces.CompraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompraServiceImplement implements CompraService {
    @Autowired
    private CompraRepository cR;

    @Override
    public void insert(Compra nombre) {
        cR.save(nombre);

    }

    @Override
    public void delete(int id) {
        cR.deleteById(id);

    }

    @Override
    public Compra listId(int id) {
        return cR.findById(id).orElse(null);
    }

    @Override
    public void update(Compra nombre) {
        cR.save(nombre);
    }
}
