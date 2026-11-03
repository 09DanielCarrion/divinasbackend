package com.upc.divinasbackend.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "tienda")
public class Tienda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tienda")
    private Integer idTienda;

    @Column(name = "nombre_tienda", nullable = false, length = 100, unique = true)
    private String nombreTienda;

    @Column(name = "direccion", nullable = false, length = 150)
    private String direccion;

    @Column(name = "telefono", nullable = false, length = 9)
    private String telefono;

    @Column(name = "usuario_admin", nullable = false, length = 30, unique = true)
    private String usuarioAdmin;

    @Column(name = "password_admin", nullable = false, length = 60)
    private String passwordAdmin;

    @Column(name = "estado", nullable = false)
    private Boolean estado = true;

    public Tienda() {
    }

    public Tienda(Integer idTienda, String nombreTienda, String direccion, String telefono,
                  String usuarioAdmin, String passwordAdmin, Boolean estado) {
        this.idTienda = idTienda;
        this.nombreTienda = nombreTienda;
        this.direccion = direccion;
        this.telefono = telefono;
        this.usuarioAdmin = usuarioAdmin;
        this.passwordAdmin = passwordAdmin;
        this.estado = estado;
    }

    public Integer getIdTienda() { return idTienda; }
    public void setIdTienda(Integer idTienda) { this.idTienda = idTienda; }

    public String getNombreTienda() { return nombreTienda; }
    public void setNombreTienda(String nombreTienda) { this.nombreTienda = nombreTienda; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getUsuarioAdmin() { return usuarioAdmin; }
    public void setUsuarioAdmin(String usuarioAdmin) { this.usuarioAdmin = usuarioAdmin; }

    public String getPasswordAdmin() { return passwordAdmin; }
    public void setPasswordAdmin(String passwordAdmin) { this.passwordAdmin = passwordAdmin; }

    public Boolean getEstado() { return estado; }
    public void setEstado(Boolean estado) { this.estado = estado; }
}