<%@ include file="../fragmentos/init.jspf" %>
<c:set var="titulo" value="Pedido ${pedido.idPedido}"/><c:set var="seccion" value="pedidos"/>
<%@ include file="../fragmentos/admin-inicio.jspf" %>

<div>
    <a href="${ctx}/admin/pedidos" class="small text-muted text-decoration-none"><i class="bi bi-arrow-left"></i> Volver al panel</a>
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mt-2 mb-4">
        <h2 class="fw-bold mb-0" >Pedido ${pedido.idPedido}
            <span class="badge badge-estado text-bg-${pedido.estado.color} fs-6 align-middle">${pedido.estado.etiqueta}</span>
        </h2>
        <span class="text-muted small"><i class="bi bi-calendar3"></i> Registrado el ${pedido.fechaTexto}</span>
    </div>

    <!-- Línea de tiempo del estado -->
    <div class="panel mb-4">
        <div class="linea-tiempo">
            <c:forEach var="e" items="${['PENDIENTE','EN_PREPARACION','EN_CAMINO','ENTREGADO']}" varStatus="st">
                <c:set var="hecho" value="${st.index <= pedido.estado.ordinal()}"/>
                <div class="paso-t ${hecho ? 'hecho' : ''}">
                    <div class="punto"><c:if test="${hecho}"><i class="bi bi-check-lg"></i></c:if></div>
                    <c:choose>
                        <c:when test="${e == 'PENDIENTE'}">Pendiente</c:when>
                        <c:when test="${e == 'EN_PREPARACION'}">En preparación</c:when>
                        <c:when test="${e == 'EN_CAMINO'}">En camino</c:when>
                        <c:otherwise>Entregado</c:otherwise>
                    </c:choose>
                </div>
            </c:forEach>
        </div>
    </div>

    <div class="row g-4">
        <div class="col-lg-8">
            <div class="panel mb-4">
                <h5 class="panel-titulo mb-3">Productos</h5>
                <div class="table-responsive">
                    <table class="table align-middle">
                        <thead class="table-light">
                        <tr><th>Producto</th><th class="text-center">Cant.</th><th class="text-end">P. unit.</th><th class="text-end">Subtotal</th></tr>
                        </thead>
                        <tbody>
                        <c:forEach var="d" items="${pedido.detalles}">
                            <tr>
                                <td><c:out value="${d.producto.nombre}"/><div class="small text-muted"><c:out value="${d.producto.categoria.nombre}"/></div></td>
                                <td class="text-center">${d.cantidad}</td>
                                <td class="text-end">S/ <fmt:formatNumber value="${d.precioUnitario}" minFractionDigits="2" maxFractionDigits="2"/></td>
                                <td class="text-end">S/ <fmt:formatNumber value="${d.subtotal}" minFractionDigits="2" maxFractionDigits="2"/></td>
                            </tr>
                        </c:forEach>
                        </tbody>
                        <tfoot>
                        <tr class="linea-total"><td colspan="3">Total</td>
                            <td class="text-end">S/ <fmt:formatNumber value="${pedido.total}" minFractionDigits="2" maxFractionDigits="2"/></td></tr>
                        </tfoot>
                    </table>
                </div>
                <c:if test="${not empty pedido.observaciones}">
                    <div class="alert alert-light border small mb-0"><i class="bi bi-chat-left-text"></i> <strong>Observaciones:</strong>
                        <c:out value="${pedido.observaciones}"/></div>
                </c:if>
            </div>

            <c:if test="${pedido.catering and not empty pedido.evento}">
                <div class="panel mb-4" style="border-left: 5px solid var(--ld-caramelo)">
                    <h5 class="panel-titulo mb-3"><i class="bi bi-balloon-heart" style="color: var(--ld-caramelo)"></i> Datos del evento</h5>
                    <div class="row g-3 small">
                        <div class="col-sm-4"><div class="text-muted">Fecha y hora</div><div class="fw-semibold">${pedido.evento.fechaEventoTexto}</div></div>
                        <div class="col-sm-4"><div class="text-muted">Tipo de evento</div><div class="fw-semibold"><c:out value="${pedido.evento.tipoServicio}"/></div></div>
                        <div class="col-sm-4"><div class="text-muted">Personas</div><div class="fw-semibold">${pedido.evento.cantidadPersonas}</div></div>
                        <div class="col-12"><div class="text-muted">Dirección</div><div class="fw-semibold"><c:out value="${pedido.evento.direccionEvento}"/></div></div>
                        <c:if test="${not empty pedido.evento.observaciones}">
                            <div class="col-12"><div class="text-muted">Detalles</div><c:out value="${pedido.evento.observaciones}"/></div>
                        </c:if>
                    </div>
                </div>
            </c:if>
        </div>

        <div class="col-lg-4">
            <div class="panel mb-4">
                <h5 class="panel-titulo mb-3">Cliente</h5>
                <div class="fw-semibold"><c:out value="${pedido.cliente.nombreCompleto}"/></div>
                <div class="small text-muted mb-2">${pedido.cliente.idCliente}</div>
                <div class="small"><i class="bi bi-telephone"></i> <c:out value="${pedido.cliente.telefono}"/></div>
                <a class="btn btn-whatsapp btn-sm mt-2" target="_blank" rel="noopener"
                   href="https://wa.me/51${pedido.cliente.telefono}?text=${fn:escapeXml('Hola, le escribimos de La Doñita Gourmet sobre su pedido ')}${pedido.idPedido}"><i class="bi bi-whatsapp"></i> Escribir al cliente</a>
                <c:if test="${not empty pedido.cliente.correo}">
                    <div class="small"><i class="bi bi-envelope"></i> <c:out value="${pedido.cliente.correo}"/></div>
                </c:if>
            </div>

            <div class="panel mb-4">
                <h5 class="panel-titulo mb-3">Entrega</h5>
                <div class="small mb-1"><span class="text-muted">Método:</span> ${pedido.metodoEntrega.etiqueta}</div>
                <div class="small mb-1"><span class="text-muted">Fecha estimada:</span> ${pedido.fechaEntregaTexto}</div>
                <c:if test="${pedido.delivery}">
                    <div class="small mb-3"><span class="text-muted">Dirección:</span> <c:out value="${pedido.direccionEntrega}"/></div>
                    <div class="small mb-2"><span class="text-muted">Repartidor:</span>
                        <c:choose>
                            <c:when test="${empty pedido.repartidor}"><span class="text-warning fw-semibold">Sin asignar</span></c:when>
                            <c:otherwise><c:out value="${pedido.repartidor.nombreCompleto}"/></c:otherwise>
                        </c:choose>
                    </div>
                    <c:if test="${not usuarioSesion.repartidor and pedido.estado.nombre != 'ENTREGADO'}">
                        <form method="post" action="${ctx}/admin/pedidos/repartidor" class="d-flex gap-2">
                            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                            <input type="hidden" name="id" value="${pedido.idPedido}">
                            <select name="idRepartidor" class="form-select form-select-sm" required aria-label="Repartidor">
                                <option value="">Elegir repartidor...</option>
                                <c:forEach var="r" items="${repartidores}">
                                    <option value="${r.idUsuario}" ${r.idUsuario == pedido.repartidor.idUsuario ? 'selected' : ''}>
                                        <c:out value="${r.nombreCompleto}"/></option>
                                </c:forEach>
                            </select>
                            <button class="btn btn-sm btn-ld-azul px-3">Asignar</button>
                        </form>
                    </c:if>
                </c:if>
            </div>

            <div class="panel">
                <h5 class="panel-titulo mb-3">Actualizar estado</h5>
                <c:choose>
                    <c:when test="${empty estadosPermitidos}">
                        <p class="small text-muted mb-0">
                            <c:choose>
                                <c:when test="${pedido.estado.nombre == 'ENTREGADO'}"><i class="bi bi-check2-all text-success"></i> Este pedido ya fue entregado.</c:when>
                                <c:otherwise>No hay cambios de estado disponibles para tu rol en este momento.</c:otherwise>
                            </c:choose>
                        </p>
                    </c:when>
                    <c:otherwise>
                        <div class="d-grid gap-2">
                            <c:forEach var="e" items="${estadosPermitidos}">
                                <form method="post" action="${ctx}/admin/pedidos/estado"
                                      data-confirmar="¿Cambiar el pedido ${pedido.idPedido} a &quot;${e.etiqueta}&quot;?">
                                    <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                                    <input type="hidden" name="id" value="${pedido.idPedido}">
                                    <input type="hidden" name="estado" value="${e.nombre}">
                                    <button class="btn btn-outline-${e.color} w-100"><i class="bi bi-arrow-right-circle"></i> Marcar como ${e.etiqueta}</button>
                                </form>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>

<%@ include file="../fragmentos/admin-fin.jspf" %>
