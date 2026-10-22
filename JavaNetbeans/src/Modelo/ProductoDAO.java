package Modelo;

import java.util.List;

/** Catálogo de productos: RF01 (mostrar), RF06 (agregar, editar, eliminar) y filtro por categoría. */
public interface ProductoDAO {
    List<Producto> listarActivos();
    List<Producto> listarPorCategoria(String idCategoria);
    Producto buscarPorId(String idProducto);
    boolean insertar(Producto producto);
    boolean actualizar(Producto producto);
    boolean eliminar(String idProducto);
}
