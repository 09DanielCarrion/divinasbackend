package com.upc.divinasbackend.serviceinterfaces;

import com.upc.divinasbackend.entities.Tienda;

import java.util.List;

public interface TiendaService {
    public void insert(Tienda tienda);
    public void delete(int id);
    public Tienda listId(int id);
    public void update(Tienda tienda);
    public List<Tienda> list();
    public void deactivate(int id);
}
