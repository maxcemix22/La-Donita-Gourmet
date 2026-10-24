<%@ include file="fragmentos/init.jspf" %>
<c:set var="titulo" value="Finalizar pedido"/>
<%@ include file="fragmentos/cabecera.jspf" %>
<c:set var="esCatering" value="${param.tipo == 'catering'}"/>
<c:set var="esDelivery" value="${param.metodoEntrega == 'delivery'}"/>

<div class="container contenedor py-5">
    <span class="eyebrow">Último paso</span>
    <h1 class="h2 mb-1 mt-1">Finaliza tu pedido</h1>
    <p class="text-muted mb-4">Completa tus datos y te contactaremos para confirmar la entrega.</p>

    <c:if test="${not empty errores}">
        <div class="alert alert-danger shadow-sm">
            <strong><i class="bi bi-exclamation-triangle"></i> Revisa los siguientes datos:</strong>
            <ul class="mb-0 mt-1">
                <c:forEach var="e" items="${errores}"><li><c:out value="${e}"/></li></c:forEach>
            </ul>
        </div>
    </c:if>

    <form method="post" action="${ctx}/pedido" class="row g-4 needs-validation" novalidate>
        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
        <div class="col-lg-8">
            <!-- 1. Datos del cliente -->
            <div class="panel mb-4">
                <h5 class="panel-titulo mb-3"><span class="paso-num">1</span>Tus datos</h5>
                <div class="row g-3">
                    <div class="col-md-6">
                        <label class="form-label" for="nombres">Nombres *</label>
                        <input class="form-control" id="nombres" name="nombres" maxlength="100" required value="<c:out value='${param.nombres}'/>">
                        <div class="invalid-feedback">Ingresa tus nombres.</div>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label" for="apellidos">Apellidos *</label>
                        <input class="form-control" id="apellidos" name="apellidos" maxlength="100" required value="<c:out value='${param.apellidos}'/>">
                        <div class="invalid-feedback">Ingresa tus apellidos.</div>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label" for="telefono">Celular *</label>
                        <input class="form-control" id="telefono" name="telefono" inputmode="numeric" pattern="\d{7,15}" maxlength="15" required
                               placeholder="987654321" value="<c:out value='${param.telefono}'/>">
                        <div class="invalid-feedback">Solo números, entre 7 y 15 dígitos.</div>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label" for="correo">Correo electrónico</label>
                        <input type="email" class="form-control" id="correo" name="correo" maxlength="100" value="<c:out value='${param.correo}'/>">
                    </div>
                    <div class="col-12">
                        <label class="form-label" for="direccion">Dirección de referencia</label>
                        <input class="form-control" id="direccion" name="direccion" maxlength="200" placeholder="Distrito, calle, referencia"
                               value="<c:out value='${param.direccion}'/>">
                    </div>
                </div>
            </div>

            <!-- 2. Tipo de pedido -->
            <div class="panel mb-4">
                <h5 class="panel-titulo mb-3"><span class="paso-num">2</span>Tipo de pedido</h5>
                <div class="row g-3 mb-3">
                    <div class="col-sm-6 opcion-tarjeta">
                        <input class="form-check-input" type="radio" name="tipo" id="tipoNormal" value="normal" ${esCatering ? '' : 'checked'}>
                        <label for="tipoNormal"><i class="bi bi-bag-heart"></i><div class="fw-semibold mt-1">Pedido normal</div>
                            <small class="text-muted">Empanadas, boxes y antojos del día.</small></label>
                    </div>
                    <div class="col-sm-6 opcion-tarjeta">
                        <input class="form-check-input" type="radio" name="tipo" id="tipoCatering" value="catering" ${esCatering ? 'checked' : ''}>
                        <label for="tipoCatering"><i class="bi bi-balloon-heart"></i><div class="fw-semibold mt-1">Catering / evento</div>
                            <small class="text-muted">Reserva para cumpleaños, reuniones y más.</small></label>
                    </div>
                </div>

                <div id="seccion-fecha-normal" class="${esCatering ? 'd-none' : ''}">
                    <label class="form-label" for="fechaEntrega">¿Para cuándo lo necesitas? <small class="text-muted">(opcional)</small></label>
                    <input type="datetime-local" class="form-control" id="fechaEntrega" name="fechaEntrega" value="<c:out value='${param.fechaEntrega}'/>">
                </div>

                <div id="seccion-evento" class="${esCatering ? '' : 'd-none'}">
                    <div class="row g-3">
                        <div class="col-md-4">
                            <label class="form-label" for="fechaEvento">Fecha del evento *</label>
                            <input type="date" class="form-control" id="fechaEvento" name="fechaEvento" min="${hoy}" data-requerido
                                   value="<c:out value='${param.fechaEvento}'/>">
                            <div class="invalid-feedback">Indica la fecha (mínimo un día de anticipación).</div>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label" for="horaEvento">Hora</label>
                            <input type="time" class="form-control" id="horaEvento" name="horaEvento" value="<c:out value='${param.horaEvento}'/>">
                        </div>
                        <div class="col-md-4">
                            <label class="form-label" for="cantidadPersonas">N.º de personas *</label>
                            <input type="number" class="form-control" id="cantidadPersonas" name="cantidadPersonas" min="1" max="5000" data-requerido
                                   value="<c:out value='${param.cantidadPersonas}'/>">
                            <div class="invalid-feedback">Indica cuántas personas asistirán.</div>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label" for="tipoServicio">Tipo de evento *</label>
                            <input class="form-control" id="tipoServicio" name="tipoServicio" maxlength="50" list="tiposEvento" data-requerido
                                   placeholder="Cumpleaños, baby shower..." value="<c:out value='${param.tipoServicio}'/>">
                            <datalist id="tiposEvento">
                                <option value="Cumpleaños"><option value="Matrimonio"><option value="Baby shower">
                                <option value="Reunión corporativa"><option value="Bautizo"><option value="Graduación">
                            </datalist>
                            <div class="invalid-feedback">Indica el tipo de evento.</div>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label" for="direccionEvento">Dirección del evento *</label>
                            <input class="form-control" id="direccionEvento" name="direccionEvento" maxlength="200" data-requerido
                                   value="<c:out value='${param.direccionEvento}'/>">
                            <div class="invalid-feedback">Indica la dirección del evento.</div>
                        </div>
                        <div class="col-12">
                            <label class="form-label" for="observacionesEvento">Detalles del evento</label>
                            <textarea class="form-control" id="observacionesEvento" name="observacionesEvento" rows="2" maxlength="300"
                                      placeholder="Temática, alergias, montaje..."><c:out value="${param.observacionesEvento}"/></textarea>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 3. Entrega -->
            <div class="panel">
                <h5 class="panel-titulo mb-3"><span class="paso-num">3</span>Entrega</h5>
                <div class="row g-3 mb-3">
                    <div class="col-sm-6 opcion-tarjeta">
                        <input class="form-check-input" type="radio" name="metodoEntrega" id="recojo" value="recojo" ${esDelivery ? '' : 'checked'}>
                        <label for="recojo"><i class="bi bi-shop"></i><div class="fw-semibold mt-1">Recojo en local</div>
                            <small class="text-muted">Te avisamos cuando esté listo.</small></label>
                    </div>
                    <div class="col-sm-6 opcion-tarjeta">
                        <input class="form-check-input" type="radio" name="metodoEntrega" id="delivery" value="delivery" ${esDelivery ? 'checked' : ''}>
                        <label for="delivery"><i class="bi bi-scooter"></i><div class="fw-semibold mt-1">Delivery</div>
                            <small class="text-muted">Lo llevamos a tu puerta.</small></label>
                    </div>
                </div>
                <div id="seccion-delivery" class="mb-3 ${esDelivery ? '' : 'd-none'}">
                    <label class="form-label" for="direccionEntrega">Dirección de entrega *</label>
                    <input class="form-control" id="direccionEntrega" name="direccionEntrega" maxlength="200" data-requerido
                           placeholder="Av. Los Olivos 123, Lima" value="<c:out value='${param.direccionEntrega}'/>">
                    <div class="form-text">En pedidos de catering puedes dejarla vacía y usaremos la dirección del evento.</div>
                </div>
                <label class="form-label" for="observaciones">Observaciones</label>
                <textarea class="form-control" id="observaciones" name="observaciones" rows="2" maxlength="300"
                          placeholder="Ej. sin ají, tocar el timbre 2 veces..."><c:out value="${param.observaciones}"/></textarea>
            </div>
        </div>

        <div class="col-lg-4">
            <div class="panel panel-suave resumen-pedido">
                <h5 class="panel-titulo mb-3">Tu pedido</h5>
                <c:forEach var="item" items="${carrito.items}">
                    <div class="d-flex justify-content-between small mb-2">
                        <span>${item.cantidad} × <c:out value="${item.producto.nombre}"/></span>
                        <span>S/ <fmt:formatNumber value="${item.subtotal}" minFractionDigits="2" maxFractionDigits="2"/></span>
                    </div>
                </c:forEach>
                <hr>
                <div class="d-flex justify-content-between linea-total mb-3">
                    <span>Total</span><span>S/ <fmt:formatNumber value="${carrito.total}" minFractionDigits="2" maxFractionDigits="2"/></span>
                </div>
                <button type="submit" class="btn btn-ld w-100 py-2"><i class="bi bi-check2-circle"></i> Confirmar pedido</button>
                <a href="${ctx}/carrito" class="btn btn-link w-100 mt-1">Volver al carrito</a>
                <p class="small text-muted mb-0 mt-2"><i class="bi bi-shield-check"></i> El pago se coordina al confirmar el pedido.</p>
            </div>
        </div>
    </form>
</div>

<%@ include file="fragmentos/pie.jspf" %>
