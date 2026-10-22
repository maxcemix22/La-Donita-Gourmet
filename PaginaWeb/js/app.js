"use strict";

const carrito = new Map(); // idProducto -> cantidad
let categoriaActiva = "todas";

const $ = (sel) => document.querySelector(sel);
const soles = (n) => "S/ " + n.toFixed(2);

function el(tag, attrs = {}, ...hijos) {
  const e = document.createElement(tag);
  for (const [k, v] of Object.entries(attrs)) {
    if (k === "class") e.className = v;
    else if (k.startsWith("on")) e.addEventListener(k.slice(2), v);
    else e.setAttribute(k, v);
  }
  hijos.forEach((h) => e.append(h));
  return e;
}

/* ---------- Catálogo (RF01) ---------- */
function renderFiltros() {
  const cont = $("#filtros");
  cont.replaceChildren();
  [{ id: "todas", nombre: "Todas" }, ...CATEGORIAS].forEach((c) => {
    cont.append(el("button", {
      type: "button",
      class: "chip" + (c.id === categoriaActiva ? " activo" : ""),
      "aria-pressed": String(c.id === categoriaActiva),
      onclick: () => { categoriaActiva = c.id; renderFiltros(); renderProductos(); }
    }, c.nombre));
  });
}

function renderProductos() {
  const cont = $("#productos");
  cont.replaceChildren();
  const lista = PRODUCTOS.filter((p) => categoriaActiva === "todas" || p.categoria === categoriaActiva);
  if (!lista.length) {
    cont.append(el("p", { class: "aviso" }, "Pronto tendremos productos en esta categoría. Consúltanos por WhatsApp."));
    return;
  }
  lista.forEach((p) => {
    const foto = p.foto
      ? el("img", { src: p.foto, alt: p.nombre, loading: "lazy" })
      : el("div", { class: "foto-vacia", "aria-hidden": "true" }, "Foto próximamente");
    cont.append(el("article", { class: "card" },
      foto,
      el("h3", {}, p.nombre),
      el("p", {}, p.descripcion),
      el("p", { class: "precio" }, soles(p.precio)),
      el("button", { type: "button", class: "btn", onclick: () => agregar(p.id) }, "Agregar al pedido")
    ));
  });
}

/* ---------- Carrito ---------- */
function agregar(id) { carrito.set(id, (carrito.get(id) || 0) + 1); renderCarrito(); }
function cambiarCantidad(id, delta) {
  const n = (carrito.get(id) || 0) + delta;
  if (n <= 0) carrito.delete(id); else carrito.set(id, n);
  renderCarrito();
}

function totalPedido() {
  let t = 0;
  carrito.forEach((cant, id) => { t += PRODUCTOS.find((p) => p.id === id).precio * cant; });
  return t;
}

function renderCarrito() {
  const vacio = carrito.size === 0;
  $("#carrito-vacio").hidden = !vacio;
  $("#carrito-contenido").hidden = vacio;
  $("#cart-count").textContent = [...carrito.values()].reduce((a, b) => a + b, 0);
  const ul = $("#items");
  ul.replaceChildren();
  carrito.forEach((cant, id) => {
    const p = PRODUCTOS.find((x) => x.id === id);
    ul.append(el("li", {},
      el("span", { class: "nombre" }, p.nombre),
      el("span", { class: "cant" },
        el("button", { type: "button", "aria-label": "Quitar uno", onclick: () => cambiarCantidad(id, -1) }, "−"),
        String(cant),
        el("button", { type: "button", "aria-label": "Agregar uno", onclick: () => cambiarCantidad(id, 1) }, "+")),
      el("span", { class: "sub" }, soles(p.precio * cant))
    ));
  });
  $("#total").textContent = soles(totalPedido());
}

/* ---------- Formulario (RF02, RF03) ---------- */
const form = $("#pedido-form");
const valor = (name) => (form.elements[name].value || "").trim();
const radio = (name) => form.querySelector(`input[name="${name}"]:checked`).value;

form.addEventListener("change", () => {
  $("#evento").hidden = radio("tipo") !== "catering";
  $("#dir-wrap").hidden = radio("entrega") !== "delivery";
});

function validar() {
  const faltan = [];
  if (!valor("nombre")) faltan.push("nombre");
  if (!/^[+\d][\d\s-]{6,}$/.test(valor("telefono"))) faltan.push("teléfono válido");
  if (radio("entrega") === "delivery" && !valor("direccion")) faltan.push("dirección de entrega");
  if (radio("tipo") === "catering") {
    if (!valor("fechaEvento")) faltan.push("fecha del evento");
    if (!valor("tipoServicio")) faltan.push("tipo de servicio");
    if (!(Number(valor("personas")) > 0)) faltan.push("cantidad de personas");
    if (!valor("direccionEvento")) faltan.push("dirección del evento");
  }
  return faltan;
}

function armarMensaje() {
  const l = ["Hola, quiero hacer un pedido en La Doñita Gourmet:", ""];
  carrito.forEach((cant, id) => {
    const p = PRODUCTOS.find((x) => x.id === id);
    l.push(`• ${cant} x ${p.nombre} — ${soles(p.precio * cant)}`);
  });
  l.push("", `Total: ${soles(totalPedido())}`, "",
    `Nombre: ${valor("nombre")}`, `Teléfono: ${valor("telefono")}`,
    `Tipo de pedido: ${radio("tipo") === "catering" ? "Catering / corporativo" : "Regular"}`);
  if (radio("tipo") === "catering") {
    l.push(`Evento: ${valor("tipoServicio")}, ${valor("fechaEvento")}${valor("horaEvento") ? " " + valor("horaEvento") : ""}`,
      `Personas: ${valor("personas")}`, `Dirección del evento: ${valor("direccionEvento")}`);
  }
  l.push(`Entrega: ${radio("entrega")}${radio("entrega") === "delivery" ? " — " + valor("direccion") : ""}`);
  if (valor("fechaEntrega")) l.push(`Fecha deseada: ${valor("fechaEntrega")}`);
  if (valor("pago")) l.push(`Pago preferido: ${valor("pago")}`);
  if (valor("observaciones")) l.push(`Observaciones: ${valor("observaciones")}`);
  return l.join("\n");
}

form.addEventListener("submit", (ev) => {
  ev.preventDefault();
  const err = $("#error");
  const faltan = validar();
  if (faltan.length) {
    err.textContent = "Completa: " + faltan.join(", ") + ".";
    err.hidden = false;
    return;
  }
  if (!WHATSAPP_NUMBER) {
    err.textContent = "Falta configurar WHATSAPP_NUMBER en js/productos.js.";
    err.hidden = false;
    return;
  }
  err.hidden = true;
  const url = "https://wa.me/" + encodeURIComponent(WHATSAPP_NUMBER) + "?text=" + encodeURIComponent(armarMensaje());
  window.open(url, "_blank", "noopener");
});

renderFiltros();
renderProductos();
renderCarrito();
