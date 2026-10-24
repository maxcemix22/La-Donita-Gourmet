<%@ include file="fragmentos/init.jspf" %>
<c:set var="titulo" value="Empanadas & Catering Artesanal"/><c:set var="seccion" value="catalogo"/>
<%@ include file="fragmentos/cabecera.jspf" %>

<!-- 1. Portada -->
<section class="hero">
    <div class="container contenedor">
        <div class="row g-5 align-items-center">
            <div class="col-lg-7">
                <div class="pastilla-lugar mb-4">
                    <span class="punto"></span>
                    <span class="eyebrow" style="color: var(--ld-cacao)">Lima · Tradición culinaria · Calidad artesanal</span>
                </div>
                <h1 class="mb-3">La Doñita <em>Gourmet</em></h1>
                <p class="lead mb-4">Empanadas de hojaldre crujiente horneadas cada amanecer, bocaditos de autor y catering de alta escuela que convierten cada reunión o celebración en una experiencia memorable.</p>
                <div class="d-flex flex-wrap gap-3 mb-4">
                    <a href="#catalogo" class="btn btn-ld"><i class="bi bi-journal-richtext me-1"></i> Ver catálogo completo</a>
                    <a href="${ctx}/catalogo?categoria=CAT002#catalogo" class="btn btn-suave"><i class="bi bi-balloon-heart me-1"></i> Reservar catering</a>
                </div>
                <div class="row g-3 confianza" style="max-width: 34rem">
                    <div class="col-4 d-flex align-items-center gap-2"><i class="bi bi-alarm"></i> Horneado diario a pedido</div>
                    <div class="col-4 d-flex align-items-center gap-2"><i class="bi bi-patch-check"></i> Mantequilla 100% pura</div>
                    <div class="col-4 d-flex align-items-center gap-2"><i class="bi bi-scooter"></i> Envíos express en Lima</div>
                </div>
            </div>
            <div class="col-lg-5">
                <div class="row g-3 mosaico">
                    <div class="col-6"><div class="tile"><div class="emoji">🥟</div><h6>Artesanales</h6><p>Masa hojaldrada hecha a mano y rellenos generosos.</p></div></div>
                    <div class="col-6"><div class="tile"><div class="emoji">🎉</div><h6>+500 Eventos</h6><p>Reuniones corporativas, universidades y celebraciones.</p></div></div>
                    <div class="col-6"><div class="tile"><div class="emoji">⭐</div><h6>Gourmet</h6><p>Insumos selectos: lomo saltado, ají de gallina y quesos andinos.</p></div></div>
                    <div class="col-6"><div class="tile oscuro"><div class="emoji">🛵</div><h6>Lima Total</h6><p>Delivery a domicilio o recojo en nuestro local.</p></div></div>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- 2. Catálogo -->
<section class="seccion pt-4" id="catalogo">
    <div class="container contenedor">
        <div class="text-center mx-auto mb-4" style="max-width: 46rem">
            <span class="eyebrow">Menú completo · Selección del chef</span>
            <h2 class="titulo-seccion mt-2">Nuestro Catálogo Artesanal</h2>
            <p class="text-muted fs-6">Escoge tus favoritos, agrégalos al carrito y confirma tu pedido en minutos. ¡Así de fácil!</p>
        </div>

        <div class="barra-filtros d-flex flex-column flex-lg-row align-items-lg-center justify-content-between gap-3 mb-4">
            <div class="d-flex gap-2 overflow-auto pb-1 pb-lg-0">
                <a href="${ctx}/catalogo#catalogo" class="chip-categoria ${empty categoriaActual ? 'activa' : ''}"><span>🍽️</span> Todos</a>
                <c:forEach var="cat" items="${categorias}">
                    <a href="${ctx}/catalogo?categoria=${cat.idCategoria}#catalogo"
                       class="chip-categoria ${cat.idCategoria == categoriaActual ? 'activa' : ''}">
                        <span><c:choose>
                            <c:when test="${cat.idCategoria == 'CAT001'}">&#x1F95F;</c:when>
                            <c:when test="${cat.idCategoria == 'CAT002'}">&#x1F371;</c:when>
                            <c:when test="${cat.idCategoria == 'CAT003'}">&#x1F381;</c:when>
                            <c:otherwise>&#x1F9C1;</c:otherwise>
                        </c:choose></span>
                        <c:out value="${cat.nombre}"/>
                    </a>
                </c:forEach>
            </div>
            <form class="buscador-ld" method="get" action="${ctx}/catalogo#catalogo" role="search">
                <c:if test="${not empty categoriaActual}"><input type="hidden" name="categoria" value="${categoriaActual}"></c:if>
                <i class="bi bi-search"></i>
                <input type="search" name="q" class="form-control" maxlength="60" placeholder="Buscar sabor o producto..."
                       value="<c:out value='${busqueda}'/>" aria-label="Buscar productos">
            </form>
        </div>

        <c:choose>
            <c:when test="${empty productos}">
                <div class="panel estado-vacio">
                    <i class="bi bi-search-heart"></i>
                    <h4 class="mt-3">No encontramos productos</h4>
                    <p>Prueba con otra búsqueda o revisa todas las categorías.</p>
                    <a href="${ctx}/catalogo#catalogo" class="btn btn-ld">Ver todo el catálogo</a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-4">
                    <c:forEach var="p" items="${productos}">
                        <div class="col">
                            <article class="card-producto">
                                <div class="foto">
                                    <img src="${p.fotoExterna ? '' : ctx.concat('/')}${p.fotoRuta}" loading="lazy" alt="<c:out value='${p.nombre}'/>"
                                         onerror="this.onerror=null;this.src='${ctx}/recursos/img/categorias/${p.categoria.idCategoria}.jpg'">
                                    <div class="etiquetas">
                                        <c:choose>
                                            <c:when test="${p.stock == 0}"><span class="etiqueta gris">AGOTADO</span></c:when>
                                            <c:when test="${p.stock <= 10}"><span class="etiqueta alerta">¡ÚLTIMAS ${p.stock}!</span></c:when>
                                            <c:when test="${p.categoria.idCategoria == 'CAT001'}"><span class="etiqueta caramelo">HORNEADO DEL DÍA</span></c:when>
                                        </c:choose>
                                        <span class="etiqueta"><c:out value="${p.categoria.nombre}"/></span>
                                    </div>
                                </div>
                                <h3><c:out value="${p.nombre}"/></h3>
                                <p class="descripcion"><c:out value="${p.descripcion}"/></p>
                                <form method="post" action="${ctx}/carrito" class="pie-card">
                                    <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                                    <input type="hidden" name="accion" value="agregar">
                                    <input type="hidden" name="idProducto" value="${p.idProducto}">
                                    <input type="hidden" name="categoria" value="${categoriaActual}">
                                    <div>
                                        <span class="precio-label">Precio</span>
                                        <span class="precio">S/ <fmt:formatNumber value="${p.precio}" minFractionDigits="2" maxFractionDigits="2"/></span>
                                    </div>
                                    <div class="d-flex align-items-center gap-2">
                                        <input type="number" name="cantidad" value="1" min="1" max="${p.stock}" class="form-control form-control-sm input-cantidad"
                                               aria-label="Cantidad" ${p.disponible ? '' : 'disabled'}>
                                        <button type="submit" class="btn btn-ld btn-sm" ${p.disponible ? '' : 'disabled'}>
                                            <i class="bi bi-cart-plus"></i> Agregar
                                        </button>
                                    </div>
                                </form>
                            </article>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</section>

<!-- 3. Líneas de producto (banda oscura) -->
<section class="seccion seccion-oscura">
    <div class="container contenedor">
        <div class="text-center mx-auto mb-5" style="max-width: 44rem">
            <span class="eyebrow">Fechas especiales · Eventos · Antojos</span>
            <h2 class="titulo-seccion mt-2">Una delicia para cada ocasión</h2>
            <p>Del antojo de media tarde al buffet de tu evento: elige la línea que necesitas.</p>
        </div>
        <div class="row g-4">
            <c:forEach var="cat" items="${categorias}">
                <div class="col-md-4">
                    <div class="tarjeta-oscura d-flex flex-column">
                        <div class="banner-categoria mb-3">
                            <img src="${ctx}/recursos/img/categorias/${cat.idCategoria}.jpg" alt="" loading="lazy"
                                 onerror="this.parentNode.style.display='none'">
                        </div>
                        <h3 class="h4"><c:out value="${cat.nombre}"/></h3>
                        <p class="flex-grow-1"><c:out value="${cat.descripcion}"/></p>
                        <a href="${ctx}/catalogo?categoria=${cat.idCategoria}#catalogo" class="btn btn-light rounded-pill fw-semibold align-self-start" style="color: var(--ld-cacao)">
                            Ver opciones <i class="bi bi-arrow-right"></i></a>
                    </div>
                </div>
            </c:forEach>
        </div>
    </div>
</section>

<!-- 4. Cómo funciona -->
<section class="seccion">
    <div class="container contenedor">
        <div class="text-center mx-auto mb-5" style="max-width: 40rem">
            <span class="eyebrow">Proceso fácil &amp; rápido</span>
            <h2 class="titulo-seccion mt-2">¿Cómo funciona tu pedido?</h2>
            <p class="text-muted">Pide tus bocaditos y empanadas en 3 simples pasos sin contratiempos.</p>
        </div>
        <div class="row g-4">
            <div class="col-md-4"><div class="paso"><span class="numero">1</span><div class="icono"><i class="bi bi-basket2"></i></div>
                <h5>Elige tus productos</h5><p>Navega por el catálogo, selecciona sabores y agrégalos a tu carrito.</p></div></div>
            <div class="col-md-4"><div class="paso"><span class="numero">2</span><div class="icono"><i class="bi bi-clipboard-check"></i></div>
                <h5>Confirma tu pedido</h5><p>Déjanos tus datos, elige delivery o recojo y, si es un evento, cuéntanos los detalles.</p></div></div>
            <div class="col-md-4"><div class="paso"><span class="numero">3</span><div class="icono"><i class="bi bi-rocket-takeoff"></i></div>
                <h5>¡Listo para disfrutar!</h5><p>Te contactamos para coordinar el horario exacto de entrega o recojo.</p></div></div>
        </div>
        <div class="text-center mt-4">
            <a href="#catalogo" class="btn btn-ld px-4">Comenzar mi pedido ahora <i class="bi bi-arrow-right"></i></a>
        </div>
    </div>
</section>

<%@ include file="fragmentos/pie.jspf" %>
