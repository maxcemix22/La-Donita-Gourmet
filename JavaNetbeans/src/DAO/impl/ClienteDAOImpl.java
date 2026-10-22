package DAO.impl;

import DAO.ClienteDAO;
import DAO.DAOException;
import Modelo.Cliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClienteDAOImpl implements ClienteDAO {
    private static final String SELECT =
            "SELECT id_cliente, nombres, apellidos, telefono, correo, direccion_referencia FROM cliente";

    @Override
    public Cliente buscarPorId(String idCliente) {
        return buscar(SELECT + " WHERE id_cliente = ?", idCliente);
    }

    /** Si hay varios clientes con el mismo teléfono devuelve el más reciente. */
    @Override
    public Cliente buscarPorTelefono(String telefono) {
        return buscar(SELECT + " WHERE telefono = ? ORDER BY id_cliente DESC LIMIT 1", telefono);
    }

    @Override
    public boolean insertar(Cliente cl) {
        String sql = "INSERT INTO cliente (id_cliente, nombres, apellidos, telefono, correo, direccion_referencia) VALUES (?,?,?,?,?,?)";
        try (Connection c = Conexion.obtener()) {
            String id = Ids.siguiente(c, "cliente", "id_cliente", "CLI");
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, id);
                ps.setString(2, cl.getNombres());
                ps.setString(3, cl.getApellidos());
                ps.setString(4, cl.getTelefono());
                ps.setString(5, cl.getCorreo());
                ps.setString(6, cl.getDireccionReferencia());
                boolean ok = ps.executeUpdate() == 1;
                if (ok) cl.setIdCliente(id);
                return ok;
            }
        } catch (SQLException e) {
            throw new DAOException("Error al insertar el cliente", e);
        }
    }

    private Cliente buscar(String sql, String parametro) {
        try (Connection c = Conexion.obtener();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new DAOException("Error al buscar el cliente", e);
        }
    }

    static Cliente mapear(ResultSet rs) throws SQLException {
        return new Cliente(rs.getString("id_cliente"), rs.getString("nombres"), rs.getString("apellidos"),
                rs.getString("telefono"), rs.getString("correo"), rs.getString("direccion_referencia"));
    }
}
