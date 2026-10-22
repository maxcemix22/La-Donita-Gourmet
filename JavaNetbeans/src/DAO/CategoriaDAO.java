package DAO;

import Modelo.Categoria;
import java.util.List;

/*
 * NUEVO: faltaba para poder mostrar los filtros por categoría del catálogo (RNF01).
 */
public interface CategoriaDAO {
    List<Categoria> listarActivas();
    Categoria buscarPorId(String idCategoria);
}
