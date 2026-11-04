package com.upc.divinasbackend.ServiceImplements;

import com.upc.divinasbackend.entities.Cuota;
import com.upc.divinasbackend.exceptions.ConflictoException;
import com.upc.divinasbackend.exceptions.RecursoNoEncontradoException;
import com.upc.divinasbackend.exceptions.SolicitudInvalidaException;
import com.upc.divinasbackend.repositories.CuotaRepository;
import com.upc.divinasbackend.serviceinterfaces.CuotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class CuotaServiceImplement implements CuotaService {
    @Autowired
    private CuotaRepository cR;

    @Override
    public void insert(Cuota nombre) {
        cR.save(nombre);

    }

    @Override
    public void delete(int id) {
        cR.deleteById(id);

    }

    @Override
    public Cuota listId(int id) {
        return cR.findById(id).orElse(null);
    }

    @Override
    public void update(Cuota nombre) {
        cR.save(nombre);
    }

    @Override
    @Transactional
    public Cuota pagar(int idCompra, int numeroCuota, LocalDate fechaPago) {
        List<Cuota> cuotas = cR.findByCompra_IdCompraOrderByNumeroCuota(idCompra);
        Cuota cuota = cuotas.stream()
                .filter(c -> c.getNumeroCuota() == numeroCuota)
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException("La cuota no existe en esta compra."));
        if ("Pagado".equals(cuota.getEstado())) {
            throw new ConflictoException("La cuota ya está pagada.");
        }
        boolean hayAnteriorPendiente = cuotas.stream()
                .anyMatch(c -> c.getNumeroCuota() < numeroCuota && "Pendiente".equals(c.getEstado()));
        if (hayAnteriorPendiente) {
            throw new ConflictoException("Primero se deben pagar las cuotas anteriores.");
        }
        if (fechaPago.isBefore(cuota.getCompra().getFechaCompra())) {
            throw new SolicitudInvalidaException("La fecha de pago no puede ser anterior a la fecha de la compra.");
        }
        cuota.setEstado("Pagado");
        cuota.setFechaPagoReal(fechaPago);
        return cR.save(cuota);
    }

    @Override
    public List<Cuota> listByCliente(int idCliente) {
        return cR.findByCompra_Cliente_IdClienteOrderByCompra_IdCompraAscNumeroCuotaAsc(idCliente);
    }

    @Override
    public List<Cuota> proximosVencimientos(int idTienda) {
        return cR.findTop10ByCompra_Cliente_Tienda_IdTiendaAndEstadoOrderByFechaVencimientoAsc(idTienda, "Pendiente");
    }

    @Override
    public long comprasEnCurso(int idTienda) {
        return cR.contarComprasEnCurso(idTienda);
    }
}
