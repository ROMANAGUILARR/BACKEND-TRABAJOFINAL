package com.DisenoProductos.EcoSolido.Config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Documentación interactiva de la API (Swagger UI).
 * Se abre en /swagger-ui.html. Para probar rutas protegidas, inicie sesión en
 * POST /usuario/autenticar, copie el token y péguelo en el botón "Authorize".
 */
@Configuration
public class OpenApiConfig {

    public static final String ESQUEMA_JWT = "bearerAuth";

    @Bean
    public OpenAPI ecoSolidoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API EcoSolido")
                        .version("1.0")
                        .description("Backend para registrar, seguir y gestionar incidencias ambientales. "
                                + "Autenticación con JWT y roles CIUDADANO y ADMIN."))
                .tags(List.of(
                        new Tag().name("Autenticación")
                                .description("Registro, inicio de sesión y recuperación de contraseña (rutas públicas)"),
                        new Tag().name("Usuarios")
                                .description("Gestión de usuarios (solo ADMIN)")))
                .components(new Components()
                        .addSecuritySchemes(ESQUEMA_JWT, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token devuelto por POST /usuario/autenticar")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_JWT));
    }
}
