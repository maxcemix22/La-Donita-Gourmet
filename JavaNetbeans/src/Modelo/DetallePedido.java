/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

/**
 *
 * @author Administrator
 */
import java.math.BigDecimal;
public class DetallePedido {
     private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    // Relaciones del diagrama
    private Pedido pedido;
    private Producto producto;

    // Constructor
    public DetallePedido(int cantidad, BigDecimal precioUnitario, BigDecimal subtotal, 
                         Pedido pedido, Producto producto) {
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal; // Lógica opcional: puedes calcularlo como precioUnitario * cantidad
        this.pedido = pedido;
        this.producto = producto;
    }

}
