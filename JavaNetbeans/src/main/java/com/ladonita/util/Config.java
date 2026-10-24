package com.ladonita.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Configuración de la aplicación (src/main/resources/db.properties).
 * Cada clave puede sobrescribirse con una variable de entorno o propiedad del sistema:
 * db.url -> LADONITA_DB_URL, app.whatsapp -> LADONITA_APP_WHATSAPP, etc.
 */
public final class Config {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                PROPS.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new ExceptionInInitializerError("No se pudo leer db.properties: " + e.getMessage());
        }
    }

    private Config() {
    }

    public static String get(String clave, String porDefecto) {
        String variable = "LADONITA_" + clave.toUpperCase().replace('.', '_');
        String v = System.getenv(variable);
        if (v == null) {
            v = System.getProperty(variable);
        }
        if (v == null) {
            v = PROPS.getProperty(clave);
        }
        return v == null || v.isBlank() && !clave.equals("db.password") ? porDefecto : v.trim();
    }

    /** "mysql" (por defecto) o "demo" (base H2 embebida, sin instalar nada). */
    public static boolean modoDemo() {
        return "demo".equalsIgnoreCase(get("db.modo", "mysql"));
    }

    /** Número de WhatsApp del negocio con código de país, solo dígitos (ej. 51912679851). */
    public static String whatsapp() {
        return get("app.whatsapp", "51912679851").replaceAll("\\D", "");
    }

    /** Carpeta donde se guardan las fotos subidas desde el panel (fuera del WAR para no perderlas). */
    public static Path carpetaImagenes() {
        String ruta = get("app.imagenes", System.getProperty("user.home") + "/ladonita-gourmet/imagenes");
        return Path.of(ruta);
    }
}
