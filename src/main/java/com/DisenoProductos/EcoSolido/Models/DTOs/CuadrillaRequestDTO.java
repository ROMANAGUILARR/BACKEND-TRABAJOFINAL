package com.DisenoProductos.EcoSolido.Models.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CuadrillaRequestDTO(
        @NotBlank(message = "El nombre de la cuadrilla es obligatorio")
        @Size(max = 100, message = "El nombre debe tener como máximo 100 caracteres")
        String nombre,
        @Size(max = 150, message = "La zona debe tener como máximo 150 caracteres")
        String zona,
        Boolean activa
) {}
