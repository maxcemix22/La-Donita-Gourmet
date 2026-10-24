package com.ladonita.util;

import com.ladonita.modelo.entidades.DetallePedido;
import com.ladonita.modelo.entidades.Pedido;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Arma el enlace wa.me con el resumen del pedido (misma idea que armarMensaje() de la página
 * PaginaWeb/js/app.js del repositorio): el pedido ya quedó guardado y el cliente puede
 * enviarlo al negocio con un clic para coordinar pago y entrega.
 */
public final class WhatsApp {

    private WhatsApp() {
    }

    public static String enlacePedido(Pedido p) {
        StringBuilder m = new StringBuilder();
        m.append("¡Hola La Doñita Gourmet! Acabo de registrar mi pedido *").append(p.getIdPedido()).append("*\n\n");
        for (DetallePedido d : p.getDetalles()) {
            m.append("• ").append(d.getCantidad()).append(" x ").append(d.getProducto().getNombre())
                    .append(" — S/ ").append(d.getSubtotal()).append('\n');
        }
        m.append("\n*Total: S/ ").append(p.getTotal()).append("*\n");
        m.append("Tipo: ").append(p.getTipo().getEtiqueta()).append(" | Entrega: ").append(p.getMetodoEntrega().getEtiqueta()).append('\n');
        if (p.isDelivery() && p.getDireccionEntrega() != null) {
            m.append("Dirección: ").append(p.getDireccionEntrega()).append('\n');
        }
        if (p.isCatering() && p.getEvento() != null) {
            m.append("Evento: ").append(p.getEvento().getTipoServicio()).append(", ")
                    .append(p.getEvento().getCantidadPersonas()).append(" personas, ")
                    .append(p.getEvento().getFechaEventoTexto()).append('\n');
        }
        m.append("A nombre de: ").append(p.getCliente().getNombreCompleto());
        return enlace(m.toString());
    }

    public static String enlace(String mensaje) {
        return "https://wa.me/" + Config.whatsapp() + "?text=" + URLEncoder.encode(mensaje, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /** Número con formato legible para mostrar (ej. +51 912 679 851). */
    public static String numeroVisible() {
        String n = Config.whatsapp();
        if (n.length() == 11 && n.startsWith("51")) {
            return "+51 " + n.substring(2, 5) + " " + n.substring(5, 8) + " " + n.substring(8);
        }
        return "+" + n;
    }
}
