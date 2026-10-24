package com.ladonita.modelo.dao;

import com.ladonita.modelo.entidades.Categoria;
import java.util.List;

public interface CategoriaDAO {

    /** Categorías activas (catálogo público). */
    List<Categoria> listar();

    /** Todas las categorías con la cantidad de productos de cada una (panel). */
    List<Categoria> listarTodas();

    Categoria buscarPorId(String id);

    boolean insertar(Categoria c);

    boolean actualizar(Categoria c);

    boolean cambiarEstado(String id, boolean activa);
}
