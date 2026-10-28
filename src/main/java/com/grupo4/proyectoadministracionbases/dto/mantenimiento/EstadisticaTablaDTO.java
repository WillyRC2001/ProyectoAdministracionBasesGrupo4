package com.grupo4.proyectoadministracionbases.dto.mantenimiento;

import java.time.LocalDateTime;

public record EstadisticaTablaDTO(
        String esquema,
        String tabla,
        int cantidadEstadisticas,
        LocalDateTime ultimaActualizacion,
        long filas,
        long modificaciones) {

    private static final double UMBRAL_ADVERTENCIA = 10.0;
    private static final double UMBRAL_CRITICO = 20.0;

    /** Porcentaje de filas modificadas desde la última actualización de estadísticas. */
    public double porcentajeModificado() {
        if (filas > 0) {
            return modificaciones * 100.0 / filas;
        }
        return modificaciones > 0 ? 100.0 : 0.0;
    }

    /** OK, ADVERTENCIA, CRITICO o INFO (la tabla no tiene estadísticas). */
    public String nivel() {
        if (cantidadEstadisticas == 0) {
            return "INFO";
        }
        double porcentaje = porcentajeModificado();
        if (porcentaje >= UMBRAL_CRITICO) {
            return "CRITICO";
        }
        if (porcentaje >= UMBRAL_ADVERTENCIA) {
            return "ADVERTENCIA";
        }
        return "OK";
    }
}