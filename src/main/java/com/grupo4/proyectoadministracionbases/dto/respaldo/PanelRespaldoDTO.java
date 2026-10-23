package com.grupo4.proyectoadministracionbases.dto.respaldo;

import java.util.List;

public record PanelRespaldoDTO(
        String baseDatos,
        String modeloRecuperacion,
        List<UltimoRespaldoDTO> ultimos,
        List<RespaldoDTO> historial,
        List<HallazgoRespaldoDTO> hallazgos) {
}