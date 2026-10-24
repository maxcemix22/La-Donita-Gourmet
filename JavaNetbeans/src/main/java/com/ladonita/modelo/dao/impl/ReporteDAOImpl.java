package com.ladonita.modelo.dao.impl;

import com.ladonita.modelo.dao.DAOException;
import com.ladonita.modelo.dao.ReporteDAO;
import com.ladonita.modelo.entidades.Cliente;
import com.ladonita.modelo.entidades.DatosEvento;
import com.ladonita.modelo.entidades.EstadoPedido;
import com.ladonita.modelo.entidades.Pedido;
import com.ladonita.modelo.entidades.Producto;
import com.ladonita.modelo.entidades.TablaReporte;
import com.ladonita.modelo.entidades.TipoPedido;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reportes R01–R08. Las consultas usan solo SQL estándar (CAST, YEAR, MONTH, CASE) para que funcionen
 * igual en MySQL y en la base H2 del modo demo. BaseDatos/03_reportes.sql define las mismas consultas
 * como vistas para usarlas desde MySQL Workbench.
 */
public class ReporteDAOImpl implements ReporteDAO {

    private static final String[] MESES = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio",
        "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};

    private final Connection cn;

    public ReporteDAOImpl(Connection cn) {
        this.cn = cn;
    }

    @Override
    public TablaReporte r01VentasDiarias(LocalDate desde, LocalDate hasta) {
        TablaReporte t = new TablaReporte("R01", "Ventas diarias", "Pedidos y monto vendido por día en el rango elegido.")
                .columna("Fecha", "fecha").columna("Pedidos", "entero").columna("Normales (S/)", "moneda")
                .columna("Catering (S/)", "moneda").columna("Total vendido (S/)", "moneda").columna("Ticket promedio (S/)", "moneda");
        String sql = "SELECT CAST(fecha_pedido AS DATE) AS dia, COUNT(*), "
                + "SUM(CASE WHEN tipo_pedido = 'normal' THEN total_pedido ELSE 0 END), "
                + "SUM(CASE WHEN tipo_pedido = 'catering' THEN total_pedido ELSE 0 END), "
                + "SUM(total_pedido), ROUND(AVG(total_pedido), 2) "
                + "FROM Pedido WHERE fecha_pedido >= ? AND fecha_pedido < ? "
                + "GROUP BY CAST(fecha_pedido AS DATE) ORDER BY dia DESC";
        return llenar(t, sql, inicio(desde), inicio(hasta.plusDays(1)));
    }

    @Override
    public TablaReporte r01VentasMensuales(int anio) {
        TablaReporte t = new TablaReporte("R01", "Ventas mensuales " + anio, "Pedidos y monto vendido por mes.")
                .columna("Mes", "texto").columna("Pedidos", "entero").columna("Total vendido (S/)", "moneda")
                .columna("Ticket promedio (S/)", "moneda");
        String sql = "SELECT MONTH(fecha_pedido) AS mes, COUNT(*), SUM(total_pedido), ROUND(AVG(total_pedido), 2) "
                + "FROM Pedido WHERE YEAR(fecha_pedido) = ? GROUP BY MONTH(fecha_pedido) ORDER BY mes";
        llenar(t, sql, anio);
        for (List<Object> fila : t.getFilas()) {
            fila.set(0, MESES[((Number) fila.get(0)).intValue() - 1]);
        }
        return t;
    }

    @Override
    public TablaReporte r02PedidosPendientes() {
        TablaReporte t = new TablaReporte("R02", "Pedidos pendientes y en preparación",
                "Ordenados del más antiguo al más reciente, para atenderlos en orden.")
                .columna("Código", "texto").columna("Fecha", "fechahora").columna("Estado", "estado").columna("Tipo", "texto")
                .columna("Entrega", "texto").columna("Cliente", "texto").columna("Teléfono", "texto").columna("Total (S/)", "moneda");
        String sql = "SELECT p.id_pedido, p.fecha_pedido, p.estado_pedido, p.tipo_pedido, p.metodo_entrega, "
                + "CONCAT(c.nombres, ' ', c.apellidos), c.telefono, p.total_pedido "
                + "FROM Pedido p JOIN Cliente c ON c.id_cliente = p.id_cliente "
                + "WHERE p.estado_pedido IN (?, ?) ORDER BY p.fecha_pedido";
        return llenar(t, sql, EstadoPedido.PENDIENTE.getValorBD(), EstadoPedido.EN_PREPARACION.getValorBD());
    }

    @Override
    public TablaReporte r03ProductosMasSolicitados(LocalDate desde, LocalDate hasta) {
        TablaReporte t = new TablaReporte("R03", "Productos más solicitados", "Unidades vendidas por producto en el rango elegido.")
                .columna("Código", "texto").columna("Producto", "texto").columna("Categoría", "texto")
                .columna("Unidades", "entero").columna("Pedidos", "cantidad").columna("Monto (S/)", "moneda");
        String sql = "SELECT pr.id_producto, pr.nombre_producto, c.nombre_categoria, SUM(d.cantidad) AS unidades, "
                + "COUNT(DISTINCT d.id_pedido), SUM(d.subtotal) "
                + "FROM DetallePedido d JOIN Pedido p ON p.id_pedido = d.id_pedido "
                + "JOIN Producto pr ON pr.id_producto = d.id_producto JOIN Categoria c ON c.id_categoria = pr.id_categoria "
                + "WHERE p.fecha_pedido >= ? AND p.fecha_pedido < ? "
                + "GROUP BY pr.id_producto, pr.nombre_producto, c.nombre_categoria ORDER BY unidades DESC";
        return llenar(t, sql, inicio(desde), inicio(hasta.plusDays(1)));
    }

    @Override
    public TablaReporte r04Inventario() {
        TablaReporte t = new TablaReporte("R04", "Inventario y stock",
                "Productos activos ordenados de menor a mayor stock. Stock bajo: " + Producto.STOCK_MINIMO + " unidades o menos.")
                .columna("Código", "texto").columna("Producto", "texto").columna("Categoría", "texto").columna("Precio (S/)", "moneda")
                .columna("Stock", "entero").columna("Nivel", "nivel").columna("Valor en stock (S/)", "moneda");
        String sql = "SELECT p.id_producto, p.nombre_producto, c.nombre_categoria, p.precio, p.stock_disponible, "
                + "CASE WHEN p.stock_disponible = 0 THEN 'agotado' WHEN p.stock_disponible <= " + Producto.STOCK_MINIMO
                + " THEN 'bajo' ELSE 'disponible' END, p.precio * p.stock_disponible "
                + "FROM Producto p JOIN Categoria c ON c.id_categoria = p.id_categoria "
                + "WHERE p.estado = TRUE ORDER BY p.stock_disponible, p.nombre_producto";
        return llenar(t, sql);
    }

    @Override
    public TablaReporte r05Clientes() {
        TablaReporte t = new TablaReporte("R05", "Clientes: frecuencia y monto facturado", "Ordenados por monto total comprado.")
                .columna("Código", "texto").columna("Cliente", "texto").columna("Teléfono", "texto").columna("Pedidos", "entero")
                .columna("Monto total (S/)", "moneda").columna("Ticket promedio (S/)", "moneda").columna("Último pedido", "fechahora");
        String sql = "SELECT c.id_cliente, CONCAT(c.nombres, ' ', c.apellidos), c.telefono, COUNT(p.id_pedido), "
                + "COALESCE(SUM(p.total_pedido), 0) AS monto, COALESCE(ROUND(AVG(p.total_pedido), 2), 0), MAX(p.fecha_pedido) "
                + "FROM Cliente c LEFT JOIN Pedido p ON p.id_cliente = c.id_cliente "
                + "GROUP BY c.id_cliente, c.nombres, c.apellidos, c.telefono ORDER BY monto DESC";
        return llenar(t, sql);
    }

    @Override
    public TablaReporte r06ReservasCatering(boolean soloProximas) {
        TablaReporte t = new TablaReporte("R06", soloProximas ? "Reservas de catering próximas" : "Reservas de catering (todas)",
                "Eventos ordenados por fecha y hora.")
                .columna("Pedido", "texto").columna("Fecha evento", "fecha").columna("Hora", "texto").columna("Servicio", "texto")
                .columna("Personas", "entero").columna("Dirección", "texto").columna("Estado", "estado").columna("Cliente", "texto")
                .columna("Teléfono", "texto").columna("Total (S/)", "moneda");
        String sql = "SELECT p.id_pedido, e.fecha_evento, e.hora_evento, e.tipo_servicio, e.cantidad_personas, e.direccion_evento, "
                + "p.estado_pedido, CONCAT(c.nombres, ' ', c.apellidos), c.telefono, p.total_pedido "
                + "FROM Pedido p JOIN DatosEvento e ON e.id_pedido = p.id_pedido JOIN Cliente c ON c.id_cliente = p.id_cliente "
                + (soloProximas ? "WHERE e.fecha_evento >= CURRENT_DATE " : "")
                + "ORDER BY e.fecha_evento, e.hora_evento";
        return llenar(t, sql);
    }

    @Override
    public TablaReporte r07EntregasAsignadas(String idRepartidor) {
        TablaReporte t = new TablaReporte("R07", "Entregas asignadas pendientes", "Pedidos delivery asignados que aún no se entregan.")
                .columna("Repartidor", "texto").columna("Pedido", "texto").columna("Estado", "estado").columna("Dirección", "texto")
                .columna("Entrega estimada", "fechahora").columna("Cliente", "texto").columna("Teléfono", "texto");
        String sql = "SELECT CONCAT(u.nombres, ' ', u.apellidos), p.id_pedido, p.estado_pedido, p.direccion_entrega, "
                + "p.fecha_entrega_estimada, CONCAT(c.nombres, ' ', c.apellidos), c.telefono "
                + "FROM Pedido p JOIN Cliente c ON c.id_cliente = p.id_cliente JOIN Usuario u ON u.id_usuario = p.id_repartidor "
                + "WHERE p.metodo_entrega = 'delivery' AND p.estado_pedido <> 'entregado' "
                + (idRepartidor == null ? "" : "AND p.id_repartidor = ? ")
                + "ORDER BY p.fecha_entrega_estimada, p.fecha_pedido";
        return idRepartidor == null ? llenar(t, sql) : llenar(t, sql, idRepartidor);
    }

    @Override
    public TablaReporte r08PedidosEntregados(String idRepartidor, LocalDate desde, LocalDate hasta) {
        TablaReporte t = new TablaReporte("R08", "Historial de pedidos entregados", "Pedidos entregados en el rango elegido.")
                .columna("Pedido", "texto").columna("Fecha pedido", "fechahora").columna("Entrega estimada", "fechahora")
                .columna("Método", "texto").columna("Repartidor", "texto").columna("Cliente", "texto").columna("Total (S/)", "moneda");
        String sql = "SELECT p.id_pedido, p.fecha_pedido, p.fecha_entrega_estimada, p.metodo_entrega, "
                + "COALESCE(CONCAT(u.nombres, ' ', u.apellidos), '—'), CONCAT(c.nombres, ' ', c.apellidos), p.total_pedido "
                + "FROM Pedido p JOIN Cliente c ON c.id_cliente = p.id_cliente LEFT JOIN Usuario u ON u.id_usuario = p.id_repartidor "
                + "WHERE p.estado_pedido = 'entregado' AND p.fecha_pedido >= ? AND p.fecha_pedido < ? "
                + (idRepartidor == null ? "" : "AND p.id_repartidor = ? ")
                + "ORDER BY p.fecha_pedido DESC";
        return idRepartidor == null
                ? llenar(t, sql, inicio(desde), inicio(hasta.plusDays(1)))
                : llenar(t, sql, inicio(desde), inicio(hasta.plusDays(1)), idRepartidor);
    }

    @Override
    public BigDecimal ventas(LocalDate desde, LocalDate hasta) {
        Object v = escalar("SELECT COALESCE(SUM(total_pedido), 0) FROM Pedido WHERE fecha_pedido >= ? AND fecha_pedido < ?",
                inicio(desde), inicio(hasta.plusDays(1)));
        return v == null ? BigDecimal.ZERO : new BigDecimal(v.toString());
    }

    @Override
    public int cantidadPedidos(LocalDate desde, LocalDate hasta) {
        Object v = escalar("SELECT COUNT(*) FROM Pedido WHERE fecha_pedido >= ? AND fecha_pedido < ?",
                inicio(desde), inicio(hasta.plusDays(1)));
        return v == null ? 0 : ((Number) v).intValue();
    }

    @Override
    public Map<LocalDate, BigDecimal> ventasPorDia(int dias) {
        LocalDate hoy = LocalDate.now();
        LocalDate desde = hoy.minusDays(dias - 1L);
        Map<LocalDate, BigDecimal> mapa = new LinkedHashMap<>();
        for (LocalDate d = desde; !d.isAfter(hoy); d = d.plusDays(1)) {
            mapa.put(d, BigDecimal.ZERO);
        }
        TablaReporte t = r01VentasDiarias(desde, hoy);
        for (List<Object> fila : t.getFilas()) {
            mapa.put((LocalDate) fila.get(0), new BigDecimal(fila.get(4).toString()));
        }
        return mapa;
    }

    @Override
    public List<Pedido> proximosEventos(int limite) {
        String sql = "SELECT p.id_pedido, p.estado_pedido, p.total_pedido, c.nombres, c.apellidos, c.telefono, "
                + "e.fecha_evento, e.hora_evento, e.tipo_servicio, e.cantidad_personas "
                + "FROM Pedido p JOIN DatosEvento e ON e.id_pedido = p.id_pedido JOIN Cliente c ON c.id_cliente = p.id_cliente "
                + "WHERE e.fecha_evento >= CURRENT_DATE AND p.estado_pedido <> 'entregado' "
                + "ORDER BY e.fecha_evento, e.hora_evento LIMIT " + Math.max(1, limite);
        List<Pedido> lista = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Pedido p = new Pedido();
                p.setIdPedido(rs.getString("id_pedido"));
                p.setEstado(EstadoPedido.desdeBD(rs.getString("estado_pedido")));
                p.setTotal(rs.getBigDecimal("total_pedido"));
                p.setTipo(TipoPedido.CATERING);
                Cliente c = new Cliente();
                c.setNombres(rs.getString("nombres"));
                c.setApellidos(rs.getString("apellidos"));
                c.setTelefono(rs.getString("telefono"));
                p.setCliente(c);
                DatosEvento e = new DatosEvento();
                e.setFechaEvento(rs.getDate("fecha_evento").toLocalDate());
                Time h = rs.getTime("hora_evento");
                e.setHoraEvento(h == null ? null : h.toLocalTime());
                e.setTipoServicio(rs.getString("tipo_servicio"));
                e.setCantidadPersonas(rs.getInt("cantidad_personas"));
                p.setEvento(e);
                lista.add(p);
            }
        } catch (SQLException e) {
            throw new DAOException("Error al consultar los próximos eventos", e);
        }
        return lista;
    }

    // ------------------------------------------------------------------

    private static Timestamp inicio(LocalDate fecha) {
        return Timestamp.valueOf(fecha.atStartOfDay());
    }

    private TablaReporte llenar(TablaReporte t, String sql, Object... params) {
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                int n = t.getColumnas().size();
                while (rs.next()) {
                    List<Object> fila = new ArrayList<>(n);
                    for (int i = 0; i < n; i++) {
                        fila.add(convertir(rs.getObject(i + 1), t.getColumnas().get(i).tipo()));
                    }
                    t.agregarFila(fila);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al generar el reporte " + t.getCodigo(), e);
        }
        return t;
    }

    private Object convertir(Object v, String tipo) {
        if (v == null) {
            return null;
        }
        if (v instanceof Timestamp ts) {
            v = ts.toLocalDateTime();
        } else if (v instanceof java.sql.Date d) {
            v = d.toLocalDate();
        } else if (v instanceof Time h) {
            return h.toLocalTime().toString();
        }
        if ("fecha".equals(tipo) && v instanceof LocalDateTime ldt) {
            return ldt.toLocalDate();
        }
        if ("estado".equals(tipo)) {
            return EstadoPedido.desdeBD(v.toString());
        }
        if ("moneda".equals(tipo) && v instanceof Number num) {
            return new BigDecimal(num.toString()).setScale(2, java.math.RoundingMode.HALF_UP);
        }
        if (("entero".equals(tipo) || "cantidad".equals(tipo)) && v instanceof Number num) {
            return num.longValue();
        }
        return v;
    }

    private Object escalar(String sql, Object... params) {
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getObject(1) : null;
            }
        } catch (SQLException e) {
            throw new DAOException("Error al calcular el indicador", e);
        }
    }
}
