-- Cifra con BCrypt las respuestas de seguridad que aun estan en texto plano.
-- Se normalizan igual que en el backend (minusculas, sin espacios al inicio/fin)
-- para que los usuarios sigan recuperando su cuenta con la misma respuesta.
-- pgcrypto ya viene activo en Supabase (esquema "extensions"); esto lo asegura en otros entornos.
CREATE SCHEMA IF NOT EXISTS extensions;
CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA extensions;

UPDATE usuario
SET respuesta_pregunta = extensions.crypt(lower(btrim(respuesta_pregunta)), extensions.gen_salt('bf', 10))
WHERE respuesta_pregunta IS NOT NULL
  AND respuesta_pregunta !~ '^\$2[aby]\$';
