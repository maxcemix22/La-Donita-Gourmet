package com.ladonita.modelo.entidades;

public enum MetodoEntrega {
    RECOJO("recojo en local", "Recojo en local"),
    DELIVERY("delivery", "Delivery");

    private final String valorBD;
    private final String etiqueta;

    MetodoEntrega(String valorBD, String etiqueta) {
        this.valorBD = valorBD;
        this.etiqueta = etiqueta;
    }

    public String getValorBD() {
        return valorBD;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static MetodoEntrega desdeBD(String valor) {
        for (MetodoEntrega m : values()) {
            if (m.valorBD.equalsIgnoreCase(valor)) {
                return m;
            }
        }
        throw new IllegalArgumentException("Método de entrega desconocido: " + valor);
    }
}
