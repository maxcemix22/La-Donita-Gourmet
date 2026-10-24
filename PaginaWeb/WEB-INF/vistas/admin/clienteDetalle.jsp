<%@ include file="../fragmentos/init.jspf" %>
<c:set var="titulo" value="Ficha del cliente"/><c:set var="seccion" value="clientes"/>
<%@ include file="../fragmentos/admin-inicio.jspf" %>

<a href="${ctx}/admin/clientes" class="small text-muted text-decoration-none"><i class="bi bi-arrow-left"></i> Volver a clientes</a>

<div class="row g-4 mt-1">
    <div class="col-lg-4">
        <form method="post" action="${ctx}/admin/clientes/guardar" class="panel needs-validation" novalidate>
            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
            <input type="hidden" name="idCliente" value="${cliente.idCliente}">
            <h2 class="panel-titulo mb-1"><c:out value="${cliente.nombreCompleto}"/></h2>
            <p class="small text-muted">${cliente.idCliente} · registrado el ${cliente.fechaRegistroTexto}</p>
            <div class="mb-2"><label class="form-label" for="nombres">Nombres</label>
                <input class="form-control" id="nombres" name="nombres" maxlength="100" required value="<c:out value='${cliente.nombres}'/>"></div>
            <div class="mb-2"><label class="form-label" for="apellidos">Apellidos</label>
                <input class="form-control" id="apellidos" name="apellidos" maxlength="100" required value="<c:out value='${cliente.apellidos}'/>"></div>
            <div class="mb-2"><label class="form-label" for="telefono">Teléfono</label>
                <input class="form-control" id="telefono" name="telefono" pattern="\d{7,15}" maxlength="15" required value="<c:out value='${cliente.telefono}'/>"></div>
            <div class="mb-2"><label class="form-label" for="correo">Correo</label>
                <input class="form-control" id="correo" name="correo" type="email" maxlength="100" value="<c:out value='${cliente.correo}'/>"></div>
            <div class="mb-3"><label class="form-label" for="direccion">Dirección de referencia</label>
                <input class="form-control" id="direccion" name="direccion" maxlength="200" value="<c:out value='${cliente.direccionReferencia}'/>"></div>
            <div class="d-grid gap-2">
                <button class="btn btn-ld"><i class="bi bi-check2-circle"></i> Guardar cambios</button>
                <a class="btn btn-whatsapp" target="_blank" rel="noopener" href="https://wa.me/51${cliente.telefono}"><i class="bi bi-whatsapp"></i> Escribir por WhatsApp</a>
            </div>
        </form>
    </div>
    <div class="col-lg-8">
        <c:set var="totalCliente" value="${0}"/>
        <c:forEach var="p" items="${pedidos}"><c:set var="totalCliente" value="${totalCliente + p.total}"/></c:forEach>
        <div class="row g-3 mb-3">
            <div class="col-6"><div class="indicador"><div class="etiqueta-i">Pedidos</div><div class="valor">${pedidos.size()}</div></div></div>
            <div class="col-6"><div class="indicador oscuro"><div class="etiqueta-i">Total comprado</div>
                <div class="valor">S/ <fmt:formatNumber value="${totalCliente}" minFractionDigits="2" maxFractionDigits="2"/></div></div></div>
        </div>
        <div class="panel p-0 overflow-hidden">
            <h3 class="panel-titulo p-3 pb-0 mb-2">Historial de pedidos</h3>
            <c:choose>
                <c:when test="${empty pedidos}"><p class="text-muted p-3 mb-0">Este cliente aún no tiene pedidos.</p></c:when>
                <c:otherwise>
                    <div class="table-responsive">
                        <table class="table tabla-ld mb-0 align-middle">
                            <thead><tr><th class="ps-3">Código</th><th>Fecha</th><th>Tipo</th><th class="text-end">Total</th><th>Estado</th><th></th></tr></thead>
                            <tbody>
                            <c:forEach var="p" items="${pedidos}">
                                <tr>
                                    <td class="ps-3 fw-semibold">${p.idPedido}</td>
                                    <td class="small">${p.fechaTexto}</td>
                                    <td>${p.tipo.etiqueta}</td>
                                    <td class="text-end">S/ <fmt:formatNumber value="${p.total}" minFractionDigits="2" maxFractionDigits="2"/></td>
                                    <td><span class="badge badge-estado text-bg-${p.estado.color}">${p.estado.etiqueta}</span></td>
                                    <td class="pe-3 text-end"><a href="${ctx}/admin/pedidos/detalle?id=${p.idPedido}" class="btn btn-sm btn-outline-ld">Ver</a></td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<%@ include file="../fragmentos/admin-fin.jspf" %>
