package com.ladonita.modelo.dao.impl;

import com.ladonita.modelo.dao.DAOException;
import com.ladonita.modelo.dao.UsuarioDAO;
import com.ladonita.modelo.entidades.Empleado;
import com.ladonita.modelo.entidades.Repartidor;
import com.ladonita.modelo.entidades.Rol;
import com.ladonita.modelo.entidades.Usuario;
import com.ladonita.util.GeneradorId;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAOImpl implements UsuarioDAO {

    private static final String COLUMNAS = "u.id_usuario, u.nombres, u.apellidos, u.dni, u.usuario, u.`contraseña` AS clave, "
            + "u.rol, u.telefono, u.correo, u.estado";
    private static final String SELECT_DETALLE = "SELECT " + COLUMNAS + ", "
            + "e.fecha_ingreso_laboral AS e_ingreso, e.turno, e.area_asignada, e.sueldo, "
            + "r.fecha_ingreso_laboral AS r_ingreso, r.placa_vehiculo, r.tipo_vehiculo, r.zona_reparto, r.licencia_conducir "
            + "FROM Usuario u LEFT JOIN Empleado e ON e.id_usuario = u.id_usuario "
            + "LEFT JOIN Repartidor r ON r.id_usuario = u.id_usuario ";

    private final Connection cn;

    public UsuarioDAOImpl(Connection cn) {
        this.cn = cn;
    }

    @Override
    public Usuario buscarPorUsuario(String usuario) {
        List<Usuario> r = consultar(SELECT_DETALLE + "WHERE u.usuario = ?", usuario);
        return r.isEmpty() ? null : r.get(0);
    }

    @Override
    public Usuario buscarPorId(String id) {
        List<Usuario> r = consultar(SELECT_DETALLE + "WHERE u.id_usuario = ?", id);
        return r.isEmpty() ? null : r.get(0);
    }

    @Override
    public List<Usuario> listar() {
        return consultar(SELECT_DETALLE + "ORDER BY u.estado DESC, u.rol, u.nombres");
    }

    @Override
    public List<Usuario> listarPorRol(Rol rol) {
        return consultar(SELECT_DETALLE + "WHERE u.rol = ? AND u.estado = TRUE ORDER BY u.nombres", rol.getValorBD());
    }

    @Override
    public boolean insertar(Usuario u) {
        String sql = "INSERT INTO Usuario (id_usuario, nombres, apellidos, dni, usuario, `contraseña`, rol, telefono, correo, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            u.setIdUsuario(GeneradorId.siguiente(cn, "Usuario", "id_usuario", "USR"));
            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setString(1, u.getIdUsuario());
                ps.setString(2, u.getNombres());
                ps.setString(3, u.getApellidos());
                ps.setString(4, u.getDni());
                ps.setString(5, u.getUsuario());
                ps.setString(6, u.getClaveHash());
                ps.setString(7, u.getRol().getValorBD());
                ps.setString(8, u.getTelefono());
                ps.setString(9, u.getCorreo());
                ps.setBoolean(10, u.isEstado());
                ps.executeUpdate();
            }
            guardarDatosLaborales(u);
            return true;
        } catch (SQLException e) {
            throw new DAOException("Error al registrar el usuario", e);
        }
    }

    @Override
    public boolean actualizar(Usuario u) {
        String sql = "UPDATE Usuario SET nombres = ?, apellidos = ?, dni = ?, usuario = ?, rol = ?, telefono = ?, correo = ? "
                + "WHERE id_usuario = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, u.getNombres());
            ps.setString(2, u.getApellidos());
            ps.setString(3, u.getDni());
            ps.setString(4, u.getUsuario());
            ps.setString(5, u.getRol().getValorBD());
            ps.setString(6, u.getTelefono());
            ps.setString(7, u.getCorreo());
            ps.setString(8, u.getIdUsuario());
            boolean ok = ps.executeUpdate() == 1;
            guardarDatosLaborales(u);
            return ok;
        } catch (SQLException e) {
            throw new DAOException("Error al actualizar el usuario", e);
        }
    }

    /** Deja una sola fila en Empleado o Repartidor según el rol actual (el admin no tiene ninguna). */
    private void guardarDatosLaborales(Usuario u) throws SQLException {
        for (String tabla : new String[]{"Empleado", "Repartidor"}) {
            try (PreparedStatement ps = cn.prepareStatement("DELETE FROM " + tabla + " WHERE id_usuario = ?")) {
                ps.setString(1, u.getIdUsuario());
                ps.executeUpdate();
            }
        }
        if (u instanceof Empleado e && u.getRol() == Rol.EMPLEADO) {
            String sql = "INSERT INTO Empleado (id_usuario, fecha_ingreso_laboral, turno, area_asignada, sueldo) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setString(1, e.getIdUsuario());
                ps.setDate(2, Date.valueOf(e.getFechaIngresoLaboral()));
                ps.setString(3, e.getTurno());
                ps.setString(4, e.getAreaAsignada());
                ps.setBigDecimal(5, e.getSueldo());
                ps.executeUpdate();
            }
        } else if (u instanceof Repartidor r && u.getRol() == Rol.REPARTIDOR) {
            String sql = "INSERT INTO Repartidor (id_usuario, fecha_ingreso_laboral, placa_vehiculo, tipo_vehiculo, zona_reparto, "
                    + "licencia_conducir) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setString(1, r.getIdUsuario());
                ps.setDate(2, Date.valueOf(r.getFechaIngresoLaboral()));
                ps.setString(3, r.getPlacaVehiculo());
                ps.setString(4, r.getTipoVehiculo());
                ps.setString(5, r.getZonaReparto());
                ps.setString(6, r.getLicencia());
                ps.executeUpdate();
            }
        }
    }

    @Override
    public boolean cambiarEstado(String id, boolean activo) {
        return actualizarCampo("UPDATE Usuario SET estado = ? WHERE id_usuario = ?", activo, id);
    }

    @Override
    public boolean cambiarClave(String id, String claveHash) {
        try (PreparedStatement ps = cn.prepareStatement("UPDATE Usuario SET `contraseña` = ? WHERE id_usuario = ?")) {
            ps.setString(1, claveHash);
            ps.setString(2, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DAOException("Error al cambiar la contraseña", e);
        }
    }

    @Override
    public boolean existeUsuario(String usuario, String excluirId) {
        return existe("SELECT 1 FROM Usuario WHERE LOWER(usuario) = LOWER(?) AND id_usuario <> ?", usuario, excluirId);
    }

    @Override
    public boolean existeDni(String dni, String excluirId) {
        return existe("SELECT 1 FROM Usuario WHERE dni = ? AND id_usuario <> ?", dni, excluirId);
    }

    private boolean existe(String sql, String valor, String excluirId) {
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, valor);
            ps.setString(2, excluirId == null ? "" : excluirId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DAOException("Error al validar el usuario", e);
        }
    }

    private boolean actualizarCampo(String sql, boolean valor, String id) {
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBoolean(1, valor);
            ps.setString(2, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DAOException("Error al actualizar el usuario", e);
        }
    }

    private List<Usuario> consultar(String sql, String... params) {
        List<Usuario> lista = new ArrayList<>();
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
            throw new DAOException("Error al consultar usuarios", e);
        }
        return lista;
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Rol rol = Rol.desdeBD(rs.getString("rol"));
        Usuario u;
        if (rol == Rol.EMPLEADO) {
            Empleado e = new Empleado();
            Date f = rs.getDate("e_ingreso");
            e.setFechaIngresoLaboral(f == null ? null : f.toLocalDate());
            e.setTurno(rs.getString("turno"));
            e.setAreaAsignada(rs.getString("area_asignada"));
            e.setSueldo(rs.getBigDecimal("sueldo"));
            u = e;
        } else if (rol == Rol.REPARTIDOR) {
            Repartidor r = new Repartidor();
            Date f = rs.getDate("r_ingreso");
            r.setFechaIngresoLaboral(f == null ? null : f.toLocalDate());
            r.setPlacaVehiculo(rs.getString("placa_vehiculo"));
            r.setTipoVehiculo(rs.getString("tipo_vehiculo"));
            r.setZonaReparto(rs.getString("zona_reparto"));
            r.setLicencia(rs.getString("licencia_conducir"));
            u = r;
        } else {
            u = new Usuario();
        }
        u.setIdUsuario(rs.getString("id_usuario"));
        u.setNombres(rs.getString("nombres"));
        u.setApellidos(rs.getString("apellidos"));
        u.setDni(rs.getString("dni"));
        u.setUsuario(rs.getString("usuario"));
        u.setClaveHash(rs.getString("clave"));
        u.setRol(rol);
        u.setTelefono(rs.getString("telefono"));
        u.setCorreo(rs.getString("correo"));
        u.setEstado(rs.getBoolean("estado"));
        return u;
    }
}
