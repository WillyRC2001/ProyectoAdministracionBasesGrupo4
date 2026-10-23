package com.grupo4.proyectoadministracionbases.dto.respaldo;

import java.time.LocalDateTime;

public record UltimoRespaldoDTO(
        String tipo,
        LocalDateTime fechaFin,
        long minutosTranscurridos) {
}