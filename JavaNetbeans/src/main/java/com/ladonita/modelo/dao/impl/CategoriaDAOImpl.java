package com.ladonita.modelo.dao.impl;

import com.ladonita.modelo.dao.CategoriaDAO;
import com.ladonita.modelo.dao.DAOException;
import com.ladonita.modelo.entidades.Categoria;
import com.ladonita.util.GeneradorId;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAOImpl implements CategoriaDAO {

    private final Connection cn;

    public CategoriaDAOImpl(Connection cn) {
        this.cn = cn;
    }

    @Override
    public List<Categoria> listar() {
        return consultar("SELECT c.*, 0 AS cantidad FROM Categoria c WHERE c.estado = TRUE ORDER BY c.id_categoria");
    }

    @Override
    public List<Categoria> listarTodas() {
        return consultar("SELECT c.*, (SELECT COUNT(*) FROM Producto p WHERE p.id_categoria = c.id_categoria) AS cantidad "
                + "FROM Categoria c ORDER BY c.id_categoria");
    }

    @Override
    public Categoria buscarPorId(String id) {
        try (PreparedStatement ps = cn.prepareStatement("SELECT c.*, 0 AS cantidad FROM Categoria c WHERE c.id_categoria = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new DAOException("Error al buscar la categoría", e);
        }
    }

    @Override
    public boolean insertar(Categoria c) {
        String sql = "INSERT INTO Categoria (id_categoria, nombre_categoria, descripcion, estado) VALUES (?, ?, ?, ?)";
        try {
            c.setIdCategoria(GeneradorId.siguiente(cn, "Categoria", "id_categoria", "CAT"));
            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setString(1, c.getIdCategoria());
                ps.setString(2, c.getNombre());
                ps.setString(3, c.getDescripcion());
                ps.setBoolean(4, c.isEstado());
                return ps.executeUpdate() == 1;
            }
        } catch (SQLException e) {
            throw new DAOException("Error al registrar la categoría", e);
        }
    }

    @Override
    public boolean actualizar(Categoria c) {
        String sql = "UPDATE Categoria SET nombre_categoria = ?, descripcion = ?, estado = ? WHERE id_categoria = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getDescripcion());
            ps.setBoolean(3, c.isEstado());
            ps.setString(4, c.getIdCategoria());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DAOException("Error al actualizar la categoría", e);
        }
    }

    @Override
    public boolean cambiarEstado(String id, boolean activa) {
        try (PreparedStatement ps = cn.prepareStatement("UPDATE Categoria SET estado = ? WHERE id_categoria = ?")) {
            ps.setBoolean(1, activa);
            ps.setString(2, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DAOException("Error al cambiar el estado de la categoría", e);
        }
    }

    private List<Categoria> consultar(String sql) {
        List<Categoria> lista = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Error al listar categorías", e);
        }
        return lista;
    }

    private Categoria mapear(ResultSet rs) throws SQLException {
        Categoria c = new Categoria(rs.getString("id_categoria"), rs.getString("nombre_categoria"));
        c.setDescripcion(rs.getString("descripcion"));
        c.setEstado(rs.getBoolean("estado"));
        c.setCantidadProductos(rs.getInt("cantidad"));
        return c;
    }
}
