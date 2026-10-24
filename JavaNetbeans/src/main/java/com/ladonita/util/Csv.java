package com.ladonita.util;

import com.ladonita.modelo.entidades.EstadoPedido;
import com.ladonita.modelo.entidades.TablaReporte;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/** Exporta un reporte a CSV (separador ";" y BOM UTF-8 para que Excel en español lo abra bien). */
public final class Csv {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Csv() {
    }

    public static void escribir(TablaReporte t, PrintWriter out) {
        out.write('﻿');
        out.println(t.getCodigo() + " - " + celda(t.getTitulo()));
        StringBuilder cab = new StringBuilder();
        for (TablaReporte.Columna c : t.getColumnas()) {
            cab.append(cab.isEmpty() ? "" : ";").append(celda(c.titulo()));
        }
        out.println(cab);
        for (List<Object> fila : t.getFilas()) {
            StringBuilder linea = new StringBuilder();
            for (int i = 0; i < fila.size(); i++) {
                linea.append(i == 0 ? "" : ";").append(celda(texto(fila.get(i))));
            }
            out.println(linea);
        }
        out.flush();
    }

    private static String texto(Object v) {
        if (v == null) {
            return "";
        }
        if (v instanceof LocalDateTime d) {
            return d.format(FECHA_HORA);
        }
        if (v instanceof LocalDate d) {
            return d.format(FECHA);
        }
        if (v instanceof EstadoPedido e) {
            return e.getEtiqueta();
        }
        if (v instanceof java.math.BigDecimal b) {
            return b.toPlainString().replace('.', ',');
        }
        return v.toString();
    }

    /** Escapa comillas y evita inyección de fórmulas en Excel (=, +, -, @ al inicio). */
    private static String celda(String s) {
        String v = s;
        if (StringUtils.startsWithAny(v, "=", "+", "-", "@") && !v.matches("-?[\\d,]+")) {
            v = "'" + v;
        }
        if (StringUtils.containsAny(v, ';', '"', '\n')) {
            v = "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }
}
