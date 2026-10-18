package com.grupo4.proyectoadministracionbases.dto.rendimiento;

/**
 * Resumen consolidado para el panel de rendimiento.
 */
public record ResumenRendimientoDTO(
        int totalSesiones,
        int sesionesActivas,
        int sesionesBloqueadas,
        int totalConsultasEnCache,
        double cpuTotalMs,
        double tiempoPromedioGlobalMs,
        double memoriaTotalMb,
        String consultaMasCostosa,
        double porcentajeCpuConsultaTop) {
}
