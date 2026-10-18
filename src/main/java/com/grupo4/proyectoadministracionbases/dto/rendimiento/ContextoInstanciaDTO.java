package com.grupo4.proyectoadministracionbases.dto.rendimiento;

/**
 * Contexto del servidor de base de datos utilizado para interpretar las
 * métricas de rendimiento (por ejemplo, el porcentaje de memoria disponible).
 *
 * <p>Se obtiene de {@code sys.dm_os_sys_info}.</p>
 *
 * @param versionProducto   versión de SQL Server.
 * @param nucleosCpu        número de procesadores lógicos disponibles.
 * @param memoriaServidorMb memoria física del servidor (MB).
 * @param conexionesMaximas máximo de conexiones de usuario configuradas (0 = sin límite).
 */
public record ContextoInstanciaDTO(
        String versionProducto,
        int nucleosCpu,
        double memoriaServidorMb,
        int conexionesMaximas) {
}
