package com.DisenoProductos.EcoSolido.Repositories;

import com.DisenoProductos.EcoSolido.Models.Entities.IncidenciaEntity;
import com.DisenoProductos.EcoSolido.Models.States.IncidenciaEstados;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface IncidenciaRepository extends JpaRepository<IncidenciaEntity,Integer> {
    // Mejora de código eficiente: Resuelve el N+1 trayendo la incidencia y sus fotos en un solo viaje de red a Aiven
    @Query("SELECT DISTINCT i FROM IncidenciaEntity i LEFT JOIN FETCH i.fotos LEFT JOIN FETCH i.usuario WHERE i.usuario.nombreUsuario = :nombreUsuario")
    List<IncidenciaEntity> findByUsuario_NombreUsuarioConFotos(@Param("nombreUsuario") String nombreUsuario);

    @Query("SELECT DISTINCT i FROM IncidenciaEntity i LEFT JOIN FETCH i.fotos LEFT JOIN FETCH i.usuario WHERE i.usuario.idUsuario = :idUsuario")
    List<IncidenciaEntity> findByUsuario_IdUsuarioConFotos(@Param("idUsuario") Integer idUsuario);

    @Query("SELECT DISTINCT i FROM IncidenciaEntity i LEFT JOIN FETCH i.fotos LEFT JOIN FETCH i.usuario")
    List<IncidenciaEntity> findAllConFotos();

    long countByUsuario_NombreUsuario(String nombreUsuario);
    long countByUsuario_NombreUsuarioAndEstado(String nombreUsuario, IncidenciaEstados estado);
    @Query("SELECT COUNT(i) FROM IncidenciaEntity i WHERE i.usuario.nombreUsuario = :nombreUsuario " +
            "AND i.fecha >= :inicioMes")
    long countDesdeFecha(@Param("nombreUsuario") String nombreUsuario, @Param("inicioMes") LocalDateTime inicioMes);

    // Carga de trabajo por cuadrilla: filas [idCuadrilla, estado, cantidad]
    @Query("SELECT i.cuadrilla.idCuadrilla, i.estado, COUNT(i) FROM IncidenciaEntity i " +
            "WHERE i.cuadrilla IS NOT NULL GROUP BY i.cuadrilla.idCuadrilla, i.estado")
    List<Object[]> contarPorCuadrillaYEstado();

    long countByCuadrilla_IdCuadrilla(Integer idCuadrilla);

    // HU011: Obtener las incidencias del mismo usuario, categoría y descripción para calcular su distancia (50m)
    List<IncidenciaEntity> findByUsuario_NombreUsuarioAndCategoriaAndDescripcion(
            String nombreUsuario, String categoria, String descripcion);
}
