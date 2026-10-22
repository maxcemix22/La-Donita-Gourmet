package DAO;

import Modelo.Cliente;

/*
 * MOVIDO de Modelo a DAO. Se agregó buscarPorId().
 * insertar() genera el id (CLI001, CLI002, ...) y lo asigna al objeto.
 */
public interface ClienteDAO {
    Cliente buscarPorId(String idCliente);
    Cliente buscarPorTelefono(String telefono);
    boolean insertar(Cliente cliente);
}
