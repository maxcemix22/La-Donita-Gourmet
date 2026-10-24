# La Doñita Gourmet — Sistema Web de Gestión y Reserva de Pedidos

Proyecto Integrador I · **Versión final (100 %)**

Java 17+ · Jakarta Servlet 6 / JSP + JSTL · MySQL 8 (o H2 en modo demo) · Bootstrap 5 · Maven · MVC + DAO
Diseño basado en la plantilla de Google Stitch *"Artesanal Cálido Gourmet"*.

---

## Inicio rápido (sin instalar MySQL)

1. Clona o descomprime el repositorio en una carpeta tuya.
2. Doble clic en **`EJECUTAR.bat`** → presiona **Enter** (opción 2, modo demo).
3. Se abre **http://localhost:8080/ladonita** con datos de ejemplo.

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin_donita` | `Admin2026!` | Administrador |
| `cperez_emp` | `Empleado2026!` | Empleado |
| `jsalas_rep` / `mramos_rep` | `Reparto2026!` | Repartidor |

El panel interno está en **/login** (enlace *Acceso del personal* en el pie de página).

---

## Módulos

### Zona pública (clientes, sin cuenta)
- **Catálogo** con fotos, filtro por categoría y búsqueda.
- **Carrito** en sesión, que respeta el stock.
- **Registro de pedido**: normal o catering (con datos del evento), recojo o delivery. Se guarda en la BD en una sola transacción.
- **Envío por WhatsApp**: al confirmar, un botón abre WhatsApp con el resumen del pedido ya escrito.
- **Seguimiento del pedido** con código + celular, con línea de tiempo del estado.

### Panel interno (personal)
| Módulo | Admin | Empleado | Repartidor |
|---|:-:|:-:|:-:|
| Dashboard (ventas del día/mes, ticket promedio, gráfico 7 días, eventos, stock bajo) | ✔ | ✔ | — |
| Pedidos: listado, detalle, cambio de estado, asignar repartidor | ✔ (todo) | ✔ (avanza estado) | Solo sus entregas |
| Inventario: alertas y ajuste rápido (+producción, −merma, =conteo) | ✔ | ✔ | — |
| Clientes: frecuencia de compra, ficha con historial, edición | ✔ | ✔ | — |
| Productos: CRUD con foto (subida o URL) y eliminación lógica | ✔ | — | — |
| Categorías: crear, editar, activar/desactivar | ✔ | — | — |
| Usuarios: alta de empleados/repartidores, edición, desactivar, restablecer clave | ✔ | — | — |
| Reportes R01–R08 con filtros, totales, Excel (.xlsx), CSV e impresión | Todos | R02, R04, R06, R07 | R07, R08 (propios) |
| Mi perfil: cambiar la propia contraseña | ✔ | ✔ | ✔ |

---

## Instalación con MySQL

1. Ejecuta los scripts de la carpeta `BaseDatos`, en orden, en MySQL 8.0.13 o superior:
   1. `01_base_datos_ladonita.sql`: esquema oficial y datos iniciales.
   2. `02_actualizacion_sistema.sql`: agrega `foto_url` e índices y pone las contraseñas iniciales en PBKDF2. **No carga datos de ejemplo.** Se puede ejecutar más de una vez.
   3. `03_reportes.sql` (opcional): vistas R01–R08 para consultar desde Workbench.
   4. `04_datos_ejemplo_OPCIONAL.sql`: **solo para pruebas o capacitación**, nunca en la base real del negocio. Carga clientes y pedidos de ejemplo.
2. En `JavaNetbeans/src/main/resources/db.properties` pon `db.modo=mysql` y tu `db.user` / `db.password`.
3. Ejecuta el proyecto con cualquiera de las formas de abajo.

### Formas de ejecutar
| Entorno | Cómo |
|---|---|
| **Doble clic** | `EJECUTAR.bat` (en la raíz del repositorio) → opción 1 (MySQL) o 2 (demo) |
| **NetBeans** | *File → Open Project* → elige la carpeta **`JavaNetbeans`** → **Run (F6)**. Para probar sin MySQL: clic derecho → *Run Maven → Ejecutar en modo DEMO* |
| **VS Code** | Abre la carpeta raíz del repositorio. Extension Pack for Java → *Terminal → Run Task* → "Ejecutar La Doñita…" (normal o DEMO) |
| **Consola** | Dentro de `JavaNetbeans`: `mvn jetty:run` (otro puerto: `mvn jetty:run -Djetty.port=8081`) |
| **Tomcat 10.1+ / GlassFish 7** | en `JavaNetbeans`: `mvn clean package` → desplegar `JavaNetbeans/target/ladonita.war` |

`.vscode/settings.json` usa el Maven que trae NetBeans 22. Si tienes Maven en el PATH, cambia `ladonita.maven` a `mvn`.

### Configuración (`db.properties` o variables de entorno)
| Clave | Variable de entorno | Uso |
|---|---|---|
| `db.modo` | `LADONITA_DB_MODO` | `mysql` o `demo` |
| `db.url`, `db.user`, `db.password` | `LADONITA_DB_URL`, … | Conexión MySQL |
| `app.whatsapp` | `LADONITA_APP_WHATSAPP` | Número del negocio (ej. `51912679851`) |
| `app.imagenes` | `LADONITA_APP_IMAGENES` | Carpeta de fotos subidas (por defecto `~/ladonita-gourmet/imagenes`) |
| — | `LADONITA_LOG_DIR` | Carpeta de los logs (por defecto `~/ladonita-gourmet/logs`) |

La base del modo demo se crea sola en `~/ladonita-gourmet/demo`. Si borras esa carpeta, vuelve a crearse con los datos de ejemplo.

---

## Estructura del repositorio

```
La-Donita-Gourmet/
├── BaseDatos/      Scripts SQL de MySQL (01 esquema · 02 actualización · 03 reportes · 04 datos de ejemplo opcional)
├── JavaNetbeans/   Proyecto Maven (se abre en NetBeans): código Java, configuración y pom.xml
├── PaginaWeb/      Parte web: vistas JSP, estilos, JavaScript, imágenes y WEB-INF/web.xml
├── docs/           Documentación (preguntas frecuentes)
├── EJECUTAR.bat    Arranque con doble clic (Windows)
└── .vscode/        Tareas para VS Code
```

El `pom.xml` de `JavaNetbeans` apunta a `../PaginaWeb` como carpeta web (`warSourceDirectory`), así que al compilar se arma un solo `ladonita.war`.

### Dentro de JavaNetbeans

```
src/main/java/com/ladonita/
├── controlador/   AuthController, CatalogoController, CarritoController, PedidoController, SeguimientoController,
│                  DashboardController, ProductoController, CategoriaController, InventarioController,
│                  ClienteController, UsuarioController, ReporteController, PerfilController, ImagenController
├── modelo/entidades/  Usuario ← Empleado, Repartidor · Cliente · Producto · Categoria · Pedido · DetallePedido ·
│                      DatosEvento · Carrito · ItemCarrito · TablaReporte · enums (Rol, EstadoPedido, TipoPedido…)
├── modelo/dao/        Interfaces: UsuarioDAO, ClienteDAO, ProductoDAO, CategoriaDAO, PedidoDAO, ReporteDAO
├── modelo/dao/impl/   Implementaciones JDBC (reciben la Connection cn)
├── filtro/            CodificacionFilter (UTF-8 + cabeceras), CsrfFilter, SeguridadFilter (/admin/*), AppListener
└── util/              Conexion (MySQL/H2), Config, Contrasenas (PBKDF2), Validador, GeneradorId, WhatsApp, Csv,
                       ExcelReporte (POI), CacheCatalogo (Guava), Bitacora (Logback)
src/main/resources/    db.properties · logback.xml · demo/ladonita-demo.sql (base del modo demo, se carga desde el classpath)
```

### Dentro de PaginaWeb

```
WEB-INF/web.xml
WEB-INF/vistas/        JSP públicas (catálogo, carrito, checkout, seguimiento, login) + admin/ (panel con barra lateral) + fragmentos/
recursos/css, js, img  Estilos, JavaScript e imágenes de productos y categorías
```

## Librerías utilizadas

| Librería | Versión | Para qué se usa en el sistema |
|---|---|---|
| **Google Guava** | 33.2.1 | `Cache` con expiración para el bloqueo de 5 min tras 5 intentos de login (`AuthController`) y para las categorías del catálogo (`CacheCatalogo`, se invalida al editar categorías); `ImmutableList`; `Preconditions` en `Carrito`, `TablaReporte`, `GeneradorId` y `Contrasenas` |
| **Apache POI** | 5.2.5 | Exportación de los reportes R01–R08 a Excel `.xlsx` (`ExcelReporte`): título, encabezados en color cacao, formato `S/`, fechas, totales con fórmula `SUM`, filtros, encabezado fijo y ajuste a página para imprimir |
| **Apache Commons** | Lang 3.14 · Validator 1.8 · IO 2.16 | `Validador` (`StringUtils`, `NumberUtils`, `EmailValidator` según RFC), `Csv` y `Bitacora` (texto), fotos de producto (`IOUtils`, `FileUtils`, `FilenameUtils`): guardado, borrado de la foto anterior y protección de rutas |
| **Logback** (vía SLF4J) | 1.5.6 | `logs/ladonita.log` (arranque, errores, CSRF rechazados) y `logs/auditoria.log` (quién hizo qué: logins, pedidos, estados, productos, stock, usuarios, exportaciones). Rotación diaria, 30 días. Carpeta: `~/ladonita-gourmet/logs` o `LADONITA_LOG_DIR` |

Configuración de Logback: `src/main/resources/logback.xml`. La auditoría nunca guarda contraseñas.

## Seguridad
- Contraseñas con **PBKDF2-HMAC-SHA256**, 120 000 iteraciones y sal aleatoria. Las claves antiguas en SHA-256 se migran solas en el primer inicio de sesión.
- **PreparedStatement** en todas las consultas, token **CSRF** en todos los POST, escape de salida contra **XSS**.
- Filtro de sesión en `/admin/*`, permisos por **rol** en cada controlador, bloqueo tras 5 intentos fallidos y cambio de ID de sesión al iniciar sesión.
- Fotos: se valida la firma real del archivo (JPG/PNG/WEBP, máx. 2 MB) y solo se sirven nombres generados por el sistema.
- Cambio de estado con control de concurrencia: si otra persona lo cambió antes, se avisa.
- CSV protegido contra inyección de fórmulas de Excel.

## Notas
- Los IDs `VARCHAR(6)` admiten hasta 999 registros por prefijo (`PED999`).
- "Stock bajo" = 10 unidades o menos (constante `Producto.STOCK_MINIMO`, igual que el reporte R04).
