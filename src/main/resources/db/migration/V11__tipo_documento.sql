-- =============================================================
-- V11__tipo_documento.sql — Tipo de documento de identidad del usuario
-- Además del DNI se aceptan carné de extranjería, pasaporte y PTP.
-- Los usuarios existentes quedan con tipo DNI.
-- =============================================================
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS tipo_documento VARCHAR(20) NOT NULL DEFAULT 'DNI';

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_usuario_tipo_documento') THEN
    ALTER TABLE usuario ADD CONSTRAINT chk_usuario_tipo_documento
      CHECK (tipo_documento IN ('DNI', 'CE', 'PASAPORTE', 'PTP'));
  END IF;
END $$;

-- Índice para las consultas de incidencias por rango de fechas
CREATE INDEX IF NOT EXISTS idx_incidencia_fecha ON incidencia (fecha);
