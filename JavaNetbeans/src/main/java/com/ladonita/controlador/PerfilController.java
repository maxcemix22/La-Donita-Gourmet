package com.ladonita.controlador;

import com.ladonita.modelo.dao.UsuarioDAO;
import com.ladonita.modelo.dao.impl.UsuarioDAOImpl;
import com.ladonita.modelo.entidades.Usuario;
import com.ladonita.util.Bitacora;
import com.ladonita.util.Contrasenas;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Mi perfil: datos del usuario en sesión y cambio de su propia contraseña. Todos los roles. */
@WebServlet(name = "PerfilController", urlPatterns = {"/admin/perfil"})
public class PerfilController extends ControladorBase {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = usuarioActual(req).getIdUsuario();
        req.setAttribute("perfil", bd(cn -> new UsuarioDAOImpl(cn).buscarPorId(id)));
        vista(req, resp, "admin/perfil");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = usuarioActual(req).getIdUsuario();
        String actual = req.getParameter("actual");
        String nueva = req.getParameter("nueva");
        String confirmar = req.getParameter("confirmar");

        String error = Contrasenas.validarNueva(nueva);
        if (error == null && !nueva.equals(confirmar)) {
            error = "La confirmación no coincide con la nueva contraseña.";
        }
        if (error == null) {
            boolean ok = bd(cn -> {
                UsuarioDAO dao = new UsuarioDAOImpl(cn);
                Usuario u = dao.buscarPorId(id);
                if (u == null || !Contrasenas.verificar(actual, u.getClaveHash())) {
                    return false;
                }
                return dao.cambiarClave(id, Contrasenas.hash(nueva));
            });
            error = ok ? null : "La contraseña actual no es correcta.";
        }
        if (error != null) {
            Bitacora.alerta(req, "CAMBIO_CLAVE_RECHAZADO", error);
            flash(req, "danger", error);
        } else {
            Bitacora.info(req, "CAMBIO_CLAVE", "");
            flash(req, "success", "Tu contraseña se actualizó correctamente.");
        }
        redirigir(req, resp, "/admin/perfil");
    }
}
