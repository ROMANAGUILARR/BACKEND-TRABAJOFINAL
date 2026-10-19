package com.DisenoProductos.EcoSolido.Middleware;

import com.DisenoProductos.EcoSolido.Services.HuggingFaceException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationsErrors(MethodArgumentNotValidException ex){
        String mensaje = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("Error de validación");
        return ResponseEntity.badRequest().body(mensaje);
    }
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(404)
                .body(Map.of(
                    "error", "No encontrado",
                    "message", ex.getMessage() != null ? ex.getMessage() : "Recurso no encontrado."
                ));
    }
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(org.springframework.security.access.AccessDeniedException ex) {
        return ResponseEntity.status(403)
                .body(Map.of(
                    "error", "Acceso denegado",
                    "message", "No tienes permisos para realizar esta acción."
                ));
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", "Solicitud inválida",
                    "message", ex.getMessage() != null ? ex.getMessage() : "Datos inválidos."
                ));
    }
    @ExceptionHandler(HuggingFaceException.class)
    public ResponseEntity<?> handleIAError(HuggingFaceException ex) {
        return ResponseEntity.status(503)
                .body(Map.of(
                    "error", "Error de IA",
                    "message", ex.getMessage() != null ? ex.getMessage() : 
                        "Lo siento, no pude ver muy bien las fotos. ¿Podrías volver a intentarlo?"
                ));
    }
    
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalStateError(IllegalStateException ex) {
        return ResponseEntity.status(500)
                .body(Map.of(
                    "error", "Error del servicio",
                    "message", ex.getMessage() != null ? ex.getMessage() : "Ocurrió un error inesperado."
                ));
    }
    
    @ExceptionHandler(com.DisenoProductos.EcoSolido.Services.DuplicateIncidentException.class)
    public ResponseEntity<?> handleDuplicateIncidentError(com.DisenoProductos.EcoSolido.Services.DuplicateIncidentException ex) {
        return ResponseEntity.status(409)
                .body(Map.of(
                    "error", "Conflicto",
                    "message", ex.getMessage()
                ));
    }
    
    @ExceptionHandler(com.DisenoProductos.EcoSolido.Services.ConflictoException.class)
    public ResponseEntity<?> handleConflicto(com.DisenoProductos.EcoSolido.Services.ConflictoException ex) {
        return ResponseEntity.status(409)
                .body(Map.of(
                    "error", "Conflicto",
                    "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericError(Exception ex) {
        return ResponseEntity.internalServerError()
                .body(Map.of(
                    "error", "Error interno",
                    "message", ex.getMessage() != null ? ex.getMessage() : "Ocurrió un error inesperado."
                ));
    }
}
