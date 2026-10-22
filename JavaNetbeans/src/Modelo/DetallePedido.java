package Modelo;

/*
 * MODIFICADO (alineación con el informe y la BD ladonita_gourmet):
 * - Se agregó idDetalle y getters. El constructor ahora recibe (idDetalle, producto, cantidad).
 * - El precio unitario se congela desde el producto y el subtotal se calcula (precio x cantidad).
 * - Valida cantidad > 0 (CHECK de la BD). La referencia al pedido la asigna Pedido.agregarDetalle.
 * - desdeBD(): reconstruye un detalle guardado con su precio histórico (lo usa PedidoDAO).
 * - setIdDetalle(): lo usa PedidoDAO para asignar el id generado al registrar.
 */

import java.math.BigDecimal;

public class DetallePedido {
    private String idDetalle;
    private Pedido pedido;          // se asigna en Pedido.agregarDetalle
    private Producto producto;
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    /** El precio unitario se congela al momento del pedido (Tabla 9 del informe). */
    public DetallePedido(String idDetalle, Producto producto, int cantidad) {
        if (producto == null) throw new IllegalArgumentException("Producto obligatorio");
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        this.idDetalle = idDetalle;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = producto.getPrecio();
        this.subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    /** Reconstruye un detalle ya guardado, respetando el precio unitario que tenía al momento del pedido. */
    public static DetallePedido desdeBD(String idDetalle, Producto producto, int cantidad,
                                        BigDecimal precioUnitario, BigDecimal subtotal) {
        DetallePedido d = new DetallePedido(idDetalle, producto, cantidad);
        d.precioUnitario = precioUnitario;
        d.subtotal = subtotal;
        return d;
    }

    /** Lo asigna el DAO al registrar el pedido. */
    public void setIdDetalle(String idDetalle) { this.idDetalle = idDetalle; }

    void setPedido(Pedido pedido) { this.pedido = pedido; }

    public String getIdDetalle() { return idDetalle; }
    public Pedido getPedido() { return pedido; }
    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
}
