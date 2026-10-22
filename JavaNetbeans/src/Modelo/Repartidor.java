package Modelo;

import java.time.LocalDate;

public class Repartidor extends Usuario {
    private LocalDate fechaIngresoLaboral;
    private String placaVehiculo;
    private String tipoVehiculo;   // "moto", "auto" o "bicicleta"
    private String zonaReparto;
    private String licenciaConducir;

    public Repartidor(String id, String nombres, String apellidos, String dni, String usuario,
                      String telefono, String correo, boolean estado,
                      LocalDate fechaIngresoLaboral, String placaVehiculo, String tipoVehiculo,
                      String zonaReparto, String licenciaConducir) {
        super(id, nombres, apellidos, dni, usuario, ROL_REPARTIDOR, telefono, correo, estado);
        if (!"moto".equals(tipoVehiculo) && !"auto".equals(tipoVehiculo) && !"bicicleta".equals(tipoVehiculo)) {
            throw new IllegalArgumentException("Tipo de vehículo no válido: " + tipoVehiculo);
        }
        this.fechaIngresoLaboral = fechaIngresoLaboral;
        this.placaVehiculo = placaVehiculo;
        this.tipoVehiculo = tipoVehiculo;
        this.zonaReparto = zonaReparto;
        this.licenciaConducir = licenciaConducir;
    }

    public LocalDate getFechaIngresoLaboral() { return fechaIngresoLaboral; }
    public String getPlacaVehiculo() { return placaVehiculo; }
    public String getTipoVehiculo() { return tipoVehiculo; }
    public String getZonaReparto() { return zonaReparto; }
    public String getLicenciaConducir() { return licenciaConducir; }
}
