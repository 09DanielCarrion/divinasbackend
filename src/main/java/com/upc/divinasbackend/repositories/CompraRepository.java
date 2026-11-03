package com.upc.divinasbackend.repositories;

import com.upc.divinasbackend.entities.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Integer> {

    List<Compra> findByCliente_IdCliente(Integer idCliente);
}
