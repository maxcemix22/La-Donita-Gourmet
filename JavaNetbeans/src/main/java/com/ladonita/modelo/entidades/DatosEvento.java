package com.ladonita.modelo.entidades;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/** Datos adicionales de un pedido de catering. */
public class DatosEvento implements Serializable {

    private LocalDate fechaEvento;
    private LocalTime horaEvento;
    private String tipoServicio;
    private int cantidadPersonas;
    private String direccionEvento;
    private String observaciones;

    public LocalDate getFechaEvento() { return fechaEvento; }
    public void setFechaEvento(LocalDate fechaEvento) { this.fechaEvento = fechaEvento; }
    public LocalTime getHoraEvento() { return horaEvento; }
    public void setHoraEvento(LocalTime horaEvento) { this.horaEvento = horaEvento; }
    public String getTipoServicio() { return tipoServicio; }
    public void setTipoServicio(String tipoServicio) { this.tipoServicio = tipoServicio; }
    public int getCantidadPersonas() { return cantidadPersonas; }
    public void setCantidadPersonas(int cantidadPersonas) { this.cantidadPersonas = cantidadPersonas; }
    public String getDireccionEvento() { return direccionEvento; }
    public void setDireccionEvento(String direccionEvento) { this.direccionEvento = direccionEvento; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    /** Mes abreviado en español (ene, feb...) para las tarjetas de fecha del panel. */
    public String getMesCorto() {
        return fechaEvento == null ? "" : fechaEvento.getMonth()
                .getDisplayName(java.time.format.TextStyle.SHORT, new java.util.Locale("es", "PE")).replace(".", "");
    }

    public int getDiaEvento() {
        return fechaEvento == null ? 0 : fechaEvento.getDayOfMonth();
    }

    public String getFechaEventoTexto() {
        if (fechaEvento == null) {
            return "";
        }
        String f = fechaEvento.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        return horaEvento == null ? f : f + " " + horaEvento.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}
