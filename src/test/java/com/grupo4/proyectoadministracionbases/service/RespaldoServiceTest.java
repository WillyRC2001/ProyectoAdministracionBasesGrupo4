package com.grupo4.proyectoadministracionbases.service;

import com.grupo4.proyectoadministracionbases.dto.respaldo.PanelRespaldoDTO;
import com.grupo4.proyectoadministracionbases.dto.respaldo.UltimoRespaldoDTO;
import com.grupo4.proyectoadministracionbases.repository.RespaldoRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class RespaldoServiceTest {

    @Test
    void construyePanelConLosDatosDelRepositorio() {
        RespaldoRepository repository = mock(RespaldoRepository.class);
        given(repository.obtenerBaseDatos()).willReturn("DB_ProyectoGrupo4");
        given(repository.obtenerModeloRecuperacion()).willReturn("FULL");
        given(repository.listarUltimosExitosos()).willReturn(
                List.of(new UltimoRespaldoDTO("Completo", LocalDateTime.of(2026, 10, 28, 10, 0), 15)));
        given(repository.listarHistorial()).willReturn(List.of());

        PanelRespaldoDTO panel = new RespaldoService(repository).construirPanel();

        assertEquals("DB_ProyectoGrupo4", panel.baseDatos());
        assertEquals("FULL", panel.modeloRecuperacion());
        assertEquals(1, panel.ultimos().size());
        assertFalse(panel.hallazgos().isEmpty());
    }
}