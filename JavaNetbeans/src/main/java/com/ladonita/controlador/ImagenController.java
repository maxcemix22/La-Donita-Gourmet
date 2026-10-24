package com.ladonita.controlador;

import com.ladonita.util.Config;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

/** Sirve las fotos de producto subidas desde el panel (guardadas fuera del WAR). */
@WebServlet(name = "ImagenController", urlPatterns = {"/imagenes/*"})
public class ImagenController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // FilenameUtils.getName (Commons IO) descarta cualquier ruta: "../../x.jpg" -> "x.jpg"
        String nombre = FilenameUtils.getName(StringUtils.defaultString(req.getPathInfo()));
        // Solo nombres generados por el sistema: evita salir de la carpeta (../)
        if (!nombre.matches("[A-Z]{3}\\d{3}-\\d+\\.(jpg|png|webp)")) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Path archivo = Config.carpetaImagenes().resolve(nombre);
        if (!Files.isRegularFile(archivo)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String ext = FilenameUtils.getExtension(nombre);
        resp.setContentType("jpg".equals(ext) ? "image/jpeg" : "image/" + ext);
        resp.setHeader("Cache-Control", "public, max-age=86400");
        resp.setContentLengthLong(Files.size(archivo));
        Files.copy(archivo, resp.getOutputStream());
    }
}
