package Modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Empleado extends Usuario {
    private LocalDate fechaIngresoLaboral;
    private String turno;          // "mañana" o "tarde"
    private String areaAsignada;
    private BigDecimal sueldo;

    public Empleado(String id, String nombres, String apellidos, String dni, String usuario,
                    String telefono, String correo, boolean estado,
                    LocalDate fechaIngresoLaboral, String turno, String areaAsignada, BigDecimal sueldo) {
        super(id, nombres, apellidos, dni, usuario, ROL_EMPLEADO, telefono, correo, estado);
        if (!"mañana".equals(turno) && !"tarde".equals(turno)) throw new IllegalArgumentException("Turno no válido: " + turno);
        if (sueldo == null || sueldo.signum() < 0) throw new IllegalArgumentException("El sueldo no puede ser negativo");
        this.fechaIngresoLaboral = fechaIngresoLaboral;
        this.turno = turno;
        this.areaAsignada = areaAsignada;
        this.sueldo = sueldo;
    }

    public LocalDate getFechaIngresoLaboral() { return fechaIngresoLaboral; }
    public String getTurno() { return turno; }
    public String getAreaAsignada() { return areaAsignada; }
    public BigDecimal getSueldo() { return sueldo; }
}
