<%@ include file="../fragmentos/init.jspf" %>
<c:set var="esNuevo" value="${empty producto.idProducto}"/>
<c:set var="titulo" value="${esNuevo ? 'Nuevo producto' : 'Editar producto'}"/><c:set var="seccion" value="productos"/>
<%@ include file="../fragmentos/admin-inicio.jspf" %>

<a href="${ctx}/admin/productos" class="small text-muted text-decoration-none"><i class="bi bi-arrow-left"></i> Volver a productos</a>

<c:if test="${not empty errores}">
    <div class="alert alert-danger mt-3"><strong>Revisa los datos:</strong>
        <ul class="mb-0"><c:forEach var="e" items="${errores}"><li><c:out value="${e}"/></li></c:forEach></ul></div>
</c:if>

<%-- El token CSRF va también en la URL porque el formulario es multipart (sube la foto). --%>
<form method="post" enctype="multipart/form-data" action="${ctx}/admin/productos/guardar?_csrf=${sessionScope.csrfToken}"
      class="row g-4 mt-1 needs-validation" novalidate>
    <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
    <input type="hidden" name="idProducto" value="${producto.idProducto}">
    <div class="col-lg-8">
        <div class="panel">
            <h2 class="panel-titulo mb-3">Datos del producto <c:if test="${not esNuevo}"><small class="text-muted fs-6">${producto.idProducto}</small></c:if></h2>
            <div class="row g-3">
                <div class="col-md-8">
                    <label class="form-label" for="nombre">Nombre *</label>
                    <input class="form-control" id="nombre" name="nombre" maxlength="100" required value="<c:out value='${producto.nombre}'/>">
                    <div class="invalid-feedback">El nombre es obligatorio.</div>
                </div>
                <div class="col-md-4">
                    <label class="form-label" for="idCategoria">Categoría *</label>
                    <select class="form-select" id="idCategoria" name="idCategoria" required>
                        <option value="">Elegir...</option>
                        <c:forEach var="cat" items="${categorias}">
                            <option value="${cat.idCategoria}" ${cat.idCategoria == producto.categoria.idCategoria ? 'selected' : ''}>
                                <c:out value="${cat.nombre}"/>${cat.estado ? '' : ' (inactiva)'}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-12">
                    <label class="form-label" for="descripcion">Descripción</label>
                    <textarea class="form-control" id="descripcion" name="descripcion" rows="3" maxlength="300"
                              placeholder="Lo que verá el cliente en el catálogo"><c:out value="${producto.descripcion}"/></textarea>
                </div>
                <div class="col-sm-4">
                    <label class="form-label" for="precio">Precio (S/) *</label>
                    <input class="form-control" id="precio" name="precio" type="number" min="0" step="0.01" required
                           value="<fmt:formatNumber value='${producto.precio}' minFractionDigits='2' maxFractionDigits='2' groupingUsed='false'/>">
                </div>
                <div class="col-sm-4">
                    <label class="form-label" for="stock">Stock disponible *</label>
                    <input class="form-control" id="stock" name="stock" type="number" min="0" step="1" required value="${producto.stock}">
                </div>
                <div class="col-sm-4 d-flex align-items-end">
                    <div class="form-check form-switch fs-6 mb-2">
                        <input class="form-check-input" type="checkbox" role="switch" id="estado" name="estado" ${producto.estado ? 'checked' : ''}>
                        <label class="form-check-label" for="estado">Visible en el catálogo</label>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="col-lg-4">
        <div class="panel">
            <h2 class="panel-titulo mb-3">Foto</h2>
            <img id="fotoPreview" class="foto-preview mb-3" alt="Vista previa"
                 src="${producto.fotoExterna ? '' : ctx.concat('/')}${producto.fotoRuta}"
                 onerror="this.onerror=null;this.src='${ctx}/recursos/img/logo.png'">
            <label class="form-label" for="foto">Subir imagen (JPG, PNG o WEBP, máx. 2 MB)</label>
            <input class="form-control mb-3" type="file" id="foto" name="foto" accept="image/jpeg,image/png,image/webp">
            <label class="form-label" for="fotoUrl">…o pegar una URL https</label>
            <input class="form-control mb-2" id="fotoUrl" name="fotoUrl" maxlength="255" placeholder="https://..."
                   value="${producto.fotoExterna ? producto.fotoUrl : ''}">
            <c:if test="${not empty producto.fotoUrl}">
                <div class="form-check small">
                    <input class="form-check-input" type="checkbox" id="accionFoto" name="accionFoto" value="quitar">
                    <label class="form-check-label" for="accionFoto">Quitar la foto actual</label>
                </div>
            </c:if>
            <p class="small text-muted mt-2 mb-0">Sin foto propia se muestra la imagen predeterminada del producto o de su categoría.</p>
        </div>
        <div class="d-grid gap-2 mt-3">
            <button class="btn btn-ld py-2"><i class="bi bi-check2-circle"></i> ${esNuevo ? 'Registrar producto' : 'Guardar cambios'}</button>
            <a href="${ctx}/admin/productos" class="btn btn-link text-muted">Cancelar</a>
        </div>
    </div>
</form>

<%@ include file="../fragmentos/admin-fin.jspf" %>
