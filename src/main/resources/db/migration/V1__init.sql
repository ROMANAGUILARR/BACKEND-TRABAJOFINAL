-- =============================================================
-- V1__init.sql — Esquema inicial de EcoSolido
-- Basado en el modelo de datos: usuario, incidencia, foto, insignia
-- =============================================================

CREATE TABLE IF NOT EXISTS usuario (
    id_usuario          BIGINT NOT NULL AUTO_INCREMENT,
    nombre_completo     VARCHAR(255) NOT NULL,
    apellido_completo   VARCHAR(255) NOT NULL,
    dni                 VARCHAR(255) DEFAULT NULL,
    telefono            VARCHAR(255) DEFAULT NULL,
    nombre_usuario      VARCHAR(255) NOT NULL,
    contrasena          VARCHAR(255) NOT NULL,
    pregunta_seguridad  VARCHAR(255) DEFAULT NULL,
    correo_electronico  VARCHAR(255) DEFAULT NULL,
    respuesta_pregunta  VARCHAR(255) DEFAULT NULL,
    puntos              INT NOT NULL DEFAULT 0,
    rol                 ENUM('CIUDADANO','ADMIN') DEFAULT 'CIUDADANO',
    PRIMARY KEY (id_usuario),
    UNIQUE KEY uk_usuario_nombre (nombre_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS incidencia (
    id_incidencia   BIGINT NOT NULL AUTO_INCREMENT,
    descripcion     TEXT NOT NULL,
    categoria       VARCHAR(255) DEFAULT NULL,
    estado          ENUM('PENDIENTE','EN_PROCESO','RESUELTO') DEFAULT 'PENDIENTE',
    fecha           DATETIME DEFAULT CURRENT_TIMESTAMP,
    id_ciudadano    BIGINT DEFAULT NULL,
    titulo          VARCHAR(255) DEFAULT NULL,
    latitud         DOUBLE DEFAULT NULL,
    longitud        DOUBLE DEFAULT NULL,
    direccion_texto VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id_incidencia),
    KEY idx_incidencia_ciudadano (id_ciudadano),
    CONSTRAINT fk_incidencia_usuario FOREIGN KEY (id_ciudadano)
        REFERENCES usuario (id_usuario),
    CONSTRAINT chk_estado CHECK (estado IN ('PENDIENTE','EN_PROCESO','RESUELTO'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS incidencia_foto (
    id_foto        BIGINT NOT NULL AUTO_INCREMENT,
    id_incidencia  BIGINT NOT NULL,
    url_foto       VARCHAR(500) DEFAULT NULL,
    public_id      VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id_foto),
    KEY idx_foto_incidencia (id_incidencia),
    CONSTRAINT fk_foto_incidencia FOREIGN KEY (id_incidencia)
        REFERENCES incidencia (id_incidencia)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS insignia (
    id_insignia             INT NOT NULL AUTO_INCREMENT,
    nombre                  VARCHAR(255) NOT NULL,
    descripcion             VARCHAR(1000) NOT NULL,
    icono                   VARCHAR(255) NOT NULL,
    requisito_incidencias   INT NOT NULL,
    recompensa              VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id_insignia),
    UNIQUE KEY uk_insignia_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS usuario_insignia (
    id_usuario_insignia INT NOT NULL AUTO_INCREMENT,
    id_usuario          BIGINT NOT NULL,
    id_insignia         INT NOT NULL,
    fecha_desbloqueo    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_usuario_insignia),
    UNIQUE KEY uk_usuario_insignia (id_usuario, id_insignia),
    KEY idx_ui_insignia (id_insignia),
    CONSTRAINT fk_ui_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario),
    CONSTRAINT fk_ui_insignia FOREIGN KEY (id_insignia)
        REFERENCES insignia (id_insignia)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
