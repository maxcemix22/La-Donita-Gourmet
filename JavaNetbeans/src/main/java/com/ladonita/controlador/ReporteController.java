package com.ladonita.controlador;

import com.ladonita.modelo.dao.ReporteDAO;
import com.ladonita.modelo.dao.impl.ReporteDAOImpl;
import com.ladonita.modelo.entidades.Rol;
import com.ladonita.modelo.entidades.TablaReporte;
import com.ladonita.modelo.entidades.Usuario;
import com.ladonita.util.Bitacora;
import com.ladonita.util.Csv;
import com.ladonita.util.ExcelReporte;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reportes R01–R08 del informe (Tabla 11), con filtros y exportación a CSV.
 * Cada reporte solo lo ven los roles indicados en el informe.
 */
@WebServlet(name = "ReporteController", urlPatterns = {"/admin/reportes"})
public class ReporteController extends ControladorBase {

    /** Código -> [nombre corto, roles permitidos]. */
    private static final Map<String, Object[]> REPORTES = new LinkedHashMap<>();

    static {
        REPORTES.put("R01", new Object[]{"Ventas", Set.of(Rol.ADMIN)});
        REPORTES.put("R02", new Object[]{"Pendientes", Set.of(Rol.ADMIN, Rol.EMPLEADO)});
        REPORTES.put("R03", new Object[]{"Más vendidos", Set.of(Rol.ADMIN)});
        REPORTES.put("R04", new Object[]{"Inventario", Set.of(Rol.ADMIN, Rol.EMPLEADO)});
        REPORTES.put("R05", new Object[]{"Clientes", Set.of(Rol.ADMIN)});
        REPORTES.put("R06", new Object[]{"Catering", Set.of(Rol.ADMIN, Rol.EMPLEADO)});
        REPORTES.put("R07", new Object[]{"Entregas asignadas", Set.of(Rol.ADMIN, Rol.EMPLEADO, Rol.REPARTIDOR)});
        REPORTES.put("R08", new Object[]{"Entregados", Set.of(Rol.ADMIN, Rol.REPARTIDOR)});
    }

    /** Pestaña del menú de reportes. */
    public record Pestana(String codigo, String nombre) {
        public String getCodigo() { return codigo; }
        public String getNombre() { return nombre; }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Usuario u = usuarioActual(req);
        List<Pestana> permitidos = REPORTES.entrySet().stream()
                .filter(e -> ((Set<Rol>) e.getValue()[1]).contains(u.getRol()))
                .map(e -> new Pestana(e.getKey(), (String) e.getValue()[0])).toList();
        String codigo = req.getParameter("r");
        if (codigo == null || permitidos.stream().noneMatch(p -> p.codigo().equals(codigo))) {
            if (codigo != null && REPORTES.containsKey(codigo)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Tu rol no tiene acceso a este reporte.");
                return;
            }
            redirigir(req, resp, "/admin/reportes?r=" + permitidos.get(0).codigo());
            return;
        }

        LocalDate hoy = LocalDate.now();
        LocalDate desde = fecha(req.getParameter("desde"), hoy.withDayOfMonth(1));
        LocalDate hasta = fecha(req.getParameter("hasta"), hoy);
        if (desde.isAfter(hasta)) {
            LocalDate t = desde;
            desde = hasta;
            hasta = t;
        }
        int anio = com.ladonita.util.Validador.entero(req.getParameter("anio"), hoy.getYear());
        if (anio < 2000 || anio > 2100) {
            anio = hoy.getYear();
        }
        String vistaR01 = "mensual".equals(req.getParameter("vista")) ? "mensual" : "diario";
        boolean todas = "todas".equals(req.getParameter("eventos"));
        // El repartidor solo ve sus propias entregas.
        String idRepartidor = u.isRepartidor() ? u.getIdUsuario() : null;

        final LocalDate d = desde, h = hasta;
        final int a = anio;
        TablaReporte tabla = bd(cn -> {
            ReporteDAO dao = new ReporteDAOImpl(cn);
            return switch (codigo) {
                case "R01" -> "mensual".equals(vistaR01) ? dao.r01VentasMensuales(a) : dao.r01VentasDiarias(d, h);
                case "R02" -> dao.r02PedidosPendientes();
                case "R03" -> dao.r03ProductosMasSolicitados(d, h);
                case "R04" -> dao.r04Inventario();
                case "R05" -> dao.r05Clientes();
                case "R06" -> dao.r06ReservasCatering(!todas);
                case "R07" -> dao.r07EntregasAsignadas(idRepartidor);
                default -> dao.r08PedidosEntregados(idRepartidor, d, h);
            };
        });

        if ("1".equals(req.getParameter("xlsx"))) {
            Bitacora.info(req, "REPORTE_EXCEL", codigo + " (" + tabla.getFilas().size() + " filas)");
            resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + codigo + "_" + hoy + ".xlsx\"");
            ExcelReporte.escribir(tabla, u.getNombreCompleto(), resp.getOutputStream());
            return;
        }
        if ("1".equals(req.getParameter("csv"))) {
            Bitacora.info(req, "REPORTE_CSV", codigo + " (" + tabla.getFilas().size() + " filas)");
            resp.setContentType("text/csv; charset=UTF-8");
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + codigo + "_" + hoy + ".csv\"");
            Csv.escribir(tabla, resp.getWriter());
            return;
        }
        req.setAttribute("tabla", tabla);
        req.setAttribute("pestanas", permitidos);
        req.setAttribute("codigo", codigo);
        req.setAttribute("desde", desde.toString());
        req.setAttribute("hasta", hasta.toString());
        req.setAttribute("anio", anio);
        req.setAttribute("vistaR01", vistaR01);
        req.setAttribute("todasEventos", todas);
        req.setAttribute("conRango", Set.of("R03", "R08").contains(codigo) || ("R01".equals(codigo) && "diario".equals(vistaR01)));
        req.setAttribute("query", req.getQueryString() == null ? "r=" + codigo : req.getQueryString().replaceAll("&?(csv|xlsx)=1", ""));
        vista(req, resp, "admin/reportes");
    }

    private LocalDate fecha(String valor, LocalDate porDefecto) {
        try {
            return valor == null || valor.isBlank() ? porDefecto : LocalDate.parse(valor);
        } catch (RuntimeException e) {
            return porDefecto;
        }
    }
}
