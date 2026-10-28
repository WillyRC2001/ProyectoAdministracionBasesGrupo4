package com.grupo4.proyectoadministracionbases.dto.mantenimiento;

import com.grupo4.proyectoadministracionbases.dto.auditoria.ObjetoInvalidoDTO;

import java.util.List;

public record PanelMantenimientoDTO(
        String baseDatos,
        List<EstadisticaTablaDTO> estadisticas,
        List<ObjetoInvalidoDTO> objetos) {
}