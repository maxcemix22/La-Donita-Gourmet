package com.ladonita.modelo.dao;

import com.ladonita.modelo.entidades.Producto;
import java.util.List;

public interface ProductoDAO {

    boolean insertar(Producto p);

    boolean actualizar(Producto p);

    /**
     * Elimina el producto. Si ya figura en algún pedido no se borra (se perdería el historial):
     * se desactiva. Devuelve true si se borró físicamente, false si solo se desactivó.
     */
    boolean eliminar(String idProducto);

    List<Producto> listar();

    List<Producto> listarPorCategoria(String idCategoria);

    /** Productos activos para el catálogo público, filtrados por categoría y/o texto (ambos opcionales). */
    List<Producto> buscarCatalogo(String idCategoria, String texto);

    /** Búsqueda del panel: incluye inactivos. activo = null para todos. */
    List<Producto> buscarAdmin(String texto, String idCategoria, Boolean activo);

    Producto buscarPorId(String id);

    boolean ajustarStock(String idProducto, int nuevoStock);

    /** Productos activos con stock menor o igual a {@link Producto#STOCK_MINIMO}. */
    List<Producto> listarStockBajo();
}
