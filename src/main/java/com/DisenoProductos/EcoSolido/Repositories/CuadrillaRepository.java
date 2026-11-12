package com.DisenoProductos.EcoSolido.Repositories;

import com.DisenoProductos.EcoSolido.Models.Entities.CuadrillaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CuadrillaRepository extends JpaRepository<CuadrillaEntity, Integer> {
    // Trae cada cuadrilla con sus integrantes en una sola consulta
    @Query("SELECT DISTINCT c FROM CuadrillaEntity c LEFT JOIN FETCH c.trabajadores ORDER BY c.nombre")
    List<CuadrillaEntity> findAllConTrabajadores();

    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdCuadrillaNot(String nombre, Integer idCuadrilla);
}
