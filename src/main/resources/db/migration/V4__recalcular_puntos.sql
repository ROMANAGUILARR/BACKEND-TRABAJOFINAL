-- Los puntos equivalen a 10 por cada incidencia registrada.
-- Corrige los puntos iniciales que tenian los usuarios demo.
UPDATE usuario u
SET puntos = (
    SELECT COUNT(*) FROM incidencia i WHERE i.id_ciudadano = u.id_usuario
) * 10;
