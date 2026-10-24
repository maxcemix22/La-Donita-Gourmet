<%@ include file="../fragmentos/init.jspf" %>
<c:set var="titulo" value="${usuarioSesion.repartidor ? 'Mis entregas' : 'Pedidos'}"/><c:set var="seccion" value="pedidos"/>
<%@ include file="../fragmentos/admin-inicio.jspf" %>

<div>
    <p class="text-muted mb-4">
        <c:choose>
            <c:when test="${usuarioSesion.repartidor}">Pedidos asignados a ti para reparto. Márcalos como <strong>En camino</strong> al salir y <strong>Entregado</strong> al llegar.</c:when>
            <c:otherwise>Gestiona los pedidos y reservas de catering de los clientes. Haz clic en un indicador para filtrar.</c:otherwise>
        </c:choose>
    </p>

    <!-- Indicadores por estado (también sirven de filtro) -->
    <div class="row g-3 mb-4">
        <div class="col-6 col-md">
            <a href="${ctx}/admin/pedidos" class="kpi ${empty filtro ? 'activo' : ''}">
                <div class="d-flex justify-content-between align-items-center">
                    <div><div class="small text-muted">Todos</div><div class="numero">${totalPedidos}</div></div>
                    <div class="icono" style="background: var(--ld-melocoton); color: var(--ld-cacao)"><i class="bi bi-receipt"></i></div>
                </div>
            </a>
        </div>
        <c:forEach var="e" items="${estados}">
            <div class="col-6 col-md">
                <a href="${ctx}/admin/pedidos?estado=${e.nombre}" class="kpi ${filtro == e ? 'activo' : ''}">
                    <div class="d-flex justify-content-between align-items-center">
                        <div><div class="small text-muted">${e.etiqueta}</div><div class="numero">${conteo[e]}</div></div>
                        <div class="icono bg-${e.color}-subtle text-${e.color}-emphasis">
                            <c:choose>
                                <c:when test="${e.nombre == 'PENDIENTE'}"><i class="bi bi-hourglass-split"></i></c:when>
                                <c:when test="${e.nombre == 'EN_PREPARACION'}"><i class="bi bi-fire"></i></c:when>
                                <c:when test="${e.nombre == 'EN_CAMINO'}"><i class="bi bi-scooter"></i></c:when>
                                <c:otherwise><i class="bi bi-check2-all"></i></c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </a>
            </div>
        </c:forEach>
    </div>

    <div class="panel p-0 overflow-hidden">
        <c:choose>
            <c:when test="${empty pedidos}">
                <div class="estado-vacio">
                    <i class="bi bi-inbox"></i>
                    <h5 class="mt-3">No hay pedidos ${empty filtro ? '' : 'en este estado'}</h5>
                </div>
            </c:when>
            <c:otherwise>
                <div class="table-responsive">
                    <table class="table table-hover tabla-ld mb-0">
                        <thead>
                        <tr>
                            <th class="ps-4">Código</th>
                            <th>Fecha</th>
                            <th>Cliente</th>
                            <th>Tipo</th>
                            <th>Entrega</th>
                            <th class="text-end">Total</th>
                            <th>Estado</th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="p" items="${pedidos}">
                            <tr>
                                <td class="ps-4 fw-semibold">${p.idPedido}</td>
                                <td class="small">${p.fechaTexto}</td>
                                <td>
                                    <div><c:out value="${p.cliente.nombreCompleto}"/></div>
                                    <small class="text-muted"><i class="bi bi-telephone"></i> <c:out value="${p.cliente.telefono}"/></small>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${p.catering}"><span class="badge rounded-pill" style="background: var(--ld-caramelo)"><i class="bi bi-balloon-heart"></i> Catering</span></c:when>
                                        <c:otherwise><span class="badge rounded-pill text-bg-light border">Normal</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="small">
                                    ${p.metodoEntrega.etiqueta}
                                    <c:if test="${p.delivery}">
                                        <div class="text-muted">
                                            <c:choose>
                                                <c:when test="${empty p.repartidor}"><i class="bi bi-exclamation-circle text-warning"></i> Sin repartidor</c:when>
                                                <c:otherwise><i class="bi bi-person"></i> <c:out value="${p.repartidor.nombres}"/></c:otherwise>
                                            </c:choose>
                                        </div>
                                    </c:if>
                                </td>
                                <td class="text-end fw-semibold">S/ <fmt:formatNumber value="${p.total}" minFractionDigits="2" maxFractionDigits="2"/></td>
                                <td><span class="badge badge-estado text-bg-${p.estado.color}">${p.estado.etiqueta}</span></td>
                                <td class="pe-3 text-end">
                                    <a href="${ctx}/admin/pedidos/detalle?id=${p.idPedido}" class="btn btn-sm btn-outline-ld">Ver</a>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<%@ include file="../fragmentos/admin-fin.jspf" %>
