package com.grupo4.proyectoadministracionbases.dto.almacenamiento;

import java.math.BigDecimal;

public record TamanoBaseDatosDTO(
        String baseDatos,
        BigDecimal datosMb,
        BigDecimal logMb,
        BigDecimal totalMb) {
}