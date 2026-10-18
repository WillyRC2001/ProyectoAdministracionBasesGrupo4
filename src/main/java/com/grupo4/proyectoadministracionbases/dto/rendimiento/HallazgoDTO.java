package com.grupo4.proyectoadministracionbases.dto.rendimiento;

/**
 * Hallazgo derivado de la interpretación de las métricas. Apoya al
 * administrador indicando qué se observó y qué acción se recomienda.
 *
 * @param nivel         severidad del hallazgo.
 * @param titulo        título corto del hallazgo.
 * @param mensaje       descripción de lo observado.
 * @param recomendacion acción sugerida al administrador.
 */
public record HallazgoDTO(
        NivelHallazgo nivel,
        String titulo,
        String mensaje,
        String recomendacion) {
}
