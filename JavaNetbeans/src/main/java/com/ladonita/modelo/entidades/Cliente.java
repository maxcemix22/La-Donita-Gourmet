package com.ladonita.modelo.entidades;

import java.io.Serializable;
import java.time.LocalDate;

public class Cliente implements Serializable {

    private String idCliente;
    private String nombres;
    private String apellidos;
    private String telefono;
    private String correo;
    private String direccionReferencia;
    private LocalDate fechaRegistro;
    // Estadísticas (solo se llenan en el listado del panel, reporte R05)
    private int cantidadPedidos;
    private java.math.BigDecimal montoTotal = java.math.BigDecimal.ZERO;
    private java.time.LocalDateTime ultimoPedido;

    public Cliente() {
    }

    public int getCantidadPedidos() { return cantidadPedidos; }
    public void setCantidadPedidos(int cantidadPedidos) { this.cantidadPedidos = cantidadPedidos; }
    public java.math.BigDecimal getMontoTotal() { return montoTotal; }
    public void setMontoTotal(java.math.BigDecimal montoTotal) { this.montoTotal = montoTotal; }
    public java.time.LocalDateTime getUltimoPedido() { return ultimoPedido; }
    public void setUltimoPedido(java.time.LocalDateTime ultimoPedido) { this.ultimoPedido = ultimoPedido; }

    public String getUltimoPedidoTexto() {
        return ultimoPedido == null ? "—" : ultimoPedido.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String getFechaRegistroTexto() {
        return fechaRegistro == null ? "" : fechaRegistro.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String getIdCliente() { return idCliente; }
    public void setIdCliente(String idCliente) { this.idCliente = idCliente; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getDireccionReferencia() { return direccionReferencia; }
    public void setDireccionReferencia(String direccionReferencia) { this.direccionReferencia = direccionReferencia; }
    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }
}
