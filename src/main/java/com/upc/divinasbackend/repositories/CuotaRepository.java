package com.upc.divinasbackend.repositories;

import com.upc.divinasbackend.entities.Cuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CuotaRepository extends JpaRepository<Cuota, Integer> {

    List<Cuota> findByCompra_IdCompraOrderByNumeroCuota(Integer idCompra);
}
