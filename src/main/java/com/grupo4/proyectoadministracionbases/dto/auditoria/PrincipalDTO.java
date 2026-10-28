package com.grupo4.proyectoadministracionbases.dto.auditoria;

import java.time.LocalDateTime;

public record PrincipalDTO(
        String nombre,
        String tipo,
        String estado,
        LocalDateTime fechaCreacion,
        String detalle) {
}