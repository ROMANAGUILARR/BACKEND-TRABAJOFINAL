# Registro de cambios

Formato de versiones: MAJOR.MINOR.PATCH (versionado semántico).

- MAJOR: cambios incompatibles con la API anterior.
- MINOR: funcionalidades nuevas compatibles.
- PATCH: correcciones de errores.

## [1.0.0] - 2026-10-19

### Añadido
- API REST con Spring Boot 3.4.5 y Java 21, organizada en capas.
- Modelo de datos versionado con 6 migraciones Flyway.
- Autenticación con JWT de 8 horas, contraseñas con BCrypt y rutas exclusivas del ADMIN.
- CRUD de incidencias con validaciones y códigos HTTP adecuados.
- Edición y eliminación de usuarios para el ADMIN.
- Insignias, puntos y recomendaciones ambientales con IA.
- Documentación interactiva con Swagger UI en `/swagger-ui.html`.
- Despliegue en Render con Docker y `render.yaml`.
- Pruebas unitarias de `UsuarioService`, `JwtUtil` y `OpenApiConfig`.

### Corregido
- Los residuos aprovechables van en el contenedor verde (NTP 900.058:2019).
- Ortografía en comentarios y textos del código.
