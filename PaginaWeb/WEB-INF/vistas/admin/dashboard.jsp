<%@ include file="../fragmentos/init.jspf" %>
<c:set var="titulo" value="Inicio"/><c:set var="seccion" value="dashboard"/>
<%@ include file="../fragmentos/admin-inicio.jspf" %>

<div class="d-flex flex-wrap justify-content-between align-items-end gap-2 mb-4">
    <div>
        <span class="eyebrow">Resumen del negocio</span>
        <h2 class="h3 mb-0 mt-1">Hola, <c:out value="${usuarioSesion.nombres}"/> 👋</h2>
    </div>
    <div class="d-flex gap-2">
        <a href="${ctx}/admin/pedidos?estado=PENDIENTE" class="btn btn-ld btn-sm"><i class="bi bi-hourglass-split"></i> Ver pendientes</a>
        <c:if test="${usuarioSesion.admin}">
            <a href="${ctx}/admin/productos/nuevo" class="btn btn-suave btn-sm"><i class="bi bi-plus-lg"></i> Nuevo producto</a>
        </c:if>
    </div>
</div>

<!-- Indicadores -->
<div class="row g-3 mb-4">
    <div class="col-sm-6 col-xl-3">
        <div class="indicador oscuro d-flex justify-content-between">
            <div><div class="etiqueta-i">Ventas de hoy</div>
                <div class="valor">S/ <fmt:formatNumber value="${ventasHoy}" minFractionDigits="2" maxFractionDigits="2"/></div>
                <div class="small" style="color:#e2bfb2">${pedidosHoy} pedido(s)</div></div>
            <div class="icono-i"><i class="bi bi-cash-coin"></i></div>
        </div>
    </div>
    <div class="col-sm-6 col-xl-3">
        <div class="indicador d-flex justify-content-between">
            <div><div class="etiqueta-i">Ventas de ${mesActual}</div>
                <div class="valor">S/ <fmt:formatNumber value="${ventasMes}" minFractionDigits="2" maxFractionDigits="2"/></div>
                <div class="small text-muted">${pedidosMes} pedidos en el mes</div></div>
            <div class="icono-i"><i class="bi bi-graph-up-arrow"></i></div>
        </div>
    </div>
    <div class="col-sm-6 col-xl-3">
        <div class="indicador d-flex justify-content-between">
            <div><div class="etiqueta-i">Ticket promedio</div>
                <div class="valor">S/ <fmt:formatNumber value="${ticketPromedio}" minFractionDigits="2" maxFractionDigits="2"/></div>
                <div class="small text-muted">por pedido este mes</div></div>
            <div class="icono-i"><i class="bi bi-receipt-cutoff"></i></div>
        </div>
    </div>
    <div class="col-sm-6 col-xl-3">
        <a class="indicador d-flex justify-content-between text-decoration-none" href="${ctx}/admin/reportes?r=R02">
            <div><div class="etiqueta-i">Por atender</div>
                <div class="valor">${porAtender}</div>
                <div class="small text-muted">pendientes + en preparación</div></div>
            <div class="icono-i" style="background:#fef3c7;color:#92400e"><i class="bi bi-bell"></i></div>
        </a>
    </div>
</div>

<div class="row g-4">
    <div class="col-xl-8">
        <div class="panel h-100">
            <div class="d-flex justify-content-between align-items-center">
                <div><h3 class="panel-titulo mb-0">Ventas de los últimos 7 días</h3>
                    <span class="small text-muted">Monto total de pedidos registrados por día</span></div>
                <c:if test="${usuarioSesion.admin}">
                    <a href="${ctx}/admin/reportes?r=R01" class="small fw-semibold text-decoration-none">Ver reporte <i class="bi bi-arrow-right"></i></a>
                </c:if>
            </div>
            <div class="grafico" role="img" aria-label="Gráfico de ventas de los últimos 7 días">
                <c:forEach var="b" items="${barras}" varStatus="st">
                    <div class="col-barra ${st.last ? 'hoy' : ''}">
                        <span class="monto-barra"><c:if test="${b.monto > 0}">S/ <fmt:formatNumber value="${b.monto}" maxFractionDigits="0"/></c:if></span>
                        <div class="barra" style="height: ${b.porcentaje < 2 ? 2 : b.porcentaje}%" title="${b.fecha}: S/ ${b.monto}"></div>
                        <span class="dia-barra">${st.last ? 'Hoy' : b.dia}<br><small>${b.fecha}</small></span>
                    </div>
                </c:forEach>
            </div>
        </div>
    </div>
    <div class="col-xl-4">
        <div class="panel h-100">
            <h3 class="panel-titulo mb-3">Pedidos por estado</h3>
            <c:forEach var="e" items="${estados}">
                <a href="${ctx}/admin/pedidos?estado=${e.nombre}" class="d-flex justify-content-between align-items-center py-2 border-bottom text-decoration-none" style="border-color: var(--ld-borde) !important">
                    <span><span class="badge badge-estado text-bg-${e.color}">${e.etiqueta}</span></span>
                    <span class="serif fs-4 fw-bold" style="color: var(--ld-espresso)">${conteo[e]}</span>
                </a>
            </c:forEach>
        </div>
    </div>

    <div class="col-lg-6">
        <div class="panel h-100">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <h3 class="panel-titulo mb-0"><i class="bi bi-balloon-heart" style="color: var(--ld-caramelo)"></i> Próximos eventos</h3>
                <a href="${ctx}/admin/reportes?r=R06" class="small fw-semibold text-decoration-none">Ver todos</a>
            </div>
            <c:choose>
                <c:when test="${empty eventos}"><p class="text-muted small mb-0 py-3">No hay eventos de catering programados.</p></c:when>
                <c:otherwise>
                    <ul class="lista-simple">
                        <c:forEach var="ev" items="${eventos}">
                            <li>
                                <div class="fecha-cuadro"><div class="d">${ev.evento.diaEvento}</div>
                                    <div class="m">${ev.evento.mesCorto}</div></div>
                                <div class="flex-grow-1 small">
                                    <a href="${ctx}/admin/pedidos/detalle?id=${ev.idPedido}" class="fw-semibold text-decoration-none" style="color: var(--ld-espresso)">
                                        <c:out value="${ev.evento.tipoServicio}"/> · ${ev.evento.cantidadPersonas} personas</a>
                                    <div class="text-muted"><c:out value="${ev.cliente.nombreCompleto}"/> · ${ev.idPedido}
                                        <c:if test="${not empty ev.evento.horaEvento}"> · ${ev.evento.horaEvento}</c:if></div>
                                </div>
                                <span class="badge badge-estado text-bg-${ev.estado.color}">${ev.estado.etiqueta}</span>
                            </li>
                        </c:forEach>
                    </ul>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
    <div class="col-lg-6">
        <div class="panel h-100">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <h3 class="panel-titulo mb-0"><i class="bi bi-exclamation-triangle" style="color: #b45309"></i> Stock bajo</h3>
                <a href="${ctx}/admin/inventario?bajo=1" class="small fw-semibold text-decoration-none">Reponer</a>
            </div>
            <c:choose>
                <c:when test="${empty stockBajo}"><p class="text-muted small mb-0 py-3"><i class="bi bi-check-circle text-success"></i> Todo el catálogo tiene stock suficiente.</p></c:when>
                <c:otherwise>
                    <ul class="lista-simple">
                        <c:forEach var="p" items="${stockBajo}">
                            <li>
                                <img class="miniatura-tabla" src="${p.fotoExterna ? '' : ctx.concat('/')}${p.fotoRuta}" alt=""
                                     onerror="this.onerror=null;this.src='${ctx}/recursos/img/categorias/${p.categoria.idCategoria}.jpg'">
                                <div class="flex-grow-1 small"><div class="fw-semibold"><c:out value="${p.nombre}"/></div>
                                    <div class="text-muted"><c:out value="${p.categoria.nombre}"/></div></div>
                                <span class="badge rounded-pill ${p.stock == 0 ? 'nivel-agotado' : 'nivel-bajo'}"><c:choose><c:when test="${p.stock == 0}">Agotado</c:when><c:otherwise>${p.stock} und.</c:otherwise></c:choose></span>
                            </li>
                        </c:forEach>
                    </ul>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<%@ include file="../fragmentos/admin-fin.jspf" %>
