package Modelo;

/*
 * MODIFICADO (alineación con el informe y la BD ladonita_gourmet):
 * - Se agregaron descripcion y estado (columnas de la tabla categoria).
 * - Se agregaron getters y validación de id y nombre. Se mantiene el constructor corto anterior.
 */

public class Categoria {
    private String idCategoria;
    private String nombre;
    private String descripcion;
    private boolean estado;

    public Categoria(String idCategoria, String nombre, String descripcion, boolean estado) {
        if (idCategoria == null || idCategoria.isBlank()) throw new IllegalArgumentException("idCategoria obligatorio");
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("nombre obligatorio");
        this.idCategoria = idCategoria;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.estado = estado;
    }

    public Categoria(String idCategoria, String nombre) {
        this(idCategoria, nombre, null, true);
    }

    public String getIdCategoria() { return idCategoria; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public boolean isEstado() { return estado; }
}
