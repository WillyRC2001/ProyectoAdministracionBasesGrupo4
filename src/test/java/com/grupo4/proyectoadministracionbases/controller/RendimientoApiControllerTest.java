package com.grupo4.proyectoadministracionbases.controller;

import com.grupo4.proyectoadministracionbases.dto.rendimiento.PanelRendimientoDTO;
import com.grupo4.proyectoadministracionbases.service.RendimientoService;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Prueba del endpoint JSON del Módulo 2. Verifica que el controlador delegue
 * en el servicio y devuelva el panel construido.
 */
class RendimientoApiControllerTest {

    private final RendimientoService servicio = mock(RendimientoService.class);
    private final RendimientoApiController controlador = new RendimientoApiController(servicio);

    @Test
    void panelDevuelveElPanelDelServicio() {
        PanelRendimientoDTO panel = new PanelRendimientoDTO(
                OffsetDateTime.now(), null, null,
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
        given(servicio.construirPanel()).willReturn(panel);

        assertSame(panel, controlador.panel());
    }
}
