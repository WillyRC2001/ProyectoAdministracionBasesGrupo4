package com.grupo4.proyectoadministracionbases.dto.rendimiento;

/**
 * Nivel de severidad de un hallazgo generado por la interpretación de las
 * métricas de rendimiento del Módulo 2.
 *
 * <p>Se utiliza para definir el color y la prioridad con que se presenta la
 * interpretación al administrador de bases de datos.</p>
 */
public enum NivelHallazgo {

    /** Condición saludable, no requiere acción. */
    OK,

    /** Dato de contexto que puede ser de interés para el administrador. */
    INFORMATIVO,

    /** Condición que conviene revisar pero no es crítica. */
    ADVERTENCIA,

    /** Condición que requiere atención inmediata del administrador. */
    CRITICO
}
