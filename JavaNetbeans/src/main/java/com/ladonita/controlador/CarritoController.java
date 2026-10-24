package com.ladonita.controlador;

import com.ladonita.modelo.dao.ProductoDAO;
import com.ladonita.modelo.dao.impl.ProductoDAOImpl;
import com.ladonita.modelo.entidades.Carrito;
import com.ladonita.modelo.entidades.Producto;
import com.ladonita.util.Conexion;
import com.ladonita.util.Validador;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/** Carrito de compras del cliente, guardado en la sesión. */
@WebServlet(name = "CarritoController", urlPatterns = {"/carrito"})
public class CarritoController extends ControladorBase {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        verCarrito(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String accion = req.getParameter("accion");
        String idProducto = req.getParameter("idProducto");
        Carrito carrito = obtenerCarrito(req);

        if ("vaciar".equals(accion)) {
            carrito.vaciar();
            flash(req, "info", "Tu carrito quedó vacío.");
            redirigir(req, resp, "/carrito");
            return;
        }
        if (!Validador.id(idProducto)) {
            redirigir(req, resp, "/carrito");
            return;
        }
        switch (accion == null ? "" : accion) {
            case "agregar" -> agregar(req, resp, carrito, idProducto);
            case "actualizar" -> actualizar(req, resp, carrito, idProducto);
            case "quitar" -> {
                carrito.quitar(idProducto);
                flash(req, "info", "Producto retirado del carrito.");
                redirigir(req, resp, "/carrito");
            }
            default -> redirigir(req, resp, "/carrito");
        }
    }

    private void verCarrito(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("carrito", obtenerCarrito(req));
        vista(req, resp, "carrito");
    }

    private void agregar(HttpServletRequest req, HttpServletResponse resp, Carrito carrito, String idProducto)
            throws ServletException, IOException {
        int cantidad = Math.max(1, Validador.entero(req.getParameter("cantidad"), 1));
        Producto p = buscarProducto(idProducto);
        if (p == null || !p.isDisponible()) {
            flash(req, "warning", "Ese producto no está disponible por ahora.");
        } else {
            int antes = carrito.getItems().stream()
                    .filter(i -> i.getProducto().getIdProducto().equals(idProducto))
                    .mapToInt(i -> i.getCantidad()).sum();
            int ahora = carrito.agregar(p, cantidad);
            if (ahora - antes < cantidad) {
                flash(req, "warning", "Solo quedan " + p.getStock() + " unidades de " + p.getNombre()
                        + ". Ajustamos la cantidad en tu carrito.");
            } else {
                flash(req, "success", p.getNombre() + " se agregó a tu carrito.");
            }
        }
        // Regresa a la categoría que el cliente estaba viendo.
        String categoria = req.getParameter("categoria");
        redirigir(req, resp, Validador.id(categoria) ? "/catalogo?categoria=" + categoria + "#catalogo" : "/catalogo#catalogo");
    }

    private void actualizar(HttpServletRequest req, HttpServletResponse resp, Carrito carrito, String idProducto)
            throws ServletException, IOException {
        int cantidad = Validador.entero(req.getParameter("cantidad"), -1);
        Producto p = buscarProducto(idProducto);
        if (p == null || !p.isDisponible() || cantidad <= 0) {
            carrito.quitar(idProducto);
            flash(req, "info", "Producto retirado del carrito.");
        } else {
            carrito.actualizar(p, cantidad);
            if (cantidad > p.getStock()) {
                flash(req, "warning", "Solo quedan " + p.getStock() + " unidades de " + p.getNombre() + ".");
            }
        }
        redirigir(req, resp, "/carrito");
    }

    private Producto buscarProducto(String id) throws ServletException {
        try (Connection cn = Conexion.getConexion()) {
            ProductoDAO dao = new ProductoDAOImpl(cn);
            return dao.buscarPorId(id);
        } catch (SQLException e) {
            throw new ServletException("No se pudo conectar a la base de datos", e);
        }
    }
}
