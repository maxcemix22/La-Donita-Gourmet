<%@ include file="../fragmentos/init.jspf" %>
<c:set var="titulo" value="Mi perfil"/><c:set var="seccion" value="perfil"/>
<%@ include file="../fragmentos/admin-inicio.jspf" %>

<div class="row g-4">
    <div class="col-lg-5">
        <div class="panel">
            <div class="d-flex align-items-center gap-3 mb-3">
                <span class="btn-circulo fs-4" style="width:64px;height:64px"><c:out value="${perfil.nombres.substring(0, 1)}"/></span>
                <div><h2 class="panel-titulo mb-0"><c:out value="${perfil.nombreCompleto}"/></h2>
                    <span class="badge rounded-pill text-bg-dark">${perfil.rol.etiqueta}</span></div>
            </div>
            <dl class="row small mb-0">
                <dt class="col-5 text-muted">Usuario</dt><dd class="col-7"><code><c:out value="${perfil.usuario}"/></code></dd>
                <dt class="col-5 text-muted">Código</dt><dd class="col-7">${perfil.idUsuario}</dd>
                <dt class="col-5 text-muted">DNI</dt><dd class="col-7">${perfil.dni}</dd>
                <dt class="col-5 text-muted">Teléfono</dt><dd class="col-7"><c:out value="${perfil.telefono}"/></dd>
                <dt class="col-5 text-muted">Correo</dt><dd class="col-7"><c:out value="${empty perfil.correo ? '—' : perfil.correo}"/></dd>
                <c:if test="${perfil.tipoClase == 'Empleado'}">
                    <dt class="col-5 text-muted">Área / turno</dt><dd class="col-7"><c:out value="${perfil.areaAsignada}"/> · ${perfil.turno}</dd>
                </c:if>
                <c:if test="${perfil.tipoClase == 'Repartidor'}">
                    <dt class="col-5 text-muted">Vehículo</dt><dd class="col-7">${perfil.tipoVehiculo} <c:out value="${perfil.placaVehiculo}"/></dd>
                    <dt class="col-5 text-muted">Zona</dt><dd class="col-7"><c:out value="${perfil.zonaReparto}"/></dd>
                </c:if>
            </dl>
            <p class="small text-muted mt-3 mb-0">Para cambiar tus datos personales, pídeselo a un administrador.</p>
        </div>
    </div>
    <div class="col-lg-7">
        <form method="post" action="${ctx}/admin/perfil" class="panel needs-validation" novalidate autocomplete="off">
            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
            <h2 class="panel-titulo mb-3"><i class="bi bi-shield-lock"></i> Cambiar contraseña</h2>
            <div class="mb-3"><label class="form-label" for="actual">Contraseña actual</label>
                <input class="form-control" id="actual" name="actual" type="password" required autocomplete="current-password"></div>
            <div class="row g-3 mb-3">
                <div class="col-md-6"><label class="form-label" for="nueva">Nueva contraseña</label>
                    <input class="form-control" id="nueva" name="nueva" type="password" minlength="8" maxlength="100" required autocomplete="new-password"></div>
                <div class="col-md-6"><label class="form-label" for="confirmar">Confirmar nueva contraseña</label>
                    <input class="form-control" id="confirmar" name="confirmar" type="password" minlength="8" maxlength="100" required autocomplete="new-password"></div>
            </div>
            <p class="small text-muted">Mínimo 8 caracteres, combinando letras y números. Se guarda cifrada con PBKDF2 + sal aleatoria.</p>
            <button class="btn btn-ld">Actualizar contraseña</button>
        </form>
    </div>
</div>

<%@ include file="../fragmentos/admin-fin.jspf" %>
