package com.ladonita.filtro;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Protección CSRF: cada sesión tiene un token aleatorio que todos los formularios
 * POST deben enviar en el campo oculto "_csrf".
 */
public class CsrfFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(CsrfFilter.class);
    public static final String ATRIBUTO = "csrfToken";
    public static final String PARAMETRO = "_csrf";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        if ("POST".equalsIgnoreCase(req.getMethod())) {
            HttpSession sesion = req.getSession(false);
            String esperado = sesion == null ? null : (String) sesion.getAttribute(ATRIBUTO);
            String recibido = req.getParameter(PARAMETRO);
            if (esperado == null || recibido == null || !MessageDigest.isEqual(
                    esperado.getBytes(StandardCharsets.UTF_8), recibido.getBytes(StandardCharsets.UTF_8))) {
                LOG.warn("POST rechazado por token CSRF inválido: {} desde {}", req.getRequestURI(), req.getRemoteAddr());
                resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "La sesión expiró o el formulario no es válido. Recarga la página e inténtalo de nuevo.");
                return;
            }
        } else if (!esRecursoEstatico(req)) {
            HttpSession sesion = req.getSession();
            if (sesion.getAttribute(ATRIBUTO) == null) {
                byte[] bytes = new byte[32];
                RANDOM.nextBytes(bytes);
                sesion.setAttribute(ATRIBUTO, Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
            }
        }
        chain.doFilter(request, response);
    }

    private boolean esRecursoEstatico(HttpServletRequest req) {
        return req.getServletPath().startsWith("/recursos/");
    }
}
