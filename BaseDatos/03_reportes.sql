-- ============================================================
-- Reportes R01–R08 del informe (Tabla 11) como VISTAS de MySQL.
-- Opcional: el sistema web calcula los mismos reportes por su cuenta (/admin/reportes);
-- estas vistas sirven para consultarlos desde MySQL Workbench. Ejecutar DESPUÉS de 01 y 02.
--   SELECT * FROM v_r02_pedidos_pendientes;
-- Basado en BaseDatos/reportes.sql del repositorio (nombres de tabla como en el esquema oficial).
-- ============================================================
USE ladonita_gourmet;

-- R01 Reporte de ventas (diario y mensual) — Administrador
CREATE OR REPLACE VIEW v_r01_ventas_diarias AS
SELECT DATE(fecha_pedido) AS fecha, COUNT(*) AS cantidad_pedidos, SUM(total_pedido) AS monto_total
FROM Pedido GROUP BY DATE(fecha_pedido) ORDER BY fecha DESC;

CREATE OR REPLACE VIEW v_r01_ventas_mensuales AS
SELECT DATE_FORMAT(fecha_pedido, '%Y-%m') AS mes, COUNT(*) AS cantidad_pedidos, SUM(total_pedido) AS monto_total
FROM Pedido GROUP BY DATE_FORMAT(fecha_pedido, '%Y-%m') ORDER BY mes DESC;

-- R02 Pedidos pendientes y en preparación — Administrador, Empleado
CREATE OR REPLACE VIEW v_r02_pedidos_pendientes AS
SELECT p.id_pedido, p.fecha_pedido, p.estado_pedido, p.tipo_pedido, p.metodo_entrega,
       CONCAT(c.nombres, ' ', c.apellidos) AS cliente, c.telefono, p.total_pedido
FROM Pedido p JOIN Cliente c ON c.id_cliente = p.id_cliente
WHERE p.estado_pedido IN ('pendiente', 'en preparación')
ORDER BY p.fecha_pedido;

-- R03 Productos más solicitados — Administrador
CREATE OR REPLACE VIEW v_r03_productos_mas_solicitados AS
SELECT pr.id_producto, pr.nombre_producto, SUM(d.cantidad) AS unidades_solicitadas,
       COUNT(DISTINCT d.id_pedido) AS pedidos, SUM(d.subtotal) AS monto
FROM DetallePedido d JOIN Producto pr ON pr.id_producto = d.id_producto
GROUP BY pr.id_producto, pr.nombre_producto
ORDER BY unidades_solicitadas DESC;

-- R04 Inventario / stock — Administrador (umbral de stock bajo: 10 unidades)
CREATE OR REPLACE VIEW v_r04_inventario AS
SELECT id_producto, nombre_producto, stock_disponible,
       CASE WHEN stock_disponible = 0 THEN 'agotado'
            WHEN stock_disponible <= 10 THEN 'bajo'
            ELSE 'disponible' END AS nivel_stock
FROM Producto WHERE estado = 1 ORDER BY stock_disponible;

-- R05 Clientes, frecuencia de compra y monto facturado — Administrador
CREATE OR REPLACE VIEW v_r05_clientes AS
SELECT c.id_cliente, CONCAT(c.nombres, ' ', c.apellidos) AS cliente, c.telefono,
       COUNT(p.id_pedido) AS cantidad_pedidos, COALESCE(SUM(p.total_pedido), 0) AS monto_total,
       MAX(p.fecha_pedido) AS ultimo_pedido
FROM Cliente c LEFT JOIN Pedido p ON p.id_cliente = c.id_cliente
GROUP BY c.id_cliente, c.nombres, c.apellidos, c.telefono
ORDER BY monto_total DESC;

-- R06 Reservas de catering — Administrador, Empleado
CREATE OR REPLACE VIEW v_r06_reservas_catering AS
SELECT p.id_pedido, e.fecha_evento, e.hora_evento, e.tipo_servicio, e.cantidad_personas,
       e.direccion_evento, p.estado_pedido, CONCAT(c.nombres, ' ', c.apellidos) AS cliente, c.telefono
FROM Pedido p JOIN DatosEvento e ON e.id_pedido = p.id_pedido JOIN Cliente c ON c.id_cliente = p.id_cliente
ORDER BY e.fecha_evento, e.hora_evento;

-- R07 Entregas asignadas a repartidores (pendientes de entregar) — Repartidor
--   SELECT * FROM v_r07_entregas_asignadas WHERE id_repartidor = 'USR003';
CREATE OR REPLACE VIEW v_r07_entregas_asignadas AS
SELECT p.id_repartidor, p.id_pedido, p.estado_pedido, p.direccion_entrega, p.fecha_entrega_estimada,
       CONCAT(c.nombres, ' ', c.apellidos) AS cliente, c.telefono
FROM Pedido p JOIN Cliente c ON c.id_cliente = p.id_cliente
WHERE p.metodo_entrega = 'delivery' AND p.id_repartidor IS NOT NULL AND p.estado_pedido <> 'entregado'
ORDER BY p.fecha_entrega_estimada;

-- R08 Historial de pedidos entregados — Administrador, Repartidor
CREATE OR REPLACE VIEW v_r08_pedidos_entregados AS
SELECT p.id_pedido, p.fecha_pedido, p.fecha_entrega_estimada, p.metodo_entrega, p.id_repartidor,
       TIMESTAMPDIFF(HOUR, p.fecha_pedido, p.fecha_entrega_estimada) AS horas_hasta_entrega_estimada,
       p.total_pedido
FROM Pedido p WHERE p.estado_pedido = 'entregado'
ORDER BY p.fecha_pedido DESC;
