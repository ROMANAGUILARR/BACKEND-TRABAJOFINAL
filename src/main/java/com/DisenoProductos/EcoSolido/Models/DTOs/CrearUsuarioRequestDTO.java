package com.DisenoProductos.EcoSolido.Models.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CrearUsuarioRequestDTO {
    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombreCompleto;

    @NotBlank(message = "El apellido completo es obligatorio")
    @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
    private String apellidoCompleto;

    // DNI, CE (carné de extranjería), PASAPORTE o PTP. Si no se envía, se asume DNI.
    @Pattern(regexp = "DNI|CE|PASAPORTE|PTP", message = "El tipo de documento no es válido")
    private String tipoDocumento;

    // El formato exacto se valida según el tipo de documento en UsuarioService
    @NotBlank(message = "El número de documento es obligatorio")
    @Pattern(regexp = "[A-Za-z0-9]{6,12}", message = "El número de documento debe tener entre 6 y 12 letras o dígitos")
    private String dni;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "\\d{9}", message = "El teléfono debe tener exactamente 9 dígitos")
    private String telefono;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico no tiene un formato válido")
    private String correoElectronico;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    private String nombreUsuario;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 72, message = "La contraseña debe tener al menos 6 caracteres")
    private String contrasena;

    @NotBlank(message = "La pregunta de seguridad es obligatoria")
    private String preguntaSeguridad;

    @NotBlank(message = "La respuesta de seguridad es obligatoria")
    @Size(max = 50, message = "La respuesta de seguridad debe tener como máximo 50 caracteres")
    private String respuestaPregunta;

    public String getNombreCompleto() {
        return nombreCompleto;
    }
    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }
    public String getApellidoCompleto() {
        return apellidoCompleto;
    }
    public void setApellidoCompleto(String apellidoCompleto) {
        this.apellidoCompleto = apellidoCompleto;
    }
    public String getTipoDocumento() {
        return tipoDocumento;
    }
    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }
    public String getDni() {
        return dni;
    }
    public void setDni(String dni) {
        this.dni = dni;
    }
    public String getTelefono() {
        return telefono;
    }
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    public String getNombreUsuario() {
        return nombreUsuario;
    }
    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }
    public String getContrasena() {
        return contrasena;
    }
    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
    public String getPreguntaSeguridad() {
        return preguntaSeguridad;
    }
    public void setPreguntaSeguridad(String preguntaSeguridad) {
        this.preguntaSeguridad = preguntaSeguridad;
    }
    public String getRespuestaPregunta() {return respuestaPregunta;}
    public void setRespuestaPregunta(String respuestaPregunta) {this.respuestaPregunta = respuestaPregunta;}
    public String getCorreoElectronico() {return correoElectronico;}
    public void setCorreoElectronico(String correoElectronico) {this.correoElectronico = correoElectronico;}
}
