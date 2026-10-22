package Controlador;

import Modelo.Categoria;
import Modelo.Cliente;
import Modelo.DatosEvento;
import Modelo.DetallePedido;
import Modelo.EstadoPedido;
import Modelo.Pedido;
import Modelo.Producto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * NUEVO: clase principal de demostración (antes el proyecto no tenía main y no se podía ejecutar).
 * Arma el pedido de ejemplo de la BD (PED001: catering con delivery) y recorre su flujo de estados
 * sin conexión a base de datos.
 */
public class Main {
    public static void main(String[] args) {
        Categoria catering = new Categoria("CAT002", "Catering", "Servicio de catering para eventos", true);
        Producto box = new Producto("PRO002", catering, "Box de catering básico",
                "Box de bocaditos surtidos para eventos", new BigDecimal("15.00"), null, 50, true);
        Cliente cliente = new Cliente("CLI001", "Maria", "Lorenzo", "987654321",
                "mariloren@gmail.com", "Av. Los Olivos 123, Lima");

        Pedido pedido = new Pedido("PED001", cliente, LocalDateTime.now(),
                Pedido.TIPO_CATERING, Pedido.ENTREGA_DELIVERY, "Av. Los Olivos 123, Lima");
        pedido.agregarDetalle(new DetallePedido("DET001", box, 6));
        pedido.setDatosEvento(new DatosEvento(LocalDate.of(2026, 10, 5), LocalTime.of(18, 0),
                "Cumpleaños", 30, "Av. Los Olivos 123, Lima", null));

        System.out.println("Pedido " + pedido.getIdPedido() + " de " + cliente.getNombres() + " " + cliente.getApellidos());
        System.out.println("Productos: " + pedido.getDetalles().size() + " | Total: S/ " + pedido.getTotal());
        System.out.println("Estado inicial: " + pedido.getEstado().getEtiqueta());

        pedido.cambiarEstado(EstadoPedido.EN_PREPARACION);
        pedido.cambiarEstado(EstadoPedido.EN_CAMINO);
        pedido.cambiarEstado(EstadoPedido.ENTREGADO);
        System.out.println("Estado final: " + pedido.getEstado().getEtiqueta());

        try {
            pedido.cambiarEstado(EstadoPedido.PENDIENTE);
        } catch (IllegalStateException e) {
            System.out.println("Transición bloqueada: " + e.getMessage());
        }
    }
}
