package DAO;

import Modelo.Repartidor;
import Modelo.Usuario;
import java.util.List;

/*
 * MOVIDO de Modelo a DAO. Autenticación y registro de usuarios (RNF04).
 * - buscarPorUsuario() devuelve Empleado o Repartidor según el rol.
 * - autenticar() compara contra el hash guardado (Util.Contrasenas); nunca se guarda texto plano.
 * - insertar() exige una contraseña ya hasheada con Util.Contrasenas.hash().
 * - listarRepartidoresActivos(): para que el administrador asigne pedidos.
 */
public interface UsuarioDAO {
    Usuario buscarPorUsuario(String usuario);
    boolean autenticar(String usuario, String contrasena);
    boolean insertar(Usuario usuario, String contrasenaHash);
    List<Repartidor> listarRepartidoresActivos();
}
