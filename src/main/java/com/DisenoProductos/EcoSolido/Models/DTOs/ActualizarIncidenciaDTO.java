package com.DisenoProductos.EcoSolido.Models.DTOs;

import com.DisenoProductos.EcoSolido.Models.States.IncidenciaEstados;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ActualizarIncidenciaDTO {
    @Size(max = 200, message = "El título no puede superar los 200 caracteres")
    private String titulo;

    @NotBlank(message = "La descripción no puede estar vacía")
    @Size(max = 800, message = "La descripción no puede superar los 800 caracteres")
    private String descripcion;

    @NotBlank(message = "La categoría no puede estar vacía")
    private String categoria;

    @DecimalMin(value = "-90.0", message = "La latitud no es válida")
    @DecimalMax(value = "90.0", message = "La latitud no es válida")
    private Double latitud;
    @DecimalMin(value = "-180.0", message = "La longitud no es válida")
    @DecimalMax(value = "180.0", message = "La longitud no es válida")
    private Double longitud;

    @NotBlank(message = "La dirección no puede estar vacía")
    private String direccionTexto;

    private IncidenciaEstados estado;

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }
    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }
    public String getDireccionTexto() { return direccionTexto; }
    public void setDireccionTexto(String direccionTexto) { this.direccionTexto = direccionTexto; }
    public IncidenciaEstados getEstado() { return estado; }
    public void setEstado(IncidenciaEstados estado) { this.estado = estado; }
}
