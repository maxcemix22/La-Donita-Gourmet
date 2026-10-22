package DAO.impl;

import DAO.DAOException;
import DAO.ProductoDAO;
import Modelo.Categoria;
import Modelo.Producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAOImpl implements ProductoDAO {
    /** Columnas y joins reutilizados por PedidoDAOImpl para cargar los productos de un detalle. */
    static final String COLUMNAS =
            "p.id_producto, p.nombre_producto, p.descripcion, p.precio, p.foto_url, p.stock_disponible, p.estado, "
            + "c.id_categoria, c.nombre_categoria, c.descripcion AS descripcion_categoria, c.estado AS estado_categoria";
    static final String DESDE =
            " FROM producto p JOIN categoria c ON c.id_categoria = p.id_categoria";
    private static final String SELECT = "SELECT " + COLUMNAS + DESDE;

    @Override
    public List<Producto> listarActivos() {
        return listar(SELECT + " WHERE p.estado = 1 AND c.estado = 1 ORDER BY c.nombre_categoria, p.nombre_producto", null);
    }

    @Override
    public List<Producto> listarPorCategoria(String idCategoria) {
        return listar(SELECT + " WHERE p.estado = 1 AND p.id_categoria = ? ORDER BY p.nombre_producto", idCategoria);
    }

    @Override
    public Producto buscarPorId(String idProducto) {
        List<Producto> r = listar(SELECT + " WHERE p.id_producto = ?", idProducto);
        return r.isEmpty() ? null : r.get(0);
    }

    @Override
    public boolean insertar(Producto pr) {
        String sql = "INSERT INTO producto (id_producto, id_categoria, nombre_producto, descripcion, precio, foto_url, stock_disponible, estado) "
                + "VALUES (?,?,?,?,?,?,?,?)";
        try (Connection c = Conexion.obtener()) {
            String id = Ids.siguiente(c, "producto", "id_producto", "PRO");
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, id);
                ps.setString(2, pr.getCategoria().getIdCategoria());
                ps.setString(3, pr.getNombre());
                ps.setString(4, pr.getDescripcion());
                ps.setBigDecimal(5, pr.getPrecio());
                ps.setString(6, pr.getFotoUrl());
                ps.setInt(7, pr.getStock());
                ps.setBoolean(8, pr.isEstado());
                boolean ok = ps.executeUpdate() == 1;
                if (ok) pr.setIdProducto(id);
                return ok;
            }
        } catch (SQLException e) {
            throw new DAOException("Error al insertar el producto", e);
        }
    }

    @Override
    public boolean actualizar(Producto pr) {
        String sql = "UPDATE producto SET id_categoria=?, nombre_producto=?, descripcion=?, precio=?, foto_url=?, "
                + "stock_disponible=?, estado=? WHERE id_producto=?";
        try (Connection c = Conexion.obtener();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, pr.getCategoria().getIdCategoria());
            ps.setString(2, pr.getNombre());
            ps.setString(3, pr.getDescripcion());
            ps.setBigDecimal(4, pr.getPrecio());
            ps.setString(5, pr.getFotoUrl());
            ps.setInt(6, pr.getStock());
            ps.setBoolean(7, pr.isEstado());
            ps.setString(8, pr.getIdProducto());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DAOException("Error al actualizar el producto " + pr.getIdProducto(), e);
        }
    }

    @Override
    public boolean eliminar(String idProducto) {
        try (Connection c = Conexion.obtener()) {
            boolean usado;
            try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM detallepedido WHERE id_producto = ? LIMIT 1")) {
                ps.setString(1, idProducto);
                try (ResultSet rs = ps.executeQuery()) { usado = rs.next(); }
            }
            String sql = usado ? "UPDATE producto SET estado = 0 WHERE id_producto = ?"
                               : "DELETE FROM producto WHERE id_producto = ?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, idProducto);
                return ps.executeUpdate() == 1;
            }
        } catch (SQLException e) {
            throw new DAOException("Error al eliminar el producto " + idProducto, e);
        }
    }

    private List<Producto> listar(String sql, String parametro) {
        try (Connection c = Conexion.obtener();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (parametro != null) ps.setString(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                List<Producto> lista = new ArrayList<>();
                while (rs.next()) lista.add(mapear(rs));
                return lista;
            }
        } catch (SQLException e) {
            throw new DAOException("Error al consultar productos", e);
        }
    }

    static Producto mapear(ResultSet rs) throws SQLException {
        Categoria cat = new Categoria(rs.getString("id_categoria"), rs.getString("nombre_categoria"),
                rs.getString("descripcion_categoria"), rs.getBoolean("estado_categoria"));
        return new Producto(rs.getString("id_producto"), cat, rs.getString("nombre_producto"),
                rs.getString("descripcion"), rs.getBigDecimal("precio"), rs.getString("foto_url"),
                rs.getInt("stock_disponible"), rs.getBoolean("estado"));
    }
}
