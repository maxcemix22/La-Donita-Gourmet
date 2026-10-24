package com.ladonita.controlador;

import com.ladonita.modelo.dao.CategoriaDAO;
import com.ladonita.modelo.dao.impl.CategoriaDAOImpl;
import com.ladonita.modelo.entidades.Categoria;
import com.ladonita.modelo.entidades.Rol;
import com.ladonita.util.Bitacora;
import com.ladonita.util.CacheCatalogo;
import com.ladonita.util.Validador;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Mantenimiento de categorías. Solo administrador. Las categorías no se borran: se desactivan. */
@WebServlet(name = "CategoriaController", urlPatterns = {
    "/admin/categorias", "/admin/categorias/guardar", "/admin/categorias/estado"})
public class CategoriaController extends ControladorBase {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requerirRol(req, resp, Rol.ADMIN)) {
            return;
        }
        String editar = req.getParameter("editar");
        bd(cn -> {
            CategoriaDAO dao = new CategoriaDAOImpl(cn);
            req.setAttribute("categorias", dao.listarTodas());
            if (Validador.id(editar)) {
                req.setAttribute("edicion", dao.buscarPorId(editar));
            }
            return null;
        });
        vista(req, resp, "admin/categorias");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requerirRol(req, resp, Rol.ADMIN)) {
            return;
        }
        String id = req.getParameter("idCategoria");
        if ("/admin/categorias/estado".equals(req.getServletPath())) {
            boolean activar = "true".equals(req.getParameter("activar"));
            if (Validador.id(id)) {
                bd(cn -> new CategoriaDAOImpl(cn).cambiarEstado(id, activar));
                CacheCatalogo.invalidar();
                Bitacora.info(req, activar ? "CATEGORIA_ACTIVADA" : "CATEGORIA_DESACTIVADA", id);
                flash(req, "success", "Categoría " + id + (activar ? " activada." : " desactivada: sus productos ya no se muestran en el catálogo."));
            }
            redirigir(req, resp, "/admin/categorias");
            return;
        }

        Categoria c = new Categoria(Validador.vacio(id) ? null : id, Validador.limpiar(req.getParameter("nombre")));
        c.setDescripcion(Validador.vacio(req.getParameter("descripcion")) ? null : req.getParameter("descripcion").trim());
        c.setEstado(!"false".equals(req.getParameter("estado")));
        if (!Validador.longitudValida(c.getNombre(), 50) || (c.getDescripcion() != null && c.getDescripcion().length() > 200)) {
            flash(req, "danger", "El nombre es obligatorio (máx. 50 caracteres) y la descripción admite hasta 200.");
            redirigir(req, resp, "/admin/categorias" + (c.getIdCategoria() == null ? "" : "?editar=" + c.getIdCategoria()));
            return;
        }
        boolean nueva = c.getIdCategoria() == null;
        if (!nueva && !Validador.id(c.getIdCategoria())) {
            redirigir(req, resp, "/admin/categorias");
            return;
        }
        bdTransaccion(cn -> nueva ? new CategoriaDAOImpl(cn).insertar(c) : new CategoriaDAOImpl(cn).actualizar(c));
        CacheCatalogo.invalidar();
        Bitacora.info(req, nueva ? "CATEGORIA_CREADA" : "CATEGORIA_EDITADA", c.getIdCategoria() + " " + c.getNombre());
        flash(req, "success", "Categoría " + c.getIdCategoria() + (nueva ? " creada." : " actualizada."));
        redirigir(req, resp, "/admin/categorias");
    }
}
