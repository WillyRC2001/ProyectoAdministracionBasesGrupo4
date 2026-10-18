package com.grupo4.proyectoadministracionbases.dto.rendimiento;

/**
 * Sesión bloqueada detectada en SQL Server.
 */
public record BloqueoDTO(
        int sessionId,
        int sesionBloqueante,
        String tipoEspera,
        double tiempoEsperaMs,
        String estado,
        String comando,
        long cpuMs,
        String baseDatos,
        String consulta) {
}
