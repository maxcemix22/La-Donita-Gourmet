package com.ladonita.modelo.entidades;

import java.io.Serializable;
import java.math.BigDecimal;

public class Producto implements Serializable {

    private String idProducto;
    private Categoria categoria;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private int stock;
    private boolean estado;
    private String fotoUrl;

    /** Umbral de stock bajo (la BD no tiene columna de stock mínimo; igual que el reporte R04). */
    public static final int STOCK_MINIMO = 10;

    public Producto() {
    }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    /** true si la foto es una URL https externa (se usa tal cual, sin anteponer el contexto). */
    public boolean isFotoExterna() {
        return fotoUrl != null && fotoUrl.startsWith("https://");
    }

    /** Ruta de la foto: URL externa, foto subida (imagenes/...) o la foto local por código. */
    public String getFotoRuta() {
        if (fotoUrl != null && !fotoUrl.isBlank()) {
            return fotoUrl;
        }
        return "recursos/img/productos/" + idProducto + ".jpg";
    }

    public boolean isStockBajo() {
        return stock <= STOCK_MINIMO;
    }

    public String getIdProducto() { return idProducto; }
    public void setIdProducto(String idProducto) { this.idProducto = idProducto; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public boolean isEstado() { return estado; }
    public void setEstado(boolean estado) { this.estado = estado; }

    public boolean isDisponible() {
        return estado && stock > 0;
    }
}
