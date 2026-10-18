package com.grupo4.proyectoadministracionbases.dto.rendimiento;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Panel consolidado entregado por la API JSON del Módulo 2.
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
