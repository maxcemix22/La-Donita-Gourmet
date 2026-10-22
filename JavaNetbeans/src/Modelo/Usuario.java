package Modelo;

/*
 * MODIFICADO (alineación con el informe y la BD ladonita_gourmet):
 * - Se agregaron nombres, apellidos, dni, telefono, correo y estado (según la tabla usuario).
 * - Valida rol (admin/empleado/repartidor) y DNI de 8 dígitos. Se agregaron getters.
 * - La contraseña NO se guarda en el objeto; la maneja solo UsuarioDAO (hash).
 * - setId(): lo usa UsuarioDAO para asignar el id generado al insertar.
 */

/**
 * Usuario del sistema (admin, empleado o repartidor).
 * La contraseña NO se guarda en el objeto: la maneja solo el DAO (ver UsuarioDAO).
 */
public class Usuario {
    public static final String ROL_ADMIN = "admin";
    public static final String ROL_EMPLEADO = "empleado";
    public static final String ROL_REPARTIDOR = "repartidor";

    protected String id;
    protected String nombres;
    protected String apellidos;
    protected String dni;
    protected String usuario;
    protected String rol;
    protected String telefono;
    protected String correo;
    protected boolean estado;

    public Usuario(String id, String nombres, String apellidos, String dni, String usuario,
                   String rol, String telefono, String correo, boolean estado) {
        if (!ROL_ADMIN.equals(rol) && !ROL_EMPLEADO.equals(rol) && !ROL_REPARTIDOR.equals(rol)) {
            throw new IllegalArgumentException("Rol no válido: " + rol);
        }
        if (dni == null || !dni.matches("\\d{8}")) throw new IllegalArgumentException("El DNI debe tener 8 dígitos");
        if (usuario == null || usuario.isBlank()) throw new IllegalArgumentException("Usuario obligatorio");
        this.id = id;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.dni = dni;
        this.usuario = usuario;
        this.rol = rol;
        this.telefono = telefono;
        this.correo = correo;
        this.estado = estado;
    }

    /** Lo asigna el DAO al insertar (los ids los genera la capa de datos). */
    public void setId(String id) { this.id = id; }

    public String getId() { return id; }
    public String getNombres() { return nombres; }
    public String getApellidos() { return apellidos; }
    public String getDni() { return dni; }
    public String getUsuario() { return usuario; }
    public String getRol() { return rol; }
    public String getTelefono() { return telefono; }
    public String getCorreo() { return correo; }
    public boolean isEstado() { return estado; }
}
