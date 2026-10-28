package com.grupo4.proyectoadministracionbases.controller;

import com.grupo4.proyectoadministracionbases.dto.respaldo.PanelRespaldoDTO;
import com.grupo4.proyectoadministracionbases.service.RespaldoService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class RespaldoApiControllerTest {

    @Test
    void devuelveElPanelDelServicio() {
        RespaldoService service = mock(RespaldoService.class);
        PanelRespaldoDTO panel = new PanelRespaldoDTO("DB", "FULL", List.of(), List.of(), List.of());
        given(service.construirPanel()).willReturn(panel);

        assertSame(panel, new RespaldoApiController(service).panel());
    }
}