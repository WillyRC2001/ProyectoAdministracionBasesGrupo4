package com.grupo4.proyectoadministracionbases.dto.rendimiento;

/**
 * Respuesta de error controlada que la API del Módulo 2 devuelve al navegador.
 * Evita exponer detalles internos de la excepción al usuario final.
 *
 * @param codigo  código corto y estable del error.
 * @param mensaje mensaje seguro para mostrar al usuario.
 */
public record RespuestaErrorDTO(
        String codigo,
        String mensaje) {
}
