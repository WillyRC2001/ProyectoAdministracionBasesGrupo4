package com.grupo4.proyectoadministracionbases.service;

import com.grupo4.proyectoadministracionbases.dto.almacenamiento.ArchivoBaseDatosDTO;
import com.grupo4.proyectoadministracionbases.dto.almacenamiento.CrecimientoDTO;
import com.grupo4.proyectoadministracionbases.dto.almacenamiento.FilegroupDTO;
import com.grupo4.proyectoadministracionbases.dto.almacenamiento.HallazgoAlmacenamientoDTO;
import com.grupo4.proyectoadministracionbases.dto.almacenamiento.SnapshotAlmacenamientoDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InterpretacionAlmacenamientoTest {

    private static FilegroupDTO filegroup(String usado) {
        return new FilegroupDTO("PRIMARY", true, 1, new BigDecimal("8.00"),
                new BigDecimal(usado), new BigDecimal("8.00").subtract(new BigDecimal(usado)));
    }

    private static ArchivoBaseDatosDTO archivo(String usado) {
        return new ArchivoBaseDatosDTO("DB_ProyectoGrupo4", "C:\\datos\\db.mdf", "ROWS", "PRIMARY",
                new BigDecimal("8.00"), new BigDecimal(usado), new BigDecimal("-1"),
                new BigDecimal("64.00"), false);
    }

    private static SnapshotAlmacenamientoDTO snapshot(LocalDateTime fecha, String usadoDatos) {
        return new SnapshotAlmacenamientoDTO(fecha, "DB_ProyectoGrupo4", new BigDecimal("8.00"),
                new BigDecimal("8.00"), new BigDecimal(usadoDatos), new BigDecimal("1.00"));
    }

    @Test
    void sinProblemasDevuelveUnSoloHallazgoOk() {
        List<HallazgoAlmacenamientoDTO> hallazgos = InterpretacionAlmacenamiento.generarHallazgos(
                List.of(filegroup("3.69")), List.of(archivo("3.69")));

        assertThat(hallazgos).hasSize(1);
        assertThat(hallazgos.get(0).nivel()).isEqualTo(InterpretacionAlmacenamiento.OK);
    }

    @Test
    void archivoCasiLlenoGeneraHallazgoCriticoPrimero() {
        List<HallazgoAlmacenamientoDTO> hallazgos = InterpretacionAlmacenamiento.generarHallazgos(
                List.of(filegroup("3.69")), List.of(archivo("7.50")));

        assertThat(hallazgos).isNotEmpty();
        assertThat(hallazgos.get(0).nivel()).isEqualTo(InterpretacionAlmacenamiento.CRITICO);
    }

    @Test
    void usoEntreSetentaYOchentaYCincoGeneraAdvertencia() {
        List<HallazgoAlmacenamientoDTO> hallazgos = InterpretacionAlmacenamiento.generarHallazgos(
                List.of(filegroup("6.00")), List.of());

        assertThat(hallazgos.get(0).nivel()).isEqualTo(InterpretacionAlmacenamiento.ADVERTENCIA);
    }

    @Test
    void crecimientoConUnaSolaCapturaNoCalculaRitmo() {
        CrecimientoDTO crecimiento = InterpretacionAlmacenamiento.calcularCrecimiento(
                "DB_ProyectoGrupo4",
                List.of(snapshot(LocalDateTime.of(2026, 10, 22, 8, 0), "3.69")));

        assertThat(crecimiento.mbPorDia()).isNull();
        assertThat(crecimiento.crecimientoUsadoMb()).isNull();
        assertThat(crecimiento.interpretacion()).contains("suficientes");
    }

    @Test
    void crecimientoCalculaDiferenciaYRitmoPorDia() {
        CrecimientoDTO crecimiento = InterpretacionAlmacenamiento.calcularCrecimiento(
                "DB_ProyectoGrupo4",
                List.of(snapshot(LocalDateTime.of(2026, 10, 20, 8, 0), "10.00"),
                        snapshot(LocalDateTime.of(2026, 10, 22, 8, 0), "20.00")));

        assertThat(crecimiento.crecimientoUsadoMb()).isEqualByComparingTo("10.00");
        assertThat(crecimiento.mbPorDia()).isEqualByComparingTo("5.00");
    }

    @Test
    void intervaloMenorAUnaHoraNoEntregaRitmoDiario() {
        CrecimientoDTO crecimiento = InterpretacionAlmacenamiento.calcularCrecimiento(
                "DB_ProyectoGrupo4",
                List.of(snapshot(LocalDateTime.of(2026, 10, 22, 8, 0), "10.00"),
                        snapshot(LocalDateTime.of(2026, 10, 22, 8, 20), "12.00")));

        assertThat(crecimiento.crecimientoUsadoMb()).isEqualByComparingTo("2.00");
        assertThat(crecimiento.mbPorDia()).isNull();
    }
}