package com.grupo4.proyectoadministracionbases.service;

import com.grupo4.proyectoadministracionbases.dto.almacenamiento.ArchivoBaseDatosDTO;
import com.grupo4.proyectoadministracionbases.dto.almacenamiento.CrecimientoDTO;
import com.grupo4.proyectoadministracionbases.dto.almacenamiento.FilegroupDTO;
import com.grupo4.proyectoadministracionbases.dto.almacenamiento.HallazgoAlmacenamientoDTO;
import com.grupo4.proyectoadministracionbases.dto.almacenamiento.SnapshotAlmacenamientoDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class InterpretacionAlmacenamiento {

    public static final String OK = "OK";
    public static final String ADVERTENCIA = "ADVERTENCIA";
    public static final String CRITICO = "CRITICO";

    private static final double UMBRAL_ADVERTENCIA = 70.0;
    private static final double UMBRAL_CRITICO = 85.0;
    private static final double UMBRAL_LIMITE_MAXIMO = 90.0;
    private static final long SEGUNDOS_MINIMOS_RITMO = 3600;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private InterpretacionAlmacenamiento() {
    }

    public static List<HallazgoAlmacenamientoDTO> generarHallazgos(List<FilegroupDTO> filegroups,
                                                                   List<ArchivoBaseDatosDTO> archivos) {
        List<HallazgoAlmacenamientoDTO> hallazgos = new ArrayList<>();

        for (FilegroupDTO filegroup : filegroups) {
            if (filegroup.archivos() == 0) {
                hallazgos.add(new HallazgoAlmacenamientoDTO(
                        ADVERTENCIA,
                        "Filegroup sin archivos: " + filegroup.nombre(),
                        "El filegroup no tiene archivos asociados, por lo que no puede almacenar objetos."));
                continue;
            }
            agregarPorUso(hallazgos, "Filegroup " + filegroup.nombre(),
                    porcentaje(filegroup.usadoMb(), filegroup.tamanoMb()),
                    "del espacio asignado está en uso. Conviene ampliar el filegroup o agregar un archivo.");
        }

        for (ArchivoBaseDatosDTO archivo : archivos) {
            agregarPorUso(hallazgos, "Archivo " + archivo.nombreLogico(),
                    porcentaje(archivo.usadoMb(), archivo.tamanoMb()),
                    "del tamaño del archivo está en uso. Si se llena, dependerá del autogrowth.");

            if (archivo.maxMb() != null && archivo.maxMb().signum() > 0) {
                double cercaniaLimite = porcentaje(archivo.tamanoMb(), archivo.maxMb());
                if (cercaniaLimite >= UMBRAL_LIMITE_MAXIMO) {
                    hallazgos.add(new HallazgoAlmacenamientoDTO(
                            CRITICO,
                            "Archivo " + archivo.nombreLogico() + " cerca de su límite máximo",
                            String.format(Locale.ROOT,
                                    "El archivo ya alcanzó el %.1f %% de su tamaño máximo; no podrá crecer mucho más.",
                                    cercaniaLimite)));
                }
            }

            if (archivo.crecimientoPorcentual()) {
                hallazgos.add(new HallazgoAlmacenamientoDTO(
                        ADVERTENCIA,
                        "Archivo " + archivo.nombreLogico() + " con crecimiento porcentual",
                        "El autogrowth porcentual produce incrementos cada vez mayores; se recomienda un valor fijo en MB."));
            }
        }

        if (hallazgos.isEmpty()) {
            hallazgos.add(new HallazgoAlmacenamientoDTO(
                    OK,
                    "Almacenamiento en condiciones adecuadas",
                    "Ningún filegroup ni archivo supera el 70 % de uso y los archivos tienen margen de crecimiento."));
        }

        hallazgos.sort(Comparator.comparingInt(h -> rango(h.nivel())));
        return hallazgos;
    }

    public static CrecimientoDTO calcularCrecimiento(String baseDatos, List<SnapshotAlmacenamientoDTO> historial) {
        List<SnapshotAlmacenamientoDTO> validos = historial.stream()
                .filter(s -> s.usadoDatosMb() != null)
                .toList();

        if (validos.size() < 2) {
            return new CrecimientoDTO(baseDatos, historial.size(), null, null, null, null, historial,
                    "Aún no hay suficientes capturas con espacio usado para calcular el crecimiento. "
                            + "El sistema registra una captura cada hora.");
        }

        SnapshotAlmacenamientoDTO primero = validos.get(0);
        SnapshotAlmacenamientoDTO ultimo = validos.get(validos.size() - 1);
        BigDecimal crecimiento = ultimo.usadoDatosMb().subtract(primero.usadoDatosMb());
        long segundos = Duration.between(primero.fechaCaptura(), ultimo.fechaCaptura()).getSeconds();

        BigDecimal mbPorDia = null;
        if (segundos >= SEGUNDOS_MINIMOS_RITMO) {
            mbPorDia = crecimiento
                    .multiply(BigDecimal.valueOf(86400))
                    .divide(BigDecimal.valueOf(segundos), 2, RoundingMode.HALF_UP);
        }

        return new CrecimientoDTO(baseDatos, historial.size(), primero.fechaCaptura(), ultimo.fechaCaptura(),
                crecimiento, mbPorDia, historial,
                redactarInterpretacion(primero, ultimo, crecimiento, mbPorDia, validos.size()));
    }

    private static String redactarInterpretacion(SnapshotAlmacenamientoDTO primero,
                                                 SnapshotAlmacenamientoDTO ultimo,
                                                 BigDecimal crecimiento,
                                                 BigDecimal mbPorDia,
                                                 int capturas) {
        String periodo = "entre " + primero.fechaCaptura().format(FORMATO_FECHA)
                + " y " + ultimo.fechaCaptura().format(FORMATO_FECHA)
                + " (" + capturas + " capturas)";

        if (crecimiento.signum() == 0) {
            return "El espacio usado en datos no ha cambiado " + periodo + ".";
        }
        if (crecimiento.signum() < 0) {
            return "El espacio usado en datos disminuyó " + crecimiento.abs().toPlainString()
                    + " MB " + periodo + ", lo que sugiere depuración de datos o reorganización de índices.";
        }
        String ritmo = mbPorDia == null
                ? " El intervalo es menor a una hora, por lo que el ritmo diario aún no es representativo."
                : " Ritmo estimado: " + mbPorDia.toPlainString() + " MB por día.";
        return "El espacio usado en datos creció " + crecimiento.toPlainString() + " MB " + periodo + "." + ritmo;
    }

    private static void agregarPorUso(List<HallazgoAlmacenamientoDTO> hallazgos, String sujeto,
                                      double porcentaje, String explicacion) {
        if (porcentaje >= UMBRAL_CRITICO) {
            hallazgos.add(new HallazgoAlmacenamientoDTO(CRITICO, sujeto + " con uso crítico",
                    String.format(Locale.ROOT, "El %.1f %% %s", porcentaje, explicacion)));
        } else if (porcentaje >= UMBRAL_ADVERTENCIA) {
            hallazgos.add(new HallazgoAlmacenamientoDTO(ADVERTENCIA, sujeto + " con uso elevado",
                    String.format(Locale.ROOT, "El %.1f %% %s", porcentaje, explicacion)));
        }
    }

    private static double porcentaje(BigDecimal parte, BigDecimal total) {
        if (parte == null || total == null || total.signum() <= 0) {
            return 0.0;
        }
        return parte.multiply(BigDecimal.valueOf(100))
                .divide(total, 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private static int rango(String nivel) {
        return switch (nivel) {
            case CRITICO -> 0;
            case ADVERTENCIA -> 1;
            default -> 2;
        };
    }
}