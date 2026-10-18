package com.grupo4.proyectoadministracionbases.controller;

import com.grupo4.proyectoadministracionbases.dto.rendimiento.PanelRendimientoDTO;
import com.grupo4.proyectoadministracionbases.service.RendimientoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API JSON del Módulo 2.
 *
 * <p>Expone las métricas de rendimiento en un único objeto para que el
 * navegador (Chart.js) las consuma sin recargar la página. Todas las rutas
 * son de solo lectura y de tipo {@code GET}.</p>
 */
@RestController
@RequestMapping("/rendimiento/api")
public class RendimientoApiController {

    private final RendimientoService rendimientoService;

    public RendimientoApiController(RendimientoService rendimientoService) {
        this.rendimientoService = rendimientoService;
    }

    /**
     * Devuelve el panel consolidado de rendimiento.
     *
     * @return métricas de rendimiento de la instancia monitoreada.
     */
    @GetMapping("/panel")
    public PanelRendimientoDTO panel() {
        return rendimientoService.construirPanel();
    }
}
