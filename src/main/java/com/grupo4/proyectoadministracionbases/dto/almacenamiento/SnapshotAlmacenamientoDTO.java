package com.grupo4.proyectoadministracionbases.dto.almacenamiento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SnapshotAlmacenamientoDTO(
        LocalDateTime fechaCaptura,
        String baseDatos,
        BigDecimal datosMb,
        BigDecimal logMb,
        BigDecimal usadoDatosMb,
        BigDecimal usadoLogMb) {
}