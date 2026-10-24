package com.ladonita.modelo.dao;

import com.ladonita.modelo.entidades.Cliente;
import java.util.List;

public interface ClienteDAO {

    /** Inserta el cliente generando su código (CLI###) y lo asigna al objeto. */
    boolean insertar(Cliente c);

    boolean actualizar(Cliente c);

    Cliente buscarPorId(String id);

    Cliente buscarPorTelefono(String telefono);

    List<Cliente> listar();

    /** Clientes con su frecuencia de compra y monto facturado, filtrados por nombre/teléfono/correo. */
    List<Cliente> buscarConEstadisticas(String texto);
}
