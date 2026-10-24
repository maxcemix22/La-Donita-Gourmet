package com.ladonita.modelo.entidades;

import com.google.common.base.Preconditions;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Resultado genérico de un reporte: columnas con su tipo (para alinear y formatear en la vista
 * y exportar a CSV) y filas de valores.
 */
public class TablaReporte {

    /** Tipos de columna: texto, entero, cantidad (número que no se suma), moneda, fecha, fechahora, estado, nivel. */
    public record Columna(String titulo, String tipo) {
        public String getTitulo() { return titulo; }
        public String getTipo() { return tipo; }
        public boolean isNumerica() { return "entero".equals(tipo) || "moneda".equals(tipo) || "cantidad".equals(tipo); }
        public boolean isSumable() { return "entero".equals(tipo) || "moneda".equals(tipo); }
    }

    private final String codigo;
    private final String titulo;
    private final String descripcion;
    private final List<Columna> columnas = new ArrayList<>();
    private final List<List<Object>> filas = new ArrayList<>();

    public TablaReporte(String codigo, String titulo, String descripcion) {
        this.codigo = codigo;
        this.titulo = titulo;
        this.descripcion = descripcion;
    }

    public TablaReporte columna(String titulo, String tipo) {
        columnas.add(new Columna(titulo, tipo));
        return this;
    }

    public void agregarFila(List<Object> fila) {
        Preconditions.checkArgument(fila.size() == columnas.size(),
                "La fila tiene %s valores y el reporte %s columnas", fila.size(), columnas.size());
        filas.add(fila);
    }

    public String getCodigo() { return codigo; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public List<Columna> getColumnas() { return columnas; }
    public List<List<Object>> getFilas() { return filas; }

    public boolean isVacio() {
        return filas.isEmpty();
    }

    /** Suma de una columna de moneda o entero (para la fila de totales). Null si no es numérica. */
    public Object totalDe(int indice) {
        if (!columnas.get(indice).isSumable()) {
            return null;
        }
        BigDecimal suma = BigDecimal.ZERO;
        for (List<Object> f : filas) {
            Object v = f.get(indice);
            if (v instanceof Number n) {
                suma = suma.add(new BigDecimal(n.toString()));
            }
        }
        return "entero".equals(columnas.get(indice).tipo()) ? (Object) suma.longValue() : suma;
    }

    /** Totales por columna, listos para la vista (null en las no numéricas). */
    public List<Object> getTotales() {
        List<Object> t = new ArrayList<>();
        for (int i = 0; i < columnas.size(); i++) {
            t.add(totalDe(i));
        }
        return t;
    }

    /** Si tiene sentido mostrar fila de totales (hay alguna columna de moneda). */
    public boolean isConTotales() {
        return !filas.isEmpty() && columnas.stream().anyMatch(c -> "moneda".equals(c.tipo()));
    }
}
