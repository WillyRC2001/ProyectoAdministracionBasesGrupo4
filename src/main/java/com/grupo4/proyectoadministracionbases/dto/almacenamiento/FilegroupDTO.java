package com.grupo4.proyectoadministracionbases.dto.almacenamiento;

import java.math.BigDecimal;

public record FilegroupDTO(
        String nombre,
        boolean predeterminado,
        int archivos,
        BigDecimal tamanoMb,
        BigDecimal usadoMb,
        BigDecimal disponibleMb) {
}