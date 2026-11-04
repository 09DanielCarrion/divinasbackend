package com.upc.divinasbackend.repositories;

import com.upc.divinasbackend.entities.ProductoServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoServicioRepository extends JpaRepository<ProductoServicio, Integer> {

    List<ProductoServicio> findByTienda_IdTienda(Integer idTienda);

    List<ProductoServicio> findByTienda_IdTiendaAndEstadoTrue(Integer idTienda);

    long countByTienda_IdTiendaAndEstadoTrue(Integer idTienda);
}
