package com.DisenoProductos.EcoSolido.Models.Entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Equipo de limpieza que atiende las incidencias de una zona. */
@Entity
@Table(name = "cuadrilla")
public class CuadrillaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cuadrilla")
    private Integer idCuadrilla;

    @Column(nullable = false, length = 100)
    private String nombre;

    /** Distrito o zona que cubre, p. ej. "Los Olivos". */
    @Column(length = 150)
    private String zona;

    @Column(nullable = false)
    private Boolean activa = true;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @OneToMany(mappedBy = "cuadrilla", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("esJefe DESC, nombreCompleto ASC")
    private List<TrabajadorEntity> trabajadores = new ArrayList<>();

    public Integer getIdCuadrilla() { return idCuadrilla; }
    public void setIdCuadrilla(Integer idCuadrilla) { this.idCuadrilla = idCuadrilla; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }
    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public List<TrabajadorEntity> getTrabajadores() { return trabajadores; }
    public void setTrabajadores(List<TrabajadorEntity> trabajadores) { this.trabajadores = trabajadores; }
}
