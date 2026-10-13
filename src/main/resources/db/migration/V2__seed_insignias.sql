-- =============================================================
-- V2__seed_insignias.sql — Insignias oficiales del sistema
-- Coinciden EXACTAMENTE con InsigniaService.inicializarInsignias()
-- =============================================================

INSERT IGNORE INTO insignia (nombre, descripcion, icono, requisito_incidencias, recompensa) VALUES
('Primer reporte',
 '¡Felicidades! Has registrado tu primera incidencia y comenzado a transformar tu comunidad.',
 '🌱', 1,
 'Bono de S/20 en tu billetera digital para canjear en mercados locales.'),
('Reportero activo',
 'Has registrado 5 incidencias. Tu compromiso con el medio ambiente es notable.',
 '📢', 5,
 'Bono de S/50 en tu billetera digital + recarga de 10 GB móviles.'),
('Guardián del barrio',
 'Has registrado 10 incidencias. Eres un referente de cuidado ambiental en tu zona.',
 '🛡️', 10,
 'Vale de S/100 en canasta familiar + reconocimiento público en redes sociales.'),
('EcoHéroe',
 'Has registrado 15 incidencias. Tu dedicación es inspiradora para toda la comunidad.',
 '🏆', 15,
 'Vale de S/200 en canasta familiar + kit de productos ecológicos para el hogar.'),
('Embajador EcoSólido',
 'Has registrado 20 incidencias. Eres un embajador del cambio ambiental.',
 '🌍', 20,
 'Vale de S/500 en canasta familiar + kit EcoSólido + certificado de Embajador Ambiental.');
