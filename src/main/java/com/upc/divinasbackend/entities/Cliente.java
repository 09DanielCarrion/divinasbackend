package com.upc.divinasbackend.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "cliente",
        uniqueConstraints = @UniqueConstraint(name = "uk_cliente_tienda_usuario",
                columnNames = {"id_tienda", "usuario_cliente"}))
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Integer idCliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tienda", nullable = false)
    private Tienda tienda;

    @Column(name = "nombre_cliente", nullable = false, length = 100)
    private String nombreCliente;

    @Column(name = "tipo_tasa", nullable = false, length = 10)
    private String tipoTasa;

    @Column(name = "tasa_interes", nullable = false, precision = 10, scale = 4)
    private BigDecimal tasaInteres;

    @Column(name = "capitalizacion", length = 15)
    private String capitalizacion;

    @Column(name = "moneda", nullable = false, length = 10)
    private String moneda = "Soles";

    @Column(name = "limite_max_credito", nullable = false, precision = 12, scale = 2)
    private BigDecimal limiteMaxCredito;

    @Column(name = "max_meses_credito", nullable = false)
    private Integer maxMesesCredito;

    @Column(name = "fecha_corte", nullable = false)
    private Integer fechaCorte;

    @Column(name = "fecha_pago", nullable = false)
    private Integer fechaPago;

    @Column(name = "usuario_cliente", nullable = false, length = 30)
    private String usuarioCliente;

    @Column(name = "password_cliente", nullable = false, length = 60)
    private String passwordCliente;

    @Column(name = "estado", nullable = false)
    private Boolean estado = true;

    public Cliente() {
    }

    public Cliente(Integer idCliente, Tienda tienda, String nombreCliente, String tipoTasa,
                   BigDecimal tasaInteres, String capitalizacion, String moneda,
                   BigDecimal limiteMaxCredito, Integer maxMesesCredito, Integer fechaCorte,
                   Integer fechaPago, String usuarioCliente, String passwordCliente, Boolean estado) {
        this.idCliente = idCliente;
        this.tienda = tienda;
        this.nombreCliente = nombreCliente;
        this.tipoTasa = tipoTasa;
        this.tasaInteres = tasaInteres;
        this.capitalizacion = capitalizacion;
        this.moneda = moneda;
        this.limiteMaxCredito = limiteMaxCredito;
        this.maxMesesCredito = maxMesesCredito;
        this.fechaCorte = fechaCorte;
        this.fechaPago = fechaPago;
        this.usuarioCliente = usuarioCliente;
        this.passwordCliente = passwordCliente;
        this.estado = estado;
    }

    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }

    public Tienda getTienda() { return tienda; }
    public void setTienda(Tienda tienda) { this.tienda = tienda; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getTipoTasa() { return tipoTasa; }
    public void setTipoTasa(String tipoTasa) { this.tipoTasa = tipoTasa; }

    public BigDecimal getTasaInteres() { return tasaInteres; }
    public void setTasaInteres(BigDecimal tasaInteres) { this.tasaInteres = tasaInteres; }

    public String getCapitalizacion() { return capitalizacion; }
    public void setCapitalizacion(String capitalizacion) { this.capitalizacion = capitalizacion; }

    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }

    public BigDecimal getLimiteMaxCredito() { return limiteMaxCredito; }
    public void setLimiteMaxCredito(BigDecimal limiteMaxCredito) { this.limiteMaxCredito = limiteMaxCredito; }

    public Integer getMaxMesesCredito() { return maxMesesCredito; }
    public void setMaxMesesCredito(Integer maxMesesCredito) { this.maxMesesCredito = maxMesesCredito; }

    public Integer getFechaCorte() { return fechaCorte; }
    public void setFechaCorte(Integer fechaCorte) { this.fechaCorte = fechaCorte; }

    public Integer getFechaPago() { return fechaPago; }
    public void setFechaPago(Integer fechaPago) { this.fechaPago = fechaPago; }

    public String getUsuarioCliente() { return usuarioCliente; }
    public void setUsuarioCliente(String usuarioCliente) { this.usuarioCliente = usuarioCliente; }

    public String getPasswordCliente() { return passwordCliente; }
    public void setPasswordCliente(String passwordCliente) { this.passwordCliente = passwordCliente; }

    public Boolean getEstado() { return estado; }
    public void setEstado(Boolean estado) { this.estado = estado; }
}
