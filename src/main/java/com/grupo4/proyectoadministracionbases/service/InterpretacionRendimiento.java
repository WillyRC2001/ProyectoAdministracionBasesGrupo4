package com.grupo4.proyectoadministracionbases.service;

import com.grupo4.proyectoadministracionbases.dto.rendimiento.BloqueoDTO;
import com.grupo4.proyectoadministracionbases.dto.rendimiento.ConsultaConsumoDTO;
import com.grupo4.proyectoadministracionbases.dto.rendimiento.ContextoInstanciaDTO;
import com.grupo4.proyectoadministracionbases.dto.rendimiento.HallazgoDTO;
import com.grupo4.proyectoadministracionbases.dto.rendimiento.NivelHallazgo;
import com.grupo4.proyectoadministracionbases.dto.rendimiento.ResumenRendimientoDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Interpretación de las métricas de rendimiento.
 *
 * <p>Aplica umbrales documentados sobre las métricas obtenidas del SGBD y
 * genera hallazgos con una recomendación administrativa, cumpliendo el
 * criterio de "interpretación de resultados" del Módulo 2.</p>
 *
 * <p>Los umbrales son constantes ajustables en un solo punto del código.</p>
 */
@Component
public class InterpretacionRendimiento {

    /** Porcentaje de CPU a partir del cual una sola consulta se considera dominante. */
    static final double UMBRAL_CPU_CONSULTA_TOP_PCT = 50.0;

    /** Tiempo de ejecución promedio global que se considera elevado (ms). */
    static final double UMBRAL_TIEMPO_PROMEDIO_MS = 1000.0;

    /** Porcentaje de la memoria del servidor consumido por las sesiones que se considera alto. */
    static final double UMBRAL_MEMORIA_SESIONES_PCT = 60.0;

    /** Tiempo de espera de un bloqueo a partir del cual se considera crítico (ms). */
    static final long UMBRAL_ESPERA_BLOQUEO_MS = 5_000L;

    /**
     * Genera la lista de hallazgos a partir de las métricas observadas.
     *
     * @param contexto          contexto del servidor (CPU, memoria).
     * @param resumen           métricas consolidadas.
     * @param consultasMayorCpu ranking de consultas por CPU.
     * @param bloqueos          sesiones bloqueadas detectadas.
     * @return hallazgos ordenados por relevancia.
     */
    public List<HallazgoDTO> analizar(ContextoInstanciaDTO contexto,
                                      ResumenRendimientoDTO resumen,
                                      List<ConsultaConsumoDTO> consultasMayorCpu,
                                      List<BloqueoDTO> bloqueos) {
        List<HallazgoDTO> hallazgos = new ArrayList<>();

        analizarBloqueos(bloqueos, hallazgos);
        analizarCpu(resumen, consultasMayorCpu, hallazgos);
        analizarTiempo(resumen, hallazgos);
        analizarMemoria(contexto, resumen, hallazgos);
        analizarActividad(resumen, hallazgos);
        agregarEstadoGeneral(hallazgos);

        return hallazgos;
    }

    private void analizarBloqueos(List<BloqueoDTO> bloqueos, List<HallazgoDTO> hallazgos) {
        if (bloqueos.isEmpty()) {
            hallazgos.add(new HallazgoDTO(
                    NivelHallazgo.OK,
                    "Sin sesiones bloqueadas",
                    "No se detectaron sesiones bloqueadas en el momento de la consulta.",
                    "No se requiere ninguna acción de contención."));
            return;
        }

        long esperaMaximaMs = bloqueos.stream()
                .mapToLong(b -> (long) b.tiempoEsperaMs())
                .max()
                .orElse(0L);

        NivelHallazgo nivel = esperaMaximaMs >= UMBRAL_ESPERA_BLOQUEO_MS
                ? NivelHallazgo.CRITICO
                : NivelHallazgo.ADVERTENCIA;

        hallazgos.add(new HallazgoDTO(
                nivel,
                "Bloqueos detectados",
                "Se detectaron %d sesión(es) bloqueada(s). Espera máxima observada: %d ms."
                        .formatted(bloqueos.size(), esperaMaximaMs),
                "Revisar la sesión bloqueante (session_id) y la consulta asociada; evaluar índices, "
                        + "transacciones abiertas o niveles de aislamiento."));
    }

    private void analizarCpu(ResumenRendimientoDTO resumen,
                             List<ConsultaConsumoDTO> consultasMayorCpu,
                             List<HallazgoDTO> hallazgos) {
        if (consultasMayorCpu.isEmpty()) {
            return;
        }
        double porcentajeTop = consultasMayorCpu.get(0).porcentajeCpu();
        if (porcentajeTop >= UMBRAL_CPU_CONSULTA_TOP_PCT) {
            hallazgos.add(new HallazgoDTO(
                    NivelHallazgo.ADVERTENCIA,
                    "Consulta dominante en CPU",
                    "La consulta de mayor consumo concentra el %.1f%% del CPU acumulado de la instancia."
                            .formatted(porcentajeTop),
                    "Analizar el plan de ejecución de esa consulta y considerar índices o reescritura."));
        }
    }

    private void analizarTiempo(ResumenRendimientoDTO resumen, List<HallazgoDTO> hallazgos) {
        if (resumen.tiempoPromedioGlobalMs() > UMBRAL_TIEMPO_PROMEDIO_MS) {
            hallazgos.add(new HallazgoDTO(
                    NivelHallazgo.ADVERTENCIA,
                    "Tiempo promedio elevado",
                    "El tiempo de ejecución promedio global es de %.1f ms, por encima del umbral de %.0f ms."
                            .formatted(resumen.tiempoPromedioGlobalMs(), UMBRAL_TIEMPO_PROMEDIO_MS),
                    "Revisar las consultas con mayor tiempo promedio y verificar estadísticas e índices."));
        }
    }

    private void analizarMemoria(ContextoInstanciaDTO contexto,
                                 ResumenRendimientoDTO resumen,
                                 List<HallazgoDTO> hallazgos) {
        if (contexto.memoriaServidorMb() <= 0) {
            return;
        }
        double porcentaje = resumen.memoriaTotalMb() / contexto.memoriaServidorMb() * 100.0;
        if (porcentaje >= UMBRAL_MEMORIA_SESIONES_PCT) {
            hallazgos.add(new HallazgoDTO(
                    NivelHallazgo.ADVERTENCIA,
                    "Consumo de memoria elevado",
                    "Las sesiones consumen aproximadamente el %.1f%% de la memoria física del servidor."
                            .formatted(porcentaje),
                    "Identificar las sesiones con mayor consumo y verificar consultas que generen "
                            + "grandes conjuntos de resultados."));
        }
    }

    private void analizarActividad(ResumenRendimientoDTO resumen, List<HallazgoDTO> hallazgos) {
        if (resumen.totalConsultasEnCache() == 0) {
            hallazgos.add(new HallazgoDTO(
                    NivelHallazgo.INFORMATIVO,
                    "Sin actividad registrada",
                    "No hay consultas registradas en la caché de planes de la instancia.",
                    "Ejecutar actividad en la instancia para que el monitoreo muestre métricas representativas."));
        }
    }

    private void agregarEstadoGeneral(List<HallazgoDTO> hallazgos) {
        boolean existeRiesgo = hallazgos.stream()
                .anyMatch(h -> h.nivel() == NivelHallazgo.ADVERTENCIA || h.nivel() == NivelHallazgo.CRITICO);
        if (!existeRiesgo) {
            hallazgos.add(new HallazgoDTO(
                    NivelHallazgo.OK,
                    "Rendimiento estable",
                    "Las métricas observadas no superan ninguno de los umbrales de alerta configurados.",
                    "Mantener el monitoreo periódico."));
        }
    }
}
