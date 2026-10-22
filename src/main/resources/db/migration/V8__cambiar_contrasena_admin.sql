-- Nueva contraseña del usuario administrador.
-- Credenciales: admin / Palacios13579  (hash BCrypt, el mismo formato que usa Spring Security)
UPDATE usuario
SET contrasena = '$2a$10$5wfTs.kCnjKpCKxndcv9.eTjf5dqdLKc.IvlnMJdzUn5qx.7O4Qve'
WHERE nombre_usuario = 'admin';
