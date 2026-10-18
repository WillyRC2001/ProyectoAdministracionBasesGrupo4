package com.grupo4.proyectoadministracionbases.service;

import com.grupo4.proyectoadministracionbases.dto.rendimiento.ConsultaConsumoDTO;
import com.grupo4.proyectoadministracionbases.dto.rendimiento.ContextoInstanciaDTO;
import com.grupo4.proyectoadministracionbases.dto.rendimiento.PanelRendimientoDTO;
import com.grupo4.proyectoadministracionbases.dto.rendimiento.SesionDTO;
import com.grupo4.proyectoadministracionbases.repository.RendimientoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Pruebas de la lógica del servicio de rendimiento: agregación de métricas,
 * cálculo de porcentajes y límite de elementos del ranking de sesiones.
 */
class RendimientoServiceTest {

    private RendimientoRepository repositorio;
    private RendimientoService servicio;

    @BeforeEach
    void preparar() {
        repositorio = mock(RendimientoRepository.class);
        servicio = new RendimientoService(repositorio, new InterpretacionRendimiento());
    }

    @Test
    void construyeElResumenYCalculaPorcentajeDeCpu() {
        given(repositorio.obtenerContexto()).willReturn(new ContextoInstanciaDTO("16.0", 8, 16000, 0));
        given(repositorio.sumaCpuTotalMs()).willReturn(1000d);
        given(repositorio.promedioTiempoEjecucionMs()).willReturn(10d);
        given(repositorio.contarConsultasEnCache()).willReturn(5L);

        ConsultaConsumoDTO consultaTop = new ConsultaConsumoDTO(
                "db", "SELECT 1", 10, 500d, 50d, 100d, 10d, 100, 0, 10d, 0d);
        given(repositorio.buscarConsultasMayorCpu(15)).willReturn(List.of(consultaTop));
        given(repositorio.buscarConsultasMayorTiempoPromedio(15)).willReturn(List.of(consultaTop));

        given(repositorio.buscarSesiones()).willReturn(List.of(
                new SesionDTO(1, "sa", "host", "app", "db", "running", 100, 10d, 1, 1, "2026-01-01"),
                new SesionDTO(2, "user", "host", "app", "db", "sleeping", 0, 5d, 1, 1, "2026-01-01")));
        given(repositorio.buscarBloqueos()).willReturn(List.of());

        PanelRendimientoDTO panel = servicio.construirPanel();

        assertEquals(2, panel.resumen().totalSesiones());
        assertEquals(1, panel.resumen().sesionesActivas());
        assertEquals(0, panel.resumen().sesionesBloqueadas());
        assertEquals(15d, panel.resumen().memoriaTotalMb(), 0.001);
        assertEquals(50d, panel.resumen().porcentajeCpuConsultaTop(), 0.001);
        assertEquals(50d, panel.consultasMayorCpu().get(0).porcentajeCpu(), 0.001);
        assertEquals("SELECT 1", panel.resumen().consultaMasCostosa());
        assertNotNull(panel.generadoEn());
        assertNotNull(panel.interpretacion());
    }

    @Test
    void porcentajeCpuEsCeroCuandoNoHayCpuRegistrado() {
        given(repositorio.obtenerContexto()).willReturn(new ContextoInstanciaDTO("16.0", 8, 16000, 0));
        given(repositorio.sumaCpuTotalMs()).willReturn(0d);
        given(repositorio.promedioTiempoEjecucionMs()).willReturn(0d);
        given(repositorio.contarConsultasEnCache()).willReturn(0L);
        given(repositorio.buscarConsultasMayorCpu(15)).willReturn(List.of());
        given(repositorio.buscarConsultasMayorTiempoPromedio(15)).willReturn(List.of());
        given(repositorio.buscarSesiones()).willReturn(List.of());
        given(repositorio.buscarBloqueos()).willReturn(List.of());

        PanelRendimientoDTO panel = servicio.construirPanel();

        assertEquals(0d, panel.resumen().porcentajeCpuConsultaTop(), 0.001);
        assertNull(panel.resumen().consultaMasCostosa());
    }

    @Test
    void limitaElRankingDeSesionesA10() {
        given(repositorio.obtenerContexto()).willReturn(new ContextoInstanciaDTO("16.0", 8, 16000, 0));
        given(repositorio.sumaCpuTotalMs()).willReturn(0d);
        given(repositorio.promedioTiempoEjecucionMs()).willReturn(0d);
        given(repositorio.contarConsultasEnCache()).willReturn(0L);
        given(repositorio.buscarConsultasMayorCpu(15)).willReturn(List.of());
        given(repositorio.buscarConsultasMayorTiempoPromedio(15)).willReturn(List.of());

        List<SesionDTO> sesiones = java.util.stream.IntStream.range(0, 15)
                .mapToObj(i -> new SesionDTO(i, "u" + i, "h", "p", "db", "sleeping", i, 1d, 0, 0, "2026-01-01"))
                .toList();
        given(repositorio.buscarSesiones()).willReturn(sesiones);
        given(repositorio.buscarBloqueos()).willReturn(List.of());

        PanelRendimientoDTO panel = servicio.construirPanel();

        assertEquals(15, panel.resumen().totalSesiones());
        assertEquals(10, panel.consumoPorSesion().size());
    }
}
