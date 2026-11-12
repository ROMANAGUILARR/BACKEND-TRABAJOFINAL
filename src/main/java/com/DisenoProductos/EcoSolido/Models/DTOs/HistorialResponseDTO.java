package com.DisenoProductos.EcoSolido.Models.DTOs;

public record HistorialResponseDTO(
        String tipo,
        String estadoAnterior,
        String estadoNuevo,
        String detalle,
        String realizadoPor,
        String fecha
) {}
