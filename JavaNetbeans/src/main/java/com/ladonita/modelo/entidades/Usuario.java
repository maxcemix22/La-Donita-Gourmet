package com.ladonita.modelo.entidades;

import java.io.Serializable;

/** Usuario interno del sistema (admin, empleado o repartidor). */
public class Usuario implements Serializable {

    private String idUsuario;
    private String nombres;
    private String apellidos;
    private String dni;
    private String usuario;
    private transient String claveHash; // no viaja en la sesión serializada
    private Rol rol;
    private String telefono;
    private String correo;
    private boolean estado;

    public Usuario() {
    }

    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getClaveHash() { return claveHash; }
    public void setClaveHash(String claveHash) { this.claveHash = claveHash; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public boolean isEstado() { return estado; }
    public void setEstado(boolean estado) { this.estado = estado; }

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    /** Nombre de la clase concreta (Usuario, Empleado o Repartidor), útil en las vistas. */
    public String getTipoClase() {
        return getClass().getSimpleName();
    }

    public boolean isAdmin() {
        return rol == Rol.ADMIN;
    }

    public boolean isRepartidor() {
        return rol == Rol.REPARTIDOR;
    }
}
