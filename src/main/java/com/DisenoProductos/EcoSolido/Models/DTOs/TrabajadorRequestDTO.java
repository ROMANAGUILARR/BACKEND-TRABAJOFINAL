package com.DisenoProductos.EcoSolido.Models.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TrabajadorRequestDTO(
        @NotBlank(message = "El nombre del trabajador es obligatorio")
        @Size(max = 200, message = "El nombre debe tener como máximo 200 caracteres")
        String nombreCompleto,
        @Pattern(regexp = "^$|\\d{8}", message = "El DNI debe tener exactamente 8 dígitos")
        String dni,
        @Pattern(regexp = "^$|\\d{9}", message = "El teléfono debe tener exactamente 9 dígitos")
        String telefono,
        Boolean esJefe,
        Boolean activo
) {}
