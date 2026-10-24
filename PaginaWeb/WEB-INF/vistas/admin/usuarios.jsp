<%@ include file="../fragmentos/init.jspf" %>
<c:set var="titulo" value="Usuarios"/><c:set var="seccion" value="usuarios"/>
<%@ include file="../fragmentos/admin-inicio.jspf" %>

<div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
    <p class="text-muted mb-0">Personal con acceso al panel. Un usuario desactivado ya no puede iniciar sesión.</p>
    <a href="${ctx}/admin/usuarios/nuevo" class="btn btn-ld"><i class="bi bi-person-plus"></i> Nuevo usuario</a>
</div>

<div class="panel p-0 overflow-hidden">
    <div class="table-responsive">
        <table class="table tabla-ld mb-0 align-middle">
            <thead><tr><th class="ps-4">Nombre</th><th>Usuario</th><th>Rol</th><th>Detalle laboral</th><th>Contacto</th><th>Estado</th><th class="text-end pe-4">Acciones</th></tr></thead>
            <tbody>
            <c:forEach var="u" items="${usuarios}">
                <tr class="${u.estado ? '' : 'estado-inactivo'}">
                    <td class="ps-4"><div class="fw-semibold"><c:out value="${u.nombreCompleto}"/></div><small class="text-muted">${u.idUsuario} · DNI ${u.dni}</small></td>
                    <td><code><c:out value="${u.usuario}"/></code></td>
                    <td><span class="badge rounded-pill ${u.admin ? 'text-bg-dark' : u.repartidor ? 'text-bg-primary' : 'text-bg-info'}">${u.rol.etiqueta}</span></td>
                    <td class="small">
                        <c:choose>
                            <c:when test="${u.rol.name() == 'EMPLEADO'}"><c:out value="${u.areaAsignada}"/> · turno ${u.turno}</c:when>
                            <c:when test="${u.repartidor}">${u.tipoVehiculo} <c:out value="${u.placaVehiculo}"/> · <c:out value="${u.zonaReparto}"/></c:when>
                            <c:otherwise>—</c:otherwise>
                        </c:choose>
                    </td>
                    <td class="small"><c:out value="${u.telefono}"/></td>
                    <td><span class="badge rounded-pill ${u.estado ? 'text-bg-success' : 'text-bg-secondary'}">${u.estado ? 'Activo' : 'Inactivo'}</span></td>
                    <td class="text-end pe-4 text-nowrap">
                        <a href="${ctx}/admin/usuarios/editar?id=${u.idUsuario}" class="btn btn-sm btn-outline-ld"><i class="bi bi-pencil"></i> Editar</a>
                        <c:if test="${u.idUsuario != usuarioSesion.idUsuario}">
                            <form method="post" action="${ctx}/admin/usuarios/estado" class="d-inline"
                                  data-confirmar="${u.estado ? '¿Desactivar este usuario? No podrá iniciar sesión.' : '¿Activar este usuario?'}">
                                <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                                <input type="hidden" name="id" value="${u.idUsuario}">
                                <input type="hidden" name="activar" value="${not u.estado}">
                                <button class="btn btn-sm btn-link ${u.estado ? 'text-danger' : 'text-success'}">${u.estado ? 'Desactivar' : 'Activar'}</button>
                            </form>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</div>

<%@ include file="../fragmentos/admin-fin.jspf" %>
