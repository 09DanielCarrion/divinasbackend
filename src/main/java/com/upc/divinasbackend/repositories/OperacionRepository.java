package com.upc.divinasbackend.repositories;

import com.upc.divinasbackend.entities.Operacion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperacionRepository extends JpaRepository<Operacion, Integer> {

    List<Operacion> findByIdTienda(Integer idTienda, Pageable pageable);
}
