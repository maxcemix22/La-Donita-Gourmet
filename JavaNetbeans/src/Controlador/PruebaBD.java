package Controlador;

import DAO.CategoriaDAO;
import DAO.ClienteDAO;
import DAO.PedidoDAO;
import DAO.ProductoDAO;
import DAO.UsuarioDAO;
import DAO.impl.CategoriaDAOImpl;
import DAO.impl.ClienteDAOImpl;
import DAO.impl.PedidoDAOImpl;
import DAO.impl.ProductoDAOImpl;
import DAO.impl.UsuarioDAOImpl;
import Modelo.Categoria;
import Modelo.Cliente;
import Modelo.DatosEvento;
import Modelo.DetallePedido;
import Modelo.EstadoPedido;
import Modelo.Pedido;
import Modelo.Producto;
import Modelo.Repartidor;
import Modelo.Usuario;
import Util.Contrasenas;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Scanner;

/**
 * NUEVO: prueba de los DAO contra la base de datos real (necesita db.properties y el driver JDBC de MySQL).
 * OJO: INSERTA datos (un cliente, un pedido, un usuario y descuenta stock). Úsala en una base de pruebas.
 */
public class PruebaBD {
    public static void main(String[] args) {
        System.out.println("Esta prueba inserta datos en la base configurada en db.properties.");
        System.out.print("Escribe SI para continuar: ");
        if (!new Scanner(System.in).nextLine().trim().equalsIgnoreCase("SI")) {
            System.out.println("Cancelado.");
            return;
        }

        CategoriaDAO categorias = new CategoriaDAOImpl();
        ProductoDAO productos = new ProductoDAOImpl();
        ClienteDAO clientes = new ClienteDAOImpl();
        PedidoDAO pedidos = new PedidoDAOImpl();
        UsuarioDAO usuarios = new UsuarioDAOImpl();

        System.out.println("\n== Catálogo ==");
        for (Categoria c : categorias.listarActivas()) System.out.println("Categoría: " + c.getNombre());
        List<Producto> catalogo = productos.listarActivos();
        for (Producto p : catalogo) System.out.println(p.getIdProducto() + " | " + p.getNombre() + " | S/ " + p.getPrecio() + " | stock " + p.getStock());
        if (catalogo.isEmpty()) { System.out.println("No hay productos activos."); return; }

        System.out.println("\n== Cliente ==");
        Cliente cliente = clientes.buscarPorTelefono("900000001");
        if (cliente == null) {
            cliente = new Cliente(null, "Cliente", "De Prueba", "900000001", null, null);
            clientes.insertar(cliente);
        }
        System.out.println("Cliente " + cliente.getIdCliente() + ": " + cliente.getNombres() + " " + cliente.getApellidos());

        System.out.println("\n== Registrar pedido de catering con delivery ==");
        Producto prod = catalogo.get(0);
        Pedido pedido = new Pedido(null, cliente, LocalDateTime.now(), Pedido.TIPO_CATERING, Pedido.ENTREGA_DELIVERY, "Av. Prueba 123");
        pedido.agregarDetalle(new DetallePedido(null, prod, 2));
        pedido.setDatosEvento(new DatosEvento(LocalDate.now().plusDays(7), LocalTime.of(18, 0), "Reunión", 10, "Av. Prueba 123", null));
        pedidos.registrar(pedido);
        System.out.println("Pedido " + pedido.getIdPedido() + " | total S/ " + pedido.getTotal()
                + " | stock de " + prod.getNombre() + ": " + prod.getStock() + " -> " + productos.buscarPorId(prod.getIdProducto()).getStock());

        System.out.println("\n== Repartidor y estados ==");
        List<Repartidor> reps = usuarios.listarRepartidoresActivos();
        if (!reps.isEmpty()) {
            pedidos.asignarRepartidor(pedido.getIdPedido(), reps.get(0).getId());
            System.out.println("Asignado a " + reps.get(0).getNombres() + " (" + reps.get(0).getZonaReparto() + ")");
        }
        pedidos.actualizarEstado(pedido.getIdPedido(), EstadoPedido.EN_PREPARACION);
        pedidos.actualizarEstado(pedido.getIdPedido(), EstadoPedido.EN_CAMINO);
        System.out.println("Estado actual: " + pedidos.buscarPorId(pedido.getIdPedido()).getEstado().getEtiqueta());
        try {
            pedidos.actualizarEstado(pedido.getIdPedido(), EstadoPedido.PENDIENTE);
        } catch (IllegalStateException e) {
            System.out.println("Bloqueado: " + e.getMessage());
        }
        System.out.println("Pedidos en camino: " + pedidos.listarPorEstado(EstadoPedido.EN_CAMINO).size()
                + " | total de pedidos: " + pedidos.listarPorFecha().size());

        System.out.println("\n== Stock insuficiente ==");
        Pedido grande = new Pedido(null, cliente, LocalDateTime.now(), Pedido.TIPO_NORMAL, Pedido.ENTREGA_LOCAL, null);
        grande.agregarDetalle(new DetallePedido(null, prod, prod.getStock() + 1000));
        try {
            pedidos.registrar(grande);
        } catch (IllegalStateException e) {
            System.out.println("Rechazado: " + e.getMessage());
        }

        System.out.println("\n== Usuario con contraseña hasheada ==");
        String login = "prueba" + (System.currentTimeMillis() % 100000);
        Usuario nuevo = new Usuario(null, "Usuario", "Prueba", String.format("%08d", System.currentTimeMillis() % 100000000),
                login, Usuario.ROL_ADMIN, "900000002", null, true);
        usuarios.insertar(nuevo, Contrasenas.hash("Clave-123"));
        System.out.println("Insertado " + nuevo.getId() + " | login correcto: " + usuarios.autenticar(login, "Clave-123")
                + " | login con clave mala: " + usuarios.autenticar(login, "otra"));
    }
}
