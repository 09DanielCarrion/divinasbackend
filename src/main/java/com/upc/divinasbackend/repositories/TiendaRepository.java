package com.upc.divinasbackend.repositories;

import com.upc.divinasbackend.entities.Tienda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TiendaRepository extends JpaRepository<Tienda, Integer> {

    boolean existsByNombreTienda(String nombreTienda);

    Optional<Tienda> findByUsuarioAdmin(String usuarioAdmin);

    // Para validar al modificar: que otro registro distinto no use el mismo valor
    boolean existsByNombreTiendaAndIdTiendaNot(String nombreTienda, Integer idTienda);

    boolean existsByUsuarioAdminAndIdTiendaNot(String usuarioAdmin, Integer idTienda);
}
