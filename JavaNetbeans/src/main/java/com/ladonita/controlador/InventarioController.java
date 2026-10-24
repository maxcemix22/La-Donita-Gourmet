package com.ladonita.controlador;

import com.ladonita.modelo.dao.ProductoDAO;
import com.ladonita.modelo.dao.impl.CategoriaDAOImpl;
import com.ladonita.modelo.dao.impl.ProductoDAOImpl;
import com.ladonita.modelo.entidades.Producto;
import com.ladonita.modelo.entidades.Rol;
import com.ladonita.util.Bitacora;
import com.ladonita.util.Validador;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Control de inventario: stock de productos activos con alertas y ajuste rápido
 * (sumar producción del día, restar mermas o fijar el conteo físico). Admin y empleado.
 */
@WebServlet(name = "InventarioController", urlPatterns = {"/admin/inventario", "/admin/inventario/ajustar"})
public class InventarioController extends ControladorBase {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requerirRol(req, resp, Rol.ADMIN, Rol.EMPLEADO)) {
            return;
        }
        String categoria = Validador.id(req.getParameter("categoria")) ? req.getParameter("categoria") : null;
        boolean soloBajo = "1".equals(req.getParameter("bajo"));
        bd(cn -> {
            ProductoDAO dao = new ProductoDAOImpl(cn);
            List<Producto> productos = soloBajo ? dao.listarStockBajo() : dao.buscarAdmin(null, categoria, Boolean.TRUE);
            productos.sort((a, b) -> Integer.compare(a.getStock(), b.getStock()));
            req.setAttribute("productos", productos);
            req.setAttribute("totalBajo", dao.listarStockBajo().size());
            req.setAttribute("categorias", new CategoriaDAOImpl(cn).listar());
            return null;
        });
        req.setAttribute("categoriaActual", categoria);
        req.setAttribute("soloBajo", soloBajo);
        req.setAttribute("stockMinimo", Producto.STOCK_MINIMO);
        vista(req, resp, "admin/inventario");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requerirRol(req, resp, Rol.ADMIN, Rol.EMPLEADO)) {
            return;
        }
        String id = req.getParameter("idProducto");
        String operacion = req.getParameter("operacion"); // sumar | restar | fijar
        int cantidad = Validador.entero(req.getParameter("cantidad"), -1);
        if (!Validador.id(id) || cantidad < 0 || cantidad > 1_000_000) {
            flash(req, "danger", "Indica una cantidad válida (número entero mayor o igual a 0).");
            redirigir(req, resp, "/admin/inventario");
            return;
        }
        String mensaje = bdTransaccion(cn -> {
            ProductoDAO dao = new ProductoDAOImpl(cn);
            Producto p = dao.buscarPorId(id);
            if (p == null) {
                return null;
            }
            int nuevo = switch (operacion == null ? "" : operacion) {
                case "sumar" -> p.getStock() + cantidad;
                case "restar" -> Math.max(0, p.getStock() - cantidad);
                default -> cantidad;
            };
            dao.ajustarStock(id, nuevo);
            return p.getNombre() + ": stock " + p.getStock() + " → " + nuevo + " unidades.";
        });
        if (mensaje == null) {
            flash(req, "warning", "El producto no existe.");
        } else {
            Bitacora.info(req, "STOCK_AJUSTE", id + " " + operacion + " " + cantidad + " | " + mensaje);
            flash(req, "success", mensaje);
        }
        String volver = req.getParameter("volver");
        redirigir(req, resp, "/admin/inventario" + ("bajo".equals(volver) ? "?bajo=1" : ""));
    }
}
