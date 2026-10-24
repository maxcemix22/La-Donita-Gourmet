package com.ladonita.controlador;

import com.ladonita.modelo.dao.ClienteDAO;
import com.ladonita.modelo.dao.PedidoDAO;
import com.ladonita.modelo.dao.ProductoDAO;
import com.ladonita.modelo.dao.StockInsuficienteException;
import com.ladonita.modelo.dao.UsuarioDAO;
import com.ladonita.modelo.dao.impl.ClienteDAOImpl;
import com.ladonita.modelo.dao.impl.PedidoDAOImpl;
import com.ladonita.modelo.dao.impl.ProductoDAOImpl;
import com.ladonita.modelo.dao.impl.UsuarioDAOImpl;
import com.ladonita.modelo.entidades.Carrito;
import com.ladonita.modelo.entidades.Cliente;
import com.ladonita.modelo.entidades.DatosEvento;
import com.ladonita.modelo.entidades.DetallePedido;
import com.ladonita.modelo.entidades.EstadoPedido;
import com.ladonita.modelo.entidades.ItemCarrito;
import com.ladonita.modelo.entidades.MetodoEntrega;
import com.ladonita.modelo.entidades.Pedido;
import com.ladonita.modelo.entidades.Producto;
import com.ladonita.modelo.entidades.Rol;
import com.ladonita.modelo.entidades.TipoPedido;
import com.ladonita.modelo.entidades.Usuario;
import com.ladonita.util.Bitacora;
import com.ladonita.util.Conexion;
import com.ladonita.util.Validador;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Registro de pedidos (público, desde el carrito) y gestión de pedidos (personal interno).
 */
@WebServlet(name = "PedidoController", urlPatterns = {
    "/pedido", "/pedido/confirmacion",
    "/admin/pedidos", "/admin/pedidos/detalle", "/admin/pedidos/estado", "/admin/pedidos/repartidor"})
public class PedidoController extends ControladorBase {

    private static final String PEDIDOS_SESION = "pedidosRealizados";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        switch (req.getServletPath()) {
            case "/pedido" -> mostrarFormulario(req, resp);
            case "/pedido/confirmacion" -> mostrarConfirmacion(req, resp);
            case "/admin/pedidos" -> listarPedidos(req, resp);
            case "/admin/pedidos/detalle" -> verDetalle(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        switch (req.getServletPath()) {
            case "/pedido" -> registrarPedido(req, resp);
            case "/admin/pedidos/estado" -> cambiarEstado(req, resp);
            case "/admin/pedidos/repartidor" -> asignarRepartidor(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    // =====================================================================
    // Zona pública: checkout y confirmación
    // =====================================================================

    private void mostrarFormulario(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Carrito carrito = obtenerCarrito(req);
        if (carrito.isVacio()) {
            flash(req, "warning", "Tu carrito está vacío. Agrega productos antes de hacer tu pedido.");
            redirigir(req, resp, "/catalogo");
            return;
        }
        req.setAttribute("carrito", carrito);
        req.setAttribute("hoy", LocalDate.now().toString());
        vista(req, resp, "checkout");
    }

    private void registrarPedido(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Carrito carrito = obtenerCarrito(req);
        if (carrito.isVacio()) {
            redirigir(req, resp, "/catalogo");
            return;
        }

        List<String> errores = new ArrayList<>();
        Cliente cliente = leerCliente(req, errores);
        Pedido pedido = leerPedido(req, errores);
        if (!errores.isEmpty()) {
            req.setAttribute("errores", errores);
            req.setAttribute("carrito", carrito);
            req.setAttribute("hoy", LocalDate.now().toString());
            vista(req, resp, "checkout");
            return;
        }

        try (Connection cn = Conexion.getConexion()) {
            cn.setAutoCommit(false);
            try {
                ProductoDAO productoDAO = new ProductoDAOImpl(cn);
                ClienteDAO clienteDAO = new ClienteDAOImpl(cn);
                PedidoDAO pedidoDAO = new PedidoDAOImpl(cn);

                // Precios siempre desde la BD, nunca desde la sesión o el formulario.
                for (ItemCarrito item : carrito.getItems()) {
                    Producto actual = productoDAO.buscarPorId(item.getProducto().getIdProducto());
                    if (actual == null || !actual.isEstado()) {
                        throw new StockInsuficienteException(item.getProducto().getNombre());
                    }
                    pedido.agregarDetalle(new DetallePedido(actual, item.getCantidad()));
                }

                // Cliente recurrente: se reconoce por su teléfono.
                Cliente existente = clienteDAO.buscarPorTelefono(cliente.getTelefono());
                if (existente != null) {
                    cliente = existente;
                } else {
                    clienteDAO.insertar(cliente);
                }
                pedido.setCliente(cliente);
                pedidoDAO.insertar(pedido);
                cn.commit();
            } catch (StockInsuficienteException e) {
                cn.rollback();
                Bitacora.alerta(req, "PEDIDO_SIN_STOCK", e.getMessage());
                flash(req, "warning", e.getMessage());
                redirigir(req, resp, "/carrito");
                return;
            } catch (RuntimeException | SQLException e) {
                cn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new ServletException("No se pudo registrar el pedido", e);
        }

        carrito.vaciar();
        pedidosDeSesion(req).add(pedido.getIdPedido());
        Bitacora.info(req, "PEDIDO_REGISTRADO", pedido.getIdPedido() + " cliente=" + pedido.getCliente().getIdCliente()
                + " tipo=" + pedido.getTipo().getValorBD() + " total=S/ " + pedido.getTotal());
        redirigir(req, resp, "/pedido/confirmacion?id=" + pedido.getIdPedido());
    }

    private void mostrarConfirmacion(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        // Solo quien hizo el pedido (misma sesión) puede ver su comprobante.
        if (!Validador.id(id) || !pedidosDeSesion(req).contains(id)) {
            redirigir(req, resp, "/catalogo");
            return;
        }
        Pedido pedido = cargarPedido(id);
        req.setAttribute("pedido", pedido);
        req.setAttribute("enlaceWhatsapp", com.ladonita.util.WhatsApp.enlacePedido(pedido));
        vista(req, resp, "confirmacion");
    }

    private Cliente leerCliente(HttpServletRequest req, List<String> errores) {
        Cliente c = new Cliente();
        c.setNombres(Validador.limpiar(req.getParameter("nombres")));
        c.setApellidos(Validador.limpiar(req.getParameter("apellidos")));
        c.setTelefono(Validador.limpiar(req.getParameter("telefono")));
        c.setCorreo(vacioANull(req.getParameter("correo")));
        c.setDireccionReferencia(vacioANull(req.getParameter("direccion")));

        if (!Validador.longitudValida(c.getNombres(), 100)) {
            errores.add("Ingresa tus nombres.");
        }
        if (!Validador.longitudValida(c.getApellidos(), 100)) {
            errores.add("Ingresa tus apellidos.");
        }
        if (!Validador.telefono(c.getTelefono())) {
            errores.add("El teléfono debe tener entre 7 y 15 dígitos, sin espacios.");
        }
        if (c.getCorreo() != null && !Validador.correo(c.getCorreo())) {
            errores.add("El correo electrónico no es válido.");
        }
        if (c.getDireccionReferencia() != null && c.getDireccionReferencia().length() > 200) {
            errores.add("La dirección no puede superar los 200 caracteres.");
        }
        return c;
    }

    private Pedido leerPedido(HttpServletRequest req, List<String> errores) {
        Pedido p = new Pedido();
        p.setTipo("catering".equals(req.getParameter("tipo")) ? TipoPedido.CATERING : TipoPedido.NORMAL);
        p.setMetodoEntrega("delivery".equals(req.getParameter("metodoEntrega"))
                ? MetodoEntrega.DELIVERY : MetodoEntrega.RECOJO);
        p.setObservaciones(vacioANull(req.getParameter("observaciones")));
        if (p.getObservaciones() != null && p.getObservaciones().length() > 300) {
            errores.add("Las observaciones no pueden superar los 300 caracteres.");
        }

        if (p.isCatering()) {
            DatosEvento e = leerEvento(req, errores);
            p.setEvento(e);
            if (e.getFechaEvento() != null) {
                p.setFechaEntrega(LocalDateTime.of(e.getFechaEvento(),
                        e.getHoraEvento() == null ? LocalTime.NOON : e.getHoraEvento()));
            }
        } else {
            String fecha = req.getParameter("fechaEntrega");
            if (!Validador.vacio(fecha)) {
                try {
                    LocalDateTime f = LocalDateTime.parse(fecha);
                    if (f.isBefore(LocalDateTime.now().plusHours(1))) {
                        errores.add("La fecha de entrega debe ser al menos 1 hora después de ahora.");
                    }
                    p.setFechaEntrega(f);
                } catch (DateTimeParseException ex) {
                    errores.add("La fecha de entrega no es válida.");
                }
            }
        }

        if (p.isDelivery()) {
            String direccion = vacioANull(req.getParameter("direccionEntrega"));
            if (direccion == null && p.isCatering() && p.getEvento() != null) {
                direccion = p.getEvento().getDireccionEvento();
            }
            if (direccion == null || direccion.length() > 200) {
                errores.add("Para delivery indica la dirección de entrega (máx. 200 caracteres).");
            }
            p.setDireccionEntrega(direccion);
        }
        return p;
    }

    private DatosEvento leerEvento(HttpServletRequest req, List<String> errores) {
        DatosEvento e = new DatosEvento();
        try {
            LocalDate fecha = LocalDate.parse(req.getParameter("fechaEvento"));
            if (!fecha.isAfter(LocalDate.now())) {
                errores.add("Los pedidos de catering se reservan con al menos un día de anticipación.");
            }
            e.setFechaEvento(fecha);
        } catch (RuntimeException ex) {
            errores.add("Indica la fecha del evento.");
        }
        String hora = req.getParameter("horaEvento");
        if (!Validador.vacio(hora)) {
            try {
                e.setHoraEvento(LocalTime.parse(hora));
            } catch (DateTimeParseException ex) {
                errores.add("La hora del evento no es válida.");
            }
        }
        e.setTipoServicio(Validador.limpiar(req.getParameter("tipoServicio")));
        if (!Validador.longitudValida(e.getTipoServicio(), 50)) {
            errores.add("Indica el tipo de evento (cumpleaños, boda, reunión...).");
        }
        e.setCantidadPersonas(Validador.entero(req.getParameter("cantidadPersonas"), 0));
        if (e.getCantidadPersonas() <= 0 || e.getCantidadPersonas() > 5000) {
            errores.add("Indica una cantidad de personas válida.");
        }
        e.setDireccionEvento(Validador.limpiar(req.getParameter("direccionEvento")));
        if (!Validador.longitudValida(e.getDireccionEvento(), 200)) {
            errores.add("Indica la dirección del evento.");
        }
        e.setObservaciones(vacioANull(req.getParameter("observacionesEvento")));
        if (e.getObservaciones() != null && e.getObservaciones().length() > 300) {
            errores.add("Las observaciones del evento no pueden superar los 300 caracteres.");
        }
        return e;
    }

    @SuppressWarnings("unchecked")
    private Set<String> pedidosDeSesion(HttpServletRequest req) {
        HttpSession sesion = req.getSession();
        Set<String> ids = (Set<String>) sesion.getAttribute(PEDIDOS_SESION);
        if (ids == null) {
            ids = new HashSet<>();
            sesion.setAttribute(PEDIDOS_SESION, ids);
        }
        return ids;
    }

    // =====================================================================
    // Zona interna (/admin/*): gestión de pedidos
    // =====================================================================

    private void listarPedidos(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Usuario u = usuarioActual(req);
        EstadoPedido filtro = EstadoPedido.desdeNombre(req.getParameter("estado"));

        try (Connection cn = Conexion.getConexion()) {
            PedidoDAO dao = new PedidoDAOImpl(cn);
            List<Pedido> pedidos;
            Map<EstadoPedido, Integer> conteo;
            if (u.isRepartidor()) {
                // El repartidor solo ve los pedidos que tiene asignados.
                List<Pedido> todos = dao.listarPorRepartidor(u.getIdUsuario(), null);
                conteo = new EnumMap<>(EstadoPedido.class);
                for (EstadoPedido e : EstadoPedido.values()) {
                    conteo.put(e, (int) todos.stream().filter(p -> p.getEstado() == e).count());
                }
                pedidos = filtro == null ? todos : todos.stream().filter(p -> p.getEstado() == filtro).toList();
            } else {
                pedidos = dao.listarPorEstado(filtro);
                conteo = dao.contarPorEstado();
            }
            req.setAttribute("pedidos", pedidos);
            req.setAttribute("conteo", conteo);
            req.setAttribute("totalPedidos", conteo.values().stream().mapToInt(Integer::intValue).sum());
            req.setAttribute("filtro", filtro);
            req.setAttribute("estados", EstadoPedido.values());
        } catch (SQLException e) {
            throw new ServletException("No se pudo conectar a la base de datos", e);
        }
        vista(req, resp, "admin/panelPedidos");
    }

    private void verDetalle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        Usuario u = usuarioActual(req);
        Pedido p = Validador.id(id) ? cargarPedido(id) : null;
        if (p == null || !puedeVer(u, p)) {
            flash(req, "warning", "El pedido no existe o no tienes acceso a él.");
            redirigir(req, resp, "/admin/pedidos");
            return;
        }
        req.setAttribute("pedido", p);
        req.setAttribute("estadosPermitidos", estadosPermitidos(u, p));
        if (!u.isRepartidor() && p.isDelivery()) {
            try (Connection cn = Conexion.getConexion()) {
                UsuarioDAO usuarioDAO = new UsuarioDAOImpl(cn);
                req.setAttribute("repartidores", usuarioDAO.listarPorRol(Rol.REPARTIDOR));
            } catch (SQLException e) {
                throw new ServletException("No se pudo conectar a la base de datos", e);
            }
        }
        vista(req, resp, "admin/detallePedido");
    }

    private void cambiarEstado(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        EstadoPedido nuevo = EstadoPedido.desdeNombre(req.getParameter("estado"));
        Usuario u = usuarioActual(req);
        Pedido p = Validador.id(id) ? cargarPedido(id) : null;

        if (p == null || nuevo == null || !puedeVer(u, p) || !estadosPermitidos(u, p).contains(nuevo)) {
            Bitacora.alerta(req, "ESTADO_NO_PERMITIDO", "pedido=" + id + " estado=" + req.getParameter("estado"));
            flash(req, "danger", "No tienes permiso para realizar ese cambio de estado.");
        } else {
            boolean cambiado = bd(cn -> new PedidoDAOImpl(cn).actualizarEstado(id, p.getEstado(), nuevo));
            if (cambiado) {
                Bitacora.info(req, "PEDIDO_ESTADO", id + " " + p.getEstado().getValorBD() + " -> " + nuevo.getValorBD());
                flash(req, "success", "Pedido " + id + " actualizado a \"" + nuevo.getEtiqueta() + "\".");
            } else {
                flash(req, "warning", "Otro usuario cambió este pedido hace un momento. Revisa su estado actual.");
            }
        }
        redirigir(req, resp, "/admin/pedidos/detalle?id=" + (Validador.id(id) ? id : ""));
    }

    private void asignarRepartidor(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        String idRepartidor = req.getParameter("idRepartidor");
        Usuario u = usuarioActual(req);
        if (u.isRepartidor() || !Validador.id(id) || !Validador.id(idRepartidor)) {
            flash(req, "danger", "No tienes permiso para asignar repartidores.");
            redirigir(req, resp, "/admin/pedidos");
            return;
        }
        try (Connection cn = Conexion.getConexion()) {
            boolean esRepartidor = new UsuarioDAOImpl(cn).listarPorRol(Rol.REPARTIDOR).stream()
                    .anyMatch(r -> r.getIdUsuario().equals(idRepartidor));
            if (esRepartidor && new PedidoDAOImpl(cn).asignarRepartidor(id, idRepartidor)) {
                Bitacora.info(req, "PEDIDO_REPARTIDOR", id + " repartidor=" + idRepartidor);
                flash(req, "success", "Repartidor asignado al pedido " + id + ".");
            } else {
                flash(req, "danger", "No se pudo asignar el repartidor.");
            }
        } catch (SQLException e) {
            throw new ServletException("No se pudo conectar a la base de datos", e);
        }
        redirigir(req, resp, "/admin/pedidos/detalle?id=" + id);
    }

    /** Admin y empleado ven todo; el repartidor solo sus pedidos asignados. */
    private boolean puedeVer(Usuario u, Pedido p) {
        return !u.isRepartidor()
                || (p.getRepartidor() != null && p.getRepartidor().getIdUsuario().equals(u.getIdUsuario()));
    }

    /**
     * Reglas de cambio de estado:
     * - Admin: puede corregir a cualquier estado.
     * - Empleado: solo avanza al siguiente estado.
     * - Repartidor: solo marca "en camino" y "entregado" en pedidos delivery asignados.
     */
    private List<EstadoPedido> estadosPermitidos(Usuario u, Pedido p) {
        List<EstadoPedido> permitidos = new ArrayList<>();
        if (u.isAdmin()) {
            for (EstadoPedido e : EstadoPedido.values()) {
                if (e != p.getEstado()) {
                    permitidos.add(e);
                }
            }
            return permitidos;
        }
        EstadoPedido siguiente = p.getEstado().getSiguiente();
        if (siguiente == null) {
            return permitidos;
        }
        if (u.getRol() == Rol.EMPLEADO) {
            permitidos.add(siguiente);
        } else if (u.isRepartidor() && p.isDelivery()
                && (siguiente == EstadoPedido.EN_CAMINO || siguiente == EstadoPedido.ENTREGADO)) {
            permitidos.add(siguiente);
        }
        return permitidos;
    }

    private Pedido cargarPedido(String id) throws ServletException {
        try (Connection cn = Conexion.getConexion()) {
            return new PedidoDAOImpl(cn).buscarPorId(id);
        } catch (SQLException e) {
            throw new ServletException("No se pudo conectar a la base de datos", e);
        }
    }

    private static String vacioANull(String s) {
        return Validador.vacio(s) ? null : s.trim();
    }
}
