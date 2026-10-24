package com.ladonita.controlador;

import com.ladonita.modelo.dao.impl.ClienteDAOImpl;
import com.ladonita.modelo.dao.impl.PedidoDAOImpl;
import com.ladonita.modelo.entidades.Cliente;
import com.ladonita.modelo.entidades.Rol;
import com.ladonita.util.Bitacora;
import com.ladonita.util.Validador;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Clientes: listado con frecuencia de compra (R05), ficha con historial y edición. Admin y empleado. */
@WebServlet(name = "ClienteController", urlPatterns = {
    "/admin/clientes", "/admin/clientes/detalle", "/admin/clientes/guardar"})
public class ClienteController extends ControladorBase {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requerirRol(req, resp, Rol.ADMIN, Rol.EMPLEADO)) {
            return;
        }
        if ("/admin/clientes/detalle".equals(req.getServletPath())) {
            String id = req.getParameter("id");
            Cliente c = Validador.id(id) ? bd(cn -> new ClienteDAOImpl(cn).buscarPorId(id)) : null;
            if (c == null) {
                flash(req, "warning", "El cliente no existe.");
                redirigir(req, resp, "/admin/clientes");
                return;
            }
            req.setAttribute("cliente", c);
            req.setAttribute("pedidos", bd(cn -> new PedidoDAOImpl(cn).listarPorCliente(id)));
            vista(req, resp, "admin/clienteDetalle");
            return;
        }
        String texto = Validador.limpiar(req.getParameter("q"));
        req.setAttribute("clientes", bd(cn -> new ClienteDAOImpl(cn).buscarConEstadisticas(texto)));
        req.setAttribute("q", texto);
        vista(req, resp, "admin/clientes");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requerirRol(req, resp, Rol.ADMIN, Rol.EMPLEADO)) {
            return;
        }
        String id = req.getParameter("idCliente");
        Cliente c = Validador.id(id) ? bd(cn -> new ClienteDAOImpl(cn).buscarPorId(id)) : null;
        if (c == null) {
            redirigir(req, resp, "/admin/clientes");
            return;
        }
        c.setNombres(Validador.limpiar(req.getParameter("nombres")));
        c.setApellidos(Validador.limpiar(req.getParameter("apellidos")));
        c.setTelefono(Validador.limpiar(req.getParameter("telefono")));
        c.setCorreo(Validador.vacio(req.getParameter("correo")) ? null : req.getParameter("correo").trim());
        c.setDireccionReferencia(Validador.vacio(req.getParameter("direccion")) ? null : req.getParameter("direccion").trim());

        String error = null;
        if (!Validador.longitudValida(c.getNombres(), 100) || !Validador.longitudValida(c.getApellidos(), 100)) {
            error = "Nombres y apellidos son obligatorios.";
        } else if (!Validador.telefono(c.getTelefono())) {
            error = "El teléfono debe tener entre 7 y 15 dígitos.";
        } else if (c.getCorreo() != null && !Validador.correo(c.getCorreo())) {
            error = "El correo no es válido.";
        } else if (c.getDireccionReferencia() != null && c.getDireccionReferencia().length() > 200) {
            error = "La dirección admite hasta 200 caracteres.";
        }
        if (error != null) {
            flash(req, "danger", error);
        } else {
            bd(cn -> new ClienteDAOImpl(cn).actualizar(c));
            Bitacora.info(req, "CLIENTE_EDITADO", id);
            flash(req, "success", "Datos del cliente actualizados.");
        }
        redirigir(req, resp, "/admin/clientes/detalle?id=" + id);
    }
}
