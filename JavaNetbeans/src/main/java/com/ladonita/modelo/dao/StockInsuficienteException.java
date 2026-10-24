package com.ladonita.modelo.dao;

/** Se lanza cuando un producto ya no tiene stock suficiente al confirmar el pedido. */
public class StockInsuficienteException extends DAOException {

    public StockInsuficienteException(String nombreProducto) {
        super("No hay stock suficiente de \"" + nombreProducto + "\". Ajusta la cantidad en tu carrito.");
    }
}
