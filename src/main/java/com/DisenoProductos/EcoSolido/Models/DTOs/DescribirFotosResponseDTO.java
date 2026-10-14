package com.DisenoProductos.EcoSolido.Models.DTOs;

public class DescribirFotosResponseDTO {
    private String descripcion;
    private String titulo;

    public DescribirFotosResponseDTO() {}

    public DescribirFotosResponseDTO(String descripcion, String titulo) {
        this.descripcion = descripcion;
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
}
