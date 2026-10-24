<%@ page isErrorPage="true" %>
<%@ include file="fragmentos/init.jspf" %>
<c:set var="codigo" value="${requestScope['jakarta.servlet.error.status_code']}"/>
<c:set var="titulo" value="Ups, algo salió mal"/>
<%@ include file="fragmentos/cabecera.jspf" %>
<%
    // El detalle técnico va al log del servidor, nunca a la pantalla del usuario.
    Throwable error = (Throwable) request.getAttribute("jakarta.servlet.error.exception");
    if (error != null) {
        org.slf4j.LoggerFactory.getLogger("com.ladonita.error")
                .error("Error en " + request.getAttribute("jakarta.servlet.error.request_uri"), error);
    }
%>
<div class="container py-5">
    <div class="panel estado-vacio mx-auto" style="max-width: 560px">
        <c:choose>
            <c:when test="${codigo == 404}">
                <i class="bi bi-signpost-split"></i>
                <h4 class="mt-3">Página no encontrada</h4>
                <p>La dirección que buscas no existe o fue movida.</p>
            </c:when>
            <c:when test="${codigo == 403}">
                <i class="bi bi-shield-lock"></i>
                <h4 class="mt-3">Acción no permitida</h4>
                <p>Tu sesión pudo haber expirado. Recarga la página e inténtalo nuevamente.</p>
            </c:when>
            <c:otherwise>
                <i class="bi bi-cup-hot"></i>
                <h4 class="mt-3">Estamos teniendo un problema</h4>
                <p>No pudimos completar tu solicitud. Inténtalo de nuevo en unos minutos.</p>
            </c:otherwise>
        </c:choose>
        <a href="${ctx}/catalogo" class="btn btn-ld">Volver al inicio</a>
    </div>
</div>
<%@ include file="fragmentos/pie.jspf" %>
