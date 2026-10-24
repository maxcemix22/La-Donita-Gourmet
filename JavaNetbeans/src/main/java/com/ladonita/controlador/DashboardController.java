package com.ladonita.controlador;

import com.ladonita.modelo.dao.ReporteDAO;
import com.ladonita.modelo.dao.impl.PedidoDAOImpl;
import com.ladonita.modelo.dao.impl.ProductoDAOImpl;
import com.ladonita.modelo.dao.impl.ReporteDAOImpl;
import com.ladonita.modelo.entidades.EstadoPedido;
import com.ladonita.modelo.entidades.Rol;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Panel de inicio: indicadores del día y del mes, ventas de la semana, alertas y próximos eventos. */
@WebServlet(name = "DashboardController", urlPatterns = {"/admin", "/admin/"})
public class DashboardController extends ControladorBase {

    /** Barra del gráfico de ventas de los últimos 7 días. */
    public record Barra(String dia, String fecha, BigDecimal monto, int porcentaje) {
        public String getDia() { return dia; }
        public String getFecha() { return fecha; }
        public BigDecimal getMonto() { return monto; }
        public int getPorcentaje() { return porcentaje; }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (usuarioActual(req).isRepartidor()) {
            redirigir(req, resp, "/admin/pedidos");
            return;
        }
        if (!requerirRol(req, resp, Rol.ADMIN, Rol.EMPLEADO)) {
            return;
        }
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        bd(cn -> {
            ReporteDAO r = new ReporteDAOImpl(cn);
            BigDecimal ventasMes = r.ventas(inicioMes, hoy);
            int pedidosMes = r.cantidadPedidos(inicioMes, hoy);
            req.setAttribute("ventasHoy", r.ventas(hoy, hoy));
            req.setAttribute("pedidosHoy", r.cantidadPedidos(hoy, hoy));
            req.setAttribute("ventasMes", ventasMes);
            req.setAttribute("pedidosMes", pedidosMes);
            req.setAttribute("ticketPromedio", pedidosMes == 0 ? BigDecimal.ZERO
                    : ventasMes.divide(BigDecimal.valueOf(pedidosMes), 2, RoundingMode.HALF_UP));
            req.setAttribute("barras", barras(r.ventasPorDia(7)));
            req.setAttribute("eventos", r.proximosEventos(5));
            req.setAttribute("stockBajo", new ProductoDAOImpl(cn).listarStockBajo());
            Map<EstadoPedido, Integer> conteo = new PedidoDAOImpl(cn).contarPorEstado();
            req.setAttribute("conteo", conteo);
            req.setAttribute("porAtender", conteo.get(EstadoPedido.PENDIENTE) + conteo.get(EstadoPedido.EN_PREPARACION));
            req.setAttribute("estados", EstadoPedido.values());
            return null;
        });
        req.setAttribute("mesActual", hoy.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "PE")));
        vista(req, resp, "admin/dashboard");
    }

    private List<Barra> barras(Map<LocalDate, BigDecimal> ventas) {
        BigDecimal max = ventas.values().stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM");
        List<Barra> lista = new ArrayList<>();
        ventas.forEach((dia, monto) -> {
            int pct = max.signum() == 0 ? 0 : monto.multiply(BigDecimal.valueOf(100)).divide(max, 0, RoundingMode.HALF_UP).intValue();
            String nombre = dia.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("es", "PE"));
            lista.add(new Barra(nombre.substring(0, 1).toUpperCase() + nombre.substring(1), dia.format(f), monto, pct));
        });
        return lista;
    }
}
