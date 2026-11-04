package com.upc.divinasbackend.repositories;

import com.upc.divinasbackend.entities.Cuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CuotaRepository extends JpaRepository<Cuota, Integer> {

    List<Cuota> findByCompra_IdCompraOrderByNumeroCuota(Integer idCompra);

    List<Cuota> findByCompra_Cliente_IdClienteOrderByCompra_IdCompraAscNumeroCuotaAsc(Integer idCliente);

    List<Cuota> findTop10ByCompra_Cliente_Tienda_IdTiendaAndEstadoOrderByFechaVencimientoAsc(Integer idTienda, String estado);

    @Query("select count(distinct c.compra.idCompra) from Cuota c "
            + "where c.compra.cliente.tienda.idTienda = :idTienda and c.estado = 'Pendiente'")
    long contarComprasEnCurso(@Param("idTienda") Integer idTienda);
}
