-- Base del MODO DEMO (H2). Generada a partir de BASE DE DATOS - LA DOÑITA GOURMET.sql
-- + columna foto_url + datos de ejemplo. No editar a mano: ver BaseDatos/02_actualizacion_sistema.sql
-- ============================================
-- BASE DE DATOS: La Doñita Gourmet
-- Proyecto Integrador I
-- MySQL
-- ============================================




-- ============================================
-- 1. CATEGORIA
-- ============================================
CREATE TABLE Categoria (
    id_categoria VARCHAR(6) PRIMARY KEY,
    nombre_categoria VARCHAR(50) NOT NULL,
    descripcion VARCHAR(200),
    estado BOOLEAN NOT NULL DEFAULT TRUE
);

-- ============================================
-- 2. CLIENTE
-- ============================================
CREATE TABLE Cliente (
    id_cliente VARCHAR(6) PRIMARY KEY,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    telefono VARCHAR(15) NOT NULL,
    correo VARCHAR(100),
    direccion_referencia VARCHAR(200),
    fecha_registro DATE NOT NULL DEFAULT (CURRENT_DATE)
);

-- ============================================
-- 3. USUARIO
-- ============================================
CREATE TABLE Usuario (
    id_usuario VARCHAR(6) PRIMARY KEY,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    dni VARCHAR(8) NOT NULL UNIQUE,
    usuario VARCHAR(50) NOT NULL UNIQUE,
    contraseña VARCHAR(255) NOT NULL,
    rol ENUM('admin', 'empleado', 'repartidor') NOT NULL,
    telefono VARCHAR(15) NOT NULL,
    correo VARCHAR(100),
    fecha_registro DATE NOT NULL DEFAULT (CURRENT_DATE),
    estado BOOLEAN NOT NULL DEFAULT TRUE
);

-- ============================================
-- 4. PRODUCTO 
-- ============================================
CREATE TABLE Producto (
    id_producto VARCHAR(6) PRIMARY KEY,
    id_categoria VARCHAR(6) NOT NULL,
    nombre_producto VARCHAR(100) NOT NULL,
    descripcion VARCHAR(300),
    precio DECIMAL(8,2) NOT NULL CHECK (precio >= 0),
    foto_url VARCHAR(255),
    stock_disponible INT NOT NULL DEFAULT 0 CHECK (stock_disponible >= 0),
    estado BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion DATE NOT NULL DEFAULT (CURRENT_DATE),
    FOREIGN KEY (id_categoria) REFERENCES Categoria(id_categoria)
);

-- ============================================
-- 5. EMPLEADO 
-- ============================================
CREATE TABLE Empleado (
    id_usuario VARCHAR(6) PRIMARY KEY,
    fecha_ingreso_laboral DATE NOT NULL,
    turno ENUM('mañana', 'tarde') NOT NULL,
    area_asignada VARCHAR(50) NOT NULL,
    sueldo DECIMAL(8,2) NOT NULL CHECK (sueldo >= 0),
    FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario)
);

-- ============================================
-- 6. REPARTIDOR 
-- ============================================
CREATE TABLE Repartidor (
    id_usuario VARCHAR(6) PRIMARY KEY,
    fecha_ingreso_laboral DATE NOT NULL,
    placa_vehiculo VARCHAR(10) NOT NULL,
    tipo_vehiculo ENUM('moto', 'auto', 'bicicleta') NOT NULL,
    zona_reparto VARCHAR(100),
    licencia_conducir VARCHAR(20),
    FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario)
);

-- ============================================
-- 7. PEDIDO 
-- ============================================
CREATE TABLE Pedido (
    id_pedido VARCHAR(6) PRIMARY KEY,
    id_cliente VARCHAR(6) NOT NULL,
    id_repartidor VARCHAR(6),
    fecha_pedido DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_entrega_estimada DATETIME,
    estado_pedido ENUM('pendiente', 'en preparación', 'en camino', 'entregado') NOT NULL DEFAULT 'pendiente',
    tipo_pedido ENUM('normal', 'catering') NOT NULL,
    metodo_entrega ENUM('recojo en local', 'delivery') NOT NULL,
    direccion_entrega VARCHAR(200),
    total_pedido DECIMAL(8,2) NOT NULL DEFAULT 0 CHECK (total_pedido >= 0),
    observaciones VARCHAR(300),
    FOREIGN KEY (id_cliente) REFERENCES Cliente(id_cliente),
    FOREIGN KEY (id_repartidor) REFERENCES Usuario(id_usuario)
);

-- ============================================
-- 8. DETALLE_PEDIDO 
-- ============================================
CREATE TABLE DetallePedido (
    id_detalle VARCHAR(6) PRIMARY KEY,
    id_pedido VARCHAR(6) NOT NULL,
    id_producto VARCHAR(6) NOT NULL,
    cantidad INT NOT NULL CHECK (cantidad > 0),
    precio_unitario DECIMAL(8,2) NOT NULL CHECK (precio_unitario >= 0),
    subtotal DECIMAL(8,2) NOT NULL CHECK (subtotal >= 0),
    FOREIGN KEY (id_pedido) REFERENCES Pedido(id_pedido),
    FOREIGN KEY (id_producto) REFERENCES Producto(id_producto)
);

-- ============================================
-- 9. DATOS_EVENTO 
-- ============================================
CREATE TABLE DatosEvento (
    id_pedido VARCHAR(6) PRIMARY KEY,
    fecha_evento DATE NOT NULL,
    hora_evento TIME,
    tipo_servicio VARCHAR(50) NOT NULL,
    cantidad_personas INT NOT NULL CHECK (cantidad_personas > 0),
    direccion_evento VARCHAR(200) NOT NULL,
    observaciones_evento VARCHAR(300),
    FOREIGN KEY (id_pedido) REFERENCES Pedido(id_pedido)
);

-- ============================================
-- DATOS DE PRUEBA 
-- ============================================

INSERT INTO Categoria (id_categoria, nombre_categoria, descripcion) VALUES
('CAT001', 'Empanadas', 'Empanadas de distintos rellenos'),
('CAT002', 'Catering', 'Servicio de catering para eventos'),
('CAT003', 'Boxes de bocaditos', 'Boxes para reuniones y obsequios');

INSERT INTO Cliente (id_cliente, nombres, apellidos, telefono, correo, direccion_referencia) VALUES
('CLI001', 'Maria', 'Lorenzo', '987654321', 'mariloren@gmail.com', 'Av. Los Olivos 123, Lima');

INSERT INTO Usuario (id_usuario, nombres, apellidos, dni, usuario, contraseña, rol, telefono, correo) VALUES
('USR001', 'Dario', 'Doñita', '70123456', 'admin_donita', 'hash_admin_123', 'admin', '999111222', 'admin@ladonitagourmet.com'),
('USR002', 'Carlos', 'Pérez', '70654321', 'cperez_emp', 'hash_emp_456', 'empleado', '999333444', 'cperez@ladonitagourmet.com'),
('USR003', 'Jorge', 'Salas', '70987654', 'jsalas_rep', 'hash_rep_789', 'repartidor', '999555666', 'jsalas@ladonitagourmet.com');

INSERT INTO Empleado (id_usuario, fecha_ingreso_laboral, turno, area_asignada, sueldo) VALUES
('USR002', '2026-01-15', 'mañana', 'cocina', 1200.00);

INSERT INTO Repartidor (id_usuario, fecha_ingreso_laboral, placa_vehiculo, tipo_vehiculo, zona_reparto, licencia_conducir) VALUES
('USR003', '2026-03-01', 'ABC-123', 'moto', 'Lima Norte', 'Q12345678');

INSERT INTO Producto (id_producto, id_categoria, nombre_producto, descripcion, precio, stock_disponible) VALUES
('PRO001', 'CAT001', 'Empanada de pollo', 'Empanada rellena de pollo desmenuzado', 4.50, 100),
('PRO002', 'CAT002', 'Box de catering básico', 'Box de bocaditos surtidos para eventos', 15.00, 50);

INSERT INTO Pedido (id_pedido, id_cliente, id_repartidor, tipo_pedido, metodo_entrega, direccion_entrega, total_pedido) VALUES
('PED001', 'CLI001', 'USR003', 'catering', 'delivery', 'Av. Los Olivos 123, Lima', 90.00);

INSERT INTO DetallePedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES
('DET001', 'PED001', 'PRO002', 6, 15.00, 90.00);

INSERT INTO DatosEvento (id_pedido, fecha_evento, hora_evento, tipo_servicio, cantidad_personas, direccion_evento) VALUES
('PED001', '2026-10-05', '18:00:00', 'Cumpleaños', 30, 'Av. Los Olivos 123, Lima');
-- Usuarios iniciales: contraseñas con PBKDF2 (Admin2026! / Empleado2026! / Reparto2026!)
-- CÁMBIALAS desde 'Mi perfil' apenas entres por primera vez.
UPDATE Usuario SET `contraseña` = 'pbkdf2$120000$xmfxknFQS+6/rKNZwSlVUw==$C8zQ4AUzdxS3LhPbvQZp8kJ/cGtsXHUNMM0bP6a3lW0=' WHERE usuario = 'admin_donita';
UPDATE Usuario SET `contraseña` = 'pbkdf2$120000$T0brEPzvIHINiGVPCISQAg==$KukfENOKpS6wDO40MCblerNA3fb5UWfg4JXjni857xE=' WHERE usuario = 'cperez_emp';
UPDATE Usuario SET `contraseña` = 'pbkdf2$120000$AR+4jyPsOhbsH57RG/Fuug==$hWkMejwz7jLcYoZOrJaQ0y4ATEvZiL747gIxFliomJc=' WHERE usuario = 'jsalas_rep';
INSERT INTO Usuario (id_usuario, nombres, apellidos, dni, usuario, `contraseña`, rol, telefono, correo) VALUES ('USR004', 'Miguel', 'Ramos Paredes', '71234567', 'mramos_rep', 'pbkdf2$120000$AR+4jyPsOhbsH57RG/Fuug==$hWkMejwz7jLcYoZOrJaQ0y4ATEvZiL747gIxFliomJc=', 'repartidor', '999777888', 'mramos@ladonitagourmet.com');
INSERT INTO Repartidor (id_usuario, fecha_ingreso_laboral, placa_vehiculo, tipo_vehiculo, zona_reparto, licencia_conducir) VALUES ('USR004', '2026-05-10', 'XYZ-789', 'moto', 'Lima Centro y Sur', 'Q87654321');

-- Productos adicionales del catálogo
INSERT INTO Producto (id_producto, id_categoria, nombre_producto, descripcion, precio, stock_disponible) VALUES ('PRO003', 'CAT001', 'Empanada de carne', 'Carne picada jugosa con aceituna y huevo', 5.00, 120);
INSERT INTO Producto (id_producto, id_categoria, nombre_producto, descripcion, precio, stock_disponible) VALUES ('PRO004', 'CAT001', 'Empanada de ají de gallina', 'Nuestro clásico ají de gallina en masa hojaldrada', 5.50, 80);
INSERT INTO Producto (id_producto, id_categoria, nombre_producto, descripcion, precio, stock_disponible) VALUES ('PRO005', 'CAT001', 'Empanada de queso', 'Queso andino derretido, ideal para los pequeños', 4.00, 60);
INSERT INTO Producto (id_producto, id_categoria, nombre_producto, descripcion, precio, stock_disponible) VALUES ('PRO006', 'CAT003', 'Box dulce x12', 'Alfajores, mini pyes y trufas surtidas', 38.00, 25);
INSERT INTO Producto (id_producto, id_categoria, nombre_producto, descripcion, precio, stock_disponible) VALUES ('PRO007', 'CAT003', 'Box salado x20', 'Mini empanadas, enrolladitos y causitas', 45.00, 8);
INSERT INTO Producto (id_producto, id_categoria, nombre_producto, descripcion, precio, stock_disponible) VALUES ('PRO008', 'CAT002', 'Catering premium x persona', 'Bocaditos dulces y salados + bebida', 22.00, 200);

-- Clientes de ejemplo
INSERT INTO Cliente (id_cliente, nombres, apellidos, telefono, correo, direccion_referencia) VALUES ('CLI002', 'Lucía', 'Ramírez Torres', '912345678', 'lucia.ramirez@gmail.com', 'Jr. Las Flores 456, Surco');
INSERT INTO Cliente (id_cliente, nombres, apellidos, telefono, correo, direccion_referencia) VALUES ('CLI003', 'José', 'Quispe Mamani', '923456789', NULL, 'Av. Universitaria 1200, Los Olivos');
INSERT INTO Cliente (id_cliente, nombres, apellidos, telefono, correo, direccion_referencia) VALUES ('CLI004', 'Andrea', 'Salazar Vega', '934567890', 'andrea.sv@hotmail.com', 'Calle Los Pinos 88, Miraflores');
INSERT INTO Cliente (id_cliente, nombres, apellidos, telefono, correo, direccion_referencia) VALUES ('CLI005', 'Universidad', 'César Vallejo (Eventos)', '945678901', 'eventos@ucv.edu.pe', 'Av. Alfredo Mendiola 6232, Los Olivos');
INSERT INTO Cliente (id_cliente, nombres, apellidos, telefono, correo, direccion_referencia) VALUES ('CLI006', 'Rosa', 'Huamán Flores', '956789012', NULL, 'Mz. B Lt. 4, San Martín de Porres');
INSERT INTO Cliente (id_cliente, nombres, apellidos, telefono, correo, direccion_referencia) VALUES ('CLI007', 'Diego', 'Fernández Ruiz', '967890123', 'diego.fr@gmail.com', 'Av. Javier Prado 2450, San Borja');

-- Pedidos de ejemplo de las últimas semanas (fechas relativas a hoy, para que los reportes tengan datos)
INSERT INTO Pedido (id_pedido, id_cliente, id_repartidor, fecha_pedido, fecha_entrega_estimada, estado_pedido, tipo_pedido, metodo_entrega, direccion_entrega, total_pedido) VALUES ('PED002', 'CLI002', 'USR003', DATEADD('HOUR', 11, DATEADD('DAY', -20, CAST(CURRENT_DATE AS TIMESTAMP))), DATEADD('HOUR', 13, DATEADD('DAY', -20, CAST(CURRENT_DATE AS TIMESTAMP))), 'entregado', 'normal', 'delivery', 'Jr. Las Flores 456, Surco', 84.00);
INSERT INTO DetallePedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES ('DET002', 'PED002', 'PRO001', 12, 4.50, 54.00);
INSERT INTO DetallePedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES ('DET003', 'PED002', 'PRO003', 6, 5.00, 30.00);
INSERT INTO Pedido (id_pedido, id_cliente, id_repartidor, fecha_pedido, fecha_entrega_estimada, estado_pedido, tipo_pedido, metodo_entrega, direccion_entrega, total_pedido) VALUES ('PED003', 'CLI003', NULL, DATEADD('HOUR', 16, DATEADD('DAY', -18, CAST(CURRENT_DATE AS TIMESTAMP))), DATEADD('HOUR', 18, DATEADD('DAY', -18, CAST(CURRENT_DATE AS TIMESTAMP))), 'entregado', 'normal', 'recojo en local', NULL, 55.00);
INSERT INTO DetallePedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES ('DET004', 'PED003', 'PRO004', 10, 5.50, 55.00);
INSERT INTO Pedido (id_pedido, id_cliente, id_repartidor, fecha_pedido, fecha_entrega_estimada, estado_pedido, tipo_pedido, metodo_entrega, direccion_entrega, total_pedido) VALUES ('PED004', 'CLI004', 'USR004', DATEADD('HOUR', 10, DATEADD('DAY', -15, CAST(CURRENT_DATE AS TIMESTAMP))), DATEADD('HOUR', 12, DATEADD('DAY', -15, CAST(CURRENT_DATE AS TIMESTAMP))), 'entregado', 'normal', 'delivery', 'Calle Los Pinos 88, Miraflores', 100.00);
INSERT INTO DetallePedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES ('DET005', 'PED004', 'PRO006', 2, 38.00, 76.00);
INSERT INTO DetallePedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES ('DET006', 'PED004', 'PRO005', 6, 4.00, 24.00);
INSERT INTO Pedido (id_pedido, id_cliente, id_repartidor, fecha_pedido, fecha_entrega_estimada, estado_pedido, tipo_pedido, metodo_entrega, direccion_entrega, total_pedido) VALUES ('PED005', 'CLI005', 'USR003', DATEADD('HOUR', 9, DATEADD('DAY', -12, CAST(CURRENT_DATE AS TIMESTAMP))), DATEADD('HOUR', 10, CAST(DATEADD('DAY', -10, CURRENT_DATE) AS TIMESTAMP)), 'entregado', 'catering', 'delivery', 'Av. Alfredo Mendiola 6232, Los Olivos', 1910.00);
INSERT INTO DetallePedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES ('DET007', 'PED005', 'PRO008', 80, 22.00, 1760.00);
INSERT INTO DetallePedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES ('DET008', 'PED005', 'PRO002', 10, 15.00, 150.00);
INSERT INTO DatosEvento (id_pedido, fecha_evento, hora_evento, tipo_servicio, cantidad_personas, direccion_evento) VALUES ('PED005', DATEADD('DAY', -10, CURRENT_DATE), '10:00:00', 'Reunión corporativa', 80, 'Auditorio principal UCV, Los Olivos');
INSERT INTO Pedido (id_pedido, id_cliente, id_repartidor, fecha_pedido, fecha_entrega_estimada, estado_pedido, tipo_pedido, metodo_entrega, direccion_entrega, total_pedido) VALUES ('PED006', 'CLI006', NULL, DATEADD('HOUR', 18, DATEADD('DAY', -9, CAST(CURRENT_DATE AS TIMESTAMP))), DATEADD('HOUR', 20, DATEADD('DAY', -9, CAST(CURRENT_DATE AS TIMESTAMP))), 'entregado', 'normal', 'recojo en local', NULL, 108.00);
INSERT INTO DetallePedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES ('DET009', 'PED006', 'PRO001', 24, 4.50, 108.00);
INSERT INTO Pedido (id_pedido, id_cliente, id_repartidor, fecha_pedido, fecha_entrega_estimada, estado_pedido, tipo_pedido, metodo_entrega, direccion_entrega, total_pedido) VALUES ('PED007', 'CLI007', 'USR004', DATEADD('HOUR', 12, DATEADD('DAY', -6, CAST(CURRENT_DATE AS TIMESTAMP))), DATEADD('HOUR', 14, DATEADD('DAY', -6, CAST(CURRENT_DATE AS TIMESTAMP))), 'entregado', 'normal', 'delivery', 'Av. Javier Prado 2450, San Borja', 105.00);
INSERT INTO DetallePedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES ('DET010', 'PED007', 'PRO007', 1, 45.00, 45.00);
INSERT INTO DetallePedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES ('DET011', 'PED007', 'PRO003', 12, 5.00, 60.00);
INSERT INTO Pedido (id_pedido, id_cliente, id_repartidor, fecha_pedido, fecha_entrega_estimada, estado_pedido, tipo_pedido, metodo_entrega, direccion_entrega, total_pedido) VALUES ('PED008', 'CLI002', NULL, DATEADD('HOUR', 17, DATEADD('DAY', -5, CAST(CURRENT_DATE AS TIMESTAMP))), DATEADD('HOUR', 19, DATEADD('DAY', -5, CAST(CURRENT_DATE AS TIMESTAMP))), 'entregado', 'normal', 'recojo en local', NULL, 38.00);
INSERT INTO DetallePedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES ('DET012', 'PED008', 'PRO006', 1, 38.00, 38.00);
INSERT INTO Pedido (id_pedido, id_cliente, id_repartidor, fecha_pedido, fecha_entrega_estimada, estado_pedido, tipo_pedido, metodo_entrega, direccion_entrega, total_pedido) VALUES ('PED009', 'CLI003', 'USR003', DATEADD('HOUR', 13, DATEADD('DAY', -4, CAST(CURRENT_DATE AS TIMESTAMP))), DATEADD('HOUR', 15, DATEADD('DAY', -4, CAST(CURRENT_DATE AS TIMESTAMP))), 'entregado', 'normal', 'delivery', 'Av. Universitaria 1200, Los Olivos', 57.00);
INSERT INTO DetallePedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES ('DET013', 'PED009', 'PRO004', 6, 5.50, 33.00);
INSERT INTO DetallePedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES ('DET014', 'PED009', 'PRO005', 6, 4.00, 24.00);
INSERT INTO Pedido (id_pedido, id_cliente, id_repartidor, fecha_pedido, fecha_entrega_estimada, estado_pedido, tipo_pedido, metodo_entrega, direccion_entrega, total_pedido) VALUES ('PED010', 'CLI004', NULL, DATEADD('HOUR', 11, DATEADD('DAY', -3, CAST(CURRENT_DATE AS TIMESTAMP))), DATEADD('HOUR', 13, DATEADD('DAY', -3, CAST(CURRENT_DATE AS TIMESTAMP))), 'entregado', 'normal', 'recojo en local', NULL, 60.00);
INSERT INTO DetallePedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES ('DET015', 'PED010', 'PRO001', 6, 4.50, 27.00);
INSERT INTO DetallePedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES ('DET016', 'PED010', 'PRO004', 6, 5.50, 33.00);
INSERT INTO Pedido (id_pedido, id_cliente, id_repartidor, fecha_pedido, fecha_entrega_estimada, estado_pedido, tipo_pedido, metodo_entrega, direccion_entrega, total_pedido) VALUES ('PED011', 'CLI007', 'USR004', DATEADD('HOUR', 15, DATEADD('DAY', -2, CAST(CURRENT_DATE AS TIMESTAMP))), DATEADD('HOUR', 17, DATEADD('DAY', -2, CAST(CURRENT_DATE AS TIMESTAMP))), 'en camino', 'normal', 'delivery', 'Av. Javier Prado 2450, San Borja', 90.00);
INSERT INTO DetallePedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES ('DET017', 'PED011', 'PRO007', 2, 45.00, 90.00);
INSERT INTO Pedido (id_pedido, id_cliente, id_repartidor, fecha_pedido, fecha_entrega_estimada, estado_pedido, tipo_pedido, metodo_entrega, direccion_entrega, total_pedido) VALUES ('PED012', 'CLI006', NULL, DATEADD('HOUR', 10, DATEADD('DAY', -1, CAST(CURRENT_DATE AS TIMESTAMP))), DATEADD('HOUR', 12, DATEADD('DAY', -1, CAST(CURRENT_DATE AS TIMESTAMP))), 'en preparación', 'normal', 'recojo en local', NULL, 168.00);
INSERT INTO DetallePedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES ('DET018', 'PED012', 'PRO003', 24, 5.00, 120.00);
INSERT INTO DetallePedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES ('DET019', 'PED012', 'PRO005', 12, 4.00, 48.00);
INSERT INTO Pedido (id_pedido, id_cliente, id_repartidor, fecha_pedido, fecha_entrega_estimada, estado_pedido, tipo_pedido, metodo_entrega, direccion_entrega, total_pedido) VALUES ('PED013', 'CLI002', 'USR003', DATEADD('HOUR', 19, DATEADD('DAY', -1, CAST(CURRENT_DATE AS TIMESTAMP))), DATEADD('HOUR', 18, CAST(DATEADD('DAY', 4, CURRENT_DATE) AS TIMESTAMP)), 'en preparación', 'catering', 'delivery', 'Jr. Las Flores 456, Surco', 846.00);
INSERT INTO DetallePedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES ('DET020', 'PED013', 'PRO008', 35, 22.00, 770.00);
INSERT INTO DetallePedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES ('DET021', 'PED013', 'PRO006', 2, 38.00, 76.00);
INSERT INTO DatosEvento (id_pedido, fecha_evento, hora_evento, tipo_servicio, cantidad_personas, direccion_evento) VALUES ('PED013', DATEADD('DAY', 4, CURRENT_DATE), '18:00:00', 'Cumpleaños', 35, 'Jr. Las Flores 456, Surco');
INSERT INTO Pedido (id_pedido, id_cliente, id_repartidor, fecha_pedido, fecha_entrega_estimada, estado_pedido, tipo_pedido, metodo_entrega, direccion_entrega, total_pedido) VALUES ('PED014', 'CLI005', NULL, DATEADD('HOUR', 9, DATEADD('DAY', -0, CAST(CURRENT_DATE AS TIMESTAMP))), DATEADD('HOUR', 16, CAST(DATEADD('DAY', 9, CURRENT_DATE) AS TIMESTAMP)), 'pendiente', 'catering', 'delivery', 'Av. Alfredo Mendiola 6232, Los Olivos', 2940.00);
INSERT INTO DetallePedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES ('DET022', 'PED014', 'PRO008', 120, 22.00, 2640.00);
INSERT INTO DetallePedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES ('DET023', 'PED014', 'PRO002', 20, 15.00, 300.00);
INSERT INTO DatosEvento (id_pedido, fecha_evento, hora_evento, tipo_servicio, cantidad_personas, direccion_evento) VALUES ('PED014', DATEADD('DAY', 9, CURRENT_DATE), '16:00:00', 'Graduación', 120, 'Campus UCV, Los Olivos');
INSERT INTO Pedido (id_pedido, id_cliente, id_repartidor, fecha_pedido, fecha_entrega_estimada, estado_pedido, tipo_pedido, metodo_entrega, direccion_entrega, total_pedido) VALUES ('PED015', 'CLI003', NULL, DATEADD('HOUR', 11, DATEADD('DAY', -0, CAST(CURRENT_DATE AS TIMESTAMP))), DATEADD('HOUR', 13, DATEADD('DAY', -0, CAST(CURRENT_DATE AS TIMESTAMP))), 'pendiente', 'normal', 'recojo en local', NULL, 54.00);
INSERT INTO DetallePedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES ('DET024', 'PED015', 'PRO001', 12, 4.50, 54.00);
INSERT INTO Pedido (id_pedido, id_cliente, id_repartidor, fecha_pedido, fecha_entrega_estimada, estado_pedido, tipo_pedido, metodo_entrega, direccion_entrega, total_pedido) VALUES ('PED016', 'CLI004', NULL, DATEADD('HOUR', 12, DATEADD('DAY', -0, CAST(CURRENT_DATE AS TIMESTAMP))), DATEADD('HOUR', 14, DATEADD('DAY', -0, CAST(CURRENT_DATE AS TIMESTAMP))), 'pendiente', 'normal', 'delivery', 'Calle Los Pinos 88, Miraflores', 104.00);
INSERT INTO DetallePedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES ('DET025', 'PED016', 'PRO004', 12, 5.50, 66.00);
INSERT INTO DetallePedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES ('DET026', 'PED016', 'PRO006', 1, 38.00, 38.00);
