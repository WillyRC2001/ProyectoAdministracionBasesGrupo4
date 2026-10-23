package com.grupo4.proyectoadministracionbases.dto.almacenamiento;

import java.util.List;

public record PanelAlmacenamientoDTO(
        TamanoBaseDatosDTO tamano,
        List<FilegroupDTO> filegroups,
        List<ArchivoBaseDatosDTO> archivos,
        List<ObjetoAlmacenamientoDTO> objetos,
        List<HallazgoAlmacenamientoDTO> hallazgos) {
}