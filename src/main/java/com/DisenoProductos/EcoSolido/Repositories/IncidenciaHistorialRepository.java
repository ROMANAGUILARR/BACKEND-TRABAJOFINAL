package com.DisenoProductos.EcoSolido.Repositories;

import com.DisenoProductos.EcoSolido.Models.Entities.IncidenciaHistorialEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidenciaHistorialRepository extends JpaRepository<IncidenciaHistorialEntity, Integer> {
    List<IncidenciaHistorialEntity> findByIncidencia_IdIncidenciaOrderByFechaAscIdHistorialAsc(Integer idIncidencia);
}
