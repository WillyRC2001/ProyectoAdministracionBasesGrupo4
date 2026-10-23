package com.grupo4.proyectoadministracionbases.dto.almacenamiento;

import java.math.BigDecimal;

public record ObjetoAlmacenamientoDTO(
        String esquema,
        String nombre,
        long filas,
        BigDecimal datosMb,
        BigDecimal indicesMb,
        BigDecimal reservadoMb) {
}