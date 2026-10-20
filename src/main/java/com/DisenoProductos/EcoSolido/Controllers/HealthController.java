package com.DisenoProductos.EcoSolido.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@Tag(name = "Salud del servicio", description = "Comprobación de que el servicio está activo")
@RestController
@RequestMapping("/api")
public class HealthController {

    @Operation(summary = "Verifica que el servicio esté activo")
    @SecurityRequirements
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "servicio", "EcoSolido"
        ));
    }
}
