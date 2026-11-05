package com.upc.divinasbackend.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "operacion")
public class Operacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_operacion")
    private Integer idOperacion;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "usuario", length = 100)
    private String usuario;

    @Column(name = "rol", length = 20)
    private String rol;

    @Column(name = "id_tienda")
    private Integer idTienda;

    @Column(name = "accion", nullable = false, length = 60)
    private String accion;

    @Column(name = "metodo", nullable = false, length = 10)
    private String metodo;

    @Column(name = "ruta", nullable = false, length = 255)
    private String ruta;

    @Column(name = "codigo_http", nullable = false)
    private Integer codigoHttp;

    public Operacion() {
    }

    public Operacion(LocalDateTime fechaHora, String usuario, String rol, Integer idTienda, String accion,
                     String metodo, String ruta, Integer codigoHttp) {
        this.fechaHora = fechaHora;
        this.usuario = usuario;
        this.rol = rol;
        this.idTienda = idTienda;
        this.accion = accion;
        this.metodo = metodo;
        this.ruta = ruta;
        this.codigoHttp = codigoHttp;
    }

    public Integer getIdOperacion() { return idOperacion; }
    public void setIdOperacion(Integer idOperacion) { this.idOperacion = idOperacion; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public Integer getIdTienda() { return idTienda; }
    public void setIdTienda(Integer idTienda) { this.idTienda = idTienda; }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }

    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }

    public String getRuta() { return ruta; }
    public void setRuta(String ruta) { this.ruta = ruta; }

    public Integer getCodigoHttp() { return codigoHttp; }
    public void setCodigoHttp(Integer codigoHttp) { this.codigoHttp = codigoHttp; }
}
