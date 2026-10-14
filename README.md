# EcoSolido — Backend

API REST desarrollada en **Java 21** con **Spring Boot 3.4.5** para la plataforma **EcoSolido**: reporte, seguimiento y gestión de incidencias ambientales con gamificación (puntos e insignias) e integración de inteligencia artificial.

---

## Descripción

El backend expone los servicios necesarios para que los ciudadanos registren incidencias ambientales (basura, contaminación, quema de residuos, etc.) con fotos, ubicación geográfica y descripción asistida por IA. Los administradores pueden gestionar el estado de las incidencias, consultar métricas y administrar usuarios.

**Arquitectura por capas:**

```
Controller → Service → Repository → Entity / DTO
                ↓
        Security (JWT) · Integrations (Cloudinary, HuggingFace, RENIEC)
```

---

## Tecnologías

| Componente | Tecnología |
|------------|------------|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.4.5 |
| Seguridad | Spring Security + JWT (jjwt 0.11.5) |
| Persistencia | Spring Data JPA + MySQL |
| Validación | Spring Validation (`@Valid`, `@NotBlank`, etc.) |
| Imágenes | Cloudinary |
| IA | Hugging Face (descripción y títulos) |
| Identidad | API RENIEC (validación de DNI) |
| Contenedores | Docker (multi-stage) |

---

## Estructura del proyecto

```
src/main/java/com/DisenoProductos/EcoSolido/
├── Controllers/          # Endpoints REST
│   ├── UsuarioController
│   ├── IncidenciaController
│   ├── InsigniaController
│   ├── RecomendacionesController
│   └── HealthController
├── Services/             # Lógica de negocio
├── Repositories/         # Acceso a datos (JPA)
├── Models/
│   ├── Entities/         # Entidades JPA
│   ├── DTOs/             # Objetos de transferencia
│   └── States/           # Enums (Rol, IncidenciaEstados)
├── Security/             # JWT, filtros y configuración
├── Integrations/         # Cloudinary, HuggingFace, RENIEC
└── Middleware/           # Manejo global de errores
```

---

## Modelo de datos

| Entidad | Descripción | Relaciones |
|---------|-------------|------------|
| `Usuario` | Ciudadanos y administradores | 1..N incidencias, N..M insignias |
| `Incidencia` | Reporte ambiental | N..1 usuario, 1..N fotos |
| `IncidenciaFoto` | Imágenes en Cloudinary | N..1 incidencia |
| `Insignia` | Recompensas por participación | — |
| `UsuarioInsignia` | Insignias desbloqueadas | N..1 usuario, N..1 insignia |

Estados de incidencia: `PENDIENTE` → `EN_PROCESO` → `RESUELTO`  
Roles: `CIUDADANO`, `ADMIN`

Scripts SQL en `Database/` (creación de BD, tablas, alteraciones, procedimientos y backups).

---

## Endpoints principales

### Autenticación y usuarios (`/usuario`)

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| POST | `/usuario/registrar` | Público | Registro de ciudadano (valida DNI vía RENIEC) |
| POST | `/usuario/autenticar` | Público | Login → devuelve **JWT** |
| GET | `/usuario/listar` | ADMIN | Lista de usuarios registrados |
| POST | `/usuario/verificar-cor-o-tel` | Público | Verifica existencia de correo/teléfono |
| GET | `/usuario/pregunta-seguridad` | Público | Obtiene la pregunta de seguridad |
| POST | `/usuario/verificar-respuesta` | Público | Valida la respuesta de seguridad |
| PATCH | `/usuario/restablecer-contrasena` | Público | Restablece la contraseña (hash BCrypt) |

### Incidencias (`/incidencias`)

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| POST | `/incidencias/registrar` | JWT | Registra incidencia con fotos y geolocalización |
| GET | `/incidencias/seguir` | JWT | Incidencias del usuario autenticado |
| GET | `/incidencias/mostrarT` | ADMIN | Todas las incidencias |
| GET | `/incidencias/usuario/{id}` | ADMIN | Incidencias de un usuario |
| PUT | `/incidencias/{id}` | ADMIN | Actualiza incidencia |
| DELETE | `/incidencias/{id}` | ADMIN | Elimina incidencia |
| PATCH | `/incidencias/cambiarEstado/{id}` | ADMIN | Cambia el estado |
| GET | `/incidencias/metricas` | JWT | Métricas del usuario |
| GET | `/incidencias/puntos` | JWT | Puntos acumulados |
| POST | `/incidencias/generar-descripcion` | JWT | Descripción con IA |
| POST | `/incidencias/subir-fotos` | JWT | Subida a Cloudinary |

### Otros

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/insignias/mis-insignias` | Insignias del usuario |
| POST | `/educacion/recomendaciones` | Recomendaciones ambientales (IA) |
| GET | `/api/health` | Health check |

**Autenticación:** enviar header `Authorization: Bearer <token>`.

---

## Variables de entorno

| Variable | Descripción | Obligatoria |
|----------|-------------|-------------|
| `PORT` | Puerto del servidor (Render lo inyecta) | No (default 8080) |
| `DB_URL` | JDBC URL de MySQL | Sí |
| `DB_USER` | Usuario de la BD | Sí |
| `DB_PASSWORD` | Contraseña de la BD | Sí |
| `TOKEN_SECRET_VALUE` | Secreto para firmar JWT | Sí |
| `HF_TOKEN` | Token de Hugging Face | Sí |
| `RENIEC_API_TOKEN` | Token de API RENIEC | Sí |
| `CLOUDINARY_URL` | URL de Cloudinary | Sí |
| `MYSQLDUMP_PATH` | Ruta a `mysqldump` (backups) | No |

Cree un archivo `.env` en la raíz (o configure las variables en Render):

```env
DB_URL=jdbc:mysql://localhost:3306/EcoSolido
DB_USER=root
DB_PASSWORD=tu_contraseña
TOKEN_SECRET_VALUE=un_secreto_largo_y_aleatorio
HF_TOKEN=hf_xxx
RENIEC_API_TOKEN=xxx
CLOUDINARY_URL=cloudinary://key:secret@cloud
```

---

## Ejecución local

### Requisitos

- Java 21
- Maven (o usar `./mvnw`)
- MySQL 8+

### Pasos

```bash
# 1. Crear la BD y tablas
mysql -u root -p < Database/Script/Creacion_BD.sql
mysql -u root -p < Database/Script/Creacion_Tablas.sql

# 2. Configurar variables de entorno (.env)

# 3. Levantar el backend
./mvnw spring-boot:run
# Windows: .\mvnw.cmd spring-boot:run
```

El servicio queda en `http://localhost:8080`. Health check: `GET /api/health`.

### Con Docker

```bash
docker build -t ecosolido-backend .
docker run -p 8080:8080 --env-file .env ecosolido-backend
```

---

## Despliegue en Render.com

1. Cree un **Web Service** en [Render](https://render.com) apuntando a este repositorio.
2. Runtime: **Docker** (usa el `Dockerfile` multi-stage incluido).
3. Health Check Path: `/api/health`.
4. Configure las variables de entorno de la tabla anterior (marcadas como secret).
5. Render inyecta `PORT`; el backend lo lee via `server.port=${PORT:8080}`.

También puede usar el archivo `render.yaml` (Blueprint) incluido.

**URL esperada:** `https://ecosolido-backend.onrender.com`

---

## Seguridad

- Autenticación con **JWT** (HS256, expiración 8 horas).
- Contraseñas almacenadas con **BCrypt** (nunca en texto plano).
- Endpoints de gestión protegidos con rol **ADMIN**.
- CORS restringido a los orígenes del frontend.
- Validación de datos de entrada en registro y operaciones CRUD.
- Manejo centralizado de errores (`@RestControllerAdvice`).

---

## Pruebas rápidas

```bash
# Health
curl http://localhost:8080/api/health

# Registro
curl -X POST http://localhost:8080/usuario/registrar \
  -H "Content-Type: application/json" \
  -d '{"nombreCompleto":"Juan","apellidoCompleto":"Perez","dni":"12345678",
       "telefono":"999888777","correoElectronico":"juan@mail.com",
       "nombreUsuario":"juanp","contrasena":"secret123",
       "preguntaSeguridad":"¿Mascota?","respuestaPregunta":"rex"}'

# Login
curl -X POST http://localhost:8080/usuario/autenticar \
  -H "Content-Type: application/json" \
  -d '{"nombreUsuario":"juanp","contrasena":"secret123"}'
```

---

## Autor

Proyecto académico — Universidad Tecnológica del Perú (UTP)  
Ciclo VIII — Herramientas de Desarrollo
Desarrollado por [ROMANAGUILARR](https://github.com/ROMANAGUILARR)
