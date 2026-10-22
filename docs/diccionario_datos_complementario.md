# Diccionario de datos complementario

Completa las Tablas 4–9 del informe con las tablas que faltaban (`categoria`, `cliente`, `datosevento`) y el campo `foto_url` de `producto`. Basado en `BaseDatos/sistema_ventas.sql`.

## Categoria

| Atributo | Descripción | Tipo | Clave | Restricciones |
|---|---|---|---|---|
| id_categoria | Código de la categoría | varchar(6) | primaria | NOT NULL |
| nombre_categoria | Nombre de la categoría | varchar(50) | no | NOT NULL |
| descripcion | Descripción de la categoría | varchar(200) | no | NULL permitido |
| estado | Indica si la categoría está activa | tinyint(1) | no | NOT NULL, default 1 |

## Cliente

| Atributo | Descripción | Tipo | Clave | Restricciones |
|---|---|---|---|---|
| id_cliente | Código del cliente | varchar(6) | primaria | NOT NULL |
| nombres | Nombres del cliente | varchar(100) | no | NOT NULL |
| apellidos | Apellidos del cliente | varchar(100) | no | NOT NULL |
| telefono | Teléfono de contacto | varchar(15) | no | NOT NULL |
| correo | Correo electrónico | varchar(100) | no | NULL permitido |
| direccion_referencia | Dirección de referencia | varchar(200) | no | NULL permitido |
| fecha_registro | Fecha de registro | date | no | NOT NULL, default fecha actual |

## DatosEvento

Solo existe para pedidos de tipo catering (relación 1:1 opcional con `pedido`).

| Atributo | Descripción | Tipo | Clave | Restricciones |
|---|---|---|---|---|
| id_pedido | Pedido al que pertenece el evento | varchar(6) | primaria/foránea | NOT NULL |
| fecha_evento | Fecha del evento | date | no | NOT NULL |
| hora_evento | Hora del evento | time | no | NULL permitido |
| tipo_servicio | Tipo de servicio (ej. cumpleaños) | varchar(50) | no | NOT NULL |
| cantidad_personas | Cantidad de personas | int | no | NOT NULL, CHECK > 0 |
| direccion_evento | Dirección del evento | varchar(200) | no | NOT NULL |
| observaciones_evento | Observaciones del evento | varchar(300) | no | NULL permitido |

## Producto: campo faltante en la Tabla 5

| Atributo | Descripción | Tipo | Clave | Restricciones |
|---|---|---|---|---|
| foto_url | Ruta o URL de la fotografía del producto | varchar(255) | no | NULL permitido |

## Diferencias entre el script SQL y las tablas del informe

- `Producto.descripcion` es `NULL` permitido en el script; el informe no lo indica.
- `Repartidor.zona_reparto` y `Repartidor.licencia_conducir` son `NULL` permitido en el script.
- `Pedido.fecha_entrega_estimada` y `Pedido.direccion_entrega` son `NULL` permitido (recojo en local no necesita dirección).
- `Pedido.id_repartidor` apunta a `usuario.id_usuario` (no a `repartidor`).
