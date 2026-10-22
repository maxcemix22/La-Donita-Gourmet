/*
 * Configuración y catálogo de la página.
 * Los productos de ejemplo salen de los datos de prueba de BaseDatos/sistema_ventas.sql.
 * TODO: reemplazar por el catálogo real (fotos/precios) o cargarlo desde la BD cuando exista el backend.
 */
const WHATSAPP_NUMBER = ""; // TODO: número del negocio con código de país, ej. "51999999999"

const CATEGORIAS = [
  { id: "CAT001", nombre: "Empanadas" },
  { id: "CAT002", nombre: "Catering" },
  { id: "CAT003", nombre: "Boxes de bocaditos" }
];

const PRODUCTOS = [
  { id: "PRO001", categoria: "CAT001", nombre: "Empanada de pollo", descripcion: "Empanada rellena de pollo desmenuzado", precio: 4.5, foto: null },
  { id: "PRO002", categoria: "CAT002", nombre: "Box de catering básico", descripcion: "Box de bocaditos surtidos para eventos", precio: 15.0, foto: null }
];
