<%@ include file="fragmentos/init.jspf" %>
<c:set var="titulo" value="Pedido registrado"/>
<%@ include file="fragmentos/cabecera.jspf" %>

<div class="container contenedor py-5">
    <div class="row justify-content-center">
        <div class="col-lg-7">
            <div class="panel text-center p-4 p-md-5">
                <div class="check-exito mb-3"><i class="bi bi-check-lg"></i></div>
                <h2>¡Gracias, <c:out value="${pedido.cliente.nombres}"/>!</h2>
                <p class="text-muted">Recibimos tu pedido. Te contactaremos al <strong><c:out value="${pedido.cliente.telefono}"/></strong> para confirmarlo.</p>
                <div class="my-4">
                    <div class="small text-muted text-uppercase">Código de pedido</div>
                    <div class="codigo-pedido">${pedido.idPedido}</div>
                </div>

                <div class="text-start">
                    <div class="row small g-2 mb-3">
                        <div class="col-6"><span class="text-muted">Tipo:</span> ${pedido.tipo.etiqueta}</div>
                        <div class="col-6"><span class="text-muted">Entrega:</span> ${pedido.metodoEntrega.etiqueta}</div>
                        <div class="col-6"><span class="text-muted">Fecha estimada:</span> ${pedido.fechaEntregaTexto}</div>
                        <div class="col-6"><span class="text-muted">Estado:</span>
                            <span class="badge badge-estado text-bg-${pedido.estado.color}">${pedido.estado.etiqueta}</span></div>
                        <c:if test="${pedido.catering}">
                            <div class="col-12"><span class="text-muted">Evento:</span>
                                <c:out value="${pedido.evento.tipoServicio}"/> · ${pedido.evento.cantidadPersonas} personas · ${pedido.evento.fechaEventoTexto}</div>
                        </c:if>
                    </div>
                    <table class="table table-sm">
                        <tbody>
                        <c:forEach var="d" items="${pedido.detalles}">
                            <tr>
                                <td>${d.cantidad} × <c:out value="${d.producto.nombre}"/></td>
                                <td class="text-end">S/ <fmt:formatNumber value="${d.subtotal}" minFractionDigits="2" maxFractionDigits="2"/></td>
                            </tr>
                        </c:forEach>
                        </tbody>
                        <tfoot>
                        <tr class="fw-bold">
                            <td>Total</td>
                            <td class="text-end">S/ <fmt:formatNumber value="${pedido.total}" minFractionDigits="2" maxFractionDigits="2"/></td>
                        </tr>
                        </tfoot>
                    </table>
                </div>
                <div class="d-flex flex-wrap justify-content-center gap-2 mt-2">
                    <a href="<c:out value='${enlaceWhatsapp}'/>" target="_blank" rel="noopener" class="btn btn-whatsapp px-4">
                        <i class="bi bi-whatsapp"></i> Enviar mi pedido por WhatsApp</a>
                    <a href="${ctx}/catalogo" class="btn btn-suave">Volver al catálogo</a>
                </div>
                <p class="small text-muted mt-3 mb-0">Guarda tu código: con él y tu celular puedes <a href="${ctx}/seguimiento">seguir tu pedido</a> en cualquier momento.</p>
            </div>
        </div>
    </div>
</div>

<%@ include file="fragmentos/pie.jspf" %>
