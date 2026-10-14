-- Usuario Itzair Roman (GASPER).
-- 1) Si GASPER quedo con los datos de prueba de Juan Perez Lopez, se le ponen los datos de Itzair.
-- 2) Se eliminan otros restos del usuario de prueba Juan Perez Lopez (que no sean GASPER).
-- 3) Si GASPER no existe, se crea (contrasena: Palacios10).

UPDATE usuario
SET nombre_completo = 'Itzair',
    apellido_completo = 'Roman',
    dni = '72844283',
    correo_electronico = 'itzairplus@gmail.com'
WHERE nombre_usuario = 'GASPER'
  AND (dni = '12345678' OR correo_electronico = 'juan@ecosolido.com');

DELETE f FROM incidencia_foto f
  JOIN incidencia i ON f.id_incidencia = i.id_incidencia
  JOIN usuario u ON i.id_ciudadano = u.id_usuario
WHERE u.nombre_usuario <> 'GASPER' AND u.dni = '12345678' AND u.correo_electronico = 'juan@ecosolido.com';

DELETE i FROM incidencia i
  JOIN usuario u ON i.id_ciudadano = u.id_usuario
WHERE u.nombre_usuario <> 'GASPER' AND u.dni = '12345678' AND u.correo_electronico = 'juan@ecosolido.com';

DELETE ui FROM usuario_insignia ui
  JOIN usuario u ON ui.id_usuario = u.id_usuario
WHERE u.nombre_usuario <> 'GASPER' AND u.dni = '12345678' AND u.correo_electronico = 'juan@ecosolido.com';

DELETE FROM usuario
WHERE nombre_usuario <> 'GASPER' AND dni = '12345678' AND correo_electronico = 'juan@ecosolido.com';

INSERT INTO usuario (
  nombre_completo, apellido_completo, dni, telefono,
  correo_electronico, nombre_usuario, contrasena,
  pregunta_seguridad, respuesta_pregunta, puntos, rol
)
SELECT 'Itzair', 'Roman', '72844283',
       -- el telefono solo se asigna si ningun otro usuario lo tiene
       CASE WHEN EXISTS (SELECT 1 FROM (SELECT telefono FROM usuario) t WHERE t.telefono = '987654321')
            THEN NULL ELSE '987654321' END,
       'itzairplus@gmail.com', 'GASPER',
       '$2a$10$S31TREoqBAQ3HzGxl/BgHem1nifeXhhmeH5QdAscbDv5v3FgPCS5G',
       '¿Cuál es el nombre de tu primera mascota?', 'rex',
       0, 'CIUDADANO'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT nombre_usuario FROM usuario) u WHERE u.nombre_usuario = 'GASPER');

-- Puntos segun incidencias registradas
UPDATE usuario u
SET u.puntos = (SELECT COUNT(*) FROM incidencia i WHERE i.id_ciudadano = u.id_usuario) * 10
WHERE u.nombre_usuario = 'GASPER';
