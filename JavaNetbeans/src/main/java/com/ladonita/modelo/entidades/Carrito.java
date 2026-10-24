package com.ladonita.modelo.entidades;

import com.google.common.base.Preconditions;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Carrito de compras guardado en la sesión del cliente. */
public class Carrito implements Serializable {

    public static final int MAX_POR_PRODUCTO = 500;

    private final Map<String, ItemCarrito> items = new LinkedHashMap<>();

    /** Agrega unidades respetando el stock disponible. Devuelve la cantidad final. */
    public int agregar(Producto p, int cantidad) {
        Preconditions.checkNotNull(p, "producto");
        Preconditions.checkNotNull(p.getIdProducto(), "el producto no tiene código");
        ItemCarrito item = items.get(p.getIdProducto());
        int nueva = (item == null ? 0 : item.getCantidad()) + cantidad;
        nueva = Math.min(nueva, Math.min(p.getStock(), MAX_POR_PRODUCTO));
        if (nueva <= 0) {
            items.remove(p.getIdProducto());
            return 0;
        }
        items.put(p.getIdProducto(), new ItemCarrito(p, nueva));
        return nueva;
    }

    public void actualizar(Producto p, int cantidad) {
        items.remove(p.getIdProducto());
        if (cantidad > 0) {
            agregar(p, cantidad);
        }
    }

    public void quitar(String idProducto) {
        items.remove(idProducto);
    }

    public void vaciar() {
        items.clear();
    }

    public List<ItemCarrito> getItems() {
        return new ArrayList<>(items.values());
    }

    public boolean isVacio() {
        return items.isEmpty();
    }

    public int getCantidadTotal() {
        return items.values().stream().mapToInt(ItemCarrito::getCantidad).sum();
    }

    public BigDecimal getTotal() {
        return items.values().stream().map(ItemCarrito::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
