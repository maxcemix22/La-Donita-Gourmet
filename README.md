# Sistema Web – La Doñita Gourmet

Sistema para mejorar la gestión de ventas de la repostería **La Doñita Gourmet**: centraliza el catálogo (empanadas, catering, boxes de bocaditos) y digitaliza el proceso de pedidos, que hoy se maneja de forma manual por WhatsApp y redes sociales.

Proyecto del curso **Proyecto Integrador I: Sistemas Software** (UTP, ciclo 2026-II, sección 34862, Grupo 3).

## Estado actual

| Componente | Estado |
|---|---|
| Base de datos MySQL (9 tablas + datos de prueba) | Hecho |
| Modelo Java (`Modelo`) con validaciones y reglas de estado del pedido | Hecho, DAO solo como interfaces |
| Página pública (catálogo, filtro por categoría, carrito, formulario, confirmación por WhatsApp, FAQ) | Prototipo funcional, sin backend |
| Panel de administración (RF04–RF06), login (RNF04), reportes R01–R08 | Pendiente |
| Implementación de DAO (JDBC), controladores y vistas Java | Pendiente |

## Estructura

```
BaseDatos/      sistema_ventas.sql   → script de creación + datos de prueba (MySQL 8)
JavaNetbeans/   proyecto NetBeans (Ant): src/Modelo, src/Controlador, src/Vista
PaginaWeb/      index.html, css/, js/   → página pública (HTML + CSS + JavaScript)
docs/           FAQ.md, diccionario_datos_complementario.md
```

## Tecnologías

- **Base de datos:** MySQL 8 (diseño físico en MySQL Workbench).
- **Java:** proyecto NetBeans con Ant, `javac.source/target = 22` (ver `JavaNetbeans/nbproject/project.properties`).
- **Frontend:** HTML, CSS y JavaScript sin librerías externas.
- **Control de versiones:** Git / GitHub.

## Instalación

1. Clonar el repositorio.
2. Crear la base de datos (el script ya incluye `CREATE DATABASE` y `USE`):
   ```bash
   mysql -u root -p < BaseDatos/sistema_ventas.sql
   ```
3. Página web: abrir `PaginaWeb/index.html` en el navegador, o servirla en local:
   ```bash
   cd PaginaWeb
   python -m http.server 8000
   ```
4. Configurar el número de WhatsApp del negocio en `PaginaWeb/js/productos.js` (`WHATSAPP_NUMBER`, con código de país, ej. `51999999999`). Sin esto el botón "Confirmar por WhatsApp" avisa que falta configurarlo.
5. Java: abrir la carpeta `JavaNetbeans` como proyecto en NetBeans.

## Base de datos

Entidades: `categoria`, `cliente`, `usuario`, `empleado`, `repartidor`, `producto`, `pedido`, `detallepedido`, `datosevento`.
Diccionario de datos del informe (Tablas 4–9) más las tablas faltantes en `docs/diccionario_datos_complementario.md`.

Estados del pedido: `pendiente` → `en preparación` → `en camino` (solo delivery) → `entregado`.

> **Seguridad:** las contraseñas de los usuarios de prueba en el script son texto de ejemplo, no hashes reales. Reemplazarlas por hashes antes de implementar el inicio de sesión. No subir credenciales reales al repositorio.

## Roles

`admin` (propietario), `empleado`, `repartidor` y el **cliente**, que no necesita registrarse para pedir.

## Documentación

- Informe de avance (Proyecto Final 02).
- Preguntas frecuentes de usuarios: `docs/FAQ.md`.
