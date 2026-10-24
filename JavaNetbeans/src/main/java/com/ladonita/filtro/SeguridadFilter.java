package com.ladonita.filtro;

import com.ladonita.controlador.ControladorBase;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Protege la zona interna (/admin/*): solo entra personal autenticado.
 * Los permisos finos por rol se validan en cada controlador.
 */
public class SeguridadFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession sesion = req.getSession(false);

        if (sesion == null || sesion.getAttribute(ControladorBase.USUARIO_SESION) == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        // Evita que el navegador muestre páginas internas desde caché tras cerrar sesión.
        resp.setHeader("Cache-Control", "no-store");
        chain.doFilter(request, response);
    }
}
