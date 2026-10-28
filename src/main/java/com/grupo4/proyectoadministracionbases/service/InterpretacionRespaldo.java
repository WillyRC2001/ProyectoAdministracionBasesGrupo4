package com.grupo4.proyectoadministracionbases.service;

import com.grupo4.proyectoadministracionbases.dto.respaldo.HallazgoRespaldoDTO;
import com.grupo4.proyectoadministracionbases.dto.respaldo.RespaldoDTO;
import com.grupo4.proyectoadministracionbases.dto.respaldo.UltimoRespaldoDTO;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class InterpretacionRespaldo {

    public static final String OK = "OK";
    public static final String ADVERTENCIA = "ADVERTENCIA";
    public static final String CRITICO = "CRITICO";

    public static final String TIPO_COMPLETO = "Completo";
    public static final String TIPO_LOG = "Log de transacciones";
    public static final String ESTADO_DANADO = "DANADO";
    public static final String MODELO_FULL = "FULL";

    private static final long MINUTOS_COMPLETO_ADVERTENCIA = 7L * 24 * 60;
    private static final long MINUTOS_COMPLETO_CRITICO = 30L * 24 * 60;
    private static final long MINUTOS_LOG_ADVERTENCIA = 24L * 60;

    private InterpretacionRespaldo() {
    }

    public static List<HallazgoRespaldoDTO> generarHallazgos(String modeloRecuperacion,
                                                             List<UltimoRespaldoDTO> ultimos,
                                                             List<RespaldoDTO> historial) {
        List<HallazgoRespaldoDTO> hallazgos = new ArrayList<>();

        if (ultimos.isEmpty()) {
            hallazgos.add(new HallazgoRespaldoDTO(
                    CRITICO,
                    "Sin respaldos registrados",
                    "msdb no registra ningún respaldo exitoso de esta base de datos. "
                            + "Ante un fallo no habría forma de recuperar la información."));
            agregarDanados(hallazgos, historial);
            return ordenar(hallazgos);
        }

        Optional<UltimoRespaldoDTO> completo = buscarTipo(ultimos, TIPO_COMPLETO);
        if (completo.isEmpty()) {
            hallazgos.add(new HallazgoRespaldoDTO(
                    CRITICO,
                    "Sin respaldo completo",
                    "Existen respaldos de otros tipos, pero ninguno completo. Los respaldos diferenciales "
                            + "y de log dependen de un respaldo completo para poder restaurarse."));
        } else {
            long minutos = Math.max(0, completo.get().minutosTranscurridos());
            if (minutos >= MINUTOS_COMPLETO_CRITICO) {
                hallazgos.add(new HallazgoRespaldoDTO(
                        CRITICO,
                        "Respaldo completo muy antiguo",
                        "El último respaldo completo fue " + formatearAntiguedad(minutos)
                                + ". Una restauración perdería todos los cambios posteriores."));
            } else if (minutos >= MINUTOS_COMPLETO_ADVERTENCIA) {
                hallazgos.add(new HallazgoRespaldoDTO(
                        ADVERTENCIA,
                        "Respaldo completo desactualizado",
                        "El último respaldo completo fue " + formatearAntiguedad(minutos)
                                + ". Se recomienda realizarlo al menos una vez por semana."));
            }
        }

        if (MODELO_FULL.equalsIgnoreCase(modeloRecuperacion)) {
            Optional<UltimoRespaldoDTO> log = buscarTipo(ultimos, TIPO_LOG);
            if (log.isEmpty()) {
                hallazgos.add(new HallazgoRespaldoDTO(
                        ADVERTENCIA,
                        "Modelo FULL sin respaldos del log",
                        "La base usa el modelo de recuperación FULL pero no tiene respaldos del log de transacciones: "
                                + "el log puede crecer sin límite y solo se podría recuperar hasta el último respaldo completo."));
            } else if (Math.max(0, log.get().minutosTranscurridos()) >= MINUTOS_LOG_ADVERTENCIA) {
                hallazgos.add(new HallazgoRespaldoDTO(
                        ADVERTENCIA,
                        "Respaldo del log desactualizado",
                        "El último respaldo del log fue " + formatearAntiguedad(log.get().minutosTranscurridos())
                                + ". En el modelo FULL conviene respaldarlo con frecuencia para limitar la pérdida de datos."));
            }
        }

        agregarDanados(hallazgos, historial);

        if (hallazgos.isEmpty()) {
            String detalle = completo
                    .map(c -> "El último respaldo completo fue " + formatearAntiguedad(c.minutosTranscurridos())
                            + " y no se detectaron respaldos dañados ni atrasos en el respaldo del log.")
                    .orElse("No se detectaron condiciones de riesgo.");
            hallazgos.add(new HallazgoRespaldoDTO(OK, "Respaldos en condiciones adecuadas", detalle));
        }

        return ordenar(hallazgos);
    }

    public static String formatearAntiguedad(long minutos) {
        long m = Math.max(0, minutos);
        if (m < 60) {
            return "hace " + m + (m == 1 ? " minuto" : " minutos");
        }
        if (m < 1440) {
            long horas = m / 60;
            return "hace " + horas + (horas == 1 ? " hora" : " horas");
        }
        long dias = m / 1440;
        return "hace " + dias + (dias == 1 ? " día" : " días");
    }

    private static void agregarDanados(List<HallazgoRespaldoDTO> hallazgos, List<RespaldoDTO> historial) {
        long danados = historial.stream().filter(r -> ESTADO_DANADO.equals(r.estado())).count();
        if (danados > 0) {
            hallazgos.add(new HallazgoRespaldoDTO(
                    CRITICO,
                    "Respaldos dañados",
                    danados + (danados == 1 ? " respaldo del historial se registró como dañado."
                            : " respaldos del historial se registraron como dañados.")
                            + " Verifíquelos con RESTORE VERIFYONLY antes de confiar en ellos."));
        }
    }

    private static Optional<UltimoRespaldoDTO> buscarTipo(List<UltimoRespaldoDTO> ultimos, String tipo) {
        return ultimos.stream().filter(u -> tipo.equals(u.tipo())).findFirst();
    }

    private static List<HallazgoRespaldoDTO> ordenar(List<HallazgoRespaldoDTO> hallazgos) {
        hallazgos.sort(Comparator.comparingInt(h -> rango(h.nivel())));
        return hallazgos;
    }

    private static int rango(String nivel) {
        return switch (nivel) {
            case CRITICO -> 0;
            case ADVERTENCIA -> 1;
            default -> 2;
        };
    }
}