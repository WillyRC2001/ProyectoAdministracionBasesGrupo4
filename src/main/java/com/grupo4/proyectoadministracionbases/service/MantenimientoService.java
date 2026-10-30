package com.grupo4.proyectoadministracionbases.service;

import com.grupo4.proyectoadministracionbases.dto.mantenimiento.EstadisticaTablaDTO;
import com.grupo4.proyectoadministracionbases.dto.mantenimiento.PanelMantenimientoDTO;
import com.grupo4.proyectoadministracionbases.repository.AuditoriaRepository;
import com.grupo4.proyectoadministracionbases.repository.MantenimientoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MantenimientoService {

    private static final Logger log = LoggerFactory.getLogger(MantenimientoService.class);

    private final MantenimientoRepository repository;
    private final AuditoriaRepository auditoriaRepository;

    public MantenimientoService(MantenimientoRepository repository,
                                AuditoriaRepository auditoriaRepository) {
        this.repository = repository;
        this.auditoriaRepository = auditoriaRepository;
    }

    public PanelMantenimientoDTO construirPanel() {
        return new PanelMantenimientoDTO(
                auditoriaRepository.obtenerNombreBaseDatos(),
                repository.listarEstadisticasPorTabla(),
                auditoriaRepository.listarObjetosInvalidos());
    }

    public String actualizarEstadisticas(String esquema, String tabla) {
        String tablaDelimitada = repository.resolverTabla(esquema, tabla)
                .orElseThrow(() -> new IllegalArgumentException(
                        "La tabla " + esquema + "." + tabla + " no existe en la base de datos."));
        repository.actualizarEstadisticas(tablaDelimitada);
        return "Estadísticas actualizadas correctamente en " + esquema + "." + tabla + ".";
    }

    public String actualizarEstadisticasTodas() {
        List<EstadisticaTablaDTO> tablas = repository.listarEstadisticasPorTabla();
        int correctas = 0;
        int fallidas = 0;
        for (EstadisticaTablaDTO t : tablas) {
            try {
                actualizarEstadisticas(t.esquema(), t.tabla());
                correctas++;
            } catch (DataAccessException | IllegalArgumentException ex) {
                fallidas++;
                log.warn("No se pudieron actualizar las estadísticas de {}.{}", t.esquema(), t.tabla(), ex);
            }
        }
        if (fallidas == 0) {
            return "Estadísticas actualizadas correctamente en " + correctas + " tabla(s).";
        }
        return "Estadísticas actualizadas en " + correctas + " tabla(s); fallaron " + fallidas
                + ". Revise los permisos del usuario de la aplicación.";
    }

    public String recompilarModulo(String esquema, String objeto) {
        String moduloDelimitado = repository.resolverModulo(esquema, objeto)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El objeto " + esquema + "." + objeto
                                + " no existe o no es un procedimiento, vista, función o trigger."));
        repository.refrescarModulo(moduloDelimitado);
        return "El objeto " + esquema + "." + objeto + " se recompiló (sp_refreshsqlmodule) correctamente. "
                + "Si la referencia sigue rota, cree el objeto que falta y vuelva a recompilar.";
    }

    public String reconstruirIndice(String esquema, String tabla, String indice) {
        String indiceDelimitado = repository.resolverIndice(esquema, tabla, indice)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El índice " + indice + " de " + esquema + "." + tabla + " no existe."));
        repository.reconstruirIndice(indiceDelimitado);
        return "Índice " + indice + " reconstruido correctamente en " + esquema + "." + tabla + ".";
    }
}