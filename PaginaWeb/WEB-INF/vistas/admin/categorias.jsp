<%@ include file="../fragmentos/init.jspf" %>
<c:set var="titulo" value="Categorías"/><c:set var="seccion" value="categorias"/>
<%@ include file="../fragmentos/admin-inicio.jspf" %>

<div class="row g-4">
    <div class="col-lg-8">
        <div class="panel p-0 overflow-hidden">
            <div class="table-responsive">
                <table class="table tabla-ld mb-0 align-middle">
                    <thead><tr><th class="ps-4">Código</th><th>Categoría</th><th class="text-center">Productos</th><th>Estado</th><th class="text-end pe-4">Acciones</th></tr></thead>
                    <tbody>
                    <c:forEach var="cat" items="${categorias}">
                        <tr class="${cat.estado ? '' : 'estado-inactivo'}">
                            <td class="ps-4 fw-semibold">${cat.idCategoria}</td>
                            <td><div class="fw-semibold"><c:out value="${cat.nombre}"/></div><small class="text-muted"><c:out value="${cat.descripcion}"/></small></td>
                            <td class="text-center"><a href="${ctx}/admin/productos?categoria=${cat.idCategoria}" class="text-decoration-none">${cat.cantidadProductos}</a></td>
                            <td><span class="badge rounded-pill ${cat.estado ? 'text-bg-success' : 'text-bg-secondary'}">${cat.estado ? 'Activa' : 'Inactiva'}</span></td>
                            <td class="text-end pe-4 text-nowrap">
                                <a href="${ctx}/admin/categorias?editar=${cat.idCategoria}" class="btn btn-sm btn-outline-ld"><i class="bi bi-pencil"></i></a>
                                <form method="post" action="${ctx}/admin/categorias/estado" class="d-inline"
                                      data-confirmar="${cat.estado ? '¿Desactivar? Sus productos dejarán de verse en el catálogo.' : '¿Activar esta categoría?'}">
                                    <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                                    <input type="hidden" name="idCategoria" value="${cat.idCategoria}">
                                    <input type="hidden" name="activar" value="${not cat.estado}">
                                    <button class="btn btn-sm ${cat.estado ? 'btn-link text-danger' : 'btn-link text-success'}">${cat.estado ? 'Desactivar' : 'Activar'}</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
    <div class="col-lg-4">
        <form method="post" action="${ctx}/admin/categorias/guardar" class="panel needs-validation" novalidate>
            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
            <input type="hidden" name="idCategoria" value="${edicion.idCategoria}">
            <h2 class="panel-titulo mb-3">${empty edicion ? 'Nueva categoría' : 'Editar '.concat(edicion.idCategoria)}</h2>
            <div class="mb-3">
                <label class="form-label" for="nombre">Nombre *</label>
                <input class="form-control" id="nombre" name="nombre" maxlength="50" required value="<c:out value='${edicion.nombre}'/>">
            </div>
            <div class="mb-3">
                <label class="form-label" for="descripcion">Descripción</label>
                <textarea class="form-control" id="descripcion" name="descripcion" rows="3" maxlength="200"><c:out value="${edicion.descripcion}"/></textarea>
            </div>
            <input type="hidden" name="estado" value="${empty edicion ? 'true' : edicion.estado}">
            <div class="d-grid gap-2">
                <button class="btn btn-ld"><i class="bi bi-check2-circle"></i> ${empty edicion ? 'Crear categoría' : 'Guardar cambios'}</button>
                <c:if test="${not empty edicion}"><a href="${ctx}/admin/categorias" class="btn btn-link text-muted">Cancelar</a></c:if>
            </div>
            <p class="small text-muted mt-3 mb-0"><i class="bi bi-info-circle"></i> Las categorías no se eliminan para no perder la relación con los productos y pedidos: se desactivan.</p>
        </form>
    </div>
</div>

<%@ include file="../fragmentos/admin-fin.jspf" %>
