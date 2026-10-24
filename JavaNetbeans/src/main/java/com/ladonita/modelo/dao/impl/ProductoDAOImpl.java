package com.ladonita.modelo.dao.impl;

import com.ladonita.modelo.dao.DAOException;
import com.ladonita.modelo.dao.ProductoDAO;
import com.ladonita.modelo.entidades.Categoria;
import com.ladonita.modelo.entidades.Producto;
import com.ladonita.util.GeneradorId;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAOImpl implements ProductoDAO {

    private static final String SELECT = "SELECT p.*, c.nombre_categoria FROM Producto p "
            + "JOIN Categoria c ON c.id_categoria = p.id_categoria ";

    private final Connection cn;

    public ProductoDAOImpl(Connection cn) {
        this.cn = cn;
    }

    @Override
    public boolean insertar(Producto p) {
        String sql = "INSERT INTO Producto (id_producto, id_categoria, nombre_producto, descripcion, precio, "
                + "foto_url, stock_disponible, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            p.setIdProducto(GeneradorId.siguiente(cn, "Producto", "id_producto", "PRO"));
            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setString(1, p.getIdProducto());
                ps.setString(2, p.getCategoria().getIdCategoria());
                ps.setString(3, p.getNombre());
                ps.setString(4, p.getDescripcion());
                ps.setBigDecimal(5, p.getPrecio());
                ps.setString(6, p.getFotoUrl());
                ps.setInt(7, p.getStock());
                ps.setBoolean(8, p.isEstado());
                return ps.executeUpdate() == 1;
            }
        } catch (SQLException e) {
            throw new DAOException("Error al registrar el producto", e);
        }
    }

    @Override
    public boolean actualizar(Producto p) {
        String sql = "UPDATE Producto SET id_categoria = ?, nombre_producto = ?, descripcion = ?, precio = ?, "
                + "foto_url = ?, stock_disponible = ?, estado = ? WHERE id_producto = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, p.getCategoria().getIdCategoria());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getDescripcion());
            ps.setBigDecimal(4, p.getPrecio());
            ps.setString(5, p.getFotoUrl());
            ps.setInt(6, p.getStock());
            ps.setBoolean(7, p.isEstado());
            ps.setString(8, p.getIdProducto());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DAOException("Error al actualizar el producto", e);
        }
    }

    @Override
    public boolean eliminar(String idProducto) {
        try {
            boolean tieneVentas;
            try (PreparedStatement ps = cn.prepareStatement("SELECT 1 FROM DetallePedido WHERE id_producto = ? LIMIT 1")) {
                ps.setString(1, idProducto);
                try (ResultSet rs = ps.executeQuery()) {
                    tieneVentas = rs.next();
                }
            }
            String sql = tieneVentas
                    ? "UPDATE Producto SET estado = FALSE WHERE id_producto = ?"
                    : "DELETE FROM Producto WHERE id_producto = ?";
            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setString(1, idProducto);
                ps.executeUpdate();
            }
            return !tieneVentas;
        } catch (SQLException e) {
            throw new DAOException("Error al eliminar el producto", e);
        }
    }

    @Override
    public List<Producto> listar() {
        return consultar(SELECT + "ORDER BY p.id_producto");
    }

    @Override
    public List<Producto> listarPorCategoria(String idCategoria) {
        return consultar(SELECT + "WHERE p.id_categoria = ? ORDER BY p.nombre_producto", idCategoria);
    }

    @Override
    public List<Producto> buscarCatalogo(String idCategoria, String texto) {
        StringBuilder sql = new StringBuilder(SELECT).append("WHERE p.estado = TRUE AND c.estado = TRUE ");
        List<String> params = new ArrayList<>();
        filtrar(sql, params, idCategoria, texto);
        sql.append("ORDER BY c.id_categoria, p.nombre_producto");
        return consultar(sql.toString(), params.toArray(new String[0]));
    }

    @Override
    public List<Producto> buscarAdmin(String texto, String idCategoria, Boolean activo) {
        StringBuilder sql = new StringBuilder(SELECT).append("WHERE 1 = 1 ");
        List<String> params = new ArrayList<>();
        filtrar(sql, params, idCategoria, texto);
        if (activo != null) {
            sql.append(activo ? "AND p.estado = TRUE " : "AND p.estado = FALSE ");
        }
        sql.append("ORDER BY p.id_producto");
        return consultar(sql.toString(), params.toArray(new String[0]));
    }

    private void filtrar(StringBuilder sql, List<String> params, String idCategoria, String texto) {
        if (idCategoria != null && !idCategoria.isBlank()) {
            sql.append("AND p.id_categoria = ? ");
            params.add(idCategoria);
        }
        if (texto != null && !texto.isBlank()) {
            sql.append("AND (LOWER(p.nombre_producto) LIKE ? OR LOWER(p.descripcion) LIKE ? OR LOWER(p.id_producto) LIKE ?) ");
            String patron = "%" + texto.trim().toLowerCase().replace("%", "").replace("_", "") + "%";
            params.add(patron);
            params.add(patron);
            params.add(patron);
        }
    }

    @Override
    public Producto buscarPorId(String id) {
        List<Producto> r = consultar(SELECT + "WHERE p.id_producto = ?", id);
        return r.isEmpty() ? null : r.get(0);
    }

    @Override
    public boolean ajustarStock(String idProducto, int nuevoStock) {
        try (PreparedStatement ps = cn.prepareStatement("UPDATE Producto SET stock_disponible = ? WHERE id_producto = ?")) {
            ps.setInt(1, nuevoStock);
            ps.setString(2, idProducto);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DAOException("Error al ajustar el stock", e);
        }
    }

    @Override
    public List<Producto> listarStockBajo() {
        return consultar(SELECT + "WHERE p.estado = TRUE AND p.stock_disponible <= " + Producto.STOCK_MINIMO
                + " ORDER BY p.stock_disponible, p.nombre_producto");
    }

    private List<Producto> consultar(String sql, String... params) {
        List<Producto> lista = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setString(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al consultar productos", e);
        }
        return lista;
    }

    private Producto mapear(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setIdProducto(rs.getString("id_producto"));
        p.setCategoria(new Categoria(rs.getString("id_categoria"), rs.getString("nombre_categoria")));
        p.setNombre(rs.getString("nombre_producto"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setPrecio(rs.getBigDecimal("precio"));
        p.setFotoUrl(rs.getString("foto_url"));
        p.setStock(rs.getInt("stock_disponible"));
        p.setEstado(rs.getBoolean("estado"));
        return p;
    }
}
