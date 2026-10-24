package com.ladonita.controlador;

import com.ladonita.modelo.dao.CategoriaDAO;
import com.ladonita.modelo.dao.ProductoDAO;
import com.ladonita.modelo.dao.impl.CategoriaDAOImpl;
import com.ladonita.modelo.dao.impl.ProductoDAOImpl;
import com.ladonita.util.CacheCatalogo;
import com.ladonita.util.Conexion;
import com.ladonita.util.Validador;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/** Catálogo público de productos (no requiere iniciar sesión). */
@WebServlet(name = "CatalogoController", urlPatterns = {"", "/catalogo"})
public class CatalogoController extends ControladorBase {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String categoria = req.getParameter("categoria");
        if (!Validador.id(categoria)) {
            categoria = null;
        }
        String texto = Validador.limpiar(req.getParameter("q"));
        if (texto != null && texto.length() > 60) {
            texto = texto.substring(0, 60);
        }
        listarProductos(req, categoria, texto);
        vista(req, resp, "catalogo");
    }

    private void listarProductos(HttpServletRequest req, String categoria, String texto) throws ServletException {
        try (Connection cn = Conexion.getConexion()) {
            CategoriaDAO categoriaDAO = new CategoriaDAOImpl(cn);
            ProductoDAO productoDAO = new ProductoDAOImpl(cn);
            req.setAttribute("categorias", CacheCatalogo.categoriasActivas(categoriaDAO::listar));
            req.setAttribute("productos", productoDAO.buscarCatalogo(categoria, texto));
            req.setAttribute("categoriaActual", categoria);
            req.setAttribute("busqueda", texto);
        } catch (SQLException e) {
            throw new ServletException("No se pudo conectar a la base de datos", e);
        }
    }
}
