package Modelo;

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

    void setPedido(Pedido pedido) { this.pedido = pedido; }

    public String getIdDetalle() { return idDetalle; }
    public Pedido getPedido() { return pedido; }
    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
}
