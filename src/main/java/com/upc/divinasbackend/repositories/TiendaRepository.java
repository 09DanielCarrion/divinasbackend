package com.upc.divinasbackend.repositories;

import com.upc.divinasbackend.entities.Tienda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TiendaRepository extends JpaRepository<Tienda, Integer> {

    boolean existsByNombreTienda(String nombreTienda);

    Optional<Tienda> findByUsuarioAdmin(String usuarioAdmin);
}
