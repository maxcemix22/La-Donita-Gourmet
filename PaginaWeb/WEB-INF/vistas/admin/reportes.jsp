<%@ include file="../fragmentos/init.jspf" %>
<c:set var="titulo" value="Reportes"/><c:set var="seccion" value="reportes"/>
<%@ include file="../fragmentos/admin-inicio.jspf" %>

<div class="pestanas-reporte mb-3 no-imprimir">
    <c:forEach var="p" items="${pestanas}">
        <a href="${ctx}/admin/reportes?r=${p.codigo}" class="${p.codigo == codigo ? 'activa' : ''}"><small>${p.codigo}</small>${p.nombre}</a>
    </c:forEach>
</div>

<div class="panel">
    <div class="d-flex flex-wrap justify-content-between align-items-start gap-3 mb-3">
        <div>
            <span class="eyebrow">${tabla.codigo}</span>
            <h2 class="panel-titulo fs-3 mb-1"><c:out value="${tabla.titulo}"/></h2>
            <p class="text-muted small mb-0"><c:out value="${tabla.descripcion}"/></p>
        </div>
        <div class="d-flex gap-2 no-imprimir">
            <a class="btn btn-ld btn-sm" href="${ctx}/admin/reportes?<c:out value='${query}'/>&amp;xlsx=1"><i class="bi bi-file-earmark-excel"></i> Exportar Excel</a>
            <a class="btn btn-suave btn-sm" href="${ctx}/admin/reportes?<c:out value='${query}'/>&amp;csv=1"><i class="bi bi-filetype-csv"></i> CSV</a>
            <button class="btn btn-suave btn-sm" type="button" data-imprimir><i class="bi bi-printer"></i> Imprimir</button>
        </div>
    </div>

    <form method="get" action="${ctx}/admin/reportes" class="row g-2 align-items-end mb-3 no-imprimir">
        <input type="hidden" name="r" value="${codigo}">
        <c:if test="${codigo == 'R01'}">
            <div class="col-auto">
                <label class="form-label" for="vista">Vista</label>
                <select class="form-select" id="vista" name="vista" onchange="this.form.submit()">
                    <option value="diario" ${vistaR01 == 'diario' ? 'selected' : ''}>Diaria</option>
                    <option value="mensual" ${vistaR01 == 'mensual' ? 'selected' : ''}>Mensual</option>
                </select>
            </div>
            <c:if test="${vistaR01 == 'mensual'}">
                <div class="col-auto"><label class="form-label" for="anio">Año</label>
                    <input class="form-control" id="anio" name="anio" type="number" min="2020" max="2100" value="${anio}" style="width: 110px"></div>
            </c:if>
        </c:if>
        <c:if test="${conRango}">
            <div class="col-auto"><label class="form-label" for="desde">Desde</label>
                <input class="form-control" id="desde" name="desde" type="date" value="${desde}"></div>
            <div class="col-auto"><label class="form-label" for="hasta">Hasta</label>
                <input class="form-control" id="hasta" name="hasta" type="date" value="${hasta}"></div>
        </c:if>
        <c:if test="${codigo == 'R06'}">
            <div class="col-auto">
                <label class="form-label" for="eventos">Eventos</label>
                <select class="form-select" id="eventos" name="eventos" onchange="this.form.submit()">
                    <option value="proximas" ${todasEventos ? '' : 'selected'}>Próximos</option>
                    <option value="todas" ${todasEventos ? 'selected' : ''}>Todos</option>
                </select>
            </div>
        </c:if>
        <c:if test="${conRango or (codigo == 'R01' and vistaR01 == 'mensual')}">
            <div class="col-auto"><button class="btn btn-ld-azul">Aplicar</button></div>
        </c:if>
    </form>

    <c:choose>
        <c:when test="${tabla.vacio}">
            <div class="estado-vacio"><i class="bi bi-clipboard-data"></i><h5 class="mt-3">Sin datos para este reporte</h5>
                <p class="mb-0">Prueba con otro rango de fechas.</p></div>
        </c:when>
        <c:otherwise>
            <div class="table-responsive">
                <table class="table tabla-ld table-hover mb-0 align-middle">
                    <thead><tr>
                        <c:forEach var="col" items="${tabla.columnas}">
                            <th class="${col.numerica ? 'text-end' : ''}">${col.titulo}</th>
                        </c:forEach>
                    </tr></thead>
                    <tbody>
                    <c:forEach var="fila" items="${tabla.filas}">
                        <tr>
                            <c:forEach var="v" items="${fila}" varStatus="st">
                                <c:set var="tipo" value="${tabla.columnas[st.index].tipo}"/>
                                <td class="${tabla.columnas[st.index].numerica ? 'text-end' : ''} ${st.first ? 'fw-semibold' : ''}">
                                    <c:choose>
                                        <c:when test="${empty v}">—</c:when>
                                        <c:when test="${tipo == 'moneda'}"><fmt:formatNumber value="${v}" minFractionDigits="2" maxFractionDigits="2"/></c:when>
                                        <c:when test="${tipo == 'estado'}"><span class="badge badge-estado text-bg-${v.color}">${v.etiqueta}</span></c:when>
                                        <c:when test="${tipo == 'nivel'}"><span class="badge rounded-pill nivel-${v}">${v}</span></c:when>
                                        <c:when test="${tipo == 'fecha'}">${v.dayOfMonth < 10 ? '0' : ''}${v.dayOfMonth}/${v.monthValue < 10 ? '0' : ''}${v.monthValue}/${v.year}</c:when>
                                        <c:when test="${tipo == 'fechahora'}">${v.dayOfMonth < 10 ? '0' : ''}${v.dayOfMonth}/${v.monthValue < 10 ? '0' : ''}${v.monthValue}/${v.year} ${v.hour < 10 ? '0' : ''}${v.hour}:${v.minute < 10 ? '0' : ''}${v.minute}</c:when>
                                        <c:when test="${st.first and (fn:startsWith(v, 'PED'))}"><a href="${ctx}/admin/pedidos/detalle?id=${v}" class="text-decoration-none">${v}</a></c:when>
                                        <c:otherwise><c:out value="${v}"/></c:otherwise>
                                    </c:choose>
                                </td>
                            </c:forEach>
                        </tr>
                    </c:forEach>
                    </tbody>
                    <c:if test="${tabla.conTotales}">
                        <tfoot><tr>
                            <c:forEach var="t" items="${tabla.totales}" varStatus="st">
                                <td class="${tabla.columnas[st.index].numerica ? 'text-end' : ''}">
                                    <c:choose>
                                        <c:when test="${st.first}">Total (${tabla.filas.size()})</c:when>
                                        <c:when test="${empty t or tabla.columnas[st.index].titulo.startsWith('Ticket')}"></c:when>
                                        <c:when test="${tabla.columnas[st.index].tipo == 'moneda'}"><fmt:formatNumber value="${t}" minFractionDigits="2" maxFractionDigits="2"/></c:when>
                                        <c:otherwise>${t}</c:otherwise>
                                    </c:choose>
                                </td>
                            </c:forEach>
                        </tr></tfoot>
                    </c:if>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="../fragmentos/admin-fin.jspf" %>
