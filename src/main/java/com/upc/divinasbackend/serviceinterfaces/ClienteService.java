package com.upc.divinasbackend.serviceinterfaces;

import com.upc.divinasbackend.entities.Cliente;

import java.util.List;

public interface ClienteService {
    public void insert(Cliente cliente);
    public void delete(int id);
    public Cliente listId(int id);
    public void update(Cliente cliente);
    public List<Cliente> listByTienda(int idTienda);
    public void deactivate(int id);
    public long countActivos(int idTienda);
}
