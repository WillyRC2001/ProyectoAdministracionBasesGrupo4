package com.grupo4.proyectoadministracionbases.service;

import com.grupo4.proyectoadministracionbases.dto.rendimiento.BloqueoDTO;
import com.grupo4.proyectoadministracionbases.dto.rendimiento.ConsultaConsumoDTO;
import com.grupo4.proyectoadministracionbases.dto.rendimiento.ContextoInstanciaDTO;
import com.grupo4.proyectoadministracionbases.dto.rendimiento.HallazgoDTO;
import com.grupo4.proyectoadministracionbases.dto.rendimiento.PanelRendimientoDTO;
import com.grupo4.proyectoadministracionbases.dto.rendimiento.ResumenRendimientoDTO;
import com.grupo4.proyectoadministracionbases.dto.rendimiento.SesionDTO;
import com.grupo4.proyectoadministracionbases.repository.RendimientoRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Lógica del Módulo 2 (Monitoreo de rendimiento).
 *
 * <p>Coordina la lectura de métricas desde el repositorio, normaliza unidades,
 * calcula porcentajes y delega la interpretación de resultados. No realiza
 * consultas SQL por sí mismo ni acciones de escritura sobre el SGBD.</p>
 */
@Service
public class RendimientoService {

    /** Número de consultas mostradas en los rankings. */
    static final int LIMITE_CONSULTAS = 15;

    /** Número de sesiones mostradas en el ranking de consumo. */
    static final int LIMITE_SESIONES_CONSUMO = 10;

    private final RendimientoRepository rendimientoRepository;
    private final InterpretacionRendimiento interpretacionRendimiento;

    public RendimientoService(RendimientoRepository rendimientoRepository,
                              InterpretacionRendimiento interpretacionRendimiento) {
        this.rendimientoRepository = rendimientoRepository;
        this.interpretacionRendimiento = interpretacionRendimiento;
    }

    /**
     * Construye el panel completo de rendimiento que consume la API JSON.
     *
     * @return objeto consolidado con resumen, rankings, sesiones, bloqueos e interpretación.
     */
    public PanelRendimientoDTO construirPanel() {
        ContextoInstanciaDTO contexto = rendimientoRepository.obtenerContexto();
        double cpuTotalMs = rendimientoRepository.sumaCpuTotalMs();
        double tiempoPromedioGlobalMs = rendimientoRepository.promedioTiempoEjecucionMs();
        long totalConsultasEnCache = rendimientoRepository.contarConsultasEnCache();

        List<ConsultaConsumoDTO> consultasMayorCpu = aplicarPorcentajeCpu(
                rendimientoRepository.buscarConsultasMayorCpu(LIMITE_CONSULTAS), cpuTotalMs);
        List<ConsultaConsumoDTO> consultasMayorTiempo = aplicarPorcentajeCpu(
                rendimientoRepository.buscarConsultasMayorTiempoPromedio(LIMITE_CONSULTAS), cpuTotalMs);

        List<SesionDTO> sesiones = rendimientoRepository.buscarSesiones();
        List<BloqueoDTO> bloqueos = rendimientoRepository.buscarBloqueos();

        List<SesionDTO> consumoPorSesion = sesiones.stream()
                .limit(LIMITE_SESIONES_CONSUMO)
                .toList();

        int sesionesActivas = (int) sesiones.stream()
                .filter(RendimientoService::esActiva)
                .count();
        double memoriaTotalMb = sesiones.stream()
                .mapToDouble(SesionDTO::memoriaMb)
                .sum();

        String consultaMasCostosa = consultasMayorCpu.isEmpty() ? null : consultasMayorCpu.get(0).consulta();
        double porcentajeTop = consultasMayorCpu.isEmpty() ? 0d : consultasMayorCpu.get(0).porcentajeCpu();

        ResumenRendimientoDTO resumen = new ResumenRendimientoDTO(
                sesiones.size(),
                sesionesActivas,
                bloqueos.size(),
                (int) Math.min(totalConsultasEnCache, Integer.MAX_VALUE),
                redondear(cpuTotalMs),
                redondear(tiempoPromedioGlobalMs),
                redondear(memoriaTotalMb),
                consultaMasCostosa,
                redondear(porcentajeTop));

        List<HallazgoDTO> interpretacion = interpretacionRendimiento.analizar(
                contexto, resumen, consultasMayorCpu, bloqueos);

        return new PanelRendimientoDTO(
                OffsetDateTime.now(),
                contexto,
                resumen,
                consultasMayorCpu,
                consultasMayorTiempo,
                sesiones,
                bloqueos,
                consumoPorSesion,
                interpretacion);
    }

    private static List<ConsultaConsumoDTO> aplicarPorcentajeCpu(List<ConsultaConsumoDTO> consultas,
                                                                 double cpuTotalMs) {
        if (cpuTotalMs <= 0) {
            return consultas;
        }
        return consultas.stream()
                .map(consulta -> consulta.conPorcentajeCpu(
                        redondear(consulta.cpuTotalMs() / cpuTotalMs * 100.0)))
                .toList();
    }

    private static boolean esActiva(SesionDTO sesion) {
        return sesion.estado() != null && sesion.estado().equalsIgnoreCase("running");
    }

    private static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
