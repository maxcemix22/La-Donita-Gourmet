package com.ladonita.modelo.entidades;

import java.io.Serializable;

public class Categoria implements Serializable {

    private String idCategoria;
    private String nombre;
    private String descripcion;
    private boolean estado;
    private int cantidadProductos;

    public Categoria() {
    }

    public int getCantidadProductos() { return cantidadProductos; }
    public void setCantidadProductos(int cantidadProductos) { this.cantidadProductos = cantidadProductos; }

    public Categoria(String idCategoria, String nombre) {
        this.idCategoria = idCategoria;
        this.nombre = nombre;
    }

    public String getIdCategoria() { return idCategoria; }
    public void setIdCategoria(String idCategoria) { this.idCategoria = idCategoria; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public boolean isEstado() { return estado; }
    public void setEstado(boolean estado) { this.estado = estado; }
}
