package DAO;

import Modelo.EstadoPedido;
import Modelo.Pedido;
import java.util.List;

/*
 * MOVIDO de Modelo a DAO. Se agregó buscarPorId().
 * Pedidos: RF02/RF03 (registrar), RF04 (listar por fecha), RF05 (estado) y reportes R02, R07.
 * registrar() descuenta el stock de cada producto y guarda pedido, detalles y datos del evento en una sola transacción.
 */
public interface PedidoDAO {
    boolean registrar(Pedido pedido);
    Pedido buscarPorId(String idPedido);
    List<Pedido> listarPorFecha();                                       // RF04: más recientes primero
    List<Pedido> listarPorEstado(EstadoPedido estado);                   // R02
    List<Pedido> listarPorRepartidor(String idRepartidor);               // R07
    boolean actualizarEstado(String idPedido, EstadoPedido nuevoEstado); // RF05 (valida el flujo de estados)
    boolean asignarRepartidor(String idPedido, String idRepartidor);
}
