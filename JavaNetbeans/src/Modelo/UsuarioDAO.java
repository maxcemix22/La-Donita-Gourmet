package Modelo;

/** Autenticación y registro de usuarios (RNF04). La contraseña se almacena como hash, nunca en texto plano. */
public interface UsuarioDAO {
    Usuario buscarPorUsuario(String usuario);
    boolean autenticar(String usuario, String contrasena);
    boolean insertar(Usuario usuario, String contrasenaHash);
}
