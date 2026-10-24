<%@ include file="fragmentos/init.jspf" %>
<c:set var="titulo" value="Tu pedido"/>
<%@ include file="fragmentos/cabecera.jspf" %>

<div class="container contenedor py-5">
    <div class="d-flex align-items-center gap-3 mb-4">
        <span class="btn-circulo" aria-hidden="true"><i class="bi bi-bag"></i></span>
        <div>
            <h1 class="h2 mb-0">Tu Pedido</h1>
            <p class="text-muted mb-0">${carrito.cantidadTotal} delicias seleccionadas</p>
        </div>
    </div>

    <c:choose>
        <c:when test="${carrito.vacio}">
            <div class="panel estado-vacio">
                <i class="bi bi-basket"></i>
                <h4 class="mt-3">Aún no tienes delicias aquí</h4>
                <p>Explora nuestro catálogo y agrega tus empanadas y bocaditos preferidos.</p>
                <a href="${ctx}/catalogo#catalogo" class="btn btn-ld">Ver catálogo</a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="row g-4">
                <div class="col-lg-8">
                    <div class="panel">
                        <c:forEach var="item" items="${carrito.items}">
                            <div class="item-carrito">
                                <img class="miniatura" src="${item.producto.fotoExterna ? '' : ctx.concat('/')}${item.producto.fotoRuta}" alt=""
                                     onerror="this.onerror=null;this.src='${ctx}/recursos/img/categorias/${item.producto.categoria.idCategoria}.jpg'">
                                <div class="flex-grow-1">
                                    <h6><c:out value="${item.producto.nombre}"/></h6>
                                    <small class="text-muted"><c:out value="${item.producto.categoria.nombre}"/> ·
                                        S/ <fmt:formatNumber value="${item.producto.precio}" minFractionDigits="2" maxFractionDigits="2"/> c/u</small>
                                    <div class="d-flex flex-wrap align-items-center gap-2 mt-2">
                                        <form method="post" action="${ctx}/carrito" class="d-inline-flex gap-1">
                                            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                                            <input type="hidden" name="accion" value="actualizar">
                                            <input type="hidden" name="idProducto" value="${item.producto.idProducto}">
                                            <input type="number" name="cantidad" value="${item.cantidad}" min="1" max="${item.producto.stock}"
                                                   class="form-control form-control-sm input-cantidad" aria-label="Cantidad">
                                            <button class="btn btn-sm btn-suave px-2" title="Actualizar cantidad"><i class="bi bi-arrow-repeat"></i></button>
                                        </form>
                                        <form method="post" action="${ctx}/carrito">
                                            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                                            <input type="hidden" name="accion" value="quitar">
                                            <input type="hidden" name="idProducto" value="${item.producto.idProducto}">
                                            <button class="btn btn-sm btn-link text-danger text-decoration-none"><i class="bi bi-trash3"></i> Quitar</button>
                                        </form>
                                    </div>
                                </div>
                                <div class="fw-bold text-end serif fs-5" style="color: var(--ld-espresso)">
                                    S/ <fmt:formatNumber value="${item.subtotal}" minFractionDigits="2" maxFractionDigits="2"/></div>
                            </div>
                        </c:forEach>
                    </div>
                    <div class="d-flex justify-content-between mt-3">
                        <a href="${ctx}/catalogo#catalogo" class="btn btn-outline-ld px-4"><i class="bi bi-arrow-left"></i> Seguir comprando</a>
                        <form method="post" action="${ctx}/carrito" data-confirmar="¿Vaciar todo el carrito?">
                            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                            <input type="hidden" name="accion" value="vaciar">
                            <button class="btn btn-link text-muted">Vaciar carrito</button>
                        </form>
                    </div>
                </div>
                <div class="col-lg-4">
                    <div class="panel panel-suave resumen-pedido">
                        <h2 class="panel-titulo mb-3">Resumen</h2>
                        <div class="d-flex justify-content-between mb-2 small text-muted">
                            <span>Subtotal productos</span>
                            <span class="fw-semibold" style="color: var(--ld-espresso)">S/ <fmt:formatNumber value="${carrito.total}" minFractionDigits="2" maxFractionDigits="2"/></span>
                        </div>
                        <div class="d-flex justify-content-between align-items-center mb-3 small" style="color: var(--ld-caramelo)">
                            <span><i class="bi bi-truck"></i> Delivery en Lima Metropolitana</span>
                            <span class="envio-pill">Por coordinar</span>
                        </div>
                        <div class="d-flex justify-content-between linea-total mb-4 pt-2 border-top">
                            <span>Total estimado</span><span>S/ <fmt:formatNumber value="${carrito.total}" minFractionDigits="2" maxFractionDigits="2"/></span>
                        </div>
                        <a href="${ctx}/pedido" class="btn btn-ld w-100 py-3">Continuar con mi pedido <i class="bi bi-arrow-right"></i></a>
                        <p class="text-center eyebrow mt-3 mb-0" style="color: var(--ld-texto-2)">Atención directa · Hornadas del día</p>
                    </div>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="fragmentos/pie.jspf" %>
