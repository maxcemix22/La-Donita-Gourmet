package DAO.impl;

import DAO.DAOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Conexión a MySQL. Lee la configuración de db.properties (en la carpeta desde donde se ejecuta, o sea
 * JavaNetbeans/ al correr desde NetBeans) y, si existen, de las variables de entorno DB_URL, DB_USER y DB_PASSWORD.
 * Copia db.properties.example a db.properties y pon tus datos; ese archivo NO se sube a Git.
 */
public final class Conexion {
    private static final String ARCHIVO = "db.properties";
    private static Properties config;

    private Conexion() {}

    public static Connection obtener() {
        Properties p = cargarConfig();
        try {
            return DriverManager.getConnection(p.getProperty("db.url"), p.getProperty("db.user"), p.getProperty("db.password"));
        } catch (SQLException e) {
            throw new DAOException("No se pudo conectar a la base de datos: " + e.getMessage(), e);
        }
    }

    private static synchronized Properties cargarConfig() {
        if (config != null) return config;
        Properties p = new Properties();
        File f = new File(ARCHIVO);
        if (f.isFile()) {
            try (Reader r = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8)) {
                p.load(r);
            } catch (IOException e) {
                throw new DAOException("No se pudo leer " + ARCHIVO, e);
            }
        }
        sobrescribir(p, "db.url", "DB_URL");
        sobrescribir(p, "db.user", "DB_USER");
        sobrescribir(p, "db.password", "DB_PASSWORD");
        if (p.getProperty("db.url") == null || p.getProperty("db.user") == null) {
            throw new DAOException("Falta la configuración de la base de datos. Copia db.properties.example a db.properties "
                    + "(en la carpeta JavaNetbeans) y completa url, usuario y contraseña.");
        }
        if (p.getProperty("db.password") == null) p.setProperty("db.password", "");
        config = p;
        return p;
    }

    private static void sobrescribir(Properties p, String clave, String variableEntorno) {
        String valor = System.getenv(variableEntorno);
        if (valor != null && !valor.isEmpty()) p.setProperty(clave, valor);
    }
}
