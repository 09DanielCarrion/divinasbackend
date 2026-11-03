package com.upc.divinasbackend.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cuota")
public class Cuota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cuota")
    private Integer idCuota;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_compra", nullable = false)
    private Compra compra;

    @Column(name = "numero_cuota", nullable = false)
    private Integer numeroCuota;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(name = "monto_cuota", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoCuota;

    @Column(name = "estado", nullable = false, length = 10)
    private String estado = "Pendiente";

    @Column(name = "fecha_pago_real")
    private LocalDate fechaPagoReal;

    public Cuota() {
    }

    public Cuota(Integer idCuota, Compra compra, Integer numeroCuota, LocalDate fechaVencimiento,
                 BigDecimal montoCuota, String estado, LocalDate fechaPagoReal) {
        this.idCuota = idCuota;
        this.compra = compra;
        this.numeroCuota = numeroCuota;
        this.fechaVencimiento = fechaVencimiento;
        this.montoCuota = montoCuota;
        this.estado = estado;
        this.fechaPagoReal = fechaPagoReal;
    }

    public Integer getIdCuota() { return idCuota; }
    public void setIdCuota(Integer idCuota) { this.idCuota = idCuota; }

    public Compra getCompra() { return compra; }
    public void setCompra(Compra compra) { this.compra = compra; }

    public Integer getNumeroCuota() { return numeroCuota; }
    public void setNumeroCuota(Integer numeroCuota) { this.numeroCuota = numeroCuota; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public BigDecimal getMontoCuota() { return montoCuota; }
    public void setMontoCuota(BigDecimal montoCuota) { this.montoCuota = montoCuota; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDate getFechaPagoReal() { return fechaPagoReal; }
    public void setFechaPagoReal(LocalDate fechaPagoReal) { this.fechaPagoReal = fechaPagoReal; }
}
