package com.grupo4.proyectoadministracionbases.service;

import com.grupo4.proyectoadministracionbases.dto.respaldo.HallazgoRespaldoDTO;
import com.grupo4.proyectoadministracionbases.dto.respaldo.RespaldoDTO;
import com.grupo4.proyectoadministracionbases.dto.respaldo.UltimoRespaldoDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InterpretacionRespaldoTest {

    private static UltimoRespaldoDTO ultimo(String tipo, long minutos) {
        return new UltimoRespaldoDTO(tipo, LocalDateTime.of(2026, 10, 28, 10, 0), minutos);
    }

    private static RespaldoDTO historial(String estado) {
        return new RespaldoDTO(LocalDateTime.of(2026, 10, 28, 9, 0), LocalDateTime.of(2026, 10, 28, 9, 1),
                "Completo", estado, new BigDecimal("3.50"), null, 5, false, "C:\\respaldos\\db.bak");
    }

    @Test
    void sinRespaldosEsCritico() {
        List<HallazgoRespaldoDTO> h = InterpretacionRespaldo.generarHallazgos("FULL", List.of(), List.of());

        assertEquals(1, h.size());
        assertEquals(InterpretacionRespaldo.CRITICO, h.get(0).nivel());
    }

    @Test
    void sinRespaldoCompletoEsCritico() {
        List<HallazgoRespaldoDTO> h = InterpretacionRespaldo.generarHallazgos(
                "SIMPLE", List.of(ultimo("Diferencial", 10)), List.of());

        assertEquals(InterpretacionRespaldo.CRITICO, h.get(0).nivel());
        assertEquals("Sin respaldo completo", h.get(0).titulo());
    }

    @Test
    void completoRecienteSinProblemasEsOk() {
        List<HallazgoRespaldoDTO> h = InterpretacionRespaldo.generarHallazgos(
                "SIMPLE", List.of(ultimo("Completo", 30)), List.of(historial("COMPLETADO")));

        assertEquals(1, h.size());
        assertEquals(InterpretacionRespaldo.OK, h.get(0).nivel());
    }

    @Test
    void completoDeMasDeSieteDiasEsAdvertencia() {
        List<HallazgoRespaldoDTO> h = InterpretacionRespaldo.generarHallazgos(
                "SIMPLE", List.of(ultimo("Completo", 8L * 1440)), List.of());

        assertEquals(InterpretacionRespaldo.ADVERTENCIA, h.get(0).nivel());
    }

    @Test
    void completoDeMasDeTreintaDiasEsCritico() {
        List<HallazgoRespaldoDTO> h = InterpretacionRespaldo.generarHallazgos(
                "SIMPLE", List.of(ultimo("Completo", 31L * 1440)), List.of());

        assertEquals(InterpretacionRespaldo.CRITICO, h.get(0).nivel());
    }

    @Test
    void modeloFullSinLogEsAdvertencia() {
        List<HallazgoRespaldoDTO> h = InterpretacionRespaldo.generarHallazgos(
                "FULL", List.of(ultimo("Completo", 30)), List.of());

        assertEquals(InterpretacionRespaldo.ADVERTENCIA, h.get(0).nivel());
        assertEquals("Modelo FULL sin respaldos del log", h.get(0).titulo());
    }

    @Test
    void modeloSimpleNoExigeLog() {
        List<HallazgoRespaldoDTO> h = InterpretacionRespaldo.generarHallazgos(
                "SIMPLE", List.of(ultimo("Completo", 30)), List.of());

        assertEquals(InterpretacionRespaldo.OK, h.get(0).nivel());
    }

    @Test
    void respaldoDanadoEsCriticoYVaPrimero() {
        List<HallazgoRespaldoDTO> h = InterpretacionRespaldo.generarHallazgos(
                "FULL", List.of(ultimo("Completo", 30)), List.of(historial("DANADO")));

        assertEquals(InterpretacionRespaldo.CRITICO, h.get(0).nivel());
        assertTrue(h.stream().anyMatch(x -> x.titulo().equals("Respaldos dañados")));
    }

    @Test
    void formateaLaAntiguedad() {
        assertEquals("hace 1 minuto", InterpretacionRespaldo.formatearAntiguedad(1));
        assertEquals("hace 5 horas", InterpretacionRespaldo.formatearAntiguedad(300));
        assertEquals("hace 2 días", InterpretacionRespaldo.formatearAntiguedad(2 * 1440));
    }
}