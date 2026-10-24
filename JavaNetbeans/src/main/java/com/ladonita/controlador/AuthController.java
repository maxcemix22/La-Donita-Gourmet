package com.ladonita.controlador;

import com.ladonita.modelo.dao.UsuarioDAO;
import com.ladonita.modelo.dao.impl.UsuarioDAOImpl;
import com.ladonita.modelo.entidades.Usuario;
import com.ladonita.util.Bitacora;
import com.ladonita.util.Contrasenas;
import com.ladonita.util.Validador;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.TimeUnit;

/** Inicio y cierre de sesión del personal (admin, empleado, repartidor). */
@WebServlet(name = "AuthController", urlPatterns = {"/login", "/logout"})
public class AuthController extends ControladorBase {

    private static final int MAX_INTENTOS = 5;

    /**
     * Intentos fallidos por usuario (Guava Cache): cada fallo renueva la expiración, así el bloqueo
     * se levanta solo 5 minutos después del último intento. maximumSize evita que crezca sin límite.
     */
    private final Cache<String, Integer> intentos = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("/logout".equals(req.getServletPath())) {
            // Cerrar sesión solo por POST (con token CSRF).
            redirigir(req, resp, "/catalogo");
            return;
        }
        if (usuarioActual(req) != null) {
            redirigir(req, resp, inicioPanel(usuarioActual(req)));
            return;
        }
        vista(req, resp, "login");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("/logout".equals(req.getServletPath())) {
            cerrarSesion(req, resp);
        } else {
            iniciarSesion(req, resp);
        }
    }

    private void iniciarSesion(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String usuario = Validador.limpiar(req.getParameter("usuario"));
        String clave = req.getParameter("clave");

        if (Validador.vacio(usuario) || Validador.vacio(clave)) {
            error(req, resp, usuario, "Ingresa tu usuario y contraseña.");
            return;
        }
        if (bloqueado(usuario)) {
            Bitacora.alerta(req, "LOGIN_BLOQUEADO", "usuario=" + usuario);
            error(req, resp, usuario, "Demasiados intentos fallidos. Espera unos minutos e inténtalo de nuevo.");
            return;
        }

        final String claveIngresada = clave;
        Usuario u = bd(cn -> {
            UsuarioDAO dao = new UsuarioDAOImpl(cn);
            Usuario encontrado = dao.buscarPorUsuario(usuario);
            if (encontrado != null && encontrado.isEstado()
                    && Contrasenas.verificar(claveIngresada, encontrado.getClaveHash())) {
                // Migración transparente: claves antiguas (SHA-256) pasan a PBKDF2 en el primer login.
                if (Contrasenas.necesitaActualizar(encontrado.getClaveHash())) {
                    dao.cambiarClave(encontrado.getIdUsuario(), Contrasenas.hash(claveIngresada));
                }
                return encontrado;
            }
            return null;
        });

        if (u == null) {
            registrarFallo(usuario);
            Bitacora.alerta(req, "LOGIN_FALLIDO", "usuario=" + usuario);
            // Mismo mensaje para usuario inexistente o clave errada: no revela qué usuarios existen.
            error(req, resp, usuario, "Usuario o contraseña incorrectos.");
            return;
        }

        intentos.invalidate(usuario.toLowerCase());
        u.setClaveHash(null);
        req.changeSessionId(); // previene fijación de sesión
        HttpSession sesion = req.getSession();
        sesion.setAttribute(USUARIO_SESION, u);
        Bitacora.info(req, "LOGIN", "rol=" + u.getRol().getValorBD());
        flash(req, "success", "¡Bienvenido(a), " + u.getNombres() + "!");
        redirigir(req, resp, inicioPanel(u));
    }

    private void cerrarSesion(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession sesion = req.getSession(false);
        if (sesion != null) {
            Bitacora.info(req, "LOGOUT", "");
            sesion.invalidate();
        }
        redirigir(req, resp, "/login");
    }

    private void error(HttpServletRequest req, HttpServletResponse resp, String usuario, String mensaje)
            throws ServletException, IOException {
        req.setAttribute("error", mensaje);
        req.setAttribute("usuario", usuario);
        vista(req, resp, "login");
    }

    private boolean bloqueado(String usuario) {
        Integer fallos = intentos.getIfPresent(usuario.toLowerCase());
        return fallos != null && fallos >= MAX_INTENTOS;
    }

    private void registrarFallo(String usuario) {
        // asMap().merge es atómico: dos intentos simultáneos no se pierden.
        intentos.asMap().merge(usuario.toLowerCase(), 1, Integer::sum);
    }
}
