package Modelo;

/**
 * Cliente que realiza pedidos. Según la BD, teléfono es obligatorio y
 * correo/dirección son opcionales (el cliente no necesita registrarse, ver 6.2 del informe).
 */
public class Cliente {
    private String idCliente;
    private String nombres;
    private String apellidos;
    private String telefono;
    private String correo;
    private String direccionReferencia;

    public Cliente(String idCliente, String nombres, String apellidos, String telefono,
                   String correo, String direccionReferencia) {
        if (nombres == null || nombres.isBlank()) throw new IllegalArgumentException("Nombres obligatorios");
        if (apellidos == null || apellidos.isBlank()) throw new IllegalArgumentException("Apellidos obligatorios");
        this.idCliente = idCliente;
        this.nombres = nombres;
        this.apellidos = apellidos;
        setTelefono(telefono);
        this.correo = correo;
        this.direccionReferencia = direccionReferencia;
    }

    public String getIdCliente() { return idCliente; }
    public String getNombres() { return nombres; }
    public String getApellidos() { return apellidos; }
    public String getTelefono() { return telefono; }
    public String getCorreo() { return correo; }
    public String getDireccionReferencia() { return direccionReferencia; }

    public void setTelefono(String telefono) {
        if (telefono == null || telefono.isBlank()) throw new IllegalArgumentException("Teléfono obligatorio");
        this.telefono = telefono.trim();
    }

    public void setCorreo(String correo) { this.correo = correo; }
    public void setDireccionReferencia(String direccionReferencia) { this.direccionReferencia = direccionReferencia; }
}
