package com.ladonita.util;

import com.google.common.base.Preconditions;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Genera IDs correlativos con el formato de la BD (CLI001, PED001, DET001...).
 * Debe llamarse dentro de una transacción: el FOR UPDATE bloquea el último
 * registro para que dos pedidos simultáneos no obtengan el mismo código.
 */
public final class GeneradorId {

    private GeneradorId() {
    }

    public static String siguiente(Connection cn, String tabla, String columna, String prefijo) throws SQLException {
        // tabla y columna son constantes internas, nunca datos del usuario (Guava Preconditions lo garantiza).
        Preconditions.checkArgument(tabla.matches("[A-Za-z]+") && columna.matches("[a-z_]+"), "Tabla o columna no válida");
        Preconditions.checkArgument(prefijo != null && prefijo.matches("[A-Z]{3}"), "El prefijo debe tener 3 letras");
        String sql = "SELECT " + columna + " FROM " + tabla + " WHERE " + columna + " LIKE ? ORDER BY " + columna
                + " DESC LIMIT 1 FOR UPDATE";
        int numero = 0;
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, prefijo + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    numero = Integer.parseInt(rs.getString(1).substring(prefijo.length()));
                }
            }
        }
        if (numero >= 999) {
            throw new SQLException("Se alcanzó el máximo de códigos para " + tabla);
        }
        return String.format("%s%03d", prefijo, numero + 1);
    }
}
