package com.grupo4.proyectoadministracionbases.dto.rendimiento;

/**
 * Contexto del servidor SQL Server para interpretación de métricas.
 */
public record ContextoInstanciaDTO(
        String versionProducto,
        int nucleosCpu,
        double memoriaServidorMb,
        int conexionesMaximas) {
}
