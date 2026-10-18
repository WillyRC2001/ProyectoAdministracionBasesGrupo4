package com.grupo4.proyectoadministracionbases.dto.rendimiento;

/**
 * Representa una consulta registrada en la caché de planes de SQL Server
 * ({@code sys.dm_exec_query_stats}) junto con sus métricas de consumo.
 *
 * <p>Todas las unidades de tiempo se normalizan a milisegundos para su
 * presentación en el módulo de rendimiento.</p>
 *
 * @param baseDatos        base de datos a la que pertenece la consulta.
 * @param consulta         fragmento de texto SQL (truncado para la vista).
 * @param ejecuciones      número de veces que se ha ejecutado la consulta.
 * @param cpuTotalMs       tiempo de CPU acumulado (ms).
 * @param cpuPromedioMs    tiempo de CPU promedio por ejecución (ms).
 * @param tiempoTotalMs    tiempo de ejecución acumulado (ms).
 * @param tiempoPromedioMs tiempo de ejecución promedio por ejecución (ms).
 * @param lecturasLogicas  total de lecturas lógicas acumuladas.
 * @param lecturasFisicas  total de lecturas físicas acumuladas.
 * @param lecturasPromedio lecturas lógicas promedio por ejecución.
 * @param porcentajeCpu    proporción del CPU acumulado que representa esta consulta (0-100).
 */
public record ConsultaConsumoDTO(
        String baseDatos,
        String consulta,
        long ejecuciones,
        double cpuTotalMs,
        double cpuPromedioMs,
        double tiempoTotalMs,
        double tiempoPromedioMs,
        long lecturasLogicas,
        long lecturasFisicas,
        double lecturasPromedio,
        double porcentajeCpu) {

    /**
     * Devuelve una copia de este registro con el porcentaje de CPU indicado.
     * Se usa en la capa de servicio, una vez conocido el CPU total del servidor.
     *
     * @param nuevoPorcentaje porcentaje de CPU calculado (0-100).
     * @return copia con el porcentaje actualizado.
     */
    public ConsultaConsumoDTO conPorcentajeCpu(double nuevoPorcentaje) {
        return new ConsultaConsumoDTO(baseDatos, consulta, ejecuciones, cpuTotalMs, cpuPromedioMs,
                tiempoTotalMs, tiempoPromedioMs, lecturasLogicas, lecturasFisicas, lecturasPromedio,
                nuevoPorcentaje);
    }
}
