package com.ladonita.modelo.dao;

import com.ladonita.modelo.entidades.Rol;
import com.ladonita.modelo.entidades.Usuario;
import java.util.List;

public interface UsuarioDAO {

    /** Devuelve el usuario (incluido el hash de su clave) o null si no existe. */
    Usuario buscarPorUsuario(String usuario);

    /** Usuario con sus datos de Empleado o Repartidor según el rol. */
    Usuario buscarPorId(String id);

    List<Usuario> listar();

    List<Usuario> listarPorRol(Rol rol);

    /** Inserta el usuario (y su fila en Empleado/Repartidor). La clave debe venir hasheada en claveHash. */
    boolean insertar(Usuario u);

    /** Actualiza datos personales, rol y datos laborales (no la contraseña). */
    boolean actualizar(Usuario u);

    boolean cambiarEstado(String id, boolean activo);

    boolean cambiarClave(String id, String claveHash);

    boolean existeUsuario(String usuario, String excluirId);

    boolean existeDni(String dni, String excluirId);
}
