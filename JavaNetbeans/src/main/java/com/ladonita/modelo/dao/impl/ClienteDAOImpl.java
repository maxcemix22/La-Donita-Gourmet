package com.ladonita.modelo.dao.impl;

import com.ladonita.modelo.dao.ClienteDAO;
import com.ladonita.modelo.dao.DAOException;
import com.ladonita.modelo.entidades.Cliente;
import com.ladonita.util.GeneradorId;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAOImpl implements ClienteDAO {

    private final Connection cn;

    public ClienteDAOImpl(Connection cn) {
        this.cn = cn;
    }

    @Override
    public boolean insertar(Cliente c) {
        String sql = "INSERT INTO Cliente (id_cliente, nombres, apellidos, telefono, correo, direccion_referencia) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try {
            c.setIdCliente(GeneradorId.siguiente(cn, "Cliente", "id_cliente", "CLI"));
            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setString(1, c.getIdCliente());
                ps.setString(2, c.getNombres());
                ps.setString(3, c.getApellidos());
                ps.setString(4, c.getTelefono());
                ps.setString(5, c.getCorreo());
                ps.setString(6, c.getDireccionReferencia());
                return ps.executeUpdate() == 1;
            }
        } catch (SQLException e) {
            throw new DAOException("Error al registrar el cliente", e);
        }
    }

    @Override
    public boolean actualizar(Cliente c) {
        String sql = "UPDATE Cliente SET nombres = ?, apellidos = ?, telefono = ?, correo = ?, direccion_referencia = ? "
                + "WHERE id_cliente = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getNombres());
            ps.setString(2, c.getApellidos());
            ps.setString(3, c.getTelefono());
            ps.setString(4, c.getCorreo());
            ps.setString(5, c.getDireccionReferencia());
            ps.setString(6, c.getIdCliente());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DAOException("Error al actualizar el cliente", e);
        }
    }

    @Override
    public Cliente buscarPorId(String id) {
        return buscarUno("SELECT * FROM Cliente WHERE id_cliente = ?", id);
    }

    @Override
    public Cliente buscarPorTelefono(String telefono) {
        return buscarUno("SELECT * FROM Cliente WHERE telefono = ? ORDER BY id_cliente LIMIT 1", telefono);
    }

    @Override
    public List<Cliente> listar() {
        List<Cliente> lista = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement("SELECT * FROM Cliente ORDER BY apellidos, nombres");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Error al listar clientes", e);
        }
        return lista;
    }

    @Override
    public List<Cliente> buscarConEstadisticas(String texto) {
        StringBuilder sql = new StringBuilder("SELECT c.*, COUNT(p.id_pedido) AS cantidad, "
                + "COALESCE(SUM(p.total_pedido), 0) AS monto, MAX(p.fecha_pedido) AS ultimo "
                + "FROM Cliente c LEFT JOIN Pedido p ON p.id_cliente = c.id_cliente ");
        String patron = null;
        if (texto != null && !texto.isBlank()) {
            sql.append("WHERE LOWER(CONCAT(c.nombres, ' ', c.apellidos)) LIKE ? OR c.telefono LIKE ? OR LOWER(c.correo) LIKE ? ");
            patron = "%" + texto.trim().toLowerCase().replace("%", "").replace("_", "") + "%";
        }
        sql.append("GROUP BY c.id_cliente, c.nombres, c.apellidos, c.telefono, c.correo, c.direccion_referencia, c.fecha_registro "
                + "ORDER BY monto DESC, c.apellidos");
        List<Cliente> lista = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            if (patron != null) {
                ps.setString(1, patron);
                ps.setString(2, patron);
                ps.setString(3, patron);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Cliente c = mapear(rs);
                    c.setCantidadPedidos(rs.getInt("cantidad"));
                    c.setMontoTotal(rs.getBigDecimal("monto"));
                    c.setUltimoPedido(rs.getObject("ultimo", LocalDateTime.class));
                    lista.add(c);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al listar clientes", e);
        }
        return lista;
    }

    private Cliente buscarUno(String sql, String parametro) {
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new DAOException("Error al buscar el cliente", e);
        }
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setIdCliente(rs.getString("id_cliente"));
        c.setNombres(rs.getString("nombres"));
        c.setApellidos(rs.getString("apellidos"));
        c.setTelefono(rs.getString("telefono"));
        c.setCorreo(rs.getString("correo"));
        c.setDireccionReferencia(rs.getString("direccion_referencia"));
        Date f = rs.getDate("fecha_registro");
        c.setFechaRegistro(f == null ? null : f.toLocalDate());
        return c;
    }
}
