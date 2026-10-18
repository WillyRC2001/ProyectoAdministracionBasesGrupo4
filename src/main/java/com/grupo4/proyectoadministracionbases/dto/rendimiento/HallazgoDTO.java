package com.grupo4.proyectoadministracionbases.dto.rendimiento;

/**
 * Hallazgo de interpretación con nivel, título, mensaje y recomendación.
 */
public record HallazgoDTO(
        NivelHallazgo nivel,
        String titulo,
        String mensaje,
        String recomendacion) {
}
