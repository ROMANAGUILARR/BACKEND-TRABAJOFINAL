-- =============================================================
-- V12 — Restricciones de datos en la base de datos
-- Tercera capa de validación: aunque alguien se salte el frontend
-- y el backend, la base de datos rechaza los datos inválidos.
--
-- Cada restricción se agrega solo si los datos que ya existen la
-- cumplen; si alguna fila antigua no la cumple, se deja un aviso
-- (NOTICE) y la migración continúa sin romper el despliegue.
-- =============================================================

CREATE OR REPLACE FUNCTION pg_temp.agregar_check(p_tabla text, p_nombre text, p_condicion text)
RETURNS void LANGUAGE plpgsql AS $fn$
DECLARE
    filas_invalidas bigint;
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = p_nombre) THEN
        RETURN;
    END IF;
    EXECUTE format('SELECT count(*) FROM %I WHERE NOT (%s)', p_tabla, p_condicion) INTO filas_invalidas;
    IF filas_invalidas = 0 THEN
        EXECUTE format('ALTER TABLE %I ADD CONSTRAINT %I CHECK (%s)', p_tabla, p_nombre, p_condicion);
    ELSE
        RAISE NOTICE 'No se agregó %: % fila(s) existentes no la cumplen', p_nombre, filas_invalidas;
    END IF;
END
$fn$;

CREATE OR REPLACE FUNCTION pg_temp.agregar_unico(p_tabla text, p_nombre text, p_columnas text, p_filtro text)
RETURNS void LANGUAGE plpgsql AS $fn$
DECLARE
    duplicados bigint;
BEGIN
    IF EXISTS (SELECT 1 FROM pg_class WHERE relname = p_nombre) THEN
        RETURN;
    END IF;
    EXECUTE format('SELECT count(*) FROM (SELECT 1 FROM %I WHERE %s GROUP BY %s HAVING count(*) > 1) d',
                   p_tabla, p_filtro, p_columnas) INTO duplicados;
    IF duplicados = 0 THEN
        EXECUTE format('CREATE UNIQUE INDEX %I ON %I (%s) WHERE %s', p_nombre, p_tabla, p_columnas, p_filtro);
    ELSE
        RAISE NOTICE 'No se agregó %: hay % valor(es) repetidos', p_nombre, duplicados;
    END IF;
END
$fn$;

-- ---------- usuario ----------
SELECT pg_temp.agregar_check('usuario', 'chk_usuario_nombres',
    $c$char_length(btrim(nombre_completo)) BETWEEN 2 AND 100
       AND char_length(btrim(apellido_completo)) BETWEEN 2 AND 100$c$);

SELECT pg_temp.agregar_check('usuario', 'chk_usuario_nombre_usuario',
    $c$char_length(nombre_usuario) BETWEEN 3 AND 50 AND nombre_usuario ~ '^[A-Za-z0-9._-]+$'$c$);

SELECT pg_temp.agregar_check('usuario', 'chk_usuario_telefono',
    $c$telefono IS NULL OR telefono ~ '^[0-9]{9}$'$c$);

SELECT pg_temp.agregar_check('usuario', 'chk_usuario_correo',
    $c$correo_electronico IS NULL OR correo_electronico ~ '^[^@[:space:]]+@[^@[:space:]]+\.[^@[:space:]]+$'$c$);

SELECT pg_temp.agregar_check('usuario', 'chk_usuario_documento',
    $c$dni IS NULL
       OR (tipo_documento = 'DNI'       AND dni ~ '^[0-9]{8}$')
       OR (tipo_documento = 'CE'        AND dni ~ '^[A-Za-z0-9]{8,12}$')
       OR (tipo_documento = 'PASAPORTE' AND dni ~ '^[A-Za-z0-9]{6,12}$')
       OR (tipo_documento = 'PTP'       AND dni ~ '^[0-9]{9}$')$c$);

SELECT pg_temp.agregar_check('usuario', 'chk_usuario_puntos', $c$puntos >= 0$c$);

SELECT pg_temp.agregar_unico('usuario', 'uk_usuario_correo', 'correo_electronico', 'correo_electronico IS NOT NULL');
SELECT pg_temp.agregar_unico('usuario', 'uk_usuario_telefono', 'telefono', 'telefono IS NOT NULL');
SELECT pg_temp.agregar_unico('usuario', 'uk_usuario_documento', 'tipo_documento, dni', 'dni IS NOT NULL');

-- ---------- incidencia ----------
SELECT pg_temp.agregar_check('incidencia', 'chk_incidencia_categoria',
    $c$categoria IS NULL OR categoria IN (
        'Acumulación y falta de recojo', 'Basura en vía pública', 'Contenedor dañado o lleno',
        'Escombros o materiales de construcción', 'Residuos en parques o áreas verdes', 'Otro')$c$);

SELECT pg_temp.agregar_check('incidencia', 'chk_incidencia_descripcion',
    $c$char_length(btrim(descripcion)) BETWEEN 1 AND 800$c$);

SELECT pg_temp.agregar_check('incidencia', 'chk_incidencia_titulo',
    $c$titulo IS NULL OR char_length(titulo) <= 200$c$);

SELECT pg_temp.agregar_check('incidencia', 'chk_incidencia_coordenadas',
    $c$(latitud IS NULL OR latitud BETWEEN -90 AND 90)
       AND (longitud IS NULL OR longitud BETWEEN -180 AND 180)$c$);

-- ---------- cuadrillas ----------
SELECT pg_temp.agregar_check('cuadrilla', 'chk_cuadrilla_nombre',
    $c$char_length(btrim(nombre)) > 0$c$);

SELECT pg_temp.agregar_check('trabajador', 'chk_trabajador_dni',
    $c$dni IS NULL OR dni = '' OR dni ~ '^[0-9]{8}$'$c$);

SELECT pg_temp.agregar_check('trabajador', 'chk_trabajador_telefono',
    $c$telefono IS NULL OR telefono = '' OR telefono ~ '^[0-9]{9}$'$c$);

-- ---------- insignias ----------
SELECT pg_temp.agregar_check('insignia', 'chk_insignia_requisito', $c$requisito_incidencias > 0$c$);
