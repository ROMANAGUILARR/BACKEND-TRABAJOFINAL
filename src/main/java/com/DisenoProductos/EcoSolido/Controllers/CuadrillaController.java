package com.DisenoProductos.EcoSolido.Controllers;

import com.DisenoProductos.EcoSolido.Models.DTOs.CuadrillaRequestDTO;
import com.DisenoProductos.EcoSolido.Models.DTOs.TrabajadorRequestDTO;
import com.DisenoProductos.EcoSolido.Services.CuadrillaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Cuadrillas", description = "Equipos de limpieza que atienden las incidencias (solo ADMIN)")
@RestController
@RequestMapping("/cuadrillas")
public class CuadrillaController {
    private final CuadrillaService cuadrillaService;

    public CuadrillaController(CuadrillaService cuadrillaService) {
        this.cuadrillaService = cuadrillaService;
    }

    @Operation(summary = "Lista las cuadrillas con sus integrantes y carga de trabajo")
    @GetMapping
    public ResponseEntity<?> listar() {
        return ResponseEntity.ok(cuadrillaService.listar());
    }

    @Operation(summary = "Crea una cuadrilla")
    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody CuadrillaRequestDTO body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cuadrillaService.crear(body));
    }

    @Operation(summary = "Actualiza nombre, zona o estado (activa) de una cuadrilla")
    @PutMapping("/{idCuadrilla}")
    public ResponseEntity<?> actualizar(@PathVariable Integer idCuadrilla, @Valid @RequestBody CuadrillaRequestDTO body) {
        return ResponseEntity.ok(cuadrillaService.actualizar(idCuadrilla, body));
    }

    @Operation(summary = "Elimina una cuadrilla sin incidencias asignadas")
    @DeleteMapping("/{idCuadrilla}")
    public ResponseEntity<?> eliminar(@PathVariable Integer idCuadrilla) {
        cuadrillaService.eliminar(idCuadrilla);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Agrega un integrante a la cuadrilla")
    @PostMapping("/{idCuadrilla}/trabajadores")
    public ResponseEntity<?> agregarTrabajador(@PathVariable Integer idCuadrilla, @Valid @RequestBody TrabajadorRequestDTO body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cuadrillaService.agregarTrabajador(idCuadrilla, body));
    }

    @Operation(summary = "Actualiza los datos de un integrante")
    @PutMapping("/trabajadores/{idTrabajador}")
    public ResponseEntity<?> actualizarTrabajador(@PathVariable Integer idTrabajador, @Valid @RequestBody TrabajadorRequestDTO body) {
        return ResponseEntity.ok(cuadrillaService.actualizarTrabajador(idTrabajador, body));
    }

    @Operation(summary = "Quita a un integrante de la cuadrilla")
    @DeleteMapping("/trabajadores/{idTrabajador}")
    public ResponseEntity<?> eliminarTrabajador(@PathVariable Integer idTrabajador) {
        cuadrillaService.eliminarTrabajador(idTrabajador);
        return ResponseEntity.noContent().build();
    }
}
