package com.DisenoProductos.EcoSolido.Repositories;

import com.DisenoProductos.EcoSolido.Models.Entities.TrabajadorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrabajadorRepository extends JpaRepository<TrabajadorEntity, Integer> {
    List<TrabajadorEntity> findByCuadrilla_IdCuadrillaAndEsJefeTrue(Integer idCuadrilla);
}
