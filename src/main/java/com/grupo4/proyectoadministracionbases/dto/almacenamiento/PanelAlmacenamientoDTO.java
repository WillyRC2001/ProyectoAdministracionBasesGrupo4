package com.grupo4.proyectoadministracionbases.dto.almacenamiento;

import java.util.List;

public record PanelAlmacenamientoDTO(
        TamanoBaseDatosDTO tamano,
        List<ArchivoBaseDatosDTO> archivos) {
}