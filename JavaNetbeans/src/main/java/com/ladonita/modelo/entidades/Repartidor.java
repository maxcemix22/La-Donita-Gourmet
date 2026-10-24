package com.ladonita.modelo.entidades;

import java.time.LocalDate;

public class Repartidor extends Usuario {

    private LocalDate fechaIngresoLaboral;
    private String placaVehiculo;
    private String tipoVehiculo;
    private String zonaReparto;
    private String licencia;

    public LocalDate getFechaIngresoLaboral() { return fechaIngresoLaboral; }
    public void setFechaIngresoLaboral(LocalDate fechaIngresoLaboral) { this.fechaIngresoLaboral = fechaIngresoLaboral; }
    public String getPlacaVehiculo() { return placaVehiculo; }
    public void setPlacaVehiculo(String placaVehiculo) { this.placaVehiculo = placaVehiculo; }
    public String getTipoVehiculo() { return tipoVehiculo; }
    public void setTipoVehiculo(String tipoVehiculo) { this.tipoVehiculo = tipoVehiculo; }
    public String getZonaReparto() { return zonaReparto; }
    public void setZonaReparto(String zonaReparto) { this.zonaReparto = zonaReparto; }
    public String getLicencia() { return licencia; }
    public void setLicencia(String licencia) { this.licencia = licencia; }
}
