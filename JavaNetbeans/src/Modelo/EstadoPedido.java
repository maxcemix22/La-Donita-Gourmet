package Modelo;

/*
 * MODIFICADO (alineación con el informe y la BD ladonita_gourmet):
 * - Enum NUEVO con los estados de la BD (pendiente, en preparación, en camino, entregado).
 */

/** Estados del pedido (enum estado_pedido de la BD, Tabla 8 del informe). */
public enum EstadoPedido {
    PENDIENTE("pendiente"),
    EN_PREPARACION("en preparación"),
    EN_CAMINO("en camino"),
    ENTREGADO("entregado");

    private final String etiqueta;

    EstadoPedido(String etiqueta) { this.etiqueta = etiqueta; }

    /** Valor exacto guardado en MySQL. */
    public String getEtiqueta() { return etiqueta; }

    public static EstadoPedido desdeEtiqueta(String etiqueta) {
        for (EstadoPedido e : values()) {
            if (e.etiqueta.equals(etiqueta)) return e;
        }
        throw new IllegalArgumentException("Estado de pedido no válido: " + etiqueta);
    }
}
