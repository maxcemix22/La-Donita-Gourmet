package com.ladonita.filtro;

import com.ladonita.util.Config;
import com.ladonita.util.WhatsApp;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Al arrancar, deja en la aplicación los datos de configuración que usan todas las vistas. */
@WebListener
public class AppListener implements ServletContextListener {

    private static final Logger LOG = LoggerFactory.getLogger(AppListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        ctx.setAttribute("modoDemo", Config.modoDemo());
        ctx.setAttribute("whatsappNumero", Config.whatsapp());
        ctx.setAttribute("whatsappVisible", WhatsApp.numeroVisible());
        ctx.setAttribute("whatsappContacto", WhatsApp.enlace("¡Hola La Doñita Gourmet! Quisiera hacer una consulta."));
        LOG.info("La Doñita Gourmet iniciada en modo {} | WhatsApp {} | fotos en {}",
                Config.modoDemo() ? "DEMO (H2 embebida)" : "MySQL", WhatsApp.numeroVisible(), Config.carpetaImagenes());
    }
}
