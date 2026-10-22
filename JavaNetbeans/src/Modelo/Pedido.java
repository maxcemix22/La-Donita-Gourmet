package Modelo;

/*
 * MODIFICADO (alineación con el informe y la BD ladonita_gourmet):
 * - Se agregaron repartidor, metodoEntrega, direccionEntrega, observaciones, datosEvento y lista de detalles.
 * - fechaEntreega (typo, LocalDate) pasó a fechaEntregaEstimada (LocalDateTime, como en la BD).
 * - estado ahora usa el enum EstadoPedido. agregarDetalle() recalcula el total.
 * - cambiarEstado() valida el flujo pendiente > en preparación > en camino (solo delivery) > entregado.
 * - Valida tipo, método de entrega y dirección obligatoria en delivery.
 * - Si no se indica fecha, usa la fecha y hora actual.
 * - restaurarEstado() y setIdPedido(): los usa PedidoDAO al cargar o registrar pedidos.
 */

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Pedido {
    public static final String TIPO_NORMAL = "normal";
    public static final String TIPO_CATERING = "catering";
    public static final String ENTREGA_LOCAL = "recojo en local";
    public static final String ENTREGA_DELIVERY = "delivery";

    private String idPedido;
    private Cliente cliente;
    private Repartidor repartidor;               // solo si es delivery
    private LocalDateTime fecha;
    private LocalDateTime fechaEntregaEstimada;
    private EstadoPedido estado = EstadoPedido.PENDIENTE;
    private String tipo;
    private String metodoEntrega;
    private String direccionEntrega;
    private String observaciones;
    private DatosEvento datosEvento;             // solo si tipo = catering
    private final List<DetallePedido> detalles = new ArrayList<>();
    private BigDecimal total = BigDecimal.ZERO;

    public Pedido(String idPedido, Cliente cliente, LocalDateTime fecha, String tipo,
                  String metodoEntrega, String direccionEntrega) {
        if (cliente == null) throw new IllegalArgumentException("Cliente obligatorio");
        if (!TIPO_NORMAL.equals(tipo) && !TIPO_CATERING.equals(tipo)) throw new IllegalArgumentException("Tipo de pedido no válido: " + tipo);
        if (!ENTREGA_LOCAL.equals(metodoEntrega) && !ENTREGA_DELIVERY.equals(metodoEntrega)) throw new IllegalArgumentException("Método de entrega no válido: " + metodoEntrega);
        if (ENTREGA_DELIVERY.equals(metodoEntrega) && (direccionEntrega == null || direccionEntrega.isBlank())) {
            throw new IllegalArgumentException("El delivery requiere dirección de entrega");
        }
        this.idPedido = idPedido;
        this.cliente = cliente;
        this.fecha = fecha != null ? fecha : LocalDateTime.now();
        this.tipo = tipo;
        this.metodoEntrega = metodoEntrega;
        this.direccionEntrega = direccionEntrega;
    }

    public void agregarDetalle(DetallePedido d) {
        d.setPedido(this);
        detalles.add(d);
        total = total.add(d.getSubtotal());
    }

    public void setDatosEvento(DatosEvento datosEvento) {
        if (datosEvento != null && !TIPO_CATERING.equals(tipo)) {
            throw new IllegalStateException("Solo un pedido de catering puede tener datos de evento");
        }
        this.datosEvento = datosEvento;
    }

    /** Asigna el repartidor (solo delivery, lo hace el administrador). */
    public void asignarRepartidor(Repartidor r) {
        if (!ENTREGA_DELIVERY.equals(metodoEntrega)) throw new IllegalStateException("El pedido no es delivery");
        this.repartidor = r;
    }

    /**
     * Flujo: pendiente → en preparación → (en camino, solo delivery) → entregado.
     * Para recojo en local pasa directo de en preparación a entregado.
     */
    public void cambiarEstado(EstadoPedido nuevo) {
        boolean ok = switch (estado) {
            case PENDIENTE -> nuevo == EstadoPedido.EN_PREPARACION;
            case EN_PREPARACION -> ENTREGA_DELIVERY.equals(metodoEntrega)
                    ? nuevo == EstadoPedido.EN_CAMINO : nuevo == EstadoPedido.ENTREGADO;
            case EN_CAMINO -> nuevo == EstadoPedido.ENTREGADO;
            case ENTREGADO -> false;
        };
        if (!ok) throw new IllegalStateException("Transición no permitida: " + estado.getEtiqueta() + " → " + nuevo.getEtiqueta());
        estado = nuevo;
    }

    /** Lo asigna el DAO al registrar el pedido. */
    public void setIdPedido(String idPedido) { this.idPedido = idPedido; }

    /** Solo para el DAO: fija el estado que ya tiene el pedido guardado, sin validar la transición. */
    public void restaurarEstado(EstadoPedido estado) { this.estado = estado; }

    public void setFechaEntregaEstimada(LocalDateTime f) { this.fechaEntregaEstimada = f; }
    public void setObservaciones(String o) { this.observaciones = o; }

    public String getIdPedido() { return idPedido; }
    public Cliente getCliente() { return cliente; }
    public Repartidor getRepartidor() { return repartidor; }
    public LocalDateTime getFecha() { return fecha; }
    public LocalDateTime getFechaEntregaEstimada() { return fechaEntregaEstimada; }
    public EstadoPedido getEstado() { return estado; }
    public String getTipo() { return tipo; }
    public String getMetodoEntrega() { return metodoEntrega; }
    public String getDireccionEntrega() { return direccionEntrega; }
    public String getObservaciones() { return observaciones; }
    public DatosEvento getDatosEvento() { return datosEvento; }
    public List<DetallePedido> getDetalles() { return Collections.unmodifiableList(detalles); }
    public BigDecimal getTotal() { return total; }
}
