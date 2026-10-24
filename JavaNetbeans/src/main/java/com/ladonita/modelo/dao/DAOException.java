package com.ladonita.modelo.dao;

/** Error de acceso a datos. Envuelve SQLException para no acoplar los controladores a JDBC. */
public class DAOException extends RuntimeException {

    public DAOException(String mensaje) {
        super(mensaje);
    }

    public DAOException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
