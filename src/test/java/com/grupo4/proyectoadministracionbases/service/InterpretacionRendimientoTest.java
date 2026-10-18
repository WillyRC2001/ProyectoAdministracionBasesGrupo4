package com.grupo4.proyectoadministracionbases.service;

import com.grupo4.proyectoadministracionbases.dto.rendimiento.BloqueoDTO;
import com.grupo4.proyectoadministracionbases.dto.rendimiento.ConsultaConsumoDTO;
import com.grupo4.proyectoadministracionbases.dto.rendimiento.ContextoInstanciaDTO;
import com.grupo4.proyectoadministracionbases.dto.rendimiento.HallazgoDTO;
import com.grupo4.proyectoadministracionbases.dto.rendimiento.NivelHallazgo;
import com.grupo4.proyectoadministracionbases.dto.rendimiento.ResumenRendimientoDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de la interpretación de resultados del Módulo 2.
 * Verifican que los umbrales generen el nivel de severidad esperado.
 */
class InterpretacionRendimientoTest {

    private final InterpretacionRendimiento interpretacion = new InterpretacionRendimiento();

    @Test
    void sinBloqueosGeneraHallazgoSaludable() {
        List<HallazgoDTO> hallazgos = interpretacion.analizar(
                contexto(16000), resumen(500, 10, 1, 100), List.of(), List.of());

        assertTrue(contiene(hallazgos, "Sin sesiones bloqueadas", NivelHallazgo.OK));
    }

    @Test
    void bloqueoConEsperaAltaSeMarcaComoCritico() {
        BloqueoDTO bloqueo = new BloqueoDTO(55, 60, "LCK_M_X", 12_000, "suspended", "UPDATE", 5, "db", "UPDATE t SET x=1");
        List<HallazgoDTO> hallazgos = interpretacion.analizar(
                contexto(16000), resumen(500, 10, 5, 100), List.of(), List.of(bloqueo));

        assertTrue(contiene(hallazgos, "Bloqueos detectados", NivelHallazgo.CRITICO));
    }

    @Test
    void bloqueoConEsperaCortaSeMarcaComoAdvertencia() {
        BloqueoDTO bloqueo = new BloqueoDTO(55, 60, "LCK_M_S", 200, "suspended", "SELECT", 5, "db", "SELECT 1");
        List<HallazgoDTO> hallazgos = interpretacion.analizar(
                contexto(16000), resumen(500, 10, 5, 100), List.of(), List.of(bloqueo));

        assertTrue(contiene(hallazgos, "Bloqueos detectados", NivelHallazgo.ADVERTENCIA));
        assertFalse(contiene(hallazgos, "Bloqueos detectados", NivelHallazgo.CRITICO));
    }

    @Test
    void consultaDominanteEnCpuGeneraAdvertencia() {
        ConsultaConsumoDTO consulta = consulta(500, 60);
        List<HallazgoDTO> hallazgos = interpretacion.analizar(
                contexto(16000), resumen(500, 10, 1, 100), List.of(consulta), List.of());

        assertTrue(contiene(hallazgos, "Consulta dominante en CPU", NivelHallazgo.ADVERTENCIA));
    }

    @Test
    void tiempoPromedioElevadoGeneraAdvertencia() {
        List<HallazgoDTO> hallazgos = interpretacion.analizar(
                contexto(16000), resumen(500, 5_000, 1, 100), List.of(consulta(500, 10)), List.of());

        assertTrue(contiene(hallazgos, "Tiempo promedio elevado", NivelHallazgo.ADVERTENCIA));
    }

    @Test
    void memoriaDeSesionesElevadaGeneraAdvertencia() {
        // 11.000 MB de 16.000 MB = 68.75 %
        List<HallazgoDTO> hallazgos = interpretacion.analizar(
                contexto(16000), resumen(11_000, 10, 1, 100), List.of(consulta(500, 10)), List.of());

        assertTrue(contiene(hallazgos, "Consumo de memoria elevado", NivelHallazgo.ADVERTENCIA));
    }

    @Test
    void sinConsultasEnCacheGeneraHallazgoInformativo() {
        List<HallazgoDTO> hallazgos = interpretacion.analizar(
                contexto(16000), resumen(0, 0, 0, 0), List.of(), List.of());

        assertTrue(contiene(hallazgos, "Sin actividad registrada", NivelHallazgo.INFORMATIVO));
    }

    @Test
    void condicionesSaludablesGeneranEstadoGeneralOk() {
        List<HallazgoDTO> hallazgos = interpretacion.analizar(
                contexto(16000), resumen(500, 10, 1, 100), List.of(consulta(500, 10)), List.of());

        assertTrue(contiene(hallazgos, "Rendimiento estable", NivelHallazgo.OK));
    }

    private boolean contiene(List<HallazgoDTO> hallazgos, String titulo, NivelHallazgo nivel) {
        return hallazgos.stream()
                .anyMatch(h -> h.titulo().equals(titulo) && h.nivel() == nivel);
    }

    private ContextoInstanciaDTO contexto(double memoriaServidorMb) {
        return new ContextoInstanciaDTO("16.0.1000.6", 8, memoriaServidorMb, 0);
    }

    private ResumenRendimientoDTO resumen(double memoriaTotalMb, double tiempoPromedioMs,
                                          int bloqueadas, int consultasEnCache) {
        return new ResumenRendimientoDTO(20, 3, bloqueadas, consultasEnCache,
                500, tiempoPromedioMs, memoriaTotalMb, "SELECT 1", 10);
    }

    private ConsultaConsumoDTO consulta(double cpuTotalMs, double porcentajeCpu) {
        return new ConsultaConsumoDTO("db", "SELECT 1", 10, cpuTotalMs, 50, 100, 10, 100, 0, 10, porcentajeCpu);
    }
}
