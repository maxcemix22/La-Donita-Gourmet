package DAO.impl;

import DAO.CategoriaDAO;
import DAO.DAOException;
import Modelo.Categoria;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAOImpl implements CategoriaDAO {
    private static final String SELECT =
            "SELECT id_categoria, nombre_categoria, descripcion, estado FROM categoria";

    @Override
    public List<Categoria> listarActivas() {
        try (Connection c = Conexion.obtener();
             PreparedStatement ps = c.prepareStatement(SELECT + " WHERE estado = 1 ORDER BY nombre_categoria");
             ResultSet rs = ps.executeQuery()) {
            List<Categoria> lista = new ArrayList<>();
            while (rs.next()) lista.add(mapear(rs));
            return lista;
        } catch (SQLException e) {
            throw new DAOException("Error al listar categorías", e);
        }
    }

    @Override
    public Categoria buscarPorId(String idCategoria) {
        try (Connection c = Conexion.obtener();
             PreparedStatement ps = c.prepareStatement(SELECT + " WHERE id_categoria = ?")) {
            ps.setString(1, idCategoria);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new DAOException("Error al buscar la categoría " + idCategoria, e);
        }
    }

    private static Categoria mapear(ResultSet rs) throws SQLException {
        return new Categoria(rs.getString("id_categoria"), rs.getString("nombre_categoria"),
                rs.getString("descripcion"), rs.getBoolean("estado"));
    }
}
