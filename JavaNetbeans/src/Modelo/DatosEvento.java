package Modelo;

import java.time.LocalDate;
import java.time.LocalTime;

/** Datos adicionales que solo aplican a pedidos de tipo catering (RF03). */
public class DatosEvento {
    private LocalDate fechaEvento;
    private LocalTime horaEvento;       // opcional
    private String tipoServicio;
    private int cantidadPersonas;
    private String direccionEvento;
    private String observacionesEvento; // opcional

    public DatosEvento(LocalDate fechaEvento, LocalTime horaEvento, String tipoServicio,
                       int cantidadPersonas, String direccionEvento, String observacionesEvento) {
        if (fechaEvento == null) throw new IllegalArgumentException("La fecha del evento es obligatoria");
        if (tipoServicio == null || tipoServicio.isBlank()) throw new IllegalArgumentException("El tipo de servicio es obligatorio");
        if (cantidadPersonas <= 0) throw new IllegalArgumentException("La cantidad de personas debe ser mayor a 0");
        if (direccionEvento == null || direccionEvento.isBlank()) throw new IllegalArgumentException("La dirección del evento es obligatoria");
        this.fechaEvento = fechaEvento;
        this.horaEvento = horaEvento;
        this.tipoServicio = tipoServicio;
        this.cantidadPersonas = cantidadPersonas;
        this.direccionEvento = direccionEvento;
        this.observacionesEvento = observacionesEvento;
    }

    public LocalDate getFechaEvento() { return fechaEvento; }
    public LocalTime getHoraEvento() { return horaEvento; }
    public String getTipoServicio() { return tipoServicio; }
    public int getCantidadPersonas() { return cantidadPersonas; }
    public String getDireccionEvento() { return direccionEvento; }
    public String getObservacionesEvento() { return observacionesEvento; }
}
