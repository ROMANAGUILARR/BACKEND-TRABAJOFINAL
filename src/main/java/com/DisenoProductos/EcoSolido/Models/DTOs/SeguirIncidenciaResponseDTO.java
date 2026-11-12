package com.DisenoProductos.EcoSolido.Models.DTOs;

import java.util.List;

public class SeguirIncidenciaResponseDTO {
    private Integer idIncidencia;
    private String descripcion;
    private String estado;
    private String titulo;
    private String fecha;
    private String categoria;
    private String direccionTexto;
    private String nombreUsuario;
    private String reportadoPor;
    // Atención de la incidencia
    private Integer idCuadrilla;
    private String cuadrillaNombre;
    private String cuadrillaZona;
    private String fechaAsignacion;
    private String fechaResolucion;
    private List<String> urlsEvidencias;
    public Integer getIdCuadrilla() {
        return idCuadrilla;
    }
    public void setIdCuadrilla(Integer idCuadrilla) {
        this.idCuadrilla = idCuadrilla;
    }
    public String getCuadrillaNombre() {
        return cuadrillaNombre;
    }
    public void setCuadrillaNombre(String cuadrillaNombre) {
        this.cuadrillaNombre = cuadrillaNombre;
    }
    public String getCuadrillaZona() {
        return cuadrillaZona;
    }
    public void setCuadrillaZona(String cuadrillaZona) {
        this.cuadrillaZona = cuadrillaZona;
    }
    public String getFechaAsignacion() {
        return fechaAsignacion;
    }
    public void setFechaAsignacion(String fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }
    public String getFechaResolucion() {
        return fechaResolucion;
    }
    public void setFechaResolucion(String fechaResolucion) {
        this.fechaResolucion = fechaResolucion;
    }
    public List<String> getUrlsEvidencias() {
        return urlsEvidencias;
    }
    public void setUrlsEvidencias(List<String> urlsEvidencias) {
        this.urlsEvidencias = urlsEvidencias;
    }
    public String getNombreUsuario() {
        return nombreUsuario;
    }
    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }
    public String getReportadoPor() {
        return reportadoPor;
    }
    public void setReportadoPor(String reportadoPor) {
        this.reportadoPor = reportadoPor;
    }

    public String getCategoria() {
        return categoria;
    }
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
    public String getDireccionTexto() {
        return direccionTexto;
    }
    public void setDireccionTexto(String direccionTexto) {
        this.direccionTexto = direccionTexto;
    }
    private List<String> urlsImagenes;
    // Solo en el reporte por fechas: por qué la incidencia aún no se resuelve
    private String motivoNoResuelta;
    public String getMotivoNoResuelta() {
        return motivoNoResuelta;
    }
    public void setMotivoNoResuelta(String motivoNoResuelta) {
        this.motivoNoResuelta = motivoNoResuelta;
    }
    public String getTitulo() {
        return titulo;
    }
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    public List<String> getUrlsImagenes() {
        return urlsImagenes;
    }
    public void setUrlsImagenes(List<String> urlsImagenes) {
        this.urlsImagenes = urlsImagenes;
    }
    public Integer getIdIncidencia() {
        return idIncidencia;
    }
    public void setIdIncidencia(Integer idIncidencia) {
        this.idIncidencia = idIncidencia;
    }
    public String getDescripcion() {
        return descripcion;
    }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    public String getEstado() {
        return estado;
    }
    public void setEstado(String estado) {
        this.estado = estado;
    }
    public String getFecha() {
        return fecha;
    }
    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
}
