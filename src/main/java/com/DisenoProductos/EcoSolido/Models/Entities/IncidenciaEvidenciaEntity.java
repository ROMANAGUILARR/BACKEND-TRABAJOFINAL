package com.DisenoProductos.EcoSolido.Models.Entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Foto del trabajo terminado, subida al marcar la incidencia como resuelta. */
@Entity
@Table(name = "incidencia_evidencia")
public class IncidenciaEvidenciaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evidencia")
    private Integer idEvidencia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_incidencia", nullable = false)
    private IncidenciaEntity incidencia;

    @Column(name = "url_foto", nullable = false, length = 500)
    private String urlFoto;

    @Column(name = "public_id")
    private String publicId;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    public Integer getIdEvidencia() { return idEvidencia; }
    public void setIdEvidencia(Integer idEvidencia) { this.idEvidencia = idEvidencia; }
    public IncidenciaEntity getIncidencia() { return incidencia; }
    public void setIncidencia(IncidenciaEntity incidencia) { this.incidencia = incidencia; }
    public String getUrlFoto() { return urlFoto; }
    public void setUrlFoto(String urlFoto) { this.urlFoto = urlFoto; }
    public String getPublicId() { return publicId; }
    public void setPublicId(String publicId) { this.publicId = publicId; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
