package com.upc.divinasbackend.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "compra")
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_compra")
    private Integer idCompra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto_servicio", nullable = false)
    private ProductoServicio productoServicio;

    @Column(name = "fecha_compra", nullable = false)
    private LocalDate fechaCompra;

    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(name = "forma_pago", nullable = false, length = 15)
    private String formaPago;

    @Column(name = "num_meses", nullable = false)
    private Integer numMeses = 1;

    public Compra() {
    }

    public Compra(Integer idCompra, Cliente cliente, ProductoServicio productoServicio,
                  LocalDate fechaCompra, BigDecimal monto, String formaPago, Integer numMeses) {
        this.idCompra = idCompra;
        this.cliente = cliente;
        this.productoServicio = productoServicio;
        this.fechaCompra = fechaCompra;
        this.monto = monto;
        this.formaPago = formaPago;
        this.numMeses = numMeses;
    }

    public Integer getIdCompra() { return idCompra; }
    public void setIdCompra(Integer idCompra) { this.idCompra = idCompra; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public ProductoServicio getProductoServicio() { return productoServicio; }
    public void setProductoServicio(ProductoServicio productoServicio) { this.productoServicio = productoServicio; }

    public LocalDate getFechaCompra() { return fechaCompra; }
    public void setFechaCompra(LocalDate fechaCompra) { this.fechaCompra = fechaCompra; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public String getFormaPago() { return formaPago; }
    public void setFormaPago(String formaPago) { this.formaPago = formaPago; }

    public Integer getNumMeses() { return numMeses; }
    public void setNumMeses(Integer numMeses) { this.numMeses = numMeses; }
}
