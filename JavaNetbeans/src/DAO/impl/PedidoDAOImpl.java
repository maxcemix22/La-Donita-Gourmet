package DAO.impl;

import DAO.DAOException;
import DAO.PedidoDAO;
import Modelo.Cliente;
import Modelo.DatosEvento;
import Modelo.DetallePedido;
import Modelo.EstadoPedido;
import Modelo.Pedido;
import Modelo.Repartidor;
import Modelo.Producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PedidoDAOImpl implements PedidoDAO {
    private static final String SELECT =
            "SELECT p.id_pedido, p.id_repartidor, p.fecha_pedido, p.fecha_entrega_estimada, p.estado_pedido, p.tipo_pedido, "
            + "p.metodo_entrega, p.direccion_entrega, p.observaciones, "
            + "c.id_cliente, c.nombres, c.apellidos, c.telefono, c.correo, c.direccion_referencia "
            + "FROM pedido p JOIN cliente c ON c.id_cliente = p.id_cliente";

    /**
     * Guarda pedido, detalles y datos del evento en una sola transacción y descuenta el stock de cada producto.
     * Si algo falla (stock insuficiente, producto inactivo, error de BD) no se guarda nada.
     */
    @Override
    public boolean registrar(Pedido pedido) {
        if (pedido.getDetalles().isEmpty()) throw new IllegalArgumentException("El pedido debe tener al menos un producto");
        if (pedido.getCliente().getIdCliente() == null) throw new IllegalArgumentException("El cliente debe estar guardado antes de registrar el pedido");
        try (Connection c = Conexion.obtener()) {
            c.setAutoCommit(false);
            try {
                descontarStock(c, pedido);
                String idPedido = Ids.siguiente(c, "pedido", "id_pedido", "PED");
                insertarPedido(c, idPedido, pedido);
                insertarDetalles(c, idPedido, pedido);
                if (pedido.getDatosEvento() != null) insertarEvento(c, idPedido, pedido.getDatosEvento());
                c.commit();
                pedido.setIdPedido(idPedido);
                return true;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DAOException("Error al registrar el pedido", e);
        }
    }

    @Override
    public Pedido buscarPorId(String idPedido) {
        List<Pedido> r = consultar(SELECT + " WHERE p.id_pedido = ?", idPedido);
        return r.isEmpty() ? null : r.get(0);
    }

    @Override
    public List<Pedido> listarPorFecha() {
        return consultar(SELECT + " ORDER BY p.fecha_pedido DESC, p.id_pedido DESC", null);
    }

    @Override
    public List<Pedido> listarPorEstado(EstadoPedido estado) {
        return consultar(SELECT + " WHERE p.estado_pedido = ? ORDER BY p.fecha_pedido", estado.getEtiqueta());
    }

    @Override
    public List<Pedido> listarPorRepartidor(String idRepartidor) {
        return consultar(SELECT + " WHERE p.id_repartidor = ? ORDER BY p.fecha_pedido DESC", idRepartidor);
    }

    /**
     * Cambia el estado respetando el flujo del modelo (Pedido.cambiarEstado): si la transición no es válida
     * lanza IllegalStateException y no toca la BD.
     */
    @Override
    public boolean actualizarEstado(String idPedido, EstadoPedido nuevoEstado) {
        Pedido p = buscarPorId(idPedido);
        if (p == null) return false;
        EstadoPedido anterior = p.getEstado();
        p.cambiarEstado(nuevoEstado);
        try (Connection c = Conexion.obtener();
             PreparedStatement ps = c.prepareStatement("UPDATE pedido SET estado_pedido = ? WHERE id_pedido = ? AND estado_pedido = ?")) {
            ps.setString(1, nuevoEstado.getEtiqueta());
            ps.setString(2, idPedido);
            ps.setString(3, anterior.getEtiqueta()); // evita pisar un cambio hecho por otro usuario a la vez
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DAOException("Error al actualizar el estado del pedido " + idPedido, e);
        }
    }

    @Override
    public boolean asignarRepartidor(String idPedido, String idRepartidor) {
        Pedido p = buscarPorId(idPedido);
        if (p == null) return false;
        try (Connection c = Conexion.obtener()) {
            Repartidor r = UsuarioDAOImpl.repartidorPorId(c, idRepartidor);
            if (r == null || !r.isEstado()) throw new IllegalArgumentException("No existe un repartidor activo con id " + idRepartidor);
            p.asignarRepartidor(r); // lanza IllegalStateException si el pedido no es delivery
            try (PreparedStatement ps = c.prepareStatement("UPDATE pedido SET id_repartidor = ? WHERE id_pedido = ?")) {
                ps.setString(1, idRepartidor);
                ps.setString(2, idPedido);
                return ps.executeUpdate() == 1;
            }
        } catch (SQLException e) {
            throw new DAOException("Error al asignar el repartidor al pedido " + idPedido, e);
        }
    }

    // ---------- registro ----------

    private void descontarStock(Connection c, Pedido pedido) throws SQLException {
        Map<String, Integer> cantidades = new TreeMap<>(); // ordenado por id para evitar bloqueos cruzados
        for (DetallePedido d : pedido.getDetalles()) cantidades.merge(d.getProducto().getIdProducto(), d.getCantidad(), Integer::sum);
        for (Map.Entry<String, Integer> e : cantidades.entrySet()) {
            int stock;
            try (PreparedStatement ps = c.prepareStatement("SELECT stock_disponible, estado FROM producto WHERE id_producto = ? FOR UPDATE")) {
                ps.setString(1, e.getKey());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || !rs.getBoolean("estado")) throw new IllegalStateException("El producto " + e.getKey() + " no está disponible");
                    stock = rs.getInt("stock_disponible");
                }
            }
            if (stock < e.getValue()) {
                throw new IllegalStateException("Stock insuficiente de " + e.getKey() + ": hay " + stock + " y se pidieron " + e.getValue());
            }
            try (PreparedStatement ps = c.prepareStatement("UPDATE producto SET stock_disponible = stock_disponible - ? WHERE id_producto = ?")) {
                ps.setInt(1, e.getValue());
                ps.setString(2, e.getKey());
                ps.executeUpdate();
            }
        }
    }

    private void insertarPedido(Connection c, String id, Pedido p) throws SQLException {
        String sql = "INSERT INTO pedido (id_pedido, id_cliente, id_repartidor, fecha_pedido, fecha_entrega_estimada, estado_pedido, "
                + "tipo_pedido, metodo_entrega, direccion_entrega, total_pedido, observaciones) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, p.getCliente().getIdCliente());
            ps.setString(3, p.getRepartidor() != null ? p.getRepartidor().getId() : null);
            ps.setTimestamp(4, Timestamp.valueOf(p.getFecha()));
            ps.setTimestamp(5, p.getFechaEntregaEstimada() != null ? Timestamp.valueOf(p.getFechaEntregaEstimada()) : null);
            ps.setString(6, p.getEstado().getEtiqueta());
            ps.setString(7, p.getTipo());
            ps.setString(8, p.getMetodoEntrega());
            ps.setString(9, p.getDireccionEntrega());
            ps.setBigDecimal(10, p.getTotal());
            ps.setString(11, p.getObservaciones());
            ps.executeUpdate();
        }
    }

    private void insertarDetalles(Connection c, String idPedido, Pedido p) throws SQLException {
        int n = Ids.ultimo(c, "detallepedido", "id_detalle", "DET");
        String sql = "INSERT INTO detallepedido (id_detalle, id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (DetallePedido d : p.getDetalles()) {
                String idDetalle = Ids.formatear("DET", ++n);
                ps.setString(1, idDetalle);
                ps.setString(2, idPedido);
                ps.setString(3, d.getProducto().getIdProducto());
                ps.setInt(4, d.getCantidad());
                ps.setBigDecimal(5, d.getPrecioUnitario());
                ps.setBigDecimal(6, d.getSubtotal());
                ps.executeUpdate();
                d.setIdDetalle(idDetalle);
            }
        }
    }

    private void insertarEvento(Connection c, String idPedido, DatosEvento e) throws SQLException {
        String sql = "INSERT INTO datosevento (id_pedido, fecha_evento, hora_evento, tipo_servicio, cantidad_personas, direccion_evento, observaciones_evento) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, idPedido);
            ps.setDate(2, Date.valueOf(e.getFechaEvento()));
            ps.setTime(3, e.getHoraEvento() != null ? Time.valueOf(e.getHoraEvento()) : null);
            ps.setString(4, e.getTipoServicio());
            ps.setInt(5, e.getCantidadPersonas());
            ps.setString(6, e.getDireccionEvento());
            ps.setString(7, e.getObservacionesEvento());
            ps.executeUpdate();
        }
    }

    // ---------- lectura ----------

    private List<Pedido> consultar(String sql, String parametro) {
        try (Connection c = Conexion.obtener();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (parametro != null) ps.setString(1, parametro);
            List<Pedido> lista = new ArrayList<>();
            Map<String, Repartidor> repartidores = new HashMap<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(c, rs, repartidores));
            }
            return lista;
        } catch (SQLException e) {
            throw new DAOException("Error al consultar pedidos", e);
        }
    }

    private Pedido mapear(Connection c, ResultSet rs, Map<String, Repartidor> repartidores) throws SQLException {
        String id = rs.getString("id_pedido");
        Cliente cliente = ClienteDAOImpl.mapear(rs);
        Pedido p = new Pedido(id, cliente, rs.getTimestamp("fecha_pedido").toLocalDateTime(), rs.getString("tipo_pedido"),
                rs.getString("metodo_entrega"), rs.getString("direccion_entrega"));
        Timestamp entrega = rs.getTimestamp("fecha_entrega_estimada");
        if (entrega != null) p.setFechaEntregaEstimada(entrega.toLocalDateTime());
        p.setObservaciones(rs.getString("observaciones"));
        p.restaurarEstado(EstadoPedido.desdeEtiqueta(rs.getString("estado_pedido")));

        String idRepartidor = rs.getString("id_repartidor");
        if (idRepartidor != null) {
            if (!repartidores.containsKey(idRepartidor)) repartidores.put(idRepartidor, UsuarioDAOImpl.repartidorPorId(c, idRepartidor));
            Repartidor r = repartidores.get(idRepartidor);
            if (r != null) p.asignarRepartidor(r);
        }
        cargarDetalles(c, p);
        cargarEvento(c, p);
        return p;
    }

    private void cargarDetalles(Connection c, Pedido p) throws SQLException {
        String sql = "SELECT d.id_detalle, d.cantidad, d.precio_unitario, d.subtotal, " + ProductoDAOImpl.COLUMNAS
                + " FROM detallepedido d JOIN producto p ON p.id_producto = d.id_producto"
                + " JOIN categoria c ON c.id_categoria = p.id_categoria WHERE d.id_pedido = ? ORDER BY d.id_detalle";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getIdPedido());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Producto pr = ProductoDAOImpl.mapear(rs);
                    p.agregarDetalle(DetallePedido.desdeBD(rs.getString("id_detalle"), pr, rs.getInt("cantidad"),
                            rs.getBigDecimal("precio_unitario"), rs.getBigDecimal("subtotal")));
                }
            }
        }
    }

    private void cargarEvento(Connection c, Pedido p) throws SQLException {
        if (!Pedido.TIPO_CATERING.equals(p.getTipo())) return;
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT fecha_evento, hora_evento, tipo_servicio, cantidad_personas, direccion_evento, observaciones_evento "
                + "FROM datosevento WHERE id_pedido = ?")) {
            ps.setString(1, p.getIdPedido());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Time hora = rs.getTime("hora_evento");
                    p.setDatosEvento(new DatosEvento(rs.getDate("fecha_evento").toLocalDate(),
                            hora != null ? hora.toLocalTime() : null, rs.getString("tipo_servicio"),
                            rs.getInt("cantidad_personas"), rs.getString("direccion_evento"),
                            rs.getString("observaciones_evento")));
                }
            }
        }
    }
}
