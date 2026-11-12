package com.DisenoProductos.EcoSolido.Repositories;

import com.DisenoProductos.EcoSolido.Models.DTOs.LoginRequestDTO;
import com.DisenoProductos.EcoSolido.Models.Entities.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity,Integer> {
    Optional<UsuarioEntity> findByNombreUsuario(String nombreUsuario);
    Optional<UsuarioEntity> findByCorreoElectronico(String correoElectronico);
    Optional<UsuarioEntity> findByTelefono(String telefono);
    boolean existsByTelefono(String telefono);
    boolean existsByTipoDocumentoAndDni(String tipoDocumento, String dni);
    boolean existsByCorreoElectronico(String correoElectronico);
}
