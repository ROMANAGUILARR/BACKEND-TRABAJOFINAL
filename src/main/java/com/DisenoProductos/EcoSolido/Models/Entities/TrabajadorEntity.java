package com.DisenoProductos.EcoSolido.Models.Entities;

import jakarta.persistence.*;

/** Integrante de una cuadrilla. Lo registra el administrador (no inicia sesión). */
@Entity
@Table(name = "trabajador")
public class TrabajadorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_trabajador")
    private Integer idTrabajador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cuadrilla", nullable = false)
    private CuadrillaEntity cuadrilla;

    @Column(name = "nombre_completo", nullable = false, length = 200)
    private String nombreCompleto;

    @Column(length = 8)
    private String dni;

    @Column(length = 9)
    private String telefono;

    @Column(name = "es_jefe", nullable = false)
    private Boolean esJefe = false;

    @Column(nullable = false)
    private Boolean activo = true;

    public Integer getIdTrabajador() { return idTrabajador; }
    public void setIdTrabajador(Integer idTrabajador) { this.idTrabajador = idTrabajador; }
    public CuadrillaEntity getCuadrilla() { return cuadrilla; }
    public void setCuadrilla(CuadrillaEntity cuadrilla) { this.cuadrilla = cuadrilla; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public Boolean getEsJefe() { return esJefe; }
    public void setEsJefe(Boolean esJefe) { this.esJefe = esJefe; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
