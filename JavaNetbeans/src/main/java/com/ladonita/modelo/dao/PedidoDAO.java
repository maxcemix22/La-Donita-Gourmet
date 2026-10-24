package com.ladonita.modelo.dao;

import com.ladonita.modelo.entidades.EstadoPedido;
import com.ladonita.modelo.entidades.Pedido;
import java.util.List;
import java.util.Map;

public interface PedidoDAO {

    /**
     * Inserta el pedido, sus detalles y los datos del evento (si es catering),
     * y descuenta el stock. Debe ejecutarse dentro de una transacción.
     */
    boolean insertar(Pedido p);

    /** Pedido completo: cliente, repartidor, detalles y evento. */
    Pedido buscarPorId(String id);

    List<Pedido> listar();

    List<Pedido> listarPorEstado(EstadoPedido estado);

    List<Pedido> listarPorRepartidor(String idRepartidor, EstadoPedido estado);

    List<Pedido> listarPorCliente(String idCliente);

    /**
     * Cambia el estado solo si el pedido sigue en {@code estadoActual} (evita que dos personas
     * lo cambien a la vez sobre datos desactualizados). Devuelve false si otro usuario ya lo cambió.
     */
    boolean actualizarEstado(String id, EstadoPedido estadoActual, EstadoPedido nuevo);

    boolean asignarRepartidor(String idPedido, String idRepartidor);

    Map<EstadoPedido, Integer> contarPorEstado();
}
