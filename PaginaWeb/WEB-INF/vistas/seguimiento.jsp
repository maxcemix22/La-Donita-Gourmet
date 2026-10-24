<%@ include file="fragmentos/init.jspf" %>
<c:set var="titulo" value="Seguir mi pedido"/><c:set var="seccion" value="seguimiento"/>
<%@ include file="fragmentos/cabecera.jspf" %>

<div class="container contenedor py-5">
    <div class="row justify-content-center">
        <div class="col-lg-8">
            <div class="text-center mb-4">
                <span class="eyebrow">Seguimiento en línea</span>
                <h1 class="titulo-seccion mt-2">¿Dónde está mi pedido?</h1>
                <p class="text-muted">Ingresa el código que recibiste al confirmar (por ejemplo <strong>PED015</strong>) y el celular con el que hiciste el pedido.</p>
            </div>

            <form method="post" action="${ctx}/seguimiento" class="panel row g-3 align-items-end mb-4">
                <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                <div class="col-sm-5"><label class="form-label" for="codigo">Código de pedido</label>
                    <input class="form-control text-uppercase" id="codigo" name="codigo" maxlength="6" pattern="[A-Za-z]{3}\d{3}" required
                           placeholder="PED000" value="<c:out value='${codigo}'/>"></div>
                <div class="col-sm-4"><label class="form-label" for="telefono">Celular</label>
                    <input class="form-control" id="telefono" name="telefono" inputmode="numeric" pattern="\d{7,15}" maxlength="15" required placeholder="987654321"></div>
                <div class="col-sm-3 d-grid"><button class="btn btn-ld"><i class="bi bi-search"></i> Consultar</button></div>
            </form>

            <c:if test="${not empty error}">
                <div class="alert alert-warning"><i class="bi bi-info-circle"></i> <c:out value="${error}"/></div>
            </c:if>

            <c:if test="${not empty pedido}">
                <div class="panel">
                    <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-4">
                        <div><span class="eyebrow">Pedido</span>
                            <div class="codigo-pedido fs-2">${pedido.idPedido}</div></div>
                        <span class="badge badge-estado text-bg-${pedido.estado.color} fs-6">${pedido.estado.etiqueta}</span>
                    </div>
                    <div class="linea-tiempo mb-4">
                        <c:forEach var="e" items="${['Pendiente','En preparación','En camino','Entregado']}" varStatus="st">
                            <c:set var="hecho" value="${st.index <= pedido.estado.ordinal()}"/>
                            <div class="paso-t ${hecho ? 'hecho' : ''}">
                                <div class="punto"><c:if test="${hecho}"><i class="bi bi-check-lg"></i></c:if></div>
                                <c:choose>
                                    <c:when test="${st.index == 2 and not pedido.delivery}">Listo para recoger</c:when>
                                    <c:otherwise>${e}</c:otherwise>
                                </c:choose>
                            </div>
                        </c:forEach>
                    </div>
                    <div class="row small g-2 mb-3">
                        <div class="col-sm-6"><span class="text-muted">Registrado:</span> ${pedido.fechaTexto}</div>
                        <div class="col-sm-6"><span class="text-muted">Entrega estimada:</span> ${pedido.fechaEntregaTexto}</div>
                        <div class="col-sm-6"><span class="text-muted">Entrega:</span> ${pedido.metodoEntrega.etiqueta}</div>
                        <c:if test="${pedido.delivery and not empty pedido.repartidor}">
                            <div class="col-sm-6"><span class="text-muted">Repartidor:</span> <c:out value="${pedido.repartidor.nombres}"/></div>
                        </c:if>
                    </div>
                    <table class="table table-sm mb-3">
                        <c:forEach var="d" items="${pedido.detalles}">
                            <tr><td>${d.cantidad} × <c:out value="${d.producto.nombre}"/></td>
                                <td class="text-end">S/ <fmt:formatNumber value="${d.subtotal}" minFractionDigits="2" maxFractionDigits="2"/></td></tr>
                        </c:forEach>
                        <tr class="fw-bold"><td>Total</td><td class="text-end">S/ <fmt:formatNumber value="${pedido.total}" minFractionDigits="2" maxFractionDigits="2"/></td></tr>
                    </table>
                    <a class="btn btn-whatsapp" target="_blank" rel="noopener"
                       href="https://wa.me/${applicationScope.whatsappNumero}?text=Hola%2C%20consulto%20por%20mi%20pedido%20${pedido.idPedido}">
                        <i class="bi bi-whatsapp"></i> Consultar por WhatsApp</a>
                </div>
            </c:if>
        </div>
    </div>
</div>

<%@ include file="fragmentos/pie.jspf" %>
