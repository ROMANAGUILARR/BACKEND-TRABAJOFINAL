-- =============================================================
-- V7__habilitar_realtime.sql — Activa Supabase Realtime en todas las tablas
-- (no hace nada si la base no es Supabase)
-- =============================================================

DO $$
DECLARE t TEXT;
BEGIN
  IF EXISTS (SELECT 1 FROM pg_publication WHERE pubname = 'supabase_realtime') THEN
    FOREACH t IN ARRAY ARRAY['usuario','incidencia','incidencia_foto','insignia','usuario_insignia'] LOOP
      IF NOT EXISTS (SELECT 1 FROM pg_publication_tables
                     WHERE pubname = 'supabase_realtime' AND schemaname = 'public' AND tablename = t) THEN
        EXECUTE format('ALTER PUBLICATION supabase_realtime ADD TABLE public.%I', t);
      END IF;
    END LOOP;
  END IF;
END $$;
