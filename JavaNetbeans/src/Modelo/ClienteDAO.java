package Modelo;

/** Acceso a datos de clientes. Implementación pendiente (JDBC + MySQL). */
public interface ClienteDAO {
    Cliente buscarPorTelefono(String telefono);
    boolean insertar(Cliente cliente);
}
