package com.grupo4.proyectoadministracionbases.dto.rendimiento;

/**
 * Sesión de usuario activa en SQL Server con consumo de recursos.
 */
public record SesionDTO(
        int sessionId,
        String login,
        String host,
        String programa,
        String baseDatos,
        String estado,
        long cpuMs,
        double memoriaMb,
        long lecturasLogicas,
        long lecturasFisicas,
        String ultimaPeticion) {
}
