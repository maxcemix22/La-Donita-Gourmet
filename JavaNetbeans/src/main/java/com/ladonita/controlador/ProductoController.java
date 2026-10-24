package com.ladonita.controlador;

import com.ladonita.modelo.dao.ProductoDAO;
import com.ladonita.modelo.dao.impl.CategoriaDAOImpl;
import com.ladonita.modelo.dao.impl.ProductoDAOImpl;
import com.ladonita.modelo.entidades.Categoria;
import com.ladonita.modelo.entidades.Producto;
import com.ladonita.modelo.entidades.Rol;
import com.ladonita.util.Bitacora;
import com.ladonita.util.Config;
import com.ladonita.util.Validador;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

/** Mantenimiento de productos (CRUD) con foto. Solo administrador. */
@WebServlet(name = "ProductoController", urlPatterns = {
    "/admin/productos", "/admin/productos/nuevo", "/admin/productos/editar",
    "/admin/productos/guardar", "/admin/productos/eliminar"})
@MultipartConfig(maxFileSize = 2 * 1024 * 1024, maxRequestSize = 3 * 1024 * 1024)
public class ProductoController extends ControladorBase {

    private static final Logger LOG = LoggerFactory.getLogger(ProductoController.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requerirRol(req, resp, Rol.ADMIN)) {
            return;
        }
        switch (req.getServletPath()) {
            case "/admin/productos/nuevo" -> formulario(req, resp, nuevoProducto(), new ArrayList<>());
            case "/admin/productos/editar" -> {
                Producto p = Validador.id(req.getParameter("id"))
                        ? bd(cn -> new ProductoDAOImpl(cn).buscarPorId(req.getParameter("id"))) : null;
                if (p == null) {
                    flash(req, "warning", "El producto no existe.");
                    redirigir(req, resp, "/admin/productos");
                } else {
                    formulario(req, resp, p, new ArrayList<>());
                }
            }
            default -> listar(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requerirRol(req, resp, Rol.ADMIN)) {
            return;
        }
        if ("/admin/productos/eliminar".equals(req.getServletPath())) {
            eliminar(req, resp);
        } else {
            guardar(req, resp);
        }
    }

    private void listar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String texto = Validador.limpiar(req.getParameter("q"));
        String categoria = Validador.id(req.getParameter("categoria")) ? req.getParameter("categoria") : null;
        String estado = req.getParameter("estado");
        Boolean activo = "activos".equals(estado) ? Boolean.TRUE : "inactivos".equals(estado) ? Boolean.FALSE : null;
        bd(cn -> {
            req.setAttribute("productos", new ProductoDAOImpl(cn).buscarAdmin(texto, categoria, activo));
            req.setAttribute("categorias", new CategoriaDAOImpl(cn).listarTodas());
            return null;
        });
        req.setAttribute("q", texto);
        req.setAttribute("categoriaActual", categoria);
        req.setAttribute("estadoActual", estado);
        vista(req, resp, "admin/productos");
    }

    private void formulario(HttpServletRequest req, HttpServletResponse resp, Producto p, List<String> errores)
            throws ServletException, IOException {
        req.setAttribute("producto", p);
        req.setAttribute("errores", errores);
        req.setAttribute("categorias", bd(cn -> new CategoriaDAOImpl(cn).listarTodas()));
        vista(req, resp, "admin/productoForm");
    }

    private void guardar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("idProducto");
        boolean nuevo = Validador.vacio(id);
        List<String> errores = new ArrayList<>();

        Producto p = nuevo ? nuevoProducto() : bd(cn -> new ProductoDAOImpl(cn).buscarPorId(id));
        if (p == null) {
            flash(req, "warning", "El producto no existe.");
            redirigir(req, resp, "/admin/productos");
            return;
        }
        final String fotoOriginal = p.getFotoUrl();
        p.setNombre(Validador.limpiar(req.getParameter("nombre")));
        p.setDescripcion(Validador.vacio(req.getParameter("descripcion")) ? null : req.getParameter("descripcion").trim());
        String idCategoria = req.getParameter("idCategoria");
        p.setCategoria(new Categoria(idCategoria, null));
        p.setEstado("on".equals(req.getParameter("estado")) || "true".equals(req.getParameter("estado")));

        if (!Validador.longitudValida(p.getNombre(), 100)) {
            errores.add("El nombre es obligatorio (máx. 100 caracteres).");
        }
        if (p.getDescripcion() != null && p.getDescripcion().length() > 300) {
            errores.add("La descripción no puede superar los 300 caracteres.");
        }
        if (!Validador.id(idCategoria) || bd(cn -> new CategoriaDAOImpl(cn).buscarPorId(idCategoria)) == null) {
            errores.add("Elige una categoría válida.");
        }
        try {
            BigDecimal precio = new BigDecimal(req.getParameter("precio").trim().replace(',', '.'));
            if (precio.signum() < 0 || precio.compareTo(new BigDecimal("999999.99")) > 0 || precio.scale() > 2) {
                throw new NumberFormatException();
            }
            p.setPrecio(precio);
        } catch (RuntimeException e) {
            errores.add("El precio debe ser un número positivo con máximo 2 decimales.");
        }
        int stock = Validador.entero(req.getParameter("stock"), -1);
        if (stock < 0 || stock > 1_000_000) {
            errores.add("El stock debe ser un número entero mayor o igual a 0.");
        } else {
            p.setStock(stock);
        }

        String urlExterna = Validador.limpiar(req.getParameter("fotoUrl"));
        if ("quitar".equals(req.getParameter("accionFoto"))) {
            p.setFotoUrl(null);
        } else if (!Validador.vacio(urlExterna)) {
            if (!urlExterna.matches("https://[\\w.-]+(/[\\w./%~+-]*)?(\\?[\\w=&.%-]*)?") || urlExterna.length() > 255) {
                errores.add("La URL de la foto debe empezar con https:// y no tener espacios.");
            } else {
                p.setFotoUrl(urlExterna);
            }
        }
        Part foto = req.getPart("foto");
        String extension = null;
        if (foto != null && foto.getSize() > 0) {
            extension = extensionImagen(foto);
            if (extension == null) {
                errores.add("La foto debe ser JPG, PNG o WEBP (máx. 2 MB).");
            }
        }

        if (!errores.isEmpty()) {
            formulario(req, resp, p, errores);
            return;
        }

        final String ext = extension;
        bdTransaccion(cn -> {
            ProductoDAO dao = new ProductoDAOImpl(cn);
            if (nuevo) {
                dao.insertar(p);
            }
            if (ext != null) {
                p.setFotoUrl(guardarFoto(foto, p.getIdProducto(), ext));
            }
            dao.actualizar(p);
            return null;
        });
        if (fotoOriginal != null && !fotoOriginal.equals(p.getFotoUrl())) {
            borrarFotoSubida(fotoOriginal); // la foto anterior ya no la usa nadie
        }
        Bitacora.info(req, nuevo ? "PRODUCTO_CREADO" : "PRODUCTO_EDITADO", p.getIdProducto() + " " + p.getNombre()
                + " precio=S/ " + p.getPrecio() + " stock=" + p.getStock() + " activo=" + p.isEstado());
        flash(req, "success", "Producto " + p.getIdProducto() + (nuevo ? " registrado." : " actualizado."));
        redirigir(req, resp, "/admin/productos");
    }

    private void eliminar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        if (Validador.id(id)) {
            boolean borrado = bd(cn -> new ProductoDAOImpl(cn).eliminar(id));
            Bitacora.info(req, borrado ? "PRODUCTO_ELIMINADO" : "PRODUCTO_DESACTIVADO", id);
            flash(req, borrado ? "success" : "info", borrado
                    ? "Producto " + id + " eliminado."
                    : "El producto " + id + " tiene pedidos registrados: se desactivó para conservar el historial.");
        }
        redirigir(req, resp, "/admin/productos");
    }

    private Producto nuevoProducto() {
        Producto p = new Producto();
        p.setEstado(true);
        p.setPrecio(BigDecimal.ZERO);
        p.setCategoria(new Categoria());
        return p;
    }

    /** Valida tipo y contenido real del archivo (firma de bytes), no solo la extensión. */
    private String extensionImagen(Part foto) throws IOException {
        byte[] cab = new byte[12];
        int leidos;
        try (InputStream in = foto.getInputStream()) {
            leidos = IOUtils.read(in, cab); // Commons IO: lee hasta llenar el arreglo o terminar el archivo
        }
        if (leidos >= 3 && (cab[0] & 0xFF) == 0xFF && (cab[1] & 0xFF) == 0xD8 && (cab[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (leidos >= 8 && (cab[0] & 0xFF) == 0x89 && cab[1] == 'P' && cab[2] == 'N' && cab[3] == 'G') {
            return "png";
        }
        if (leidos >= 12 && cab[0] == 'R' && cab[1] == 'I' && cab[2] == 'F' && cab[3] == 'F'
                && cab[8] == 'W' && cab[9] == 'E' && cab[10] == 'B' && cab[11] == 'P') {
            return "webp";
        }
        return null;
    }

    /** Borra del disco una foto subida antes (solo las de la carpeta del sistema, nunca URLs externas). */
    private void borrarFotoSubida(String fotoUrl) {
        if (fotoUrl.startsWith("imagenes/")) {
            String nombre = FilenameUtils.getName(fotoUrl); // descarta cualquier ruta: solo el nombre del archivo
            if (!FileUtils.deleteQuietly(Config.carpetaImagenes().resolve(nombre).toFile())) {
                LOG.warn("No se pudo borrar la foto anterior {}", nombre);
            }
        }
    }

    private String guardarFoto(Part foto, String idProducto, String ext) {
        try {
            String nombre = idProducto + "-" + System.currentTimeMillis() + "." + ext;
            try (InputStream in = foto.getInputStream()) {
                // Commons IO: crea la carpeta si falta y copia el archivo
                FileUtils.copyInputStreamToFile(in, Config.carpetaImagenes().resolve(nombre).toFile());
            }
            return "imagenes/" + nombre;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar la foto en " + Config.carpetaImagenes(), e);
        }
    }
}
