# La Doñita Gourmet — Sistema Web de Gestión y Reserva de Pedidos

Proyecto Integrador I · **Versión 1.0**

Sistema web para una repostería: los clientes arman su pedido en la página (normal o catering) y el personal lo atiende desde un panel interno con inventario, clientes, usuarios y reportes.

**Tecnologías:** Java 17+ · Jakarta Servlet 6 / JSP + JSTL · MySQL 8 (o H2 en modo demo) · Bootstrap 5 · Maven · MVC + DAO

> 📘 **Manual completo** de instalación, mantenimiento y uso (para desarrolladores y personal del local) localizado en la carpeta "docs"

---

## Inicio rápido (sin instalar MySQL)

1. Clona o descomprime el repositorio.
2. Doble clic en **`EJECUTAR.bat`** y presiona **Enter** (opción 2, modo demo). La primera vez tarda unos minutos porque descarga dependencias.
3. Se abre **http://localhost:8080/ladonita** con datos de ejemplo. El panel interno está en **/login** (enlace *Acceso del personal* al pie de la tienda).

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin_donita` | `Admin2026!` | Administrador |
| `cperez_emp` | `Empleado2026!` | Empleado |
| `jsalas_rep` / `mramos_rep` | `Reparto2026!` | Repartidor |

> Son usuarios de prueba. Cambia sus contraseñas antes de usar el sistema con clientes reales.

---

## Qué incluye

**Zona pública (sin cuenta):** catálogo con filtro y búsqueda, carrito que respeta el stock, pedido normal o catering (recojo o delivery), envío del resumen por WhatsApp y seguimiento con código + celular.

**Panel interno (por rol):**

| Módulo | Administrador | Empleado | Repartidor |
|---|:-:|:-:|:-:|
| Inicio con indicadores | ✔ | ✔ | — |
| Pedidos y cambio de estado | Todo | Avanza el estado | Solo sus entregas |
| Inventario y ajuste de stock | ✔ | ✔ | — |
| Clientes | ✔ | ✔ | — |
| Productos, categorías, usuarios | ✔ | — | — |
| Reportes R01–R08 (Excel, CSV, impresión) | Todos | R02, R04, R06, R07 | R07, R08 (propios) |
| Mi perfil (cambiar contraseña) | ✔ | ✔ | ✔ |

---

## Instalación con MySQL

1. Ejecuta en **MySQL 8.0.13 o superior**, en orden, los scripts de `BaseDatos/`:
   - `01_base_datos_ladonita.sql`: base `ladonita_gourmet`, tablas y datos iniciales.
   - `02_actualizacion_sistema.sql`: agrega `foto_url` e índices y protege las contraseñas iniciales (PBKDF2). Se puede repetir.
   - `03_reportes.sql` *(opcional)*: vistas R01–R08 para consultar desde Workbench.
   - `04_datos_ejemplo_OPCIONAL.sql`: **solo pruebas o capacitación**, nunca en la base real.
2. En `JavaNetbeans/src/main/resources/db.properties` pon `db.modo=mysql`, tu `db.user` y tu `db.password`.
3. Ejecuta el proyecto:

| Entorno | Cómo |
|---|---|
| **Doble clic** | `EJECUTAR.bat` → opción 1 (MySQL) o 2 (demo) |
| **NetBeans** | *Open Project* → carpeta `JavaNetbeans` → **Run (F6)** |
| **VS Code** | Abre la raíz del repositorio → *Terminal → Run Task* → "Ejecutar La Doñita…" |
| **Consola** | En `JavaNetbeans`: `mvn jetty:run` (otro puerto: `-Djetty.port=8081`) |
| **Tomcat 10.1+ / GlassFish 7** | En `JavaNetbeans`: `mvn clean package` → desplegar `target/ladonita.war` |

### Configuración
Cada clave de `db.properties` se puede reemplazar con una variable de entorno (tiene prioridad). **No subas tu contraseña de MySQL a GitHub**: usa `LADONITA_DB_PASSWORD`.

| Clave | Variable de entorno | Uso |
|---|---|---|
| `db.modo` | `LADONITA_DB_MODO` | `mysql` o `demo` |
| `db.url` · `db.user` · `db.password` | `LADONITA_DB_URL` · `_USER` · `_PASSWORD` | Conexión a MySQL |
| `app.whatsapp` | `LADONITA_APP_WHATSAPP` | Número del negocio (ej. `51912679851`) |
| `app.imagenes` | `LADONITA_APP_IMAGENES` | Carpeta de fotos subidas |
| — | `LADONITA_LOG_DIR` | Carpeta de logs |

La base del modo demo se crea sola en `~/ladonita-gourmet/demo`; si la borras, se vuelve a generar.

---

## Estructura del repositorio

```
La-Donita-Gourmet/
├── BaseDatos/      Scripts SQL (01 esquema · 02 actualización · 03 reportes · 04 datos de ejemplo)
├── JavaNetbeans/   Proyecto Maven (se abre en NetBeans): código Java, configuración y pom.xml
├── PaginaWeb/      Vistas JSP, estilos, JavaScript, imágenes y WEB-INF/web.xml
├── docs/           Manual de instalación y uso, preguntas frecuentes
├── EJECUTAR.bat    Arranque con doble clic (Windows)
└── .vscode/        Tareas para VS Code
```

El `pom.xml` de `JavaNetbeans` apunta a `../PaginaWeb` como carpeta web, así que al compilar se genera un solo `ladonita.war`.

El código Java está en `JavaNetbeans/src/main/java/com/ladonita/`, organizado en `controlador` (servlets), `modelo/entidades`, `modelo/dao` (+ `impl` con JDBC), `filtro` (UTF-8, CSRF, sesión) y `util`.

---

## Librerías externas

| Librería | Dónde se usa |
|---|---|
| **Google Guava** | `Cache` con expiración: bloqueo de 5 min tras 5 intentos de login (`AuthController`) y categorías del catálogo (`CacheCatalogo`). |
| **Apache POI** | `ExcelReporte`: exporta los reportes R01–R08 a Excel `.xlsx` con encabezados, formato de soles y totales con fórmula `SUM`. |
| **Apache Commons** | Lang y Validator en `Validador` (textos, números, correo); IO en las fotos de productos (`ProductoController`). |
| **Logback** (SLF4J) | `ladonita.log` (arranque y errores) y `auditoria.log` (quién hizo qué), con rotación diaria y 30 días de historial (`logback.xml`, `Bitacora`). |

## Seguridad
- Contraseñas con **PBKDF2-HMAC-SHA256** y sal aleatoria; mínimo 8 caracteres con letras y números.
- Consultas con **PreparedStatement**, token **CSRF** en los formularios y escape de salida contra **XSS**.
- Permisos por **rol**, filtro de sesión en `/admin/*`, bloqueo tras 5 intentos fallidos y sesión de 30 minutos.
- Fotos validadas por tipo real (JPG/PNG/WEBP, máx. 2 MB). CSV protegido contra inyección de fórmulas.

## Notas
- Los códigos (`PED001`, `CLI001`…) son `VARCHAR(6)` y admiten hasta **999 registros por prefijo**.
- "Stock bajo" = 10 unidades o menos.
- El sistema necesita **internet** la primera vez (Maven descarga dependencias) y en los navegadores (Bootstrap y fuentes se cargan desde la red).
