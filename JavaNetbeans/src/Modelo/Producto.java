package Modelo;

import java.math.BigDecimal;

public class Producto {
    private String idProducto;
    private Categoria categoria;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private String fotoUrl;
    private int stock;
    private boolean estado;

    public Producto(String idProducto, Categoria categoria, String nombre, String descripcion,
                    BigDecimal precio, String fotoUrl, int stock, boolean estado) {
        if (precio == null || precio.signum() < 0) throw new IllegalArgumentException("El precio no puede ser negativo");
        if (stock < 0) throw new IllegalArgumentException("El stock no puede ser negativo");
        this.idProducto = idProducto;
        this.categoria = categoria;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.fotoUrl = fotoUrl;
        this.stock = stock;
        this.estado = estado;
    }

    public String getIdProducto() { return idProducto; }
    public Categoria getCategoria() { return categoria; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public String getFotoUrl() { return fotoUrl; }
    public int getStock() { return stock; }
    public boolean isEstado() { return estado; }
}
