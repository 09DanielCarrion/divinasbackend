package com.upc.divinasbackend.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "producto_servicio")
public class ProductoServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto_servicio")
    private Integer idProductoServicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tienda", nullable = false)
    private Tienda tienda;

    @Column(name = "imagen", length = 255)
    private String imagen;

    @Column(name = "proveedor", length = 80)
    private String proveedor;

    @Column(name = "marca", length = 50)
    private String marca;

    @Column(name = "descripcion", nullable = false, length = 150)
    private String descripcion;

    @Column(name = "unidad_medida", nullable = false, length = 20)
    private String unidadMedida;

    @Column(name = "precio_contado", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioContado;

    // "Al contado" o "En meses"
    @Column(name = "forma_venta", nullable = false, length = 15)
    private String formaVenta = "En meses";

    @Column(name = "estado", nullable = false)
    private Boolean estado = true;

    public ProductoServicio() {
    }

    public ProductoServicio(Integer idProductoServicio, Tienda tienda, String imagen, String proveedor,
                            String marca, String descripcion, String unidadMedida,
                            BigDecimal precioContado, String formaVenta,
                            Boolean estado) {
        this.idProductoServicio = idProductoServicio;
        this.tienda = tienda;
        this.imagen = imagen;
        this.proveedor = proveedor;
        this.marca = marca;
        this.descripcion = descripcion;
        this.unidadMedida = unidadMedida;
        this.precioContado = precioContado;
        this.formaVenta = formaVenta;
        this.estado = estado;
    }

    public Integer getIdProductoServicio() { return idProductoServicio; }
    public void setIdProductoServicio(Integer idProductoServicio) { this.idProductoServicio = idProductoServicio; }

    public Tienda getTienda() { return tienda; }
    public void setTienda(Tienda tienda) { this.tienda = tienda; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }

    public BigDecimal getPrecioContado() { return precioContado; }
    public void setPrecioContado(BigDecimal precioContado) { this.precioContado = precioContado; }


    public String getFormaVenta() { return formaVenta; }
    public void setFormaVenta(String formaVenta) { this.formaVenta = formaVenta; }

    public Boolean getEstado() { return estado; }
    public void setEstado(Boolean estado) { this.estado = estado; }
}
