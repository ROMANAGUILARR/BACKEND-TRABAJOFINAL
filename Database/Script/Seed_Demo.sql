-- ============================================================
-- EcoSolido — Datos semilla (seed) para demo y desarrollo
-- Contraseñas hasheadas con BCrypt ($2a$) — compatible Spring Security
--
-- Credenciales de prueba:
--   admin / Admin123        (rol ADMIN)
--   ciudadano / Ciudadano123 (rol CIUDADANO)
--
-- Ejecutar DESPUÉS de Creacion_BD.sql y Creacion_Tablas.sql
-- y de haber arrancado el backend al menos una vez (ddl-auto=update
-- crea las columnas nuevas: correo_electronico, respuesta_pregunta,
-- puntos, rol, titulo, latitud, longitud, etc.)
-- ============================================================

USE EcoSolido;

-- Limpiar seed previa (opcional; comentar si hay datos reales)
DELETE FROM usuario_insignia WHERE id_usuario IN (
  SELECT id_usuario FROM usuario WHERE nombre_usuario IN ('admin', 'ciudadano')
);
DELETE FROM incidencia_foto WHERE id_incidencia IN (
  SELECT i.id_incidencia FROM incidencia i
  JOIN usuario u ON i.id_ciudadano = u.id_usuario
  WHERE u.nombre_usuario IN ('admin', 'ciudadano')
);
DELETE FROM incidencia WHERE id_ciudadano IN (
  SELECT id_usuario FROM usuario WHERE nombre_usuario IN ('admin', 'ciudadano')
);
DELETE FROM usuario WHERE nombre_usuario IN ('admin', 'ciudadano');

-- ------------------------------------------------------------
-- ADMIN
-- ------------------------------------------------------------
INSERT INTO usuario (
  nombre_completo, apellido_completo, dni, telefono,
  correo_electronico, nombre_usuario, contrasena,
  pregunta_seguridad, respuesta_pregunta, puntos, rol
) VALUES (
  'Administrador', 'EcoSolido', '00000000', '999999999',
  'admin@ecosolido.com', 'admin',
  '$2a$10$3SYVNMxxjdWO/PFVnCTYAuzSZdgq0k9nkk/wKNtM7cEOZqOtUp9Fe',
  '¿Cuál es el nombre de la mascota?', 'rex',
  0, 'ADMIN'
);

-- ------------------------------------------------------------
-- CIUDADANO
-- ------------------------------------------------------------
INSERT INTO usuario (
  nombre_completo, apellido_completo, dni, telefono,
  correo_electronico, nombre_usuario, contrasena,
  pregunta_seguridad, respuesta_pregunta, puntos, rol
) VALUES (
  'Juan', 'Pérez López', '12345678', '987654321',
  'juan@ecosolido.com', 'ciudadano',
  '$2a$10$LxUe3PXGxJyanqNNW2RFG.LqzsbnnO1.638aCgUJSpv11//0vH4JG',
  '¿Cuál es el nombre de la mascota?', 'rex',
  50, 'CIUDADANO'
);

-- ------------------------------------------------------------
-- Incidencias de ejemplo (vinculadas al ciudadano)
-- ------------------------------------------------------------
SET @id_city = (SELECT id_usuario FROM usuario WHERE nombre_usuario = 'ciudadano');

INSERT INTO incidencia (descripcion, categoria, estado, fecha, id_ciudadano, titulo, latitud, longitud, direccion_texto)
VALUES
  ('Basura acumulada en la esquina de Av. Principal con Jr. Los Olivos, obstruyendo el paso peatonal.',
   'Acumulación y falta de recojo', 'PENDIENTE', NOW(), @id_city,
   'Basura en Av. Principal', -12.0464, -77.0428, 'Av. Principal 123, Lima'),
  ('Vertido de aguas residuales en el canal del barrio San Martin.',
   'Contaminacion del agua', 'EN_PROCESO', NOW(), @id_city,
   'Aguas residuales en canal', -12.0500, -77.0500, 'Jr. San Martin 456, Lima'),
  ('Quema ilegal de residuos en terreno baldio cerca del colegio.',
   'Quema de residuos', 'RESUELTO', NOW(), @id_city,
   'Quema ilegal de residuos', -12.0400, -77.0300, 'Calle Los Cedros 789, Lima');

SELECT
  (SELECT COUNT(*) FROM usuario) AS total_usuarios,
  (SELECT COUNT(*) FROM incidencia) AS total_incidencias,
  (SELECT rol FROM usuario WHERE nombre_usuario = 'admin') AS rol_admin;

-- ============================================================
-- NOTA sobre contrasenas:
-- Si cambias la contrasena, genera un nuevo hash BCrypt:
--   Java:  new BCryptPasswordEncoder().encode("tuPassword")
--   Python: bcrypt.hashpw(b'tuPassword', bcrypt.gensalt(rounds=10, prefix=b'2a')).decode()
-- ============================================================
