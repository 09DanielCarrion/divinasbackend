package com.upc.divinasbackend.serviceinterfaces;

import com.upc.divinasbackend.entities.Compra;
import com.upc.divinasbackend.entities.Cuota;

public interface CompraService {
    public void insert(Compra compra);
    public void delete(int id);
    public Compra listId(int id);
    public void update(Compra compra);
}
