// La Doñita Gourmet - comportamiento de la interfaz (solo mejora la experiencia;
// todas las reglas se validan otra vez en el servidor).
document.addEventListener("DOMContentLoaded", function () {

    // Checkout: muestra u oculta secciones según el tipo de pedido y el método de entrega.
    function alternar(nombre, valor, idSeccion) {
        var seccion = document.getElementById(idSeccion);
        if (!seccion) return;
        var actualizar = function () {
            var marcado = document.querySelector('input[name="' + nombre + '"]:checked');
            var visible = marcado && marcado.value === valor;
            seccion.classList.toggle("d-none", !visible);
            seccion.querySelectorAll("[data-requerido]").forEach(function (campo) {
                campo.required = visible;
            });
        };
        document.querySelectorAll('input[name="' + nombre + '"]').forEach(function (r) {
            r.addEventListener("change", actualizar);
        });
        actualizar();
    }
    alternar("tipo", "catering", "seccion-evento");
    alternar("tipo", "normal", "seccion-fecha-normal");
    alternar("metodoEntrega", "delivery", "seccion-delivery");

    // Confirmación antes de acciones sensibles.
    document.querySelectorAll("form[data-confirmar]").forEach(function (form) {
        form.addEventListener("submit", function (e) {
            if (!confirm(form.getAttribute("data-confirmar"))) e.preventDefault();
        });
    });

    // Validación visual de Bootstrap.
    document.querySelectorAll("form.needs-validation").forEach(function (form) {
        form.addEventListener("submit", function (e) {
            if (!form.checkValidity()) {
                e.preventDefault();
                e.stopPropagation();
            }
            form.classList.add("was-validated");
        });
    });

    // Las alertas se cierran solas a los 6 segundos.
    document.querySelectorAll(".alert-auto").forEach(function (alerta) {
        setTimeout(function () {
            if (window.bootstrap) bootstrap.Alert.getOrCreateInstance(alerta).close();
        }, 6000);
    });
});

// ---------------- Panel interno ----------------
document.addEventListener("DOMContentLoaded", function () {
    // Menú lateral en celulares
    var sidebar = document.getElementById("sidebar");
    var fondo = document.getElementById("sidebarFondo");
    var abrir = document.getElementById("abrirMenu");
    function alternarMenu(abierto) {
        if (!sidebar) return;
        sidebar.classList.toggle("abierto", abierto);
        fondo.classList.toggle("visible", abierto);
    }
    if (abrir) abrir.addEventListener("click", function () { alternarMenu(true); });
    if (fondo) fondo.addEventListener("click", function () { alternarMenu(false); });

    // Fecha de hoy en la barra superior
    var fecha = document.getElementById("fechaHoy");
    if (fecha) {
        fecha.textContent = new Date().toLocaleDateString("es-PE", { weekday: "long", day: "numeric", month: "long" });
    }

    // Vista previa de la foto del producto antes de subirla
    var inputFoto = document.getElementById("foto");
    var preview = document.getElementById("fotoPreview");
    if (inputFoto && preview) {
        inputFoto.addEventListener("change", function () {
            var archivo = inputFoto.files && inputFoto.files[0];
            if (!archivo) return;
            if (archivo.size > 2 * 1024 * 1024) {
                alert("La foto supera los 2 MB. Elige una imagen más liviana.");
                inputFoto.value = "";
                return;
            }
            preview.src = URL.createObjectURL(archivo);
        });
    }

    // Formulario de usuario: muestra los datos laborales según el rol elegido
    var selectRol = document.getElementById("rol");
    if (selectRol) {
        var mostrarSeccion = function () {
            document.querySelectorAll("[data-rol]").forEach(function (sec) {
                var visible = sec.getAttribute("data-rol").split(" ").indexOf(selectRol.value) >= 0;
                sec.classList.toggle("d-none", !visible);
                sec.querySelectorAll("[data-requerido]").forEach(function (c) { c.required = visible; });
            });
        };
        selectRol.addEventListener("change", mostrarSeccion);
        mostrarSeccion();
    }

    // Botón imprimir (reportes)
    document.querySelectorAll("[data-imprimir]").forEach(function (b) {
        b.addEventListener("click", function () { window.print(); });
    });
});
