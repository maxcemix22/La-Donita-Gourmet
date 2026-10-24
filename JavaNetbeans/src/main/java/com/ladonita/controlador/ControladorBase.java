package com.ladonita.controlador;

import com.ladonita.modelo.entidades.Carrito;
import com.ladonita.modelo.entidades.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/** Utilidades comunes a todos los servlets controladores. */
public abstract class ControladorBase extends HttpServlet {

    public static final String USUARIO_SESION = "usuarioSesion";
    public static final String CARRITO = "carrito";
    private static final String FLASH = "flash";
    private static final String FLASH_TIPO = "flashTipo";

    /** Muestra /WEB-INF/vistas/{vista}.jsp (las vistas no son accesibles directamente). */
    protected void vista(HttpServletRequest req, HttpServletResponse resp, String vista)
            throws ServletException, IOException {
        HttpSession sesion = req.getSession(false);
        if (sesion != null && sesion.getAttribute(FLASH) != null) {
            req.setAttribute("mensaje", sesion.getAttribute(FLASH));
            req.setAttribute("mensajeTipo", sesion.getAttribute(FLASH_TIPO));
            sesion.removeAttribute(FLASH);
            sesion.removeAttribute(FLASH_TIPO);
        }
        req.getRequestDispatcher("/WEB-INF/vistas/" + vista + ".jsp").forward(req, resp);
    }

    protected void redirigir(HttpServletRequest req, HttpServletResponse resp, String ruta) throws IOException {
        resp.sendRedirect(req.getContextPath() + ruta);
    }

    /** Mensaje que sobrevive a un redirect (patrón Post/Redirect/Get). tipo: success, danger, warning, info. */
    protected void flash(HttpServletRequest req, String tipo, String mensaje) {
        req.getSession().setAttribute(FLASH, mensaje);
        req.getSession().setAttribute(FLASH_TIPO, tipo);
    }

    protected Usuario usuarioActual(HttpServletRequest req) {
        HttpSession sesion = req.getSession(false);
        return sesion == null ? null : (Usuario) sesion.getAttribute(USUARIO_SESION);
    }

    /** Trabajo con la base de datos que recibe la conexión abierta. */
    @FunctionalInterface
    protected interface TrabajoBD<T> {
        T ejecutar(java.sql.Connection cn) throws java.sql.SQLException;
    }

    /** Abre una conexión, ejecuta el trabajo y la cierra. */
    protected <T> T bd(TrabajoBD<T> trabajo) throws ServletException {
        try (java.sql.Connection cn = com.ladonita.util.Conexion.getConexion()) {
            return trabajo.ejecutar(cn);
        } catch (java.sql.SQLException e) {
            throw new ServletException("No se pudo conectar a la base de datos", e);
        }
    }

    /** Igual que {@link #bd} pero dentro de una transacción: si algo falla se deshace todo. */
    protected <T> T bdTransaccion(TrabajoBD<T> trabajo) throws ServletException {
        try (java.sql.Connection cn = com.ladonita.util.Conexion.getConexion()) {
            cn.setAutoCommit(false);
            try {
                T r = trabajo.ejecutar(cn);
                cn.commit();
                return r;
            } catch (RuntimeException | java.sql.SQLException e) {
                cn.rollback();
                throw e;
            }
        } catch (java.sql.SQLException e) {
            throw new ServletException("No se pudo completar la operación en la base de datos", e);
        }
    }

    /**
     * Verifica que el usuario en sesión tenga alguno de los roles. Si no, responde 403 y devuelve false.
     * (El SeguridadFilter ya garantiza que hay sesión en /admin/*.)
     */
    protected boolean requerirRol(HttpServletRequest req, HttpServletResponse resp, com.ladonita.modelo.entidades.Rol... roles)
            throws IOException {
        Usuario u = usuarioActual(req);
        if (u != null) {
            for (com.ladonita.modelo.entidades.Rol r : roles) {
                if (u.getRol() == r) {
                    return true;
                }
            }
        }
        resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Tu rol no tiene acceso a esta sección.");
        return false;
    }

    /** Página de inicio del panel según el rol. */
    protected String inicioPanel(Usuario u) {
        return u.isRepartidor() ? "/admin/pedidos" : "/admin";
    }

    protected Carrito obtenerCarrito(HttpServletRequest req) {
        HttpSession sesion = req.getSession();
        Carrito carrito = (Carrito) sesion.getAttribute(CARRITO);
        if (carrito == null) {
            carrito = new Carrito();
            sesion.setAttribute(CARRITO, carrito);
        }
        return carrito;
    }
}
