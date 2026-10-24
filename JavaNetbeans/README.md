# JavaNetbeans

Proyecto Maven (Java 17+, Jakarta Servlet 6, JSP/JSTL). La parte web (JSP, CSS, JS e imágenes) está en `../PaginaWeb`.

## Abrir y ejecutar
- **NetBeans:** *File → Open Project* → esta carpeta → **Run (F6)**. Levanta Jetty en http://localhost:8080/ladonita.
  Para probar sin MySQL: clic derecho al proyecto → *Run Maven → Ejecutar en modo DEMO*.
- **Consola:** `mvn jetty:run` (otro puerto: `-Djetty.port=8081`). Generar el WAR: `mvn clean package` → `target/ladonita.war`.
- **Doble clic:** `EJECUTAR.bat` en la raíz del repositorio.

## Configuración
`src/main/resources/db.properties`: `db.modo` (`mysql` o `demo`), `db.url`, `db.user`, `db.password` y `app.whatsapp`.
Cada clave se puede sobrescribir con variables de entorno `LADONITA_*` (ver README de la raíz). No subas tu contraseña de MySQL a Git:
usa la variable `LADONITA_DB_PASSWORD`.

La primera vez Maven descarga las dependencias, así que necesita internet.
