package com.ladonita.util;

import com.ladonita.modelo.entidades.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bitácora de auditoría (Logback, logger "AUDITORIA" → logs/auditoria.log).
 * Registra quién hizo qué y desde dónde: inicios de sesión, pedidos, cambios de estado,
 * productos, stock y usuarios. Nunca registra contraseñas ni datos de pago.
 */
public final class Bitacora {

    private static final Logger AUDITORIA = LoggerFactory.getLogger("AUDITORIA");

    private Bitacora() {
    }

    public static void info(HttpServletRequest req, String accion, String detalle) {
        AUDITORIA.info("{} | {} | {}", quien(req), accion, limpiar(detalle));
    }

    public static void alerta(HttpServletRequest req, String accion, String detalle) {
        AUDITORIA.warn("{} | {} | {}", quien(req), accion, limpiar(detalle));
    }

    /** "usuario@ip" para el personal, "cliente@ip" para visitantes de la tienda. */
    private static String quien(HttpServletRequest req) {
        if (req == null) {
            return "sistema";
        }
        HttpSession sesion = req.getSession(false);
        Object u = sesion == null ? null : sesion.getAttribute("usuarioSesion");
        String nombre = u instanceof Usuario usr ? usr.getUsuario() : "cliente";
        return nombre + "@" + req.getRemoteAddr();
    }

    /** Evita que un dato del usuario con saltos de línea falsifique entradas de la bitácora. */
    private static String limpiar(String texto) {
        return StringUtils.abbreviate(StringUtils.normalizeSpace(StringUtils.defaultString(texto)), 300);
    }
}
