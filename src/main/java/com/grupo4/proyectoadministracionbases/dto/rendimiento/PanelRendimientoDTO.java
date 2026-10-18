package com.grupo4.proyectoadministracionbases.dto.rendimiento;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Objeto consolidado que la API del Módulo 2 entrega al navegador en una sola
 * respuesta. Agrupa el resumen, los rankings de consultas, las sesiones, los
 * bloqueos y la interpretación de resultados.
 *
 * @param generadoEn         momento en que se generó la información.
 * @param contexto           contexto del servidor (CPU, memoria, versión).
 * @param resumen            métricas consolidadas para las tarjetas.
 * @param consultasMayorCpu  ranking de consultas por mayor consumo de CPU.
 * @param consultasMayorTiempo ranking de consultas por mayor tiempo promedio.
 * @param sesiones           todas las sesiones de usuario.
 * @param bloqueos           sesiones bloqueadas detectadas.
 * @param consumoPorSesion   sesiones con mayor consumo de recursos.
 * @param interpretacion     hallazgos e interpretación de los resultados.
 */
public record PanelRendimientoDTO(
        OffsetDateTime generadoEn,
        ContextoInstanciaDTO contexto,
        ResumenRendimientoDTO resumen,
        List<ConsultaConsumoDTO> consultasMayorCpu,
        List<ConsultaConsumoDTO> consultasMayorTiempo,
        List<SesionDTO> sesiones,
        List<BloqueoDTO> bloqueos,
        List<SesionDTO> consumoPorSesion,
        List<HallazgoDTO> interpretacion) {
}
