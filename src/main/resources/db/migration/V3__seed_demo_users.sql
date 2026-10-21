-- =============================================================
-- V3__seed_demo_users.sql — Usuarios de prueba para demo
-- Contraseñas hasheadas con BCrypt
--
-- Credenciales:
--   admin      / Admin123       (rol ADMIN)
--   ciudadano  / Ciudadano123   (rol CIUDADANO)
-- =============================================================

-- ADMIN
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
) ON CONFLICT (nombre_usuario) DO NOTHING;

-- CIUDADANO
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
) ON CONFLICT (nombre_usuario) DO NOTHING;
