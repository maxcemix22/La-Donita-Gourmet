/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

/**
 *
 * @author Administrator
 */
public class Usuario {
    protected String id;
    protected String nombre;
    protected String usuario;
    protected String rol;

    public Usuario(String id, String nombre, String usuario, String rol) {
        this.id = id;
        this.nombre = nombre;
        this.usuario = usuario;
        this.rol = rol;
    }
    
}
