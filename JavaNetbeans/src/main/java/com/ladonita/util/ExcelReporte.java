package com.ladonita.util;

import com.ladonita.modelo.entidades.EstadoPedido;
import com.ladonita.modelo.entidades.TablaReporte;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Exporta un reporte (R01–R08) a Excel .xlsx con Apache POI: título, fecha de generación,
 * encabezados con los colores de la marca, montos en soles con formato numérico real
 * (se pueden sumar en Excel), fila de totales, filtros y encabezado fijo al desplazarse.
 */
public final class ExcelReporte {

    private static final byte[] CACAO = {(byte) 0x4A, (byte) 0x24, (byte) 0x11};
    private static final byte[] CREMA = {(byte) 0xF8, (byte) 0xF3, (byte) 0xEE};
    private static final DateTimeFormatter GENERADO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private ExcelReporte() {
    }

    public static void escribir(TablaReporte t, String generadoPor, OutputStream salida) throws IOException {
        try (XSSFWorkbook libro = new XSSFWorkbook()) {
            Estilos e = new Estilos(libro);
            Sheet hoja = libro.createSheet(t.getCodigo());
            int columnas = t.getColumnas().size();

            // Título y subtítulo
            Row r0 = hoja.createRow(0);
            r0.setHeightInPoints(24);
            celda(r0, 0, t.getCodigo() + " · " + t.getTitulo(), e.titulo);
            hoja.addMergedRegion(new CellRangeAddress(0, 0, 0, Math.max(0, columnas - 1)));
            Row r1 = hoja.createRow(1);
            celda(r1, 0, "La Doñita Gourmet · " + t.getDescripcion() + " · Generado el "
                    + LocalDateTime.now().format(GENERADO) + " por " + generadoPor, e.subtitulo);
            hoja.addMergedRegion(new CellRangeAddress(1, 1, 0, Math.max(0, columnas - 1)));

            // Encabezados
            int filaCab = 3;
            Row cab = hoja.createRow(filaCab);
            cab.setHeightInPoints(20);
            int[] anchos = new int[columnas];
            for (int c = 0; c < columnas; c++) {
                String titulo = t.getColumnas().get(c).titulo();
                celda(cab, c, titulo, e.encabezado);
                anchos[c] = titulo.length();
            }

            // Datos
            int f = filaCab + 1;
            for (List<Object> fila : t.getFilas()) {
                Row r = hoja.createRow(f++);
                for (int c = 0; c < columnas; c++) {
                    String tipo = t.getColumnas().get(c).tipo();
                    Object v = fila.get(c);
                    Cell cell = r.createCell(c);
                    escribirValor(cell, v, tipo, e, (f % 2) == 0);
                    anchos[c] = Math.max(anchos[c], largoVisible(v, tipo));
                }
            }

            // Totales (con fórmulas SUMA reales, así Excel recalcula si alguien edita una celda)
            if (t.isConTotales()) {
                Row tot = hoja.createRow(f);
                for (int c = 0; c < columnas; c++) {
                    TablaReporte.Columna col = t.getColumnas().get(c);
                    Cell cell = tot.createCell(c);
                    boolean ticket = StringUtils.startsWith(col.titulo(), "Ticket");
                    if (c == 0) {
                        cell.setCellValue("Total (" + t.getFilas().size() + ")");
                        cell.setCellStyle(e.total);
                    } else if (col.isSumable() && !ticket) {
                        String letra = org.apache.poi.ss.util.CellReference.convertNumToColString(c);
                        cell.setCellFormula("SUM(" + letra + (filaCab + 2) + ":" + letra + f + ")");
                        cell.setCellStyle("moneda".equals(col.tipo()) ? e.totalMoneda : e.totalEntero);
                    } else {
                        cell.setCellStyle(e.total);
                    }
                }
            }

            for (int c = 0; c < columnas; c++) {
                // Ancho calculado por texto (sin autoSizeColumn, que necesita fuentes gráficas del servidor)
                hoja.setColumnWidth(c, Math.min(60, Math.max(10, anchos[c] + 3)) * 256);
            }
            hoja.createFreezePane(0, filaCab + 1);
            if (!t.isVacio()) {
                hoja.setAutoFilter(new CellRangeAddress(filaCab, f - 1, 0, columnas - 1));
            }
            hoja.getPrintSetup().setLandscape(columnas > 5);
            hoja.setFitToPage(true);
            hoja.getPrintSetup().setFitWidth((short) 1);
            hoja.getPrintSetup().setFitHeight((short) 0);
            libro.write(salida);
        }
    }

    private static void escribirValor(Cell cell, Object v, String tipo, Estilos e, boolean alterna) {
        if (v == null) {
            cell.setCellValue("—");
            cell.setCellStyle(alterna ? e.textoAlt : e.texto);
            return;
        }
        switch (tipo) {
            case "moneda" -> {
                cell.setCellValue(((BigDecimal) v).doubleValue());
                cell.setCellStyle(alterna ? e.monedaAlt : e.moneda);
            }
            case "entero", "cantidad" -> {
                cell.setCellValue(((Number) v).doubleValue());
                cell.setCellStyle(alterna ? e.enteroAlt : e.entero);
            }
            case "fecha" -> {
                cell.setCellValue((LocalDate) v);
                cell.setCellStyle(alterna ? e.fechaAlt : e.fecha);
            }
            case "fechahora" -> {
                cell.setCellValue((LocalDateTime) v);
                cell.setCellStyle(alterna ? e.fechaHoraAlt : e.fechaHora);
            }
            case "estado" -> {
                cell.setCellValue(((EstadoPedido) v).getEtiqueta());
                cell.setCellStyle(alterna ? e.textoAlt : e.texto);
            }
            default -> {
                cell.setCellValue(StringUtils.capitalize(v.toString()));
                cell.setCellStyle(alterna ? e.textoAlt : e.texto);
            }
        }
    }

    private static int largoVisible(Object v, String tipo) {
        if (v == null) {
            return 1;
        }
        return switch (tipo) {
            case "fecha" -> 10;
            case "fechahora" -> 16;
            case "moneda" -> v.toString().length() + 4;
            case "estado" -> ((EstadoPedido) v).getEtiqueta().length();
            default -> v.toString().length();
        };
    }

    private static void celda(Row r, int c, String texto, CellStyle estilo) {
        Cell cell = r.createCell(c);
        cell.setCellValue(texto);
        cell.setCellStyle(estilo);
    }

    /** Estilos del libro (POI recomienda crearlos una sola vez y reutilizarlos). */
    private static final class Estilos {
        final CellStyle titulo, subtitulo, encabezado, texto, textoAlt, moneda, monedaAlt, entero, enteroAlt,
                fecha, fechaAlt, fechaHora, fechaHoraAlt, total, totalMoneda, totalEntero;

        Estilos(XSSFWorkbook libro) {
            short fmtMoneda = libro.createDataFormat().getFormat("\"S/\" #,##0.00");
            short fmtEntero = libro.createDataFormat().getFormat("#,##0");
            short fmtFecha = libro.createDataFormat().getFormat("dd/mm/yyyy");
            short fmtFechaHora = libro.createDataFormat().getFormat("dd/mm/yyyy hh:mm");

            XSSFFont fTitulo = libro.createFont();
            fTitulo.setBold(true);
            fTitulo.setFontHeightInPoints((short) 15);
            fTitulo.setColor(new XSSFColor(CACAO, null));
            titulo = libro.createCellStyle();
            titulo.setFont(fTitulo);

            XSSFFont fSub = libro.createFont();
            fSub.setItalic(true);
            fSub.setColor(new XSSFColor(new byte[]{(byte) 0x55, (byte) 0x43, (byte) 0x3D}, null));
            subtitulo = libro.createCellStyle();
            subtitulo.setFont(fSub);

            XSSFFont fCab = libro.createFont();
            fCab.setBold(true);
            fCab.setColor(new XSSFColor(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF}, null));
            XSSFCellStyle cabX = libro.createCellStyle();
            cabX.setFont(fCab);
            cabX.setFillForegroundColor(new XSSFColor(CACAO, null));
            cabX.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cabX.setAlignment(HorizontalAlignment.CENTER);
            encabezado = cabX;

            texto = base(libro, null, false);
            textoAlt = base(libro, null, true);
            moneda = base(libro, fmtMoneda, false);
            monedaAlt = base(libro, fmtMoneda, true);
            entero = base(libro, fmtEntero, false);
            enteroAlt = base(libro, fmtEntero, true);
            fecha = base(libro, fmtFecha, false);
            fechaAlt = base(libro, fmtFecha, true);
            fechaHora = base(libro, fmtFechaHora, false);
            fechaHoraAlt = base(libro, fmtFechaHora, true);

            Font fTotal = libro.createFont();
            fTotal.setBold(true);
            total = base(libro, null, true);
            total.setFont(fTotal);
            total.setBorderTop(BorderStyle.MEDIUM);
            totalMoneda = base(libro, fmtMoneda, true);
            totalMoneda.setFont(fTotal);
            totalMoneda.setBorderTop(BorderStyle.MEDIUM);
            totalEntero = base(libro, fmtEntero, true);
            totalEntero.setFont(fTotal);
            totalEntero.setBorderTop(BorderStyle.MEDIUM);
        }

        private static CellStyle base(XSSFWorkbook libro, Short formato, boolean alterna) {
            XSSFCellStyle s = libro.createCellStyle();
            if (formato != null) {
                s.setDataFormat(formato);
            }
            if (alterna) {
                s.setFillForegroundColor(new XSSFColor(CREMA, null));
                s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            }
            s.setBorderBottom(BorderStyle.HAIR);
            return s;
        }
    }
}
