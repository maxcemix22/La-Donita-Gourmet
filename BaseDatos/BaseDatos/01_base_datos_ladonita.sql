-- ============================================
-- BASE DE DATOS: La Doñita Gourmet
-- Proyecto Integrador I
-- MySQL
-- ============================================

CREATE DATABASE IF NOT EXISTS ladonita_gourmet;
USE ladonita_gourmet;

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