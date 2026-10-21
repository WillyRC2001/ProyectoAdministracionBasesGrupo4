package com.grupo4.proyectoadministracionbases.dto.almacenamiento;

import java.math.BigDecimal;

public record ArchivoBaseDatosDTO(
        String nombreLogico,
        String rutaFisica,
        String tipo,
        String filegroup,
        BigDecimal tamanoMb,
        BigDecimal usadoMb,
        BigDecimal maxMb,
        BigDecimal crecimiento,
        boolean crecimientoPorcentual) {
}