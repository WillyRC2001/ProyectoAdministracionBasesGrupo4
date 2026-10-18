package com.grupo4.proyectoadministracionbases.dto.rendimiento;

/**
 * Representa una sesión de usuario activa en la instancia de SQL Server
 * ({@code sys.dm_exec_sessions}) con su consumo de recursos asociado.
 *
 * @param sessionId       identificador de la sesión en SQL Server.
 * @param login           login o cuenta que abrió la sesión.
 * @param host            nombre del equipo cliente.
 * @param programa        aplicación cliente (program_name).
 * @param baseDatos       base de datos actual de la sesión.
 * @param estado          estado de la sesión (running, sleeping, dormant, etc.).
 * @param cpuMs           tiempo de CPU consumido por la sesión (ms).
 * @param memoriaMb       memoria utilizada por la sesión (MB).
 * @param lecturasLogicas lecturas lógicas acumuladas de la sesión.
 * @param lecturasFisicas lecturas físicas acumuladas de la sesión.
 * @param ultimaPeticion  fecha y hora de la última petición recibida.
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
