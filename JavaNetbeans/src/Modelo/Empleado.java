/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

/**
 *
 * @author Administrator
 */
public class Empleado extends Usuario {
   private String turno;
   private String areaAsignada;
   private float sueldo;

    public Empleado(String id, String nombre, String usuario, String rol, 
        String turno, String areaAsignada, float sueldo) { 
        super(id, nombre, usuario, rol);
        this.turno = turno;
        this.areaAsignada = areaAsignada;
        this.sueldo = sueldo;
    }
}
