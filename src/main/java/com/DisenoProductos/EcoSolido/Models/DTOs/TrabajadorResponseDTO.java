package com.DisenoProductos.EcoSolido.Models.DTOs;

public record TrabajadorResponseDTO(
        Integer idTrabajador,
        String nombreCompleto,
        String dni,
        String telefono,
        boolean esJefe,
        boolean activo
) {}
