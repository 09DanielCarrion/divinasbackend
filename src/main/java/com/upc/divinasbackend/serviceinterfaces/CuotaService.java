package com.upc.divinasbackend.serviceinterfaces;

import com.upc.divinasbackend.entities.Cuota;
import com.upc.divinasbackend.entities.Tienda;

public interface CuotaService {
    public void insert(Cuota cuota);
    public void delete(int id);
    public Cuota listId(int id);
    public void update(Cuota cuota);
}
