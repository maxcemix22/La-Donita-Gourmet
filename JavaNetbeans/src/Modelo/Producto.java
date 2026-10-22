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
public class Producto {
  private String idProducto;
    private String nombre;
    private BigDecimal precio;
    private int stock;
    private boolean estado;

    private Categoria categoria;

    
    public Producto(String idProducto, String nombre, BigDecimal precio, 
                    int stock, boolean estado, Categoria categoria) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.estado = estado;
        this.categoria = categoria;
    }  
}
