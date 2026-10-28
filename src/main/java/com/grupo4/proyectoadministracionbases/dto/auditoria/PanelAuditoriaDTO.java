package com.grupo4.proyectoadministracionbases.dto.auditoria;

import java.util.List;

public record PanelAuditoriaDTO(
        String baseDatos,
        List<PrincipalDTO> logins,
        List<PrincipalDTO> usuarios,
        List<AsignacionRolDTO> roles,
        List<PermisoDTO> permisos,
        List<ObjetoInvalidoDTO> objetosInvalidos,
        List<HallazgoAuditoriaDTO> hallazgos) {
}