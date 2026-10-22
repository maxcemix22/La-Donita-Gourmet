package DAO;

/** Error de acceso a datos (envuelve las SQLException para no obligar a manejarlas en cada capa). */
public class DAOException extends RuntimeException {
    public DAOException(String mensaje) { super(mensaje); }
    public DAOException(String mensaje, Throwable causa) { super(mensaje, causa); }
}
