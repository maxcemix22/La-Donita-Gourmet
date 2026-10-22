package DAO.impl;

import DAO.DAOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Genera los ids con el formato de la BD: prefijo + 3 dígitos (CLI001, PED002, ...).
 * Las columnas son varchar(6), por eso cada tabla admite como máximo 999 registros.
 */
final class Ids {
    private Ids() {}

    /** Último número usado en la tabla (0 si está vacía). Debe llamarse dentro de la transacción que inserta. */
    static int ultimo(Connection c, String tabla, String columna, String prefijo) throws SQLException {
        if (!tabla.matches("[a-z_]+") || !columna.matches("[a-z_]+")) throw new IllegalArgumentException("Tabla o columna no válida");
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(" + columna + ", ?) AS UNSIGNED)), 0) FROM " + tabla;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, prefijo.length() + 1);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    static String formatear(String prefijo, int numero) {
        if (numero > 999) throw new DAOException("Se agotaron los ids " + prefijo + "### (la BD usa varchar(6): máximo 999 por tabla)");
        return prefijo + String.format("%03d", numero);
    }

    static String siguiente(Connection c, String tabla, String columna, String prefijo) throws SQLException {
        return formatear(prefijo, ultimo(c, tabla, columna, prefijo) + 1);
    }
}
