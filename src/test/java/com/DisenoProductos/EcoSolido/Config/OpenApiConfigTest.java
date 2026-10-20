package com.DisenoProductos.EcoSolido.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiConfigTest {

    private final OpenAPI openAPI = new OpenApiConfig().ecoSolidoOpenAPI();

    @Test
    void documentaElTituloDeLaApi() {
        assertEquals("API EcoSolido", openAPI.getInfo().getTitle());
    }

    @Test
    void declaraElEsquemaBearerJwt() {
        SecurityScheme esquema = openAPI.getComponents().getSecuritySchemes().get(OpenApiConfig.ESQUEMA_JWT);

        assertNotNull(esquema);
        assertEquals(SecurityScheme.Type.HTTP, esquema.getType());
        assertEquals("bearer", esquema.getScheme());
        assertEquals("JWT", esquema.getBearerFormat());
    }

    @Test
    void exigeElEsquemaJwtPorDefecto() {
        assertTrue(openAPI.getSecurity().get(0).containsKey(OpenApiConfig.ESQUEMA_JWT));
    }
}
