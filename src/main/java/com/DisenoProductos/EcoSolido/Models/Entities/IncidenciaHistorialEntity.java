package com.DisenoProductos.EcoSolido.Models.Entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Registro de cada cambio en una incidencia.
 * tipo: ESTADO (cambio de estado), ASIGNACION (cuadrilla) o EVIDENCIA (fotos de resolución).
 */
@Entity
@Table(name = "incidencia_historial")
public class IncidenciaHistorialEntity {
    public static final String TIPO_ESTADO = "ESTADO";
    public static final String TIPO_ASIGNACION = "ASIGNACION";
    public static final String TIPO_EVIDENCIA = "EVIDENCIA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial")
    private Integer idHistorial;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_incidencia", nullable = false)
    private IncidenciaEntity incidencia;

    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(name = "estado_anterior", length = 20)
    private String estadoAnterior;

    @Column(name = "estado_nuevo", length = 20)
    private String estadoNuevo;

    @Column(length = 500)
    private String detalle;

    @Column(name = "realizado_por")
    private String realizadoPor;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    public Integer getIdHistorial() { return idHistorial; }
    public void setIdHistorial(Integer idHistorial) { this.idHistorial = idHistorial; }
    public IncidenciaEntity getIncidencia() { return incidencia; }
    public void setIncidencia(IncidenciaEntity incidencia) { this.incidencia = incidencia; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getEstadoAnterior() { return estadoAnterior; }
    public void setEstadoAnterior(String estadoAnterior) { this.estadoAnterior = estadoAnterior; }
    public String getEstadoNuevo() { return estadoNuevo; }
    public void setEstadoNuevo(String estadoNuevo) { this.estadoNuevo = estadoNuevo; }
    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
    public String getRealizadoPor() { return realizadoPor; }
    public void setRealizadoPor(String realizadoPor) { this.realizadoPor = realizadoPor; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
