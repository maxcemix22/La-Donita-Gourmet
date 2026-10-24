package com.ladonita.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Fábrica de conexiones JDBC.
 * - Modo "mysql" (por defecto): usa db.url / db.user / db.password.
 * - Modo "demo": base H2 embebida en ~/ladonita-gourmet/demo, creada la primera vez con
 *   el script demo/ladonita-demo.sql (mismo esquema que MySQL + datos de ejemplo).
 */
public final class Conexion {

    private static final String URL_DEMO = "jdbc:h2:file:" + System.getProperty("user.home").replace('\\', '/')
            + "/ladonita-gourmet/demo/ladonita;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE";
    private static volatile boolean demoInicializada;

    static {
        cargarDriver(Config.modoDemo() ? "org.h2.Driver" : "com.mysql.cj.jdbc.Driver");
    }

    private Conexion() {
    }

    public static Connection getConexion() throws SQLException {
        if (Config.modoDemo()) {
            Connection cn = DriverManager.getConnection(Config.get("db.url.demo", URL_DEMO), "sa", "");
            if (!demoInicializada) {
                inicializarDemo(cn);
            }
            return cn;
        }
        return DriverManager.getConnection(
                Config.get("db.url", "jdbc:mysql://localhost:3306/ladonita_gourmet"),
                Config.get("db.user", "root"),
                Config.get("db.password", ""));
    }

    private static synchronized void inicializarDemo(Connection cn) throws SQLException {
        if (demoInicializada) {
            return;
        }
        boolean existe;
        try (ResultSet rs = cn.getMetaData().getTables(null, null, "categoria", null)) {
            existe = rs.next();
        }
        if (!existe) {
            try (Statement st = cn.createStatement()) {
                st.execute("RUNSCRIPT FROM 'classpath:demo/ladonita-demo.sql' CHARSET 'UTF-8'");
            }
        }
        demoInicializada = true;
    }

    private static void cargarDriver(String clase) {
        try {
            Class.forName(clase);
        } catch (ClassNotFoundException e) {
            // El driver puede registrarse solo vía ServiceLoader.
        }
    }
}
