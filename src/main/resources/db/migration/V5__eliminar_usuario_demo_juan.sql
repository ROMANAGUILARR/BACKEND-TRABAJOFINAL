-- Elimina el usuario de prueba Juan Perez Lopez (usuario 'ciudadano', DNI 12345678)
-- junto con sus fotos, incidencias e insignias.
DELETE f FROM incidencia_foto f
  JOIN incidencia i ON f.id_incidencia = i.id_incidencia
  JOIN usuario u ON i.id_ciudadano = u.id_usuario
WHERE u.nombre_usuario = 'ciudadano' AND u.dni = '12345678' AND u.correo_electronico = 'juan@ecosolido.com';

DELETE i FROM incidencia i
  JOIN usuario u ON i.id_ciudadano = u.id_usuario
WHERE u.nombre_usuario = 'ciudadano' AND u.dni = '12345678' AND u.correo_electronico = 'juan@ecosolido.com';

DELETE ui FROM usuario_insignia ui
  JOIN usuario u ON ui.id_usuario = u.id_usuario
WHERE u.nombre_usuario = 'ciudadano' AND u.dni = '12345678' AND u.correo_electronico = 'juan@ecosolido.com';

DELETE FROM usuario
WHERE nombre_usuario = 'ciudadano' AND dni = '12345678' AND correo_electronico = 'juan@ecosolido.com';
