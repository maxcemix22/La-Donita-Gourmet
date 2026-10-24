<%@ include file="../fragmentos/init.jspf" %>
<c:set var="titulo" value="Clientes"/><c:set var="seccion" value="clientes"/>
<%@ include file="../fragmentos/admin-inicio.jspf" %>

<form class="panel panel-suave py-3 mb-3 row g-2 align-items-center" method="get" action="${ctx}/admin/clientes">
    <div class="col-md-9">
        <div class="buscador-ld"><i class="bi bi-search"></i>
            <input class="form-control" type="search" name="q" maxlength="60" placeholder="Buscar por nombre, teléfono o correo" value="<c:out value='${q}'/>"></div>
    </div>
    <div class="col-md-3 d-grid"><button class="btn btn-ld-azul">Buscar</button></div>
</form>

<div class="panel p-0 overflow-hidden">
    <c:choose>
        <c:when test="${empty clientes}">
            <div class="estado-vacio"><i class="bi bi-people"></i><h5 class="mt-3">No se encontraron clientes</h5>
                <p class="mb-0">Los clientes se registran solos al hacer su primer pedido en la web.</p></div>
        </c:when>
        <c:otherwise>
            <div class="table-responsive">
                <table class="table table-hover tabla-ld mb-0 align-middle">
                    <thead><tr><th class="ps-4">Cliente</th><th>Contacto</th><th class="text-center">Pedidos</th><th class="text-end">Monto total</th><th>Último pedido</th><th class="pe-4"></th></tr></thead>
                    <tbody>
                    <c:forEach var="c" items="${clientes}">
                        <tr>
                            <td class="ps-4"><div class="fw-semibold"><c:out value="${c.nombreCompleto}"/></div><small class="text-muted">${c.idCliente} · desde ${c.fechaRegistroTexto}</small></td>
                            <td class="small"><i class="bi bi-telephone"></i> <c:out value="${c.telefono}"/>
                                <c:if test="${not empty c.correo}"><br><i class="bi bi-envelope"></i> <c:out value="${c.correo}"/></c:if></td>
                            <td class="text-center">${c.cantidadPedidos}</td>
                            <td class="text-end fw-semibold">S/ <fmt:formatNumber value="${c.montoTotal}" minFractionDigits="2" maxFractionDigits="2"/></td>
                            <td class="small">${c.ultimoPedidoTexto}</td>
                            <td class="pe-4 text-end"><a href="${ctx}/admin/clientes/detalle?id=${c.idCliente}" class="btn btn-sm btn-outline-ld">Ver ficha</a></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="../fragmentos/admin-fin.jspf" %>
