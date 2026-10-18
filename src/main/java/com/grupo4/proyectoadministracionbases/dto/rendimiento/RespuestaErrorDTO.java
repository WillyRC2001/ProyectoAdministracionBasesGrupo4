package com.grupo4.proyectoadministracionbases.dto.rendimiento;

/**
 * Respuesta de error controlada (sin exponer detalles internos).
 */
public record RespuestaErrorDTO(
        String codigo,
        String mensaje) {
}
