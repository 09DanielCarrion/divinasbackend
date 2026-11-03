package com.upc.divinasbackend.ServiceImplements;

import com.upc.divinasbackend.entities.ProductoServicio;
import com.upc.divinasbackend.exceptions.RecursoNoEncontradoException;
import com.upc.divinasbackend.exceptions.SolicitudInvalidaException;
import com.upc.divinasbackend.repositories.ProductoServicioRepository;
import com.upc.divinasbackend.serviceinterfaces.ProductoServicioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoServicioServiceImplement implements ProductoServicioService {

    @Autowired
    private ProductoServicioRepository psR;

    @Override
    public void insert(ProductoServicio producto) {
        if (!Boolean.TRUE.equals(producto.getTienda().getEstado())) {
            throw new SolicitudInvalidaException("La tienda está dada de baja.");
        }
        validarPrecios(producto);
        if (producto.getFormaVenta() == null || producto.getFormaVenta().isBlank()) {
            producto.setFormaVenta("En meses");
        }
        producto.setEstado(true);
        psR.save(producto);

    }

    @Override
    public void delete(int id) {
        psR.deleteById(id);

    }

    @Override
    public ProductoServicio listId(int id) {
        return psR.findById(id).orElse(null);
    }

    @Override
    public void update(ProductoServicio producto) {
        validarPrecios(producto);
        psR.save(producto);
    }

    @Override
    public List<ProductoServicio> listByTienda(int idTienda, boolean soloActivos) {
        if (soloActivos) {
            return psR.findByTienda_IdTiendaAndEstadoTrue(idTienda);
        }
        return psR.findByTienda_IdTienda(idTienda);
    }

    // Baja lógica: el producto queda con estado = false (ya no está en venta)
    @Override
    public void deactivate(int id) {
        ProductoServicio producto = psR.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El producto o servicio no existe."));
        producto.setEstado(false);
        psR.save(producto);
    }

    // Regla del TP: el precio a crédito debe ser mayor que el precio al contado
    private void validarPrecios(ProductoServicio producto) {
        if (producto.getPrecioCredito().compareTo(producto.getPrecioContado()) <= 0) {
            throw new SolicitudInvalidaException("El precio a crédito debe ser mayor que el precio al contado.");
        }
    }
}
