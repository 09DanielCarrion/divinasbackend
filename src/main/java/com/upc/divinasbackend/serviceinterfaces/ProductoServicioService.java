package com.upc.divinasbackend.serviceinterfaces;

import com.upc.divinasbackend.entities.ProductoServicio;

import java.util.List;

public interface ProductoServicioService {
    public void insert(ProductoServicio productoServicio);
    public void delete(int id);
    public ProductoServicio listId(int id);
    public void update(ProductoServicio productoServicio);
    public List<ProductoServicio> listByTienda(int idTienda, boolean soloActivos);
    public void deactivate(int id);
    public long countActivos(int idTienda);
}
