package com.grupo4.proyectoadministracionbases.dto.rendimiento;

/**
 * Representa una sesión bloqueada detectada a través de
 * {@code sys.dm_exec_requests} junto con la sesión que la está bloqueando.
 *
 * @param sessionId        sesión afectada (bloqueada).
 * @param sesionBloqueante sesión que origina el bloqueo (blocking_session_id).
 * @param tipoEspera       tipo de espera (wait_type).
 * @param tiempoEsperaMs   tiempo de espera acumulado (ms).
 * @param estado           estado de la petición (running, suspended, etc.).
 * @param comando          comando que se está ejecutando (SELECT, INSERT, ...).
 * @param cpuMs            tiempo de CPU consumido por la petición (ms).
 * @param baseDatos        base de datos donde ocurre el bloqueo.
 * @param consulta         fragmento de texto SQL involucrado (truncado para la vista).
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
