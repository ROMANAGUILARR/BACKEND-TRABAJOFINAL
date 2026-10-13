package com.DisenoProductos.EcoSolido.Models.DTOs;

import com.DisenoProductos.EcoSolido.Models.States.Rol;

public class CrearUsuarioResponseDTO {
    private Integer idUsuario;
    private String nombreCompleto;
    private String apellidoCompleto;
    private String nombreUsuario;
    private String correoElectronico;
    private Rol rol;
    private Integer puntos;

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getApellidoCompleto() { return apellidoCompleto; }
    public void setApellidoCompleto(String apellidoCompleto) { this.apellidoCompleto = apellidoCompleto; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    public String getCorreoElectronico() { return correoElectronico; }
    public void setCorreoElectronico(String correoElectronico) { this.correoElectronico = correoElectronico; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public Integer getPuntos() { return puntos; }
    public void setPuntos(Integer puntos) { this.puntos = puntos; }
}
