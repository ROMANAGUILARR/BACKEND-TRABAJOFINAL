package com.DisenoProductos.EcoSolido.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.DisenoProductos.EcoSolido.Models.DTOs.*;
import com.DisenoProductos.EcoSolido.Models.Entities.UsuarioEntity;
import com.DisenoProductos.EcoSolido.Models.States.IncidenciaEstados;
import com.DisenoProductos.EcoSolido.Services.IncidenciaService;
import com.DisenoProductos.EcoSolido.Services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Incidencias", description = "Registro, seguimiento y gestión de incidencias ambientales")
@RestController
@RequestMapping("/incidencias")
public class IncidenciaController {

    private final IncidenciaService incidenciaService;
    private final UsuarioService usuarioService;
    public IncidenciaController(IncidenciaService incidenciaService, UsuarioService usuarioService) {
        this.incidenciaService = incidenciaService;
        this.usuarioService = usuarioService;
    }
    @Operation(summary = "Lista las incidencias del usuario autenticado")
    @GetMapping("/seguir")
    public ResponseEntity<?> mostrarIncidencias(Authentication authentication) {
        String userName = authentication.getName();
        return ResponseEntity.ok(incidenciaService.mostrarIncidencias(userName));
    }
    @Operation(summary = "Lista todas las incidencias (solo ADMIN)")
    @GetMapping("/mostrarT")
    public ResponseEntity<?> mostrarTodoIncidencias() {
        return ResponseEntity.ok(incidenciaService.mostrarIncidenciasTotales());
    }
    @Operation(summary = "Lista las incidencias de un usuario (solo ADMIN)")
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<?> mostrarIncidenciasPorUsuario(@PathVariable Integer idUsuario) {
        return ResponseEntity.ok(incidenciaService.mostrarIncidenciasPorUsuario(idUsuario));
    }
    @Operation(summary = "Cambia el estado de una incidencia (solo ADMIN)")
    @PatchMapping("/cambiarEstado/{idIncidencia}")
    public ResponseEntity<?> cambiarEstadoIncidencia(@PathVariable Integer idIncidencia, @RequestBody CambiarEstadoDTO body,
                                                     Authentication authentication){
        return ResponseEntity.ok(incidenciaService.cambiarEstado(idIncidencia, body.getEstado(), authentication.getName()));
    }
    @Operation(summary = "Asigna o quita la cuadrilla responsable de una incidencia (solo ADMIN)")
    @PatchMapping("/{idIncidencia}/asignar")
    public ResponseEntity<?> asignarCuadrilla(@PathVariable Integer idIncidencia, @RequestBody AsignarCuadrillaDTO body,
                                              Authentication authentication) {
        return ResponseEntity.ok(incidenciaService.asignarCuadrilla(idIncidencia, body.idCuadrilla(), authentication.getName()));
    }
    @Operation(summary = "Marca la incidencia como resuelta con fotos de evidencia (solo ADMIN)")
    @PostMapping(value = "/{idIncidencia}/resolver", consumes = "multipart/form-data")
    public ResponseEntity<?> resolverIncidencia(@PathVariable Integer idIncidencia,
                                                @RequestPart(value = "fotos", required = false) List<MultipartFile> fotos,
                                                @RequestParam(value = "comentario", required = false) String comentario,
                                                Authentication authentication) throws Exception {
        return ResponseEntity.ok(incidenciaService.resolverConEvidencia(idIncidencia, fotos, comentario, authentication.getName()));
    }
    @Operation(summary = "Historial de cambios de una incidencia (ADMIN o el ciudadano que la registró)")
    @GetMapping("/{idIncidencia}/historial")
    public ResponseEntity<?> historialIncidencia(@PathVariable Integer idIncidencia, Authentication authentication) {
        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        return ResponseEntity.ok(incidenciaService.obtenerHistorial(idIncidencia, authentication.getName(), esAdmin));
    }
    @Operation(summary = "Actualiza los datos de una incidencia (solo ADMIN)")
    @PutMapping("/{idIncidencia}")
    public ResponseEntity<?> actualizarIncidencia(
            @PathVariable Integer idIncidencia,
            @Valid @RequestBody ActualizarIncidenciaDTO body,
            Authentication authentication) {
        return ResponseEntity.ok(incidenciaService.actualizarIncidencia(idIncidencia, body, authentication.getName()));
    }
    @Operation(summary = "Elimina una incidencia (solo ADMIN)")
    @DeleteMapping("/{idIncidencia}")
    public ResponseEntity<?> eliminarIncidencia(@PathVariable Integer idIncidencia) {
        incidenciaService.eliminarIncidencia(idIncidencia);
        return ResponseEntity.noContent().build();
    }
    @Operation(summary = "Registra una incidencia con fotos y ubicación")
    @PostMapping("/registrar")
    public ResponseEntity<?> logroRegistrarIncidencia(
            @RequestPart("incidencia") @Valid IncidenciaRequestDTO incidenciaDTO,
            @RequestPart(value = "fotos", required = false) List<MultipartFile> fotos,
            @RequestParam(value = "urlsFotos", required = false) List<String> urlsFotos,
            Authentication authentication) throws Exception {
        if ((fotos == null || fotos.isEmpty()) && (urlsFotos == null || urlsFotos.isEmpty())) {
            return ResponseEntity.badRequest().body("Debe adjuntar al menos una foto para registrar la incidencia.");
        }
        String nombreUsuario = authentication.getName();

        var nuevasInsignias = incidenciaService.registrarIncidencia(incidenciaDTO, fotos, urlsFotos, nombreUsuario);
        List<Map<String, Object>> nuevasInsigniasDTO = nuevasInsignias.stream()
                .map(insignia -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("nombre", insignia.getNombre());
                    map.put("recompensa", insignia.getRecompensa() != null ? insignia.getRecompensa() : "");
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "mensaje", "Su incidencia ha sido registrada exitosamente...",
                "puntosGanados", incidenciaService.getPuntosPorIncidencia(),
                "nuevasInsignias", nuevasInsigniasDTO
        ));
    }
    @Operation(summary = "Muestra las métricas de incidencias del usuario")
    @GetMapping("/metricas")
    public ResponseEntity<?> mostrarMetricas(Authentication authentication){
        String userName=authentication.getName();
        return ResponseEntity.ok(incidenciaService.obtenerMetricas(userName));
    }
    @Operation(summary = "Genera título y descripción con IA a partir de las fotos")
    @PostMapping("/generar-descripcion")
    public ResponseEntity<?> generarDescripcion(@RequestBody DescribirFotosRequestDTO request){
        if (request.getImagenes() == null || request.getImagenes().isEmpty()) {
            return ResponseEntity.badRequest().body("Debe adjuntar al menos 1 foto para generar una descripción.");
        }
        try {
            Map<String, String> resultado = incidenciaService.generarDescripcionYTitulo(request.getImagenes());
            return ResponseEntity.ok(new DescribirFotosResponseDTO(
                    resultado.get("descripcion"),
                    resultado.get("titulo")
            ));
        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            return ResponseEntity.internalServerError().body("Error IA: " + errorMsg);
        }
    }
    @Operation(summary = "Sube las fotos de una incidencia a Cloudinary")
    @PostMapping("/subir-fotos")
    public ResponseEntity<?> subirFotos(@RequestPart("fotos") List<MultipartFile> fotos) {
        if (fotos == null || fotos.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Debe adjuntar al menos una foto.");
        }
        try {
            List<String> urls = incidenciaService.subirFotos(fotos);
            return ResponseEntity.ok(Map.of("urls", urls));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error al subir las fotos.");
        }
    }
    @Operation(summary = "Obtiene los puntos acumulados del usuario")
    @GetMapping("/puntos")
    public ResponseEntity<?> obtenerPuntos(Authentication authentication) {
        String nombreUsuario = authentication.getName();
        UsuarioEntity usuario = usuarioService.obtenerUsuarioPorNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return ResponseEntity.ok(Map.of("puntos", incidenciaService.sincronizarPuntos(usuario)));
    }
}
