package com.ladonita.modelo.entidades;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Pedido implements Serializable {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private String idPedido;
    private Cliente cliente;
    private Usuario repartidor;
    private LocalDateTime fecha;
    private LocalDateTime fechaEntrega;
    private EstadoPedido estado = EstadoPedido.PENDIENTE;
    private TipoPedido tipo = TipoPedido.NORMAL;
    private MetodoEntrega metodoEntrega = MetodoEntrega.RECOJO;
    private String direccionEntrega;
    private BigDecimal total = BigDecimal.ZERO;
    private String observaciones;
    private List<DetallePedido> detalles = new ArrayList<>();
    private DatosEvento evento;

    public Pedido() {
    }

    public void agregarDetalle(DetallePedido d) {
        detalles.add(d);
        total = total.add(d.getSubtotal());
    }

    public String getIdPedido() { return idPedido; }
    public void setIdPedido(String idPedido) { this.idPedido = idPedido; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public Usuario getRepartidor() { return repartidor; }
    public void setRepartidor(Usuario repartidor) { this.repartidor = repartidor; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public LocalDateTime getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDateTime fechaEntrega) { this.fechaEntrega = fechaEntrega; }
    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }
    public TipoPedido getTipo() { return tipo; }
    public void setTipo(TipoPedido tipo) { this.tipo = tipo; }
    public MetodoEntrega getMetodoEntrega() { return metodoEntrega; }
    public void setMetodoEntrega(MetodoEntrega metodoEntrega) { this.metodoEntrega = metodoEntrega; }
    public String getDireccionEntrega() { return direccionEntrega; }
    public void setDireccionEntrega(String direccionEntrega) { this.direccionEntrega = direccionEntrega; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public List<DetallePedido> getDetalles() { return detalles; }
    public void setDetalles(List<DetallePedido> detalles) { this.detalles = detalles; }
    public DatosEvento getEvento() { return evento; }
    public void setEvento(DatosEvento evento) { this.evento = evento; }

    public String getFechaTexto() {
        return fecha == null ? "" : fecha.format(FORMATO);
    }

    public String getFechaEntregaTexto() {
        return fechaEntrega == null ? "Por coordinar" : fechaEntrega.format(FORMATO);
    }

    public boolean isCatering() {
        return tipo == TipoPedido.CATERING;
    }

    public boolean isDelivery() {
        return metodoEntrega == MetodoEntrega.DELIVERY;
    }
}
