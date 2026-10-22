# EcoSolido — Backend

API REST de EcoSolido construida con Spring Boot 3.4.5 y Java 21. Gestiona usuarios, incidencias de residuos sólidos, cuadrillas de atención, insignias y recomendaciones ambientales.

## Despliegue en la nube

| Parte | Plataforma | URL |
|---|---|---|
| Backend (esta API) | Render (Docker) | https://backend-trabajofinal.onrender.com |
| Base de datos | Supabase (PostgreSQL 17) | Proyecto de Supabase del equipo |
| Frontend | Vercel | https://frontend-trabajofinall.vercel.app |

- Estado del servicio: `GET /api/health`
- Documentación interactiva: `/swagger-ui/index.html`

El plan gratuito de Render apaga el servicio tras unos 15 minutos sin uso; la primera petición después de eso tarda alrededor de un minuto en responder.

## Base de datos en Supabase

La base de datos está en Supabase y se crea con las migraciones de Flyway (`src/main/resources/db/migration`, V1 a V10) cada vez que la API arranca. No usa Supabase Auth ni el SDK de Supabase en el frontend: el frontend solo habla con esta API y la API se conecta a Supabase por JDBC.

Las tablas están publicadas en Supabase Realtime (`supabase_realtime`), por lo que los cambios hechos desde la aplicación se ven al instante en el Table Editor:

```
usuario, incidencia, incidencia_foto, insignia, usuario_insignia,
cuadrilla, trabajador, incidencia_evidencia, incidencia_historial
```

Variables de entorno en Render:

| Variable | Uso |
|---|---|
| `DB_URL` | `jdbc:postgresql://<host-session-pooler>:5432/postgres?sslmode=require` |
| `DB_USER` | `postgres.<id-del-proyecto>` |
| `DB_PASSWORD` | Contraseña de la base de datos |
| `TOKEN_SECRET_VALUE` | Clave para firmar los JWT |
| `CLOUDINARY_URL` | Subida de fotos y evidencias |
| `CORS_ORIGENES_EXTRA` | Frontends adicionales permitidos, separados por coma |
| `CUADRILLA_MAX_EN_CURSO` | Incidencias en curso por cuadrilla (por defecto `3`) |

Se usa el *Session pooler* de Supabase porque Render no tiene salida IPv6.

## Atención de incidencias por cuadrillas

La atención de incidencias está desplegada en la API de Render con los datos en Supabase. Los trabajadores no tienen cuenta en el sistema: el administrador registra las cuadrillas, asigna cada incidencia a una cuadrilla y la marca como resuelta subiendo fotos de evidencia del trabajo.

Endpoints principales:

```
GET    /cuadrillas
POST   /cuadrillas
PATCH  /incidencias/{idIncidencia}/asignar
POST   /incidencias/{idIncidencia}/resolver
GET    /incidencias/{idIncidencia}/historial
```

Los endpoints reciben el token JWT del inicio de sesión en `Authorization: Bearer <token>`. Para asignar, el cuerpo debe incluir el identificador de la cuadrilla:

```json
{
  "idCuadrilla": 3
}
```

Para resolver se envía `multipart/form-data` con 1 a 5 fotos en el campo `fotos` y un `comentario` opcional.

Ejemplo desde el frontend (`src/services/cuadrillasApi.js`):

```js
await asignarCuadrilla(idIncidencia, 3);

await resolverIncidencia(idIncidencia, archivos, "Punto limpiado y desinfectado");

const historial = await obtenerHistorial(idIncidencia);
```

Las tablas remotas son `cuadrilla`, `trabajador`, `incidencia_evidencia` e `incidencia_historial`. La API valida que el usuario autenticado sea ADMIN, que la cuadrilla esté activa y no tenga más de 3 incidencias en curso, y que la incidencia tenga cuadrilla asignada y evidencia antes de marcarse como resuelta. El historial registra cada cambio con su fecha y hora de Lima; el ciudadano lo ve en el seguimiento de su incidencia.

