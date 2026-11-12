package com.DisenoProductos.EcoSolido.Models.DTOs;

import java.util.List;

/** Cuadrilla con sus integrantes y su carga de trabajo por estado. */
public record CuadrillaResponseDTO(
        Integer idCuadrilla,
        String nombre,
        String zona,
        boolean activa,
        List<TrabajadorResponseDTO> trabajadores,
        long pendientes,
        long enProceso,
        long resueltas,
        int limiteEnCurso
) {}
