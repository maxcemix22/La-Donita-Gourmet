package com.ladonita.modelo.entidades;

/** Estados de un pedido, en el orden en que avanzan. */
public enum EstadoPedido {
    PENDIENTE("pendiente", "Pendiente", "warning"),
    EN_PREPARACION("en preparación", "En preparación", "info"),
    EN_CAMINO("en camino", "En camino", "primary"),
    ENTREGADO("entregado", "Entregado", "success");

    private final String valorBD;
    private final String etiqueta;
    private final String color;

    EstadoPedido(String valorBD, String etiqueta, String color) {
        this.valorBD = valorBD;
        this.etiqueta = etiqueta;
        this.color = color;
    }

    public String getValorBD() {
        return valorBD;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /** Clase de color de Bootstrap usada en las etiquetas de la vista. */
    public String getColor() {
        return color;
    }

    public String getNombre() {
        return name();
    }

    /** Siguiente estado del flujo, o null si ya fue entregado. */
    public EstadoPedido getSiguiente() {
        int i = ordinal() + 1;
        return i < values().length ? values()[i] : null;
    }

    public static EstadoPedido desdeBD(String valor) {
        for (EstadoPedido e : values()) {
            if (e.valorBD.equalsIgnoreCase(valor)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Estado desconocido: " + valor);
    }

    public static EstadoPedido desdeNombre(String nombre) {
        try {
            return nombre == null ? null : valueOf(nombre);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
