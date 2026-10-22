/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

/**
 *
 * @author Administrator
 */
public class Repartidor extends Usuario{
    private String PlacaVehiculo;
    private String tipoVehiculo;
    private String zonaReparto;
    private String licencia;

    public Repartidor(String id, String nombre, String usuario, String rol,
    String PlacaVehiculo, String tipoVehiculo, String zonaReparto, String licencia ) {
        super(id, nombre, usuario, rol);
    
    this.PlacaVehiculo = PlacaVehiculo;
    this.tipoVehiculo = tipoVehiculo;
    this.zonaReparto = zonaReparto;
    this.licencia= licencia;
}
}