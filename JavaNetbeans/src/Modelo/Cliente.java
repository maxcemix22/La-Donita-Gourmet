/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

/**
 *
 * @author Administrator
 */
public class Cliente {
    private String nombre;
    private String id;
    private String Correo;
    private String telefono;

    public Cliente(String nombre, String telefono) {
        this.nombre = nombre;
        this.telefono = telefono;
    }
    
    
    public Cliente(String Correo){
        if (Correo == null || Correo.trim().isEmpty()){
        throw new IllegalArgumentException("El correo es Obligatorio");
    }
       this.Correo=Correo;
    }
   public String getCorreo(){
       return Correo;
   }
   public void setCorreo(String Correo){
       if (Correo == null  || Correo.trim().isEmpty()) {
           throw new IllegalArgumentException(" Correo Obligatorio ");         
       }
       this.Correo=Correo;
   }
   
} 
