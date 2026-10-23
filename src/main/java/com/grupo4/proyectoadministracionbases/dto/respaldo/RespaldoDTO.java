package com.grupo4.proyectoadministracionbases.dto.respaldo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RespaldoDTO(
        LocalDateTime inicio,
        LocalDateTime fin,
        String tipo,
        String estado,
        BigDecimal tamanoMb,
        BigDecimal tamanoComprimidoMb,
        long duracionSegundos,
        boolean soloCopia,
        String dispositivo) {
}