package Modelo;

import java.util.List;

/** Pedidos: RF02/RF03 (registrar), RF04 (listar por fecha), RF05 (estado) y reportes R02, R06, R07. */
public interface PedidoDAO {
    boolean registrar(Pedido pedido);                          // inserta pedido, detalles y datos de evento
    List<Pedido> listarPorFecha();                             // RF04
    List<Pedido> listarPorEstado(EstadoPedido estado);         // R02
    List<Pedido> listarPorRepartidor(String idRepartidor);     // R07
    boolean actualizarEstado(String idPedido, EstadoPedido nuevoEstado); // RF05
    boolean asignarRepartidor(String idPedido, String idRepartidor);
}
