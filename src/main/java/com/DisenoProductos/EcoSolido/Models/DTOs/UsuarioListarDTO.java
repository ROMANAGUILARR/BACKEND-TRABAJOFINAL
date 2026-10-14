package com.DisenoProductos.EcoSolido.Models.DTOs;

import com.DisenoProductos.EcoSolido.Models.States.Rol;

public class UsuarioListarDTO {
    private Integer idUsuario;
    private String nombreCompleto;
    private String apellidoCompleto;
    private String dni;
    private String telefono;
    private String correoElectronico;
    private String nombreUsuario;
    private Integer puntos;
    private Rol rol;
    private Long totalIncidencias;
    public Long getTotalIncidencias() { return totalIncidencias; }
    public void setTotalIncidencias(Long totalIncidencias) { this.totalIncidencias = totalIncidencias; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getApellidoCompleto() { return apellidoCompleto; }
    public void setApellidoCompleto(String apellidoCompleto) { this.apellidoCompleto = apellidoCompleto; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreoElectronico() { return correoElectronico; }
    public void setCorreoElectronico(String correoElectronico) { this.correoElectronico = correoElectronico; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    public Integer getPuntos() { return puntos; }
    public void setPuntos(Integer puntos) { this.puntos = puntos; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
}
