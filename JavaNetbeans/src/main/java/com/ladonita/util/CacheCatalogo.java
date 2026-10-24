package com.ladonita.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.ladonita.modelo.entidades.Categoria;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Caché (Google Guava) de las categorías activas que muestra el catálogo público.
 * Cada visita a la tienda las necesita y cambian muy poco: se guardan 5 minutos y el
 * mantenimiento de categorías invalida la caché al crear, editar o desactivar una.
 */
public final class CacheCatalogo {

    /** Carga las categorías desde la BD cuando la caché está vacía. */
    @FunctionalInterface
    public interface Cargador {
        List<Categoria> cargar() throws Exception;
    }

    private static final String CLAVE = "activas";
    private static final Cache<String, List<Categoria>> CATEGORIAS = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(1)
            .build();

    private CacheCatalogo() {
    }

    public static List<Categoria> categoriasActivas(Cargador cargador) {
        try {
            // ImmutableList: nadie puede modificar por error la lista compartida entre peticiones.
            return CATEGORIAS.get(CLAVE, () -> ImmutableList.copyOf(cargador.cargar()));
        } catch (ExecutionException | UncheckedExecutionException e) {
            Throwable causa = e.getCause();
            throw causa instanceof RuntimeException r ? r : new IllegalStateException(causa);
        }
    }

    public static void invalidar() {
        CATEGORIAS.invalidateAll();
    }
}
