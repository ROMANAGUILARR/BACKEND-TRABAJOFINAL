-- Elimina el usuario de prueba Juan Perez Lopez (usuario 'ciudadano', DNI 12345678)
-- junto con sus fotos, incidencias e insignias.
DELETE FROM incidencia_foto f
USING incidencia i, usuario u
WHERE f.id_incidencia = i.id_incidencia
  AND i.id_ciudadano = u.id_usuario
  AND u.nombre_usuario = 'ciudadano' AND u.dni = '12345678' AND u.correo_electronico = 'juan@ecosolido.com';

DELETE FROM incidencia i
USING usuario u
WHERE i.id_ciudadano = u.id_usuario
  AND u.nombre_usuario = 'ciudadano' AND u.dni = '12345678' AND u.correo_electronico = 'juan@ecosolido.com';

DELETE FROM usuario_insignia ui
USING usuario u
WHERE ui.id_usuario = u.id_usuario
  AND u.nombre_usuario = 'ciudadano' AND u.dni = '12345678' AND u.correo_electronico = 'juan@ecosolido.com';

DELETE FROM usuario
WHERE nombre_usuario = 'ciudadano' AND dni = '12345678' AND correo_electronico = 'juan@ecosolido.com';
