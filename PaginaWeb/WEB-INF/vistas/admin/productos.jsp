<%@ include file="../fragmentos/init.jspf" %>
<c:set var="titulo" value="Productos"/><c:set var="seccion" value="productos"/>
<%@ include file="../fragmentos/admin-inicio.jspf" %>

<div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
    <p class="text-muted mb-0">${productos.size()} producto(s). Los productos con pedidos no se borran: se desactivan para conservar el historial.</p>
    <a href="${ctx}/admin/productos/nuevo" class="btn btn-ld"><i class="bi bi-plus-lg"></i> Nuevo producto</a>
</div>

<form class="panel panel-suave py-3 mb-3 row g-2 align-items-center" method="get" action="${ctx}/admin/productos">
    <div class="col-md-5">
        <div class="buscador-ld"><i class="bi bi-search"></i>
            <input class="form-control" type="search" name="q" maxlength="60" placeholder="Buscar por nombre, descripción o código" value="<c:out value='${q}'/>"></div>
    </div>
    <div class="col-6 col-md-3">
        <select name="categoria" class="form-select" aria-label="Categoría">
            <option value="">Todas las categorías</option>
            <c:forEach var="cat" items="${categorias}">
                <option value="${cat.idCategoria}" ${cat.idCategoria == categoriaActual ? 'selected' : ''}><c:out value="${cat.nombre}"/></option>
            </c:forEach>
        </select>
    </div>
    <div class="col-6 col-md-2">
        <select name="estado" class="form-select" aria-label="Estado">
            <option value="">Todos</option>
            <option value="activos" ${estadoActual == 'activos' ? 'selected' : ''}>Activos</option>
            <option value="inactivos" ${estadoActual == 'inactivos' ? 'selected' : ''}>Inactivos</option>
        </select>
    </div>
    <div class="col-md-2 d-grid"><button class="btn btn-ld-azul">Filtrar</button></div>
</form>

<div class="panel p-0 overflow-hidden">
    <c:choose>
        <c:when test="${empty productos}">
            <div class="estado-vacio"><i class="bi bi-basket"></i><h5 class="mt-3">No hay productos con esos filtros</h5></div>
        </c:when>
        <c:otherwise>
            <div class="table-responsive">
                <table class="table tabla-ld mb-0 align-middle">
                    <thead><tr><th class="ps-4">Producto</th><th>Categoría</th><th class="text-end">Precio</th><th class="text-center">Stock</th><th>Estado</th><th class="text-end pe-4">Acciones</th></tr></thead>
                    <tbody>
                    <c:forEach var="p" items="${productos}">
                        <tr class="${p.estado ? '' : 'estado-inactivo'}">
                            <td class="ps-4">
                                <div class="d-flex align-items-center gap-3">
                                    <img class="miniatura-tabla" src="${p.fotoExterna ? '' : ctx.concat('/')}${p.fotoRuta}" alt=""
                                         onerror="this.onerror=null;this.src='${ctx}/recursos/img/categorias/${p.categoria.idCategoria}.jpg'">
                                    <div><div class="fw-semibold"><c:out value="${p.nombre}"/></div><small class="text-muted">${p.idProducto}</small></div>
                                </div>
                            </td>
                            <td><c:out value="${p.categoria.nombre}"/></td>
                            <td class="text-end fw-semibold">S/ <fmt:formatNumber value="${p.precio}" minFractionDigits="2" maxFractionDigits="2"/></td>
                            <td class="text-center"><span class="badge rounded-pill ${p.stock == 0 ? 'nivel-agotado' : p.stockBajo ? 'nivel-bajo' : 'nivel-disponible'}">${p.stock}</span></td>
                            <td><span class="badge rounded-pill ${p.estado ? 'text-bg-success' : 'text-bg-secondary'}">${p.estado ? 'Activo' : 'Inactivo'}</span></td>
                            <td class="text-end pe-4 text-nowrap">
                                <a href="${ctx}/admin/productos/editar?id=${p.idProducto}" class="btn btn-sm btn-outline-ld"><i class="bi bi-pencil"></i> Editar</a>
                                <form method="post" action="${ctx}/admin/productos/eliminar" class="d-inline"
                                      data-confirmar="¿Eliminar ${p.idProducto}? Si tiene pedidos, solo se desactivará.">
                                    <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                                    <input type="hidden" name="id" value="${p.idProducto}">
                                    <button class="btn btn-sm btn-link text-danger" title="Eliminar"><i class="bi bi-trash3"></i></button>
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
