/* Configuración y catálogo. Precios y productos tomados del prototipo de alta fidelidad del informe (Figuras 16-20).
 * TODO: sincronizar con la tabla producto de BaseDatos/sistema_ventas.sql o cargar desde la BD cuando exista el backend. */
const WHATSAPP_NUMBER = "51937227925"; // WhatsApp del informe (937 227 925)
const ENVIO_DELIVERY = 12;             // tarifa fija Lima (Figura 20)

const CATEGORIAS = [
  { id: "CAT001", nombre: "Empanadas", icono: "🥟" },
  { id: "CAT003", nombre: "Bocaditos", icono: "🥐" },
  { id: "CAT004", nombre: "Combos festivos", icono: "🎉" },
  { id: "CAT002", nombre: "Catering", icono: "🍽️" }
];

const PRODUCTOS = [
  { id: "PRO001", categoria: "CAT001", nombre: "Empanada de Pollo Artesanal", descripcion: "Relleno jugoso de pechuga deshilachada con receta casera.", precio: 4.0, stock: 100, etiqueta: "Popular", icono: "🥟", foto: null },
  { id: "PRO002", categoria: "CAT001", nombre: "Empanada de Carne Tradicional", descripcion: "Carne seleccionada, cebolla caramelizada, huevo duro y aceituna.", precio: 4.5, stock: 100, etiqueta: "Más pedido", icono: "🥟", foto: null },
  { id: "PRO003", categoria: "CAT001", nombre: "Docena de Empanadas", descripcion: "12 empanadas a tu elección. Combinación perfecta para compartir.", precio: 38.0, stock: 50, icono: "🥟", foto: null },
  { id: "PRO004", categoria: "CAT003", nombre: "Tequeños Crujientes x12", descripcion: "Rellenos de queso fundente, acompañados de guacamole casero.", precio: 18.0, stock: 60, icono: "🧀", foto: null },
  { id: "PRO005", categoria: "CAT003", nombre: "Pack Bocaditos Clásicos x12", descripcion: "Surtido de mini sándwiches, croquetas y empanaditas.", precio: 25.0, stock: 40, icono: "🥐", foto: null },
  { id: "PRO006", categoria: "CAT003", nombre: "Pack Bocaditos Gourmet x20", descripcion: "Selección premium de bocaditos con rellenos especiales.", precio: 45.0, stock: 30, icono: "✨", foto: null },
  { id: "PRO007", categoria: "CAT003", nombre: "Alfajorcitos de Maicena x24", descripcion: "Masa suave que se deshace en la boca, rellenos de manjar blanco.", precio: 38.0, stock: 40, etiqueta: "Dulce tradición", icono: "🍪", foto: null },
  { id: "PRO008", categoria: "CAT003", nombre: "Alitas de Pollo Crocantes x10", descripcion: "Bañadas en salsa BBQ artesanal o maracuyá picante.", precio: 24.0, stock: 30, etiqueta: "Popular", icono: "🍗", foto: null },
  { id: "PRO009", categoria: "CAT004", nombre: "Combo San Valentín Especial", descripcion: "6 empanadas mixtas + 12 alfajorcitos gourmet en caja de regalo.", precio: 42.0, stock: 20, etiqueta: "Edición limitada", icono: "❤️", foto: null },
  { id: "PRO010", categoria: "CAT002", nombre: "Caja Mix Bocaditos x50", descripcion: "Mini empanaditas, tequeños y hojaldres con dip de palta. Rinde 8-10 personas.", precio: 85.0, stock: 20, etiqueta: "Para fiestas", icono: "🎊", foto: null }
];

const PAQUETES = [
  { nombre: "Pack Coffee Break Ejecutivo", ideal: "15 a 20 personas", items: ["25 mini empanadas clásicas de carne", "25 mini empanadas de pollo a la crema", "30 mini croissants de jamón y queso", "25 alfajorcitos de maicena"] },
  { nombre: "Pack Cóctel Social Premium", ideal: "35 a 50 personas", top: true, items: ["50 mini empanadas gourmet de ají de gallina", "50 mini empanadas de lomo saltado", "50 tartaletas de espinaca y ricota", "50 tartaletas de fresas y maracuyá"] },
  { nombre: "Gran Banquete & Buffet Criollo", ideal: "80 a 120 personas", items: ["120 mini empanadas mixtas (4 variedades)", "100 mini butifarras en pan francés", "100 tequeños de queso andino con palta", "Estación de bebidas calientes y chicha morada"] }
];
