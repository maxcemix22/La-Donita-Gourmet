package com.ladonita.modelo.entidades;

/** Roles de la tabla Usuario. */
public enum Rol {
    ADMIN("admin", "Administrador"),
    EMPLEADO("empleado", "Empleado"),
    REPARTIDOR("repartidor", "Repartidor");

    private final String valorBD;
    private final String etiqueta;

    Rol(String valorBD, String etiqueta) {
        this.valorBD = valorBD;
        this.etiqueta = etiqueta;
    }

    public String getValorBD() {
        return valorBD;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static Rol desdeBD(String valor) {
        for (Rol r : values()) {
            if (r.valorBD.equalsIgnoreCase(valor)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Rol desconocido: " + valor);
    }
}
