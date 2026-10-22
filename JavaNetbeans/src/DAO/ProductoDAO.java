package DAO;

import Modelo.Producto;
import java.util.List;

/*
 * MOVIDO de Modelo a DAO.
 * Catálogo de productos: RF01 (mostrar), RF06 (agregar, editar, eliminar) y filtro por categoría.
 * insertar() genera el id (PRO001, PRO002, ...) y lo asigna al objeto.
 * eliminar(): si el producto ya aparece en pedidos se desactiva (estado = 0) para no perder el historial;
 * si nunca se usó, se borra.
 */
public interface ProductoDAO {
    List<Producto> listarActivos();
    List<Producto> listarPorCategoria(String idCategoria);
    Producto buscarPorId(String idProducto);
    boolean insertar(Producto producto);
    boolean actualizar(Producto producto);
    boolean eliminar(String idProducto);
}
