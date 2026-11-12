package com.DisenoProductos.EcoSolido.Models.DTOs;

import java.util.List;

/**
 * Reporte de incidencias registradas entre dos fechas (incluidas).
 * Las no resueltas traen en cada incidencia el motivo de por qué siguen abiertas.
 */
public record ReporteIncidenciasDTO(
        String desde,
        String hasta,
        long totalReportadas,
        long resueltas,
        long enProceso,
        long pendientes,
        double porcentajeResueltas,
        List<ResumenPorUsuario> porUsuario,
        List<SeguirIncidenciaResponseDTO> incidencias
) {
    public record ResumenPorUsuario(String usuario, long reportadas, long resueltas, long noResueltas) {}
}
