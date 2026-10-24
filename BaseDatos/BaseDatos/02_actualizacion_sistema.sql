-- ============================================================
-- La Doñita Gourmet - Actualización del sistema (versión 100 %)
-- Ejecutar DESPUÉS de 01_base_datos_ladonita.sql. Se puede ejecutar más de una vez.
--   mysql -u root -p < sql/02_actualizacion_sistema.sql
-- ============================================================
USE ladonita_gourmet;

-- 1. Columna foto_url en Producto (la usa el catálogo y el mantenimiento de productos)
SET @existe := (SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'producto' AND COLUMN_NAME = 'foto_url');
SET @sql := IF(@existe = 0, 'ALTER TABLE Producto ADD COLUMN foto_url VARCHAR(255) NULL AFTER precio', 'SELECT 1');
PREPARE paso FROM @sql; EXECUTE paso; DEALLOCATE PREPARE paso;

-- 2. Índices para las búsquedas del panel y los reportes
SET @existe := (SELECT COUNT(*) FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'pedido' AND INDEX_NAME = 'idx_pedido_estado');
SET @sql := IF(@existe = 0, 'CREATE INDEX idx_pedido_estado ON Pedido (estado_pedido)', 'SELECT 1');
PREPARE paso FROM @sql; EXECUTE paso; DEALLOCATE PREPARE paso;
SET @existe := (SELECT COUNT(*) FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'pedido' AND INDEX_NAME = 'idx_pedido_fecha');
SET @sql := IF(@existe = 0, 'CREATE INDEX idx_pedido_fecha ON Pedido (fecha_pedido)', 'SELECT 1');
PREPARE paso FROM @sql; EXECUTE paso; DEALLOCATE PREPARE paso;
SET @existe := (SELECT COUNT(*) FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = 'cliente' AND INDEX_NAME = 'idx_cliente_telefono');
SET @sql := IF(@existe = 0, 'CREATE INDEX idx_cliente_telefono ON Cliente (telefono)', 'SELECT 1');
PREPARE paso FROM @sql; EXECUTE paso; DEALLOCATE PREPARE paso;

-- 3. Contraseñas de los usuarios iniciales
-- Usuarios iniciales: contraseñas con PBKDF2 (Admin2026! / Empleado2026! / Reparto2026!)
-- CÁMBIALAS desde 'Mi perfil' apenas entres por primera vez.
UPDATE Usuario SET `contraseña` = 'pbkdf2$120000$xmfxknFQS+6/rKNZwSlVUw==$C8zQ4AUzdxS3LhPbvQZp8kJ/cGtsXHUNMM0bP6a3lW0=' WHERE usuario = 'admin_donita';
UPDATE Usuario SET `contraseña` = 'pbkdf2$120000$T0brEPzvIHINiGVPCISQAg==$KukfENOKpS6wDO40MCblerNA3fb5UWfg4JXjni857xE=' WHERE usuario = 'cperez_emp';
UPDATE Usuario SET `contraseña` = 'pbkdf2$120000$AR+4jyPsOhbsH57RG/Fuug==$hWkMejwz7jLcYoZOrJaQ0y4ATEvZiL747gIxFliomJc=' WHERE usuario = 'jsalas_rep';
