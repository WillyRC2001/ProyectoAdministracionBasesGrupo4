package com.grupo4.proyectoadministracionbases.repository;

import com.grupo4.proyectoadministracionbases.dto.respaldo.RespaldoDTO;
import com.grupo4.proyectoadministracionbases.dto.respaldo.UltimoRespaldoDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RespaldoRepository {

    private static final String SQL_MODELO_RECUPERACION = """
            SELECT recovery_model_desc
            FROM sys.databases
            WHERE name = DB_NAME()
            """;

    private static final String SQL_ULTIMOS_EXITOSOS = """
            SELECT CASE bs.type
                       WHEN 'D' THEN 'Completo'
                       WHEN 'I' THEN 'Diferencial'
                       WHEN 'L' THEN 'Log de transacciones'
                       WHEN 'F' THEN 'Archivos'
                       WHEN 'G' THEN 'Diferencial de archivos'
                       WHEN 'P' THEN 'Parcial'
                       WHEN 'Q' THEN 'Diferencial parcial'
                       ELSE bs.type
                   END AS tipo,
                   MAX(bs.backup_finish_date) AS fecha_fin,
                   DATEDIFF(MINUTE, MAX(bs.backup_finish_date), GETDATE()) AS minutos
            FROM msdb.dbo.backupset AS bs
            WHERE bs.database_name = DB_NAME()
              AND bs.is_damaged = 0
            GROUP BY bs.type
            ORDER BY MAX(bs.backup_finish_date) DESC
            """;

    private static final String SQL_HISTORIAL = """
            SELECT TOP (20)
                   bs.backup_start_date AS inicio,
                   bs.backup_finish_date AS fin,
                   CASE bs.type
                       WHEN 'D' THEN 'Completo'
                       WHEN 'I' THEN 'Diferencial'
                       WHEN 'L' THEN 'Log de transacciones'
                       WHEN 'F' THEN 'Archivos'
                       WHEN 'G' THEN 'Diferencial de archivos'
                       WHEN 'P' THEN 'Parcial'
                       WHEN 'Q' THEN 'Diferencial parcial'
                       ELSE bs.type
                   END AS tipo,
                   CASE WHEN bs.is_damaged = 1 THEN 'DANADO' ELSE 'COMPLETADO' END AS estado,
                   CAST(bs.backup_size / 1048576.0 AS DECIMAL(18,2)) AS tamano_mb,
                   CAST(ISNULL(bs.compressed_backup_size, bs.backup_size) / 1048576.0 AS DECIMAL(18,2)) AS comprimido_mb,
                   DATEDIFF(SECOND, bs.backup_start_date, bs.backup_finish_date) AS duracion_segundos,
                   bs.is_copy_only AS solo_copia,
                   mf.physical_device_name AS dispositivo
            FROM msdb.dbo.backupset AS bs
            OUTER APPLY (
                SELECT TOP (1) f.physical_device_name
                FROM msdb.dbo.backupmediafamily AS f
                WHERE f.media_set_id = bs.media_set_id
                ORDER BY f.family_sequence_number
            ) AS mf
            WHERE bs.database_name = DB_NAME()
            ORDER BY bs.backup_finish_date DESC
            """;

    private final JdbcTemplate jdbcTemplate;

    public RespaldoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String obtenerModeloRecuperacion() {
        List<String> modelos = jdbcTemplate.query(SQL_MODELO_RECUPERACION,
                (rs, i) -> rs.getString("recovery_model_desc"));
        return modelos.isEmpty() ? "DESCONOCIDO" : modelos.get(0);
    }

    public List<UltimoRespaldoDTO> listarUltimosExitosos() {
        return jdbcTemplate.query(SQL_ULTIMOS_EXITOSOS, (rs, i) -> new UltimoRespaldoDTO(
                rs.getString("tipo"),
                rs.getTimestamp("fecha_fin").toLocalDateTime(),
                rs.getLong("minutos")));
    }

    public List<RespaldoDTO> listarHistorial() {
        return jdbcTemplate.query(SQL_HISTORIAL, (rs, i) -> new RespaldoDTO(
                rs.getTimestamp("inicio").toLocalDateTime(),
                rs.getTimestamp("fin").toLocalDateTime(),
                rs.getString("tipo"),
                rs.getString("estado"),
                rs.getBigDecimal("tamano_mb"),
                rs.getBigDecimal("comprimido_mb"),
                rs.getLong("duracion_segundos"),
                rs.getBoolean("solo_copia"),
                rs.getString("dispositivo")));
    }
}