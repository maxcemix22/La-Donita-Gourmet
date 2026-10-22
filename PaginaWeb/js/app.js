"use strict";
const carrito = new Map(); // idProducto -> cantidad
let categoriaActiva = "todas";
const CLAVE = "donita_carrito";
const $ = (s) => document.querySelector(s);
const soles = (n) => "S/ " + n.toFixed(2);
const hoy = () => new Date().toISOString().slice(0, 10);
const waUrl = (txt) => "https://wa.me/" + encodeURIComponent(WHATSAPP_NUMBER) + (txt ? "?text=" + encodeURIComponent(txt) : "");

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
const prod = (id) => PRODUCTOS.find((p) => p.id === id);

/* El carrito se recuerda en el navegador (si no hay almacenamiento, simplemente no se guarda) */
function guardar() { try { localStorage.setItem(CLAVE, JSON.stringify([...carrito])); } catch (e) {} }
function cargar() {
  try {
    JSON.parse(localStorage.getItem(CLAVE) || "[]").forEach(([id, c]) => {
      const p = prod(id);
      if (p && Number.isInteger(c) && c > 0 && p.stock > 0) carrito.set(id, Math.min(c, p.stock));
    });
  } catch (e) {}
}

/* ---------- Catálogo (RF01) ---------- */
function renderFiltros() {
  const c = $("#filtros"); c.replaceChildren();
  [{ id: "todas", nombre: "Todos", icono: "🍽️" }, ...CATEGORIAS].forEach((k) =>
    c.append(el("button", { type: "button", class: "chip" + (k.id === categoriaActiva ? " on" : ""), "aria-pressed": String(k.id === categoriaActiva),
      onclick: () => { categoriaActiva = k.id; renderFiltros(); renderProductos(); } }, `${k.icono} ${k.nombre}`)));
}
function renderProductos() {
  const c = $("#productos"); c.replaceChildren();
  const lista = PRODUCTOS.filter((p) => categoriaActiva === "todas" || p.categoria === categoriaActiva);
  if (!lista.length) { c.append(el("p", { class: "vacio" }, "Pronto tendremos productos en esta categoría. Consúltanos por WhatsApp.")); return; }
  lista.forEach((p) => {
    const foto = el("div", { class: "foto", "aria-hidden": "true" }, p.icono || "🥟");
    if (p.foto) foto.append(el("img", { src: p.foto, alt: p.nombre, loading: "lazy" }));
    if (p.etiqueta) foto.append(el("span", { class: "tag" }, p.etiqueta));
    c.append(el("article", { class: "card" }, foto, el("div", { class: "cuerpo" },
      el("h3", {}, p.nombre), el("p", {}, p.descripcion),
      el("div", { class: "pie" }, el("span", { class: "precio" }, soles(p.precio)),
        el("button", { type: "button", class: "btn sm", onclick: () => cambiar(p.id, 1), ...(p.stock > 0 ? {} : { disabled: "" }) }, p.stock > 0 ? "+ Agregar" : "Agotado")))));
  });
}

/* ---------- Carrito ---------- */
function cambiar(id, d) {
  const p = prod(id), n = (carrito.get(id) || 0) + d, av = $("#aviso-carrito");
  av.hidden = true;
  if (n > p.stock) { av.textContent = `Solo hay ${p.stock} unidades disponibles de ${p.nombre}.`; av.hidden = false; return; }
  if (n <= 0) carrito.delete(id); else carrito.set(id, n);
  renderCarrito();
}
$("#vaciar").addEventListener("click", () => { carrito.clear(); $("#aviso-carrito").hidden = true; renderCarrito(); });
const subtotal = () => [...carrito].reduce((t, [id, c]) => t + prod(id).precio * c, 0);
const esDelivery = () => form.querySelector('input[name="entrega"]:checked').value === "delivery";
const envio = () => (esDelivery() ? ENVIO_DELIVERY : 0);

function filaItem(id, c) {
  const p = prod(id);
  return el("li", {}, el("span", {}, p.nombre), el("span", { class: "sub" }, soles(p.precio * c)),
    el("span", { class: "cant" },
      el("button", { type: "button", "aria-label": "Quitar uno", onclick: () => cambiar(id, -1) }, "−"), String(c),
      el("button", { type: "button", "aria-label": "Agregar uno", onclick: () => cambiar(id, 1) }, "+"),
      el("small", {}, ` ${soles(p.precio)} c/u`)));
}
function renderCarrito() {
  const vacio = carrito.size === 0;
  $("#carrito-vacio").hidden = !vacio; $("#carrito-contenido").hidden = vacio;
  $("#mini-vacio").hidden = !vacio; $("#mini-tot").hidden = vacio;
  $("#cart-count").textContent = [...carrito.values()].reduce((a, b) => a + b, 0);
  ["#items", "#mini-items"].forEach((s) => { const ul = $(s); ul.replaceChildren(); carrito.forEach((c, id) => ul.append(filaItem(id, c))); });
  const s = subtotal();
  $("#mini-total").textContent = soles(s);
  $("#sub").textContent = soles(s);
  $("#envio").textContent = envio() ? soles(envio()) : "Gratis";
  $("#total").textContent = soles(s + envio());
  guardar();
}

/* ---------- Calculadora de catering ---------- */
const EVENTOS = ["Corporativo", "Boda / Civil", "Cumpleaños", "Bautizo", "Graduación", "Reunión"];
const ESTILOS = [{ n: "Clásico Criollo", d: "Empanadas de lomo, ají de gallina y mini butifarras.", f: 1 }, { n: "Fusión Gourmet", d: "Pastel de choclo mini, lomo al vino y quiche lorraine.", f: 1.15 }, { n: "Cóctel & Dulce", d: "Variedad salada + alfajorcitos y tartaletas.", f: 1.05 }];
const calc = { ev: EVENTOS[0], est: 0 };
function opciones(cont, lista, clave) {
  const c = $(cont); c.replaceChildren();
  lista.forEach((x, i) => {
    const on = clave === "ev" ? x === calc.ev : i === calc.est;
    c.append(el("button", { type: "button", class: "chip" + (on ? " on" : ""), title: x.d || "", onclick: () => { calc[clave] = clave === "ev" ? x : i; pintarCalc(); } }, x.n || x));
  });
}
function pintarCalc() {
  const n = Number($("#inv").value), e = ESTILOS[calc.est], bocaditos = n * 6;
  const min = Math.round(bocaditos * 2.85 * e.f), max = Math.round(bocaditos * 3.6 * e.f);
  opciones("#evt-tipos", EVENTOS, "ev"); opciones("#evt-estilos", ESTILOS, "est");
  $("#inv-n").textContent = n + " personas";
  $("#res").textContent = `${calc.ev} · ${n} personas · ${e.n} · ~${bocaditos} bocaditos`;
  $("#est").textContent = `S/ ${min} – S/ ${max}`;
  $("#cot").href = waUrl(`Hola, quiero cotizar un catering en La Doñita Gourmet:\nEvento: ${calc.ev}\nInvitados: ${n}\nEstilo: ${e.n}\nEstimado referencial: S/ ${min} – S/ ${max}`);
}
$("#inv").addEventListener("input", pintarCalc);
function renderPaquetes() {
  const c = $("#paquetes");
  PAQUETES.forEach((p) => c.append(el("article", { class: "paso", style: "text-align:left" + (p.top ? ";outline:2px solid var(--brown)" : "") },
    el("span", { class: "pill" }, "Ideal " + p.ideal), el("h3", { style: "margin:.6rem 0" }, p.nombre),
    el("ul", { style: "padding-left:1.1rem;margin:0 0 1rem;font-size:.87rem;color:var(--muted)" }, ...p.items.map((i) => el("li", {}, i))),
    el("a", { class: "btn wa sm", target: "_blank", rel: "noopener", href: waUrl(`Hola, quiero cotizar el ${p.nombre} (ideal ${p.ideal}).`) }, "Cotizar paquete"))));
}

/* ---------- Formulario (RF02, RF03) ---------- */
const form = $("#pedido-form");
const valor = (n) => (form.elements[n].value || "").trim();
const radio = (n) => form.querySelector(`input[name="${n}"]:checked`).value;
form.addEventListener("change", () => {
  $("#evento").hidden = radio("tipo") !== "catering";
  $("#dir-wrap").hidden = radio("entrega") !== "delivery";
  renderCarrito();
});
function validar() {
  const f = [];
  if (!valor("nombre")) f.push("nombre");
  if (!/^[+\d][\d\s-]{6,}$/.test(valor("telefono"))) f.push("teléfono válido");
  if (radio("entrega") === "delivery") { if (!valor("distrito")) f.push("distrito"); if (!valor("direccion")) f.push("dirección de entrega"); }
  if (valor("fechaEntrega") && valor("fechaEntrega") < hoy()) f.push("fecha de entrega que no sea pasada");
  if (radio("tipo") === "catering") {
    if (!valor("fechaEvento") || valor("fechaEvento") < hoy()) f.push("fecha del evento (hoy o posterior)");
    if (!valor("tipoServicio")) f.push("tipo de servicio");
    if (!(Number(valor("personas")) > 0)) f.push("cantidad de personas");
    if (!valor("direccionEvento")) f.push("dirección del evento");
  }
  return f;
}
function armarMensaje() {
  const l = ["Hola, quiero hacer un pedido en La Doñita Gourmet:", ""];
  carrito.forEach((c, id) => { const p = prod(id); l.push(`• ${c} x ${p.nombre} — ${soles(p.precio * c)}`); });
  l.push("", `Subtotal: ${soles(subtotal())}`, `Envío: ${envio() ? soles(envio()) : "Gratis"}`, `Total: ${soles(subtotal() + envio())}`, "",
    `Nombre: ${valor("nombre")}`, `Teléfono: ${valor("telefono")}`, `Tipo de pedido: ${radio("tipo") === "catering" ? "Catering / corporativo" : "Regular"}`);
  if (radio("tipo") === "catering") l.push(`Evento: ${valor("tipoServicio")}, ${valor("fechaEvento")}${valor("horaEvento") ? " " + valor("horaEvento") : ""}`, `Personas: ${valor("personas")}`, `Dirección del evento: ${valor("direccionEvento")}`);
  l.push(`Entrega: ${radio("entrega")}${esDelivery() ? ` — ${valor("direccion")} (${valor("distrito")})` : ""}`);
  if (valor("fechaEntrega")) l.push(`Fecha deseada: ${valor("fechaEntrega")}`);
  if (valor("pago")) l.push(`Pago preferido: ${valor("pago")}`);
  if (valor("observaciones")) l.push(`Indicaciones: ${valor("observaciones")}`);
  return l.join("\n");
}
form.addEventListener("submit", (ev) => {
  ev.preventDefault();
  const err = $("#error"), falta = validar();
  if (falta.length) { err.textContent = "Completa: " + falta.join(", ") + "."; err.hidden = false; return; }
  if (!WHATSAPP_NUMBER) { err.textContent = "Falta configurar WHATSAPP_NUMBER en js/productos.js."; err.hidden = false; return; }
  err.hidden = true; $("#exito").hidden = false;
  window.open(waUrl(armarMensaje()), "_blank", "noopener");
});
["fechaEvento", "fechaEntrega"].forEach((n) => { form.elements[n].min = hoy(); });
["#wa-top", "#wa-fab"].forEach((s) => { $(s).href = waUrl("Hola, quiero hacer una consulta."); });

cargar(); renderFiltros(); renderProductos(); renderCarrito(); renderPaquetes(); pintarCalc();
