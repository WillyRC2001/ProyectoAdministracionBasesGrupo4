package com.grupo4.proyectoadministracionbases.dto.almacenamiento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CrecimientoDTO(
        String baseDatos,
        int capturas,
        LocalDateTime desde,
        LocalDateTime hasta,
        BigDecimal crecimientoUsadoMb,
        BigDecimal mbPorDia,
        List<SnapshotAlmacenamientoDTO> historial,
        String interpretacion) {
}