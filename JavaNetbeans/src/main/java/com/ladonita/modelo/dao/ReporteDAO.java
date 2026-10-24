package com.ladonita.modelo.dao;

import com.ladonita.modelo.entidades.Pedido;
import com.ladonita.modelo.entidades.TablaReporte;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Reportes R01–R08 del informe (Tabla 11) e indicadores del dashboard. */
public interface ReporteDAO {

    TablaReporte r01VentasDiarias(LocalDate desde, LocalDate hasta);

    TablaReporte r01VentasMensuales(int anio);

    TablaReporte r02PedidosPendientes();

    TablaReporte r03ProductosMasSolicitados(LocalDate desde, LocalDate hasta);

    TablaReporte r04Inventario();

    TablaReporte r05Clientes();

    TablaReporte r06ReservasCatering(boolean soloProximas);

    /** idRepartidor = null para todos. */
    TablaReporte r07EntregasAsignadas(String idRepartidor);

    TablaReporte r08PedidosEntregados(String idRepartidor, LocalDate desde, LocalDate hasta);

    /** Monto vendido (excluye nada: el negocio cobra al confirmar) entre dos fechas, inclusive. */
    BigDecimal ventas(LocalDate desde, LocalDate hasta);

    int cantidadPedidos(LocalDate desde, LocalDate hasta);

    /** Ventas por día de los últimos {@code dias} días (incluye días sin ventas con 0). */
    Map<LocalDate, BigDecimal> ventasPorDia(int dias);

    /** Pedidos de catering con evento desde hoy, ordenados por fecha. */
    List<Pedido> proximosEventos(int limite);
}
