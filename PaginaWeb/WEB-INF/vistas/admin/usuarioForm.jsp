<%@ include file="../fragmentos/init.jspf" %>
<c:set var="esNuevo" value="${empty usuario.idUsuario}"/>
<c:set var="titulo" value="${esNuevo ? 'Nuevo usuario' : 'Editar usuario'}"/><c:set var="seccion" value="usuarios"/>
<%@ include file="../fragmentos/admin-inicio.jspf" %>
<c:set var="rolActual" value="${usuario.rol.name()}"/>

<a href="${ctx}/admin/usuarios" class="small text-muted text-decoration-none"><i class="bi bi-arrow-left"></i> Volver a usuarios</a>

<c:if test="${not empty errores}">
    <div class="alert alert-danger mt-3"><strong>Revisa los datos:</strong>
        <ul class="mb-0"><c:forEach var="e" items="${errores}"><li><c:out value="${e}"/></li></c:forEach></ul></div>
</c:if>

<div class="row g-4 mt-1">
    <div class="col-lg-8">
        <form method="post" action="${ctx}/admin/usuarios/guardar" class="panel needs-validation" novalidate autocomplete="off">
            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
            <input type="hidden" name="idUsuario" value="${usuario.idUsuario}">
            <h2 class="panel-titulo mb-3">Datos personales <c:if test="${not esNuevo}"><small class="text-muted fs-6">${usuario.idUsuario}</small></c:if></h2>
            <div class="row g-3">
                <div class="col-md-6"><label class="form-label" for="nombres">Nombres *</label>
                    <input class="form-control" id="nombres" name="nombres" maxlength="100" required value="<c:out value='${usuario.nombres}'/>"></div>
                <div class="col-md-6"><label class="form-label" for="apellidos">Apellidos *</label>
                    <input class="form-control" id="apellidos" name="apellidos" maxlength="100" required value="<c:out value='${usuario.apellidos}'/>"></div>
                <div class="col-md-4"><label class="form-label" for="dni">DNI *</label>
                    <input class="form-control" id="dni" name="dni" pattern="\d{8}" maxlength="8" inputmode="numeric" required value="<c:out value='${usuario.dni}'/>"></div>
                <div class="col-md-4"><label class="form-label" for="telefono">Teléfono *</label>
                    <input class="form-control" id="telefono" name="telefono" pattern="\d{7,15}" maxlength="15" required value="<c:out value='${usuario.telefono}'/>"></div>
                <div class="col-md-4"><label class="form-label" for="correo">Correo</label>
                    <input class="form-control" id="correo" name="correo" type="email" maxlength="100" value="<c:out value='${usuario.correo}'/>"></div>
            </div>

            <h2 class="panel-titulo mt-4 mb-3">Acceso al sistema</h2>
            <div class="row g-3">
                <div class="col-md-4"><label class="form-label" for="usuarioLogin">Usuario *</label>
                    <input class="form-control" id="usuarioLogin" name="usuarioLogin" pattern="[A-Za-z0-9_.]{4,50}" maxlength="50" required
                           value="<c:out value='${usuario.usuario}'/>"></div>
                <div class="col-md-4"><label class="form-label" for="rol">Rol *</label>
                    <select class="form-select" id="rol" name="rol">
                        <c:forEach var="r" items="${roles}">
                            <option value="${r.name()}" ${r.name() == rolActual ? 'selected' : ''}>${r.etiqueta}</option>
                        </c:forEach>
                    </select></div>
                <c:if test="${esNuevo}">
                    <div class="col-md-4"><label class="form-label" for="clave">Contraseña inicial *</label>
                        <input class="form-control" id="clave" name="clave" type="password" minlength="8" maxlength="100" required autocomplete="new-password">
                        <div class="form-text">Mínimo 8 caracteres, con letras y números.</div></div>
                </c:if>
            </div>

            <div data-rol="EMPLEADO" class="mt-4">
                <h2 class="panel-titulo mb-3">Datos laborales del empleado</h2>
                <div class="row g-3">
                    <div class="col-md-3"><label class="form-label" for="fechaIngresoE">Fecha de ingreso *</label>
                        <input class="form-control" id="fechaIngresoE" name="fechaIngresoE" type="date" max="${hoy}" data-requerido
                               value="${rolActual == 'EMPLEADO' ? usuario.fechaIngresoLaboral : ''}"></div>
                    <div class="col-md-3"><label class="form-label" for="turno">Turno *</label>
                        <select class="form-select" id="turno" name="turno">
                            <option value="mañana" ${usuario.tipoClase == 'Empleado' and usuario.turno == 'tarde' ? '' : 'selected'}>Mañana</option>
                            <option value="tarde" ${usuario.tipoClase == 'Empleado' and usuario.turno == 'tarde' ? 'selected' : ''}>Tarde</option>
                        </select></div>
                    <div class="col-md-3"><label class="form-label" for="area">Área asignada *</label>
                        <input class="form-control" id="area" name="area" maxlength="50" list="areas" data-requerido
                               value="<c:out value='${usuario.tipoClase == "Empleado" ? usuario.areaAsignada : ""}'/>">
                        <datalist id="areas"><option value="cocina"><option value="atención al cliente"><option value="empaque"><option value="caja"></datalist></div>
                    <div class="col-md-3"><label class="form-label" for="sueldo">Sueldo (S/) *</label>
                        <input class="form-control" id="sueldo" name="sueldo" type="number" min="0" step="0.01" data-requerido
                               value="${usuario.tipoClase == 'Empleado' ? usuario.sueldo : ''}"></div>
                </div>
            </div>

            <div data-rol="REPARTIDOR" class="mt-4">
                <h2 class="panel-titulo mb-3">Datos del repartidor</h2>
                <div class="row g-3">
                    <div class="col-md-4"><label class="form-label" for="fechaIngresoR">Fecha de ingreso *</label>
                        <input class="form-control" id="fechaIngresoR" name="fechaIngresoR" type="date" max="${hoy}" data-requerido
                               value="${rolActual == 'REPARTIDOR' ? usuario.fechaIngresoLaboral : ''}"></div>
                    <div class="col-md-4"><label class="form-label" for="tipoVehiculo">Vehículo *</label>
                        <select class="form-select" id="tipoVehiculo" name="tipoVehiculo">
                            <c:forEach var="t" items="${['moto','auto','bicicleta']}">
                                <option value="${t}" ${usuario.tipoClase == 'Repartidor' and usuario.tipoVehiculo == t ? 'selected' : ''}>${t}</option>
                            </c:forEach>
                        </select></div>
                    <div class="col-md-4"><label class="form-label" for="placa">Placa *</label>
                        <input class="form-control" id="placa" name="placa" maxlength="10" data-requerido
                               value="<c:out value='${usuario.tipoClase == "Repartidor" ? usuario.placaVehiculo : ""}'/>"></div>
                    <div class="col-md-6"><label class="form-label" for="zona">Zona de reparto</label>
                        <input class="form-control" id="zona" name="zona" maxlength="100"
                               value="<c:out value='${usuario.tipoClase == "Repartidor" ? usuario.zonaReparto : ""}'/>"></div>
                    <div class="col-md-6"><label class="form-label" for="licencia">Licencia de conducir</label>
                        <input class="form-control" id="licencia" name="licencia" maxlength="20"
                               value="<c:out value='${usuario.tipoClase == "Repartidor" ? usuario.licencia : ""}'/>"></div>
                </div>
            </div>

            <div class="d-flex gap-2 mt-4">
                <button class="btn btn-ld"><i class="bi bi-check2-circle"></i> ${esNuevo ? 'Crear usuario' : 'Guardar cambios'}</button>
                <a href="${ctx}/admin/usuarios" class="btn btn-link text-muted">Cancelar</a>
            </div>
        </form>
    </div>
    <c:if test="${not esNuevo}">
        <div class="col-lg-4">
            <form method="post" action="${ctx}/admin/usuarios/clave" class="panel panel-suave" data-confirmar="¿Restablecer la contraseña de este usuario?">
                <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                <input type="hidden" name="id" value="${usuario.idUsuario}">
                <h2 class="panel-titulo mb-2"><i class="bi bi-key"></i> Restablecer contraseña</h2>
                <p class="small text-muted">Úsalo si el usuario olvidó su clave. Se guarda cifrada con PBKDF2.</p>
                <input class="form-control mb-2" name="clave" type="password" minlength="8" maxlength="100" required placeholder="Nueva contraseña" autocomplete="new-password">
                <button class="btn btn-ld-azul w-100">Restablecer</button>
            </form>
        </div>
    </c:if>
</div>

<%@ include file="../fragmentos/admin-fin.jspf" %>
