package com.upc.divinasbackend.controllers;

import com.upc.divinasbackend.dtos.ProductoServicioRequestDTO;
import com.upc.divinasbackend.dtos.ProductoServicioResponseDTO;
import com.upc.divinasbackend.entities.ProductoServicio;
import com.upc.divinasbackend.entities.Tienda;
import com.upc.divinasbackend.exceptions.RecursoNoEncontradoException;
import com.upc.divinasbackend.serviceinterfaces.ProductoServicioService;
import com.upc.divinasbackend.serviceinterfaces.TiendaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tiendas/{idTienda}/productos")
public class ProductoServicioController {

    @Autowired
    private ProductoServicioService psS;

    @Autowired
    private TiendaService tS;

    @PostMapping
    public ResponseEntity<ProductoServicioResponseDTO> crear(@PathVariable("idTienda") int idTienda,
                                                             @Valid @RequestBody ProductoServicioRequestDTO dto) {
        ProductoServicio producto = new ProductoServicio();
        producto.setTienda(buscarTienda(idTienda));
        copiarDatos(producto, dto);
        psS.insert(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(aRespuesta(producto));
    }

    @GetMapping
    public List<ProductoServicioResponseDTO> listar(@PathVariable("idTienda") int idTienda,
                                                    @RequestParam(name = "soloActivos", defaultValue = "false") boolean soloActivos) {
        buscarTienda(idTienda);
        return psS.listByTienda(idTienda, soloActivos).stream().map(this::aRespuesta).toList();
    }

    @GetMapping("/{idProducto}")
    public ProductoServicioResponseDTO obtener(@PathVariable("idTienda") int idTienda,
                                               @PathVariable("idProducto") int idProducto) {
        return aRespuesta(buscarProductoDeTienda(idTienda, idProducto));
    }

    @PutMapping("/{idProducto}")
    public ProductoServicioResponseDTO actualizar(@PathVariable("idTienda") int idTienda,
                                                  @PathVariable("idProducto") int idProducto,
                                                  @Valid @RequestBody ProductoServicioRequestDTO dto) {
        ProductoServicio producto = buscarProductoDeTienda(idTienda, idProducto);
        copiarDatos(producto, dto);
        psS.update(producto);
        return aRespuesta(producto);
    }

    @DeleteMapping("/{idProducto}")
    public ResponseEntity<Void> darDeBaja(@PathVariable("idTienda") int idTienda,
                                          @PathVariable("idProducto") int idProducto) {
        buscarProductoDeTienda(idTienda, idProducto);
        psS.deactivate(idProducto);
        return ResponseEntity.noContent().build();
    }

    private void copiarDatos(ProductoServicio producto, ProductoServicioRequestDTO dto) {
        producto.setImagen(dto.imagen());
        producto.setProveedor(dto.proveedor());
        producto.setMarca(dto.marca());
        producto.setDescripcion(dto.descripcion());
        producto.setUnidadMedida(dto.unidadMedida());
        producto.setPrecioContado(dto.precioContado());
        if (dto.formaVenta() != null) {
            producto.setFormaVenta(dto.formaVenta());
        }
    }

    private Tienda buscarTienda(int idTienda) {
        Tienda tienda = tS.listId(idTienda);
        if (tienda == null) {
            throw new RecursoNoEncontradoException("La tienda no existe.");
        }
        return tienda;
    }

    private ProductoServicio buscarProductoDeTienda(int idTienda, int idProducto) {
        ProductoServicio producto = psS.listId(idProducto);
        if (producto == null || !producto.getTienda().getIdTienda().equals(idTienda)) {
            throw new RecursoNoEncontradoException("El producto o servicio no existe en esta tienda.");
        }
        return producto;
    }

    private ProductoServicioResponseDTO aRespuesta(ProductoServicio p) {
        return new ProductoServicioResponseDTO(p.getIdProductoServicio(), p.getTienda().getIdTienda(),
                p.getImagen(), p.getProveedor(), p.getMarca(), p.getDescripcion(), p.getUnidadMedida(),
                p.getPrecioContado(), p.getFormaVenta(), p.getEstado());
    }
}
