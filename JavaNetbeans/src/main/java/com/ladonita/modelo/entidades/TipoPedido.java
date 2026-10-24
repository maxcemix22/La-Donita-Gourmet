package com.ladonita.modelo.entidades;

public enum TipoPedido {
    NORMAL("normal", "Normal"),
    CATERING("catering", "Catering / evento");

    private final String valorBD;
    private final String etiqueta;

    TipoPedido(String valorBD, String etiqueta) {
        this.valorBD = valorBD;
        this.etiqueta = etiqueta;
    }

    public String getValorBD() {
        return valorBD;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static TipoPedido desdeBD(String valor) {
        for (TipoPedido t : values()) {
            if (t.valorBD.equalsIgnoreCase(valor)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Tipo de pedido desconocido: " + valor);
    }
}
