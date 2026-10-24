package com.ladonita.modelo.entidades;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Empleado extends Usuario {

    private LocalDate fechaIngresoLaboral;
    private String turno;
    private String areaAsignada;
    private BigDecimal sueldo;

    public LocalDate getFechaIngresoLaboral() { return fechaIngresoLaboral; }
    public void setFechaIngresoLaboral(LocalDate fechaIngresoLaboral) { this.fechaIngresoLaboral = fechaIngresoLaboral; }
    public String getTurno() { return turno; }
    public void setTurno(String turno) { this.turno = turno; }
    public String getAreaAsignada() { return areaAsignada; }
    public void setAreaAsignada(String areaAsignada) { this.areaAsignada = areaAsignada; }
    public BigDecimal getSueldo() { return sueldo; }
    public void setSueldo(BigDecimal sueldo) { this.sueldo = sueldo; }
}
