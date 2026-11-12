package com.DisenoProductos.EcoSolido.Models.DTOs;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class IncidenciaRequestDTO {
    @NotBlank(message = "No se ha podido registrar su incidencia. Debe colocar su descripción.")
    @Size(max = 800, message = "La descripción no puede superar los 800 caracteres.")
    private String descripcion;
    @NotBlank(message = "No se ha podido registrar su incidencia. Debe establecer la categoría de la incidencia.")
    @Pattern(regexp = "Acumulación y falta de recojo|Basura en vía pública|Contenedor dañado o lleno|Escombros o materiales de construcción|Residuos en parques o áreas verdes|Otro", message = "La categoría seleccionada no es válida.")
    private String categoria;
    @Size(max = 200, message = "El título no puede superar los 200 caracteres.")
    private String titulo;
    @NotNull(message = "No se ha podido registrar su incidencia. Debe seleccionar la ubicación en el mapa.")
    @DecimalMin(value = "-90.0", message = "La latitud no es válida.")
    @DecimalMax(value = "90.0", message = "La latitud no es válida.")
    private Double latitud;
    @NotNull(message = "No se ha podido registrar su incidencia. Debe seleccionar la ubicación en el mapa.")
    @DecimalMin(value = "-180.0", message = "La longitud no es válida.")
    @DecimalMax(value = "180.0", message = "La longitud no es válida.")
    private Double longitud;
    @NotBlank(message="No se ha podido registrar su incidencia. Debe establecer su ubicación.")
    @Size(max = 255, message = "La dirección no puede superar los 255 caracteres.")
    private String direccionTexto;

    public Double getLatitud() {
        return latitud;
    }

    public String getDireccionTexto() {
        return direccionTexto;
    }

    public void setDireccionTexto(String direccionTexto) {
        this.direccionTexto = direccionTexto;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
}
