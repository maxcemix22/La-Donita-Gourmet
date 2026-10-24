<%@ include file="fragmentos/init.jspf" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Acceso del personal | La Doñita Gourmet</title>
    <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400;0,600;0,700;1,400&family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="icon" type="image/png" href="${ctx}/recursos/img/logo.png">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link href="${ctx}/recursos/css/estilos.css" rel="stylesheet">
</head>
<body>
<div class="login-fondo">
    <div class="login-card mt-5">
        <div class="card-body p-4 p-md-5 pt-0">
            <img class="logo-grande d-block" src="${ctx}/recursos/img/logo.png" alt="La Doñita Gourmet">
            <div class="text-center mt-3 mb-4">
                <span class="eyebrow">Panel interno</span>
                <h1 class="h2 mt-1 mb-1">Bienvenido de vuelta</h1>
                <p class="text-muted small mb-0">Acceso exclusivo para el personal de La Doñita Gourmet</p>
            </div>

            <c:if test="${not empty error}">
                <div class="alert alert-danger py-2 small" role="alert"><i class="bi bi-exclamation-circle"></i> <c:out value="${error}"/></div>
            </c:if>

            <form method="post" action="${ctx}/login" class="needs-validation" novalidate autocomplete="on">
                <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
                <div class="mb-3">
                    <label class="form-label small fw-semibold" for="usuario">Usuario</label>
                    <div class="input-group">
                        <span class="input-group-text bg-white"><i class="bi bi-person"></i></span>
                        <input class="form-control" id="usuario" name="usuario" maxlength="50" required autofocus
                               autocomplete="username" value="<c:out value='${usuario}'/>">
                    </div>
                </div>
                <div class="mb-4">
                    <label class="form-label small fw-semibold" for="clave">Contraseña</label>
                    <div class="input-group">
                        <span class="input-group-text bg-white"><i class="bi bi-lock"></i></span>
                        <input type="password" class="form-control" id="clave" name="clave" maxlength="100" required
                               autocomplete="current-password">
                    </div>
                </div>
                <button type="submit" class="btn btn-ld w-100 py-2">Ingresar</button>
            </form>
            <div class="text-center mt-4">
                <a href="${ctx}/catalogo" class="small text-muted"><i class="bi bi-arrow-left"></i> Ir al catálogo</a>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="${ctx}/recursos/js/app.js"></script>
</body>
</html>
