package com.ladonita.controlador;

import com.ladonita.modelo.dao.impl.PedidoDAOImpl;
import com.ladonita.modelo.entidades.Pedido;
import com.ladonita.util.Validador;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Seguimiento público: el cliente consulta el estado de su pedido con el código y su teléfono
 * (ambos deben coincidir, así nadie ve pedidos ajenos solo probando códigos).
 */
@WebServlet(name = "SeguimientoController", urlPatterns = {"/seguimiento"})
public class SeguimientoController extends ControladorBase {

    private static final int MAX_CONSULTAS = 15;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        vista(req, resp, "seguimiento");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String codigo = Validador.limpiar(req.getParameter("codigo"));
        codigo = codigo == null ? null : codigo.toUpperCase();
        String telefono = Validador.limpiar(req.getParameter("telefono"));
        req.setAttribute("codigo", codigo);

        HttpSession sesion = req.getSession();
        Integer consultas = (Integer) sesion.getAttribute("consultasSeguimiento");
        consultas = consultas == null ? 1 : consultas + 1;
        sesion.setAttribute("consultasSeguimiento", consultas);
        if (consultas > MAX_CONSULTAS) {
            req.setAttribute("error", "Hiciste demasiadas consultas. Inténtalo más tarde o escríbenos por WhatsApp.");
            vista(req, resp, "seguimiento");
            return;
        }

        Pedido p = null;
        if (Validador.id(codigo) && Validador.telefono(telefono)) {
            final String id = codigo;
            p = bd(cn -> new PedidoDAOImpl(cn).buscarPorId(id));
            if (p != null && !p.getCliente().getTelefono().equals(telefono)) {
                p = null;
            }
        }
        if (p == null) {
            req.setAttribute("error", "No encontramos un pedido con ese código y teléfono. Revisa los datos.");
        } else {
            req.setAttribute("pedido", p);
        }
        vista(req, resp, "seguimiento");
    }
}
