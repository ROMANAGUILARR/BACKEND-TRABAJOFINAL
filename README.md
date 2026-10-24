# ♻️ EcoSolido — Backend API

Backend oficial de **EcoSolido**, una plataforma web para que los ciudadanos reporten incidencias de residuos sólidos y la municipalidad las gestione hasta su resolución. Es una API REST construida con **Spring Boot 3.4.5** y **Java 21** que gestiona usuarios, incidencias, cuadrillas de atención, insignias y recomendaciones ambientales.

---

## 📋 Tabla de Contenidos

- [Enlaces del sistema](#-enlaces-del-sistema)
- [Acceso para el docente](#-acceso-para-el-docente)
- [Funcionalidades del backend](#-funcionalidades-del-backend)
- [Tecnologías utilizadas](#️-tecnologías-utilizadas)
- [Requisitos previos](#-requisitos-previos)
- [Instalación](#-instalación)
- [Configuración de variables de entorno](#️-configuración-de-variables-de-entorno)
- [Ejecutar el backend localmente](#️-ejecutar-el-backend-localmente)
- [Endpoints y documentación de la API](#-endpoints-y-documentación-de-la-api)
- [Base de datos](#️-base-de-datos)
- [Almacenamiento de imágenes](#️-almacenamiento-de-imágenes)
- [Seguridad y autenticación](#-seguridad-y-autenticación)
- [Despliegue en Render](#-despliegue-en-render)
- [Integración con el frontend](#-integración-con-el-frontend)
- [Estado del proyecto](#-estado-del-proyecto)

---

## 🌐 Enlaces del sistema

| Recurso | Enlace |
|---|---|
| Frontend desplegado (Vercel) | https://frontend-trabajofinall.vercel.app |
| Backend desplegado (Render) | https://backend-trabajofinal.onrender.com |
| Documentación Swagger | https://backend-trabajofinal.onrender.com/swagger-ui/index.html |
| Estado del servicio | https://backend-trabajofinal.onrender.com/api/health |
| Repositorio del frontend | https://github.com/ROMANAGUILARR/FRONTEND-TRABAJOFINAL |
| Repositorio del backend | https://github.com/ROMANAGUILARR/BACKEND-TRABAJOFINAL |
| Tablero Jira | https://utp-team-drieg0fg.atlassian.net/jira/software/projects/SCRUM/boards/1 |

---

## 👨‍🏫 Acceso para el docente

El inicio de sesión se hace en el frontend con el **nombre de usuario** y la contraseña.

| Rol | Usuario | Contraseña |
|---|---|---|
| Administrador | `admin` | `Palacios13579` |
| Ciudadano | `GASPER` | `Palacios10` |
| Ciudadano (demostración) | `amendez` | `Andreina2026` |

El tablero de Jira se abre con cualquier correo **@utp.edu.pe**.

---

## 🚀 Funcionalidades del backend

- **Autenticación con JWT**: registro, inicio de sesión y recuperación de contraseña con pregunta de seguridad.
- **Roles**: `ADMIN` para la gestión municipal y `CIUDADANO` para reportar.
- **Documentos de identidad**: DNI (validado con RENIEC), carné de extranjería, pasaporte y PTP.
- **Incidencias**: registro con fotos, ubicación en el mapa y descripción generada por IA (Gemini).
- **Prevención de duplicados**: bloquea un reporte de la misma categoría a 50 m o menos de otro abierto.
- **Cuadrillas**: el administrador asigna cada incidencia a una cuadrilla, con un máximo de 3 en curso.
- **Resolución con evidencia**: ninguna incidencia se cierra sin foto, y cada cambio queda en el historial.
- **Puntos e insignias**: 10 puntos por reporte válido y 5 niveles de insignias.
- **Reportes**: métricas del dashboard y reporte por rango de fechas con el motivo de las no resueltas.
- **Educación ambiental**: recomendaciones generadas por IA.

---

## 🛠️ Tecnologías utilizadas

- Java 21
- Spring Boot 3.4.5 (Web, WebFlux, Data JPA, Validation, Security)
- Spring Security + JWT (jjwt 0.11.5)
- PostgreSQL 17 en Supabase
- Flyway (migraciones versionadas)
- Cloudinary (fotos y evidencias)
- Google Gemini (descripción de fotos y recomendaciones)
- API de RENIEC (validación de DNI)
- springdoc-openapi 2.8.6 (Swagger UI)
- spring-dotenv (variables desde `.env`)
- Maven
- Docker (despliegue en Render)

---

## 📋 Requisitos previos

- Java 21 (JDK)
- Maven 3.9 o superior (o el wrapper `mvnw` incluido)
- Una base de datos PostgreSQL (local o en Supabase)
- Cuenta de Cloudinary
- Git

---

## 📥 Instalación

1. Clonar el repositorio:

```bash
git clone https://github.com/ROMANAGUILARR/BACKEND-TRABAJOFINAL.git
```

2. Entrar a la carpeta del proyecto:

```bash
cd BACKEND-TRABAJOFINAL
```

3. Descargar las dependencias y compilar:

```bash
./mvnw clean install -DskipTests
```

---

## ⚙️ Configuración de variables de entorno

Crea un archivo `.env` en la raíz del proyecto:

```env
DB_URL=jdbc:postgresql://<host-session-pooler>:5432/postgres?sslmode=require
DB_USER=postgres.<id-del-proyecto>
DB_PASSWORD=tu_contraseña
TOKEN_SECRET_VALUE=clave_para_firmar_los_jwt
CLOUDINARY_URL=cloudinary://<api_key>:<api_secret>@<cloud_name>
GEMINI_API_KEY=tu_api_key_de_gemini
RENIEC_API_URL=url_de_la_api_reniec
RENIEC_API_TOKEN=tu_token_reniec
CORS_ORIGENES_EXTRA=https://frontend-trabajofinall.vercel.app
CUADRILLA_MAX_EN_CURSO=3
```

El archivo `.env` está en el `.gitignore` y no se sube al repositorio. Se usa el *Session pooler* de Supabase porque Render no tiene salida IPv6.

---

## ▶️ Ejecutar el backend localmente

```bash
./mvnw spring-boot:run
```

En Windows:

```bash
.\mvnw.cmd spring-boot:run
```

- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui/index.html
- Estado: http://localhost:8080/api/health

---

## 🔌 Endpoints y documentación de la API

| Módulo | Ruta base | Controlador |
|---|---|---|
| Usuarios y autenticación | `/usuario` | `UsuarioController` |
| Incidencias | `/incidencias` | `IncidenciaController` |
| Cuadrillas y trabajadores | `/cuadrillas` | `CuadrillaController` |
| Insignias | `/insignias` | `InsigniaController` |
| Educación ambiental | `/educacion` | `RecomendacionesController` |
| Estado del servicio | `/api/health` | `HealthController` |

Todas las rutas están documentadas en **Swagger UI** (`/swagger-ui/index.html`) y en formato OpenAPI (`/v3/api-docs`). Para probar las rutas protegidas, inicia sesión con `POST /usuario/autenticar`, copia el token y pégalo en el botón **Authorize**.

---

## 🗄️ Base de datos

La base de datos es **PostgreSQL 17 en Supabase**. El esquema se crea y actualiza con **Flyway** (`src/main/resources/db/migration`, migraciones V1 a V11) cada vez que la API arranca, así que no hace falta ejecutar scripts a mano. Las tablas principales son `usuario`, `incidencia`, `incidencia_foto`, `insignia`, `usuario_insignia`, `cuadrilla`, `trabajador`, `incidencia_evidencia` e `incidencia_historial`. Las tablas están publicadas en Supabase Realtime, por lo que los cambios se ven al instante en el Table Editor.

---

## 🖼️ Almacenamiento de imágenes

Las fotos de las incidencias y las evidencias de resolución se suben a **Cloudinary**. En la base de datos solo se guarda la URL de cada imagen. Se admiten hasta 10 MB por archivo y 50 MB por petición.

---

## 🔐 Seguridad y autenticación

La autenticación usa **Spring Security** con tokens **JWT** sin sesión en el servidor. Las contraseñas y las respuestas de seguridad se guardan cifradas con **BCrypt**. Cada petición protegida debe enviar el encabezado `Authorization: Bearer <token>`. Las rutas de gestión (usuarios, cuadrillas, asignar, resolver, editar o eliminar incidencias y el reporte por fechas) solo las puede usar el rol `ADMIN`. Solo el registro, el inicio de sesión, la recuperación de contraseña, Swagger y `/api/health` son públicos.

---

## 🌍 Despliegue en Render

El backend se despliega en **Render** con el `Dockerfile` del repositorio (Java 21, zona horaria `America/Lima`). Las variables de entorno se configuran en el panel de Render. El plan gratuito apaga el servicio tras unos 15 minutos sin uso, así que un *cron job* en cron-job.org llama a `/api/health` cada 10 minutos para mantenerlo activo.

Render construye la imagen con el `Dockerfile`, que ejecuta estos dos pasos:

### Build (compilación)

```bash
mvn -B -q -DskipTests package
```

### Start (arranque)

```bash
java org.springframework.boot.loader.launch.JarLauncher
```

---

## 🔗 Integración con el frontend

El frontend en React + Vite consume esta API mediante `fetch`, usando la variable `VITE_API_URL`. Los orígenes permitidos por CORS son:

- `http://localhost:5173`
- `https://frontend-trabajofinall.vercel.app` (con `CORS_ORIGENES_EXTRA`)

---

## 📌 Estado del proyecto

Proyecto académico **finalizado y desplegado en la nube**: frontend en Vercel, backend en Render y base de datos en Supabase. Incluye las mejoras pedidas por el docente: filtro por rango de fechas, prevención de duplicados entre vecinos, varios tipos de documento e iconografía estandarizada.
