package com.DisenoProductos.EcoSolido.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import com.DisenoProductos.EcoSolido.Models.DTOs.*;
import com.DisenoProductos.EcoSolido.Services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Registra un usuario validando su DNI", tags = "Autenticación")
    @SecurityRequirements
    @PostMapping("/registrar")
    public ResponseEntity<?> registrarUsuario(@Valid @RequestBody CrearUsuarioRequestDTO usuarioRequestDTO) {
        try {
            CrearUsuarioResponseDTO response = usuarioService.registrarUsuario(usuarioRequestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }

    @Operation(summary = "Inicia sesión y devuelve el token JWT", tags = "Autenticación")
    @SecurityRequirements
    @PostMapping("/autenticar")
    public ResponseEntity<?> autenticarUsuario(@Valid @RequestBody LoginRequestDTO requestDTO) {
        try {
            LoginResponseDTO response = usuarioService.autenticarUsuario(
                    requestDTO.getNombreUsuario(),
                    requestDTO.getContrasena()
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }

    @Operation(summary = "Lista los usuarios registrados (solo ADMIN)", tags = "Usuarios")
    @GetMapping("/listar")
    public ResponseEntity<List<UsuarioListarDTO>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

    @Operation(summary = "Edita los datos de un usuario (solo ADMIN)", tags = "Usuarios")
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioListarDTO> actualizarUsuario(@PathVariable Integer id,
                                                              @Valid @RequestBody ActualizarUsuarioDTO dto) {
        return ResponseEntity.ok(usuarioService.actualizarUsuario(id, dto));
    }

    @Operation(summary = "Elimina un usuario (solo ADMIN)", tags = "Usuarios")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Integer id, Authentication authentication) {
        usuarioService.eliminarUsuario(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Verifica que el correo o teléfono esté registrado", tags = "Autenticación")
    @SecurityRequirements
    @PostMapping("/verificar-cor-o-tel")
    public ResponseEntity<?> verificarCorrOTel(@RequestBody VerificarCorreoTelRequestDTO requestDTO) {
        VerificarCorreoTelResponseDTO response = usuarioService.verificarCorreoOTelefono(requestDTO);
        if (!response.isEstaRegistrado()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No existe una cuenta registrada con ese correo o teléfono");
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtiene la pregunta de seguridad del usuario", tags = "Autenticación")
    @SecurityRequirements
    @GetMapping("/pregunta-seguridad")
    public ResponseEntity<?> preguntaSeguridad(
            @RequestParam(required = false) String correo,
            @RequestParam(required = false) String telefono) {
        try {
            VerificarCorreoTelRequestDTO requestDTO = new VerificarCorreoTelRequestDTO();
            requestDTO.setCorreo(correo);
            requestDTO.setTelefono(telefono);

            String pregunta = usuarioService.obtenerPreguntaSeguridad(requestDTO);
            return ResponseEntity.ok(Map.of("pregunta", pregunta));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @Operation(summary = "Valida la respuesta a la pregunta de seguridad", tags = "Autenticación")
    @SecurityRequirements
    @PostMapping("/verificar-respuesta")
    public ResponseEntity<?> verificarRespuesta(@RequestBody RespuestaSeguridadRequestDTO requestDTO2){
        try {
            RespuestaSeguridadResponseDTO response = usuarioService.verificarRespuesta(requestDTO2);
            if (!response.isCoinciden()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La respuesta de seguridad es incorrecta");
            }
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @Operation(summary = "Restablece la contraseña del usuario", tags = "Autenticación")
    @SecurityRequirements
    @PatchMapping("/restablecer-contrasena")
    public ResponseEntity<?> restablecerContrasena(@RequestBody RestablecerContrasenaRequestDTO requestDTO3) {
        try {
            RestablecerContrasenaResponseDTO response = usuarioService.restablecerContrasena(requestDTO3);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
