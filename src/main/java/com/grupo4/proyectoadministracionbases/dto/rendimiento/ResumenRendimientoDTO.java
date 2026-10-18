package com.grupo4.proyectoadministracionbases.dto.rendimiento;

/**
 * Resumen consolidado de las métricas de rendimiento mostrado en las tarjetas
 * principales del módulo.
 *
 * @param totalSesiones            total de sesiones de usuario en la instancia.
 * @param sesionesActivas          sesiones que se encuentran ejecutando trabajo (status running).
 * @param sesionesBloqueadas       sesiones bloqueadas detectadas.
 * @param totalConsultasEnCache    consultas registradas en la caché de planes.
 * @param cpuTotalMs               CPU acumulado por todas las consultas (ms).
 * @param tiempoPromedioGlobalMs   tiempo de ejecución promedio global (ms).
 * @param memoriaTotalMb           memoria consumida por las sesiones de usuario (MB).
 * @param consultaMasCostosa       texto de la consulta de mayor consumo de CPU.
 * @param porcentajeCpuConsultaTop porcentaje de CPU que concentra la consulta más costosa.
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
