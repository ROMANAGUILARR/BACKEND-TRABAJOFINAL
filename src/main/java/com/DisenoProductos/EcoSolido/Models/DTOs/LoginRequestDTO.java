package com.DisenoProductos.EcoSolido.Models.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequestDTO {
    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String nombreUsuario;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 1, max = 72, message = "La contraseña es obligatoria")
    private String contrasena;

    public String getNombreUsuario() {return nombreUsuario;}
    public void setNombreUsuario(String nombreUsuario) {this.nombreUsuario = nombreUsuario;}
    public String getContrasena() {return contrasena;}
    public void setContrasena(String contrasena) {this.contrasena = contrasena;}
}
