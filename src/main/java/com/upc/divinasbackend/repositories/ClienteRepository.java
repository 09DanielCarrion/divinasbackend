package com.upc.divinasbackend.repositories;

import com.upc.divinasbackend.entities.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    List<Cliente> findByTienda_IdTienda(Integer idTienda);

    long countByTienda_IdTiendaAndEstadoTrue(Integer idTienda);

    boolean existsByTienda_IdTiendaAndUsuarioCliente(Integer idTienda, String usuarioCliente);

    // Para validar al modificar: otro cliente distinto de la misma tienda no puede usar ese usuario
    boolean existsByTienda_IdTiendaAndUsuarioClienteAndIdClienteNot(Integer idTienda, String usuarioCliente, Integer idCliente);
}
