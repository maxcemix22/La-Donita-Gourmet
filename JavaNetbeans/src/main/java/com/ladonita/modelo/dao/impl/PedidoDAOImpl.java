package com.ladonita.modelo.dao.impl;

import com.ladonita.modelo.dao.DAOException;
import com.ladonita.modelo.dao.PedidoDAO;
import com.ladonita.modelo.dao.StockInsuficienteException;
import com.ladonita.modelo.entidades.Categoria;
import com.ladonita.modelo.entidades.Cliente;
import com.ladonita.modelo.entidades.DatosEvento;
import com.ladonita.modelo.entidades.DetallePedido;
import com.ladonita.modelo.entidades.EstadoPedido;
import com.ladonita.modelo.entidades.MetodoEntrega;
import com.ladonita.modelo.entidades.Pedido;
import com.ladonita.modelo.entidades.Producto;
import com.ladonita.modelo.entidades.TipoPedido;
import com.ladonita.modelo.entidades.Usuario;
import com.ladonita.util.GeneradorId;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class PedidoDAOImpl implements PedidoDAO {

    private static final String SELECT = "SELECT p.*, c.nombres AS c_nombres, c.apellidos AS c_apellidos, "
            + "c.telefono AS c_telefono, c.correo AS c_correo, c.direccion_referencia AS c_direccion, "
            + "u.nombres AS r_nombres, u.apellidos AS r_apellidos, u.telefono AS r_telefono "
            + "FROM Pedido p JOIN Cliente c ON c.id_cliente = p.id_cliente "
            + "LEFT JOIN Usuario u ON u.id_usuario = p.id_repartidor ";

    private final Connection cn;

    public PedidoDAOImpl(Connection cn) {
        this.cn = cn;
    }

    @Override
    public boolean insertar(Pedido p) {
        try {
            p.setIdPedido(GeneradorId.siguiente(cn, "Pedido", "id_pedido", "PED"));
            if (p.getFecha() == null) {
                p.setFecha(LocalDateTime.now().withNano(0));
            }
            insertarCabecera(p);
            insertarDetalles(p);
            if (p.isCatering() && p.getEvento() != null) {
                insertarEvento(p);
            }
            return true;
        } catch (SQLException e) {
            throw new DAOException("Error al registrar el pedido", e);
        }
    }

    private void insertarCabecera(Pedido p) throws SQLException {
        String sql = "INSERT INTO Pedido (id_pedido, id_cliente, id_repartidor, fecha_pedido, fecha_entrega_estimada, "
                + "estado_pedido, tipo_pedido, metodo_entrega, direccion_entrega, total_pedido, observaciones) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, p.getIdPedido());
            ps.setString(2, p.getCliente().getIdCliente());
            ps.setString(3, p.getRepartidor() == null ? null : p.getRepartidor().getIdUsuario());
            ps.setObject(4, p.getFecha());
            if (p.getFechaEntrega() == null) {
                ps.setNull(5, Types.TIMESTAMP);
            } else {
                ps.setObject(5, p.getFechaEntrega());
            }
            ps.setString(6, p.getEstado().getValorBD());
            ps.setString(7, p.getTipo().getValorBD());
            ps.setString(8, p.getMetodoEntrega().getValorBD());
            ps.setString(9, p.getDireccionEntrega());
            ps.setBigDecimal(10, p.getTotal());
            ps.setString(11, p.getObservaciones());
            ps.executeUpdate();
        }
    }

    private void insertarDetalles(Pedido p) throws SQLException {
        String sqlDetalle = "INSERT INTO DetallePedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario, "
                + "subtotal) VALUES (?, ?, ?, ?, ?, ?)";
        // Descuenta stock solo si alcanza: evita vender más de lo disponible aunque haya pedidos simultáneos.
        String sqlStock = "UPDATE Producto SET stock_disponible = stock_disponible - ? "
                + "WHERE id_producto = ? AND estado = TRUE AND stock_disponible >= ?";
        String primerId = GeneradorId.siguiente(cn, "DetallePedido", "id_detalle", "DET");
        int correlativo = Integer.parseInt(primerId.substring(3));
        try (PreparedStatement psDet = cn.prepareStatement(sqlDetalle);
             PreparedStatement psStock = cn.prepareStatement(sqlStock)) {
            for (DetallePedido d : p.getDetalles()) {
                psStock.setInt(1, d.getCantidad());
                psStock.setString(2, d.getProducto().getIdProducto());
                psStock.setInt(3, d.getCantidad());
                if (psStock.executeUpdate() == 0) {
                    throw new StockInsuficienteException(d.getProducto().getNombre());
                }
                if (correlativo > 999) {
                    throw new SQLException("Se alcanzó el máximo de códigos para DetallePedido");
                }
                d.setIdDetalle(String.format("DET%03d", correlativo++));
                psDet.setString(1, d.getIdDetalle());
                psDet.setString(2, p.getIdPedido());
                psDet.setString(3, d.getProducto().getIdProducto());
                psDet.setInt(4, d.getCantidad());
                psDet.setBigDecimal(5, d.getPrecioUnitario());
                psDet.setBigDecimal(6, d.getSubtotal());
                psDet.executeUpdate();
            }
        }
    }

    private void insertarEvento(Pedido p) throws SQLException {
        DatosEvento e = p.getEvento();
        String sql = "INSERT INTO DatosEvento (id_pedido, fecha_evento, hora_evento, tipo_servicio, cantidad_personas, "
                + "direccion_evento, observaciones_evento) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, p.getIdPedido());
            ps.setDate(2, java.sql.Date.valueOf(e.getFechaEvento()));
            if (e.getHoraEvento() == null) {
                ps.setNull(3, Types.TIME);
            } else {
                ps.setTime(3, Time.valueOf(e.getHoraEvento()));
            }
            ps.setString(4, e.getTipoServicio());
            ps.setInt(5, e.getCantidadPersonas());
            ps.setString(6, e.getDireccionEvento());
            ps.setString(7, e.getObservaciones());
            ps.executeUpdate();
        }
    }

    @Override
    public Pedido buscarPorId(String id) {
        List<Pedido> r = consultar(SELECT + "WHERE p.id_pedido = ?", id);
        if (r.isEmpty()) {
            return null;
        }
        Pedido p = r.get(0);
        try {
            cargarDetalles(p);
            cargarEvento(p);
        } catch (SQLException e) {
            throw new DAOException("Error al cargar el detalle del pedido", e);
        }
        return p;
    }

    @Override
    public List<Pedido> listar() {
        return consultar(SELECT + "ORDER BY p.fecha_pedido DESC, p.id_pedido DESC");
    }

    @Override
    public List<Pedido> listarPorEstado(EstadoPedido estado) {
        if (estado == null) {
            return listar();
        }
        return consultar(SELECT + "WHERE p.estado_pedido = ? ORDER BY p.fecha_pedido DESC, p.id_pedido DESC",
                estado.getValorBD());
    }

    @Override
    public List<Pedido> listarPorRepartidor(String idRepartidor, EstadoPedido estado) {
        if (estado == null) {
            return consultar(SELECT + "WHERE p.id_repartidor = ? ORDER BY p.fecha_pedido DESC", idRepartidor);
        }
        return consultar(SELECT + "WHERE p.id_repartidor = ? AND p.estado_pedido = ? ORDER BY p.fecha_pedido DESC",
                idRepartidor, estado.getValorBD());
    }

    @Override
    public List<Pedido> listarPorCliente(String idCliente) {
        return consultar(SELECT + "WHERE p.id_cliente = ? ORDER BY p.fecha_pedido DESC", idCliente);
    }

    @Override
    public boolean actualizarEstado(String id, EstadoPedido estadoActual, EstadoPedido nuevo) {
        String sql = "UPDATE Pedido SET estado_pedido = ? WHERE id_pedido = ? AND estado_pedido = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nuevo.getValorBD());
            ps.setString(2, id);
            ps.setString(3, estadoActual.getValorBD());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DAOException("Error al actualizar el estado del pedido", e);
        }
    }

    @Override
    public boolean asignarRepartidor(String idPedido, String idRepartidor) {
        return actualizar("UPDATE Pedido SET id_repartidor = ? WHERE id_pedido = ?", idRepartidor, idPedido);
    }

    @Override
    public Map<EstadoPedido, Integer> contarPorEstado() {
        Map<EstadoPedido, Integer> conteo = new EnumMap<>(EstadoPedido.class);
        for (EstadoPedido e : EstadoPedido.values()) {
            conteo.put(e, 0);
        }
        String sql = "SELECT estado_pedido, COUNT(*) AS total FROM Pedido GROUP BY estado_pedido";
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                conteo.put(EstadoPedido.desdeBD(rs.getString("estado_pedido")), rs.getInt("total"));
            }
        } catch (SQLException e) {
            throw new DAOException("Error al contar pedidos", e);
        }
        return conteo;
    }

    private boolean actualizar(String sql, String valor, String id) {
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, valor);
            ps.setString(2, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DAOException("Error al actualizar el pedido", e);
        }
    }

    private List<Pedido> consultar(String sql, String... params) {
        List<Pedido> lista = new ArrayList<>();
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
            throw new DAOException("Error al consultar pedidos", e);
        }
        return lista;
    }

    private void cargarDetalles(Pedido p) throws SQLException {
        String sql = "SELECT d.*, pr.nombre_producto, pr.id_categoria, c.nombre_categoria FROM DetallePedido d "
                + "JOIN Producto pr ON pr.id_producto = d.id_producto "
                + "JOIN Categoria c ON c.id_categoria = pr.id_categoria "
                + "WHERE d.id_pedido = ? ORDER BY d.id_detalle";
        List<DetallePedido> detalles = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, p.getIdPedido());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Producto pr = new Producto();
                    pr.setIdProducto(rs.getString("id_producto"));
                    pr.setNombre(rs.getString("nombre_producto"));
                    pr.setCategoria(new Categoria(rs.getString("id_categoria"), rs.getString("nombre_categoria")));
                    DetallePedido d = new DetallePedido();
                    d.setIdDetalle(rs.getString("id_detalle"));
                    d.setProducto(pr);
                    d.setCantidad(rs.getInt("cantidad"));
                    d.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    d.setSubtotal(rs.getBigDecimal("subtotal"));
                    detalles.add(d);
                }
            }
        }
        p.setDetalles(detalles);
    }

    private void cargarEvento(Pedido p) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT * FROM DatosEvento WHERE id_pedido = ?")) {
            ps.setString(1, p.getIdPedido());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    DatosEvento e = new DatosEvento();
                    e.setFechaEvento(rs.getDate("fecha_evento").toLocalDate());
                    Time hora = rs.getTime("hora_evento");
                    e.setHoraEvento(hora == null ? null : hora.toLocalTime());
                    e.setTipoServicio(rs.getString("tipo_servicio"));
                    e.setCantidadPersonas(rs.getInt("cantidad_personas"));
                    e.setDireccionEvento(rs.getString("direccion_evento"));
                    e.setObservaciones(rs.getString("observaciones_evento"));
                    p.setEvento(e);
                }
            }
        }
    }

    private Pedido mapear(ResultSet rs) throws SQLException {
        Pedido p = new Pedido();
        p.setIdPedido(rs.getString("id_pedido"));
        p.setFecha(rs.getObject("fecha_pedido", LocalDateTime.class));
        p.setFechaEntrega(rs.getObject("fecha_entrega_estimada", LocalDateTime.class));
        p.setEstado(EstadoPedido.desdeBD(rs.getString("estado_pedido")));
        p.setTipo(TipoPedido.desdeBD(rs.getString("tipo_pedido")));
        p.setMetodoEntrega(MetodoEntrega.desdeBD(rs.getString("metodo_entrega")));
        p.setDireccionEntrega(rs.getString("direccion_entrega"));
        p.setTotal(rs.getBigDecimal("total_pedido"));
        p.setObservaciones(rs.getString("observaciones"));

        Cliente c = new Cliente();
        c.setIdCliente(rs.getString("id_cliente"));
        c.setNombres(rs.getString("c_nombres"));
        c.setApellidos(rs.getString("c_apellidos"));
        c.setTelefono(rs.getString("c_telefono"));
        c.setCorreo(rs.getString("c_correo"));
        c.setDireccionReferencia(rs.getString("c_direccion"));
        p.setCliente(c);

        String idRep = rs.getString("id_repartidor");
        if (idRep != null) {
            Usuario r = new Usuario();
            r.setIdUsuario(idRep);
            r.setNombres(rs.getString("r_nombres"));
            r.setApellidos(rs.getString("r_apellidos"));
            r.setTelefono(rs.getString("r_telefono"));
            p.setRepartidor(r);
        }
        return p;
    }
}
