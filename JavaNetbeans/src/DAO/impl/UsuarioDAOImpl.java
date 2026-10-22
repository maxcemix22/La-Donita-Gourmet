package DAO.impl;

import DAO.DAOException;
import DAO.UsuarioDAO;
import Modelo.Empleado;
import Modelo.Repartidor;
import Modelo.Usuario;
import Util.Contrasenas;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAOImpl implements UsuarioDAO {
    private static final String USUARIO_COLS =
            "u.id_usuario, u.nombres, u.apellidos, u.dni, u.usuario, u.rol, u.telefono, u.correo, u.estado";
    private static final String SELECT_USUARIO = "SELECT " + USUARIO_COLS + " FROM usuario u";
    private static final String SELECT_EMPLEADO = "SELECT " + USUARIO_COLS
            + ", e.fecha_ingreso_laboral, e.turno, e.area_asignada, e.sueldo"
            + " FROM usuario u JOIN empleado e ON e.id_usuario = u.id_usuario";
    private static final String SELECT_REPARTIDOR = "SELECT " + USUARIO_COLS
            + ", r.fecha_ingreso_laboral, r.placa_vehiculo, r.tipo_vehiculo, r.zona_reparto, r.licencia_conducir"
            + " FROM usuario u JOIN repartidor r ON r.id_usuario = u.id_usuario";

    @Override
    public Usuario buscarPorUsuario(String usuario) {
        try (Connection c = Conexion.obtener()) {
            String rol;
            try (PreparedStatement ps = c.prepareStatement("SELECT rol FROM usuario WHERE usuario = ?")) {
                ps.setString(1, usuario);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return null;
                    rol = rs.getString(1);
                }
            }
            String sql = switch (rol) {
                case Usuario.ROL_EMPLEADO -> SELECT_EMPLEADO;
                case Usuario.ROL_REPARTIDOR -> SELECT_REPARTIDOR;
                default -> SELECT_USUARIO;
            } + " WHERE u.usuario = ?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, usuario);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return null;
                    return switch (rol) {
                        case Usuario.ROL_EMPLEADO -> mapearEmpleado(rs);
                        case Usuario.ROL_REPARTIDOR -> mapearRepartidor(rs);
                        default -> mapearUsuario(rs);
                    };
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al buscar el usuario " + usuario, e);
        }
    }

    /** true si el usuario existe, está activo y la contraseña coincide con el hash guardado. */
    @Override
    public boolean autenticar(String usuario, String contrasena) {
        try (Connection c = Conexion.obtener();
             PreparedStatement ps = c.prepareStatement("SELECT `contraseña`, estado FROM usuario WHERE usuario = ?")) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean("estado") && Contrasenas.verificar(contrasena, rs.getString(1));
            }
        } catch (SQLException e) {
            throw new DAOException("Error al autenticar", e);
        }
    }

    @Override
    public boolean insertar(Usuario u, String contrasenaHash) {
        if (!Contrasenas.esHash(contrasenaHash)) {
            throw new IllegalArgumentException("La contraseña debe llegar hasheada (Util.Contrasenas.hash)");
        }
        if (Usuario.ROL_EMPLEADO.equals(u.getRol()) && !(u instanceof Empleado)
                || Usuario.ROL_REPARTIDOR.equals(u.getRol()) && !(u instanceof Repartidor)) {
            throw new IllegalArgumentException("Un usuario con rol " + u.getRol() + " debe crearse como objeto de esa clase");
        }
        String sql = "INSERT INTO usuario (id_usuario, nombres, apellidos, dni, usuario, `contraseña`, rol, telefono, correo, estado) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = Conexion.obtener()) {
            c.setAutoCommit(false);
            try {
                String id = Ids.siguiente(c, "usuario", "id_usuario", "USR");
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setString(1, id);
                    ps.setString(2, u.getNombres());
                    ps.setString(3, u.getApellidos());
                    ps.setString(4, u.getDni());
                    ps.setString(5, u.getUsuario());
                    ps.setString(6, contrasenaHash);
                    ps.setString(7, u.getRol());
                    ps.setString(8, u.getTelefono());
                    ps.setString(9, u.getCorreo());
                    ps.setBoolean(10, u.isEstado());
                    ps.executeUpdate();
                }
                if (u instanceof Empleado e) insertarEmpleado(c, id, e);
                if (u instanceof Repartidor r) insertarRepartidor(c, id, r);
                c.commit();
                u.setId(id);
                return true;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DAOException("Error al insertar el usuario " + u.getUsuario(), e);
        }
    }

    @Override
    public List<Repartidor> listarRepartidoresActivos() {
        try (Connection c = Conexion.obtener();
             PreparedStatement ps = c.prepareStatement(SELECT_REPARTIDOR + " WHERE u.estado = 1 ORDER BY u.apellidos, u.nombres");
             ResultSet rs = ps.executeQuery()) {
            List<Repartidor> lista = new ArrayList<>();
            while (rs.next()) lista.add(mapearRepartidor(rs));
            return lista;
        } catch (SQLException e) {
            throw new DAOException("Error al listar repartidores", e);
        }
    }

    /** Usado por PedidoDAOImpl: repartidor activo por id, o null si no existe. */
    static Repartidor repartidorPorId(Connection c, String idUsuario) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(SELECT_REPARTIDOR + " WHERE u.id_usuario = ?")) {
            ps.setString(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapearRepartidor(rs) : null;
            }
        }
    }

    private static void insertarEmpleado(Connection c, String id, Empleado e) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO empleado (id_usuario, fecha_ingreso_laboral, turno, area_asignada, sueldo) VALUES (?,?,?,?,?)")) {
            ps.setString(1, id);
            ps.setDate(2, Date.valueOf(e.getFechaIngresoLaboral()));
            ps.setString(3, e.getTurno());
            ps.setString(4, e.getAreaAsignada());
            ps.setBigDecimal(5, e.getSueldo());
            ps.executeUpdate();
        }
    }

    private static void insertarRepartidor(Connection c, String id, Repartidor r) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO repartidor (id_usuario, fecha_ingreso_laboral, placa_vehiculo, tipo_vehiculo, zona_reparto, licencia_conducir) VALUES (?,?,?,?,?,?)")) {
            ps.setString(1, id);
            ps.setDate(2, Date.valueOf(r.getFechaIngresoLaboral()));
            ps.setString(3, r.getPlacaVehiculo());
            ps.setString(4, r.getTipoVehiculo());
            ps.setString(5, r.getZonaReparto());
            ps.setString(6, r.getLicenciaConducir());
            ps.executeUpdate();
        }
    }

    private static Usuario mapearUsuario(ResultSet rs) throws SQLException {
        return new Usuario(rs.getString("id_usuario"), rs.getString("nombres"), rs.getString("apellidos"),
                rs.getString("dni"), rs.getString("usuario"), rs.getString("rol"), rs.getString("telefono"),
                rs.getString("correo"), rs.getBoolean("estado"));
    }

    private static Empleado mapearEmpleado(ResultSet rs) throws SQLException {
        return new Empleado(rs.getString("id_usuario"), rs.getString("nombres"), rs.getString("apellidos"),
                rs.getString("dni"), rs.getString("usuario"), rs.getString("telefono"), rs.getString("correo"),
                rs.getBoolean("estado"), rs.getDate("fecha_ingreso_laboral").toLocalDate(), rs.getString("turno"),
                rs.getString("area_asignada"), rs.getBigDecimal("sueldo"));
    }

    private static Repartidor mapearRepartidor(ResultSet rs) throws SQLException {
        return new Repartidor(rs.getString("id_usuario"), rs.getString("nombres"), rs.getString("apellidos"),
                rs.getString("dni"), rs.getString("usuario"), rs.getString("telefono"), rs.getString("correo"),
                rs.getBoolean("estado"), rs.getDate("fecha_ingreso_laboral").toLocalDate(),
                rs.getString("placa_vehiculo"), rs.getString("tipo_vehiculo"), rs.getString("zona_reparto"),
                rs.getString("licencia_conducir"));
    }
}
