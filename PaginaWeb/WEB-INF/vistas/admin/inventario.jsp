<%@ include file="../fragmentos/init.jspf" %>
<c:set var="titulo" value="Inventario"/><c:set var="seccion" value="inventario"/>
<%@ include file="../fragmentos/admin-inicio.jspf" %>

<div class="row g-3 mb-3">
    <div class="col-md-8">
        <div class="pestanas-reporte">
            <a href="${ctx}/admin/inventario" class="${not soloBajo and empty categoriaActual ? 'activa' : ''}">Todos</a>
            <a href="${ctx}/admin/inventario?bajo=1" class="${soloBajo ? 'activa' : ''}"><i class="bi bi-exclamation-triangle"></i> Stock bajo (${totalBajo})</a>
            <c:forEach var="cat" items="${categorias}">
                <a href="${ctx}/admin/inventario?categoria=${cat.idCategoria}" class="${cat.idCategoria == categoriaActual ? 'activa' : ''}"><c:out value="${cat.nombre}"/></a>
            </c:forEach>
        </div>
    </div>
    <div class="col-md-4 text-md-end small text-muted align-self-center">
        Stock bajo: ${stockMinimo} unidades o menos · <a href="${ctx}/admin/reportes?r=R04">Reporte R04</a>
    </div>
</div>

<div class="panel p-0 overflow-hidden">
    <c:choose>
        <c:when test="${empty productos}">
            <div class="estado-vacio"><i class="bi bi-check2-circle"></i><h5 class="mt-3">No hay productos en esta vista</h5></div>
        </c:when>
        <c:otherwise>
            <div class="table-responsive">
                <table class="table tabla-ld mb-0 align-middle">
                    <thead><tr><th class="ps-4">Producto</th><th>Categoría</th><th class="text-center">Stock actual</th><th>Nivel</th><th class="pe-4">Ajustar stock</th></tr></thead>
                    <tbody>
                    <c:forEach var="p" items="${productos}">
                        <tr>
                            <td class="ps-4"><div class="fw-semibold"><c:out value="${p.nombre}"/></div><small class="text-muted">${p.idProducto}</small></td>
                            <td><c:out value="${p.categoria.nombre}"/></td>
                            <td class="text-center serif fs-5 fw-bold" style="color: var(--ld-espresso)">${p.stock}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${p.stock == 0}"><span class="badge rounded-pill nivel-agotado">Agotado</span></c:when>
                                    <c:when test="${p.stockBajo}"><span class="badge rounded-pill nivel-bajo">Bajo</span></c:when>
                                    <c:otherwise><span class="badge rounded-pill nivel-disponible">Disponible</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td class="pe-4">
                                <form method="post" action="${ctx}/admin/inventario/ajustar" class="d-flex flex-wrap gap-1 stock-ajuste">
                                    <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                                    <input type="hidden" name="idProducto" value="${p.idProducto}">
                                    <input type="hidden" name="volver" value="${soloBajo ? 'bajo' : ''}">
                                    <select name="operacion" class="form-select form-select-sm" style="max-width: 150px" aria-label="Operación">
                                        <option value="sumar">+ Producción</option>
                                        <option value="restar">− Merma</option>
                                        <option value="fijar">= Conteo físico</option>
                                    </select>
                                    <input type="number" name="cantidad" min="0" step="1" required class="form-control form-control-sm" placeholder="Cant." aria-label="Cantidad">
                                    <button class="btn btn-sm btn-ld">Aplicar</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="../fragmentos/admin-fin.jspf" %>
