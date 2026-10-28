package com.grupo4.proyectoadministracionbases.dto.auditoria;

public record PermisoDTO(
        String ambito,
        String principal,
        String tipoPrincipal,
        String estado,
        String permiso,
        String objeto) {
}