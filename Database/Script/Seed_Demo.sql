-- ============================================================
-- EcoSolido - Datos semilla (seed) actualizados
-- Contrasenas hasheadas con BCrypt ($2a$)
--
-- Credenciales:
--   admin / Palacios13579     (rol ADMIN)
--   GASPER / Palacios10       (rol GASPER)
-- ============================================================

USE EcoSolido;

-- Limpiar seed previa
DELETE FROM usuario_insignia WHERE id_usuario IN (
  SELECT id_usuario FROM usuario WHERE nombre_usuario IN ('admin', 'ciudadano', 'GASPER')
);
DELETE FROM incidencia_foto WHERE id_incidencia IN (
  SELECT i.id_incidencia FROM incidencia i
  JOIN usuario u ON i.id_ciudadano = u.id_usuario
  WHERE u.nombre_usuario IN ('admin', 'ciudadano', 'GASPER')
);
DELETE FROM incidencia WHERE id_ciudadano IN (
  SELECT id_usuario FROM usuario WHERE nombre_usuario IN ('admin', 'ciudadano', 'GASPER')
);
DELETE FROM usuario WHERE nombre_usuario IN ('admin', 'ciudadano', 'GASPER');

-- ADMIN
INSERT INTO usuario (
  nombre_completo, apellido_completo, dni, telefono,
  correo_electronico, nombre_usuario, contrasena,
  pregunta_seguridad, respuesta_pregunta, puntos, rol
) VALUES (
  'Administrador', 'EcoSolido', '00000000', '999999999',
  'admin@ecosolido.com', 'admin',
  '$2a$10$/fw6DSHNBqOG6YXSwiOQk.87YPFcrAi2l5V9pKN5wH/saexYLJXt6',
  '¿Cuál es el nombre de la mascota?', 'rex',
  0, 'ADMIN'
);

-- GASPER
INSERT INTO usuario (
  nombre_completo, apellido_completo, dni, telefono,
  correo_electronico, nombre_usuario, contrasena,
  pregunta_seguridad, respuesta_pregunta, puntos, rol
) VALUES (
  'Juan', 'Pérez López', '12345678', '987654321',
  'juan@ecosolido.com', 'GASPER',
  '$2a$10$S31TREoqBAQ3HzGxl/BgHem1nifeXhhmeH5QdAscbDv5v3FgPCS5G',
  '¿Cuál es el nombre de la mascota?', 'rex',
  50, 'GASPER'
);

-- Incidencias de ejemplo
SET @id_gasper = (SELECT id_usuario FROM usuario WHERE nombre_usuario = 'GASPER');

INSERT INTO incidencia (descripcion, categoria, estado, fecha, id_ciudadano, titulo, latitud, longitud, direccion_texto)
VALUES
  ('Basura acumulada en la esquina de Av. Principal con Jr. Los Olivos.',
   'Acumulación y falta de recojo', 'PENDIENTE', NOW(), @id_gasper,
   'Basura en Av. Principal', -12.0464, -77.0428, 'Av. Principal 123, Lima'),
  ('Vertido de aguas residuales en el canal del barrio San Martin.',
   'Contaminacion del agua', 'EN_PROCESO', NOW(), @id_gasper,
   'Aguas residuales en canal', -12.0500, -77.0500, 'Jr. San Martin 456, Lima'),
  ('Quema ilegal de residuos en terreno baldio cerca del colegio.',
   'Quema de residuos', 'RESUELTO', NOW(), @id_gasper,
   'Quema ilegal de residuos', -12.0400, -77.0300, 'Calle Los Cedros 789, Lima');