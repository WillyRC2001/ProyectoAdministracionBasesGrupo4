package com.grupo4.proyectoadministracionbases.repository;

import com.grupo4.proyectoadministracionbases.dto.almacenamiento.ArchivoBaseDatosDTO;
import com.grupo4.proyectoadministracionbases.dto.almacenamiento.SnapshotAlmacenamientoDTO;
import com.grupo4.proyectoadministracionbases.dto.almacenamiento.TamanoBaseDatosDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AlmacenamientoRepository {

    private static final String SQL_TAMANO_BD = """
            SELECT DB_NAME() AS base_datos,
                   CAST(SUM(CASE WHEN type = 0 THEN CAST(size AS BIGINT) ELSE 0 END) * 8 / 1024.0 AS DECIMAL(18,2)) AS datos_mb,
                   CAST(SUM(CASE WHEN type = 1 THEN CAST(size AS BIGINT) ELSE 0 END) * 8 / 1024.0 AS DECIMAL(18,2)) AS log_mb,
                   CAST(SUM(CAST(size AS BIGINT)) * 8 / 1024.0 AS DECIMAL(18,2)) AS total_mb
            FROM sys.database_files
            """;

    private static final String SQL_ARCHIVOS = """
            SELECT f.name AS nombre_logico,
                   f.physical_name AS ruta_fisica,
                   f.type_desc AS tipo,
                   fg.name AS filegroup,
                   CAST(CAST(f.size AS BIGINT) * 8 / 1024.0 AS DECIMAL(18,2)) AS tamano_mb,
                   CAST(CAST(FILEPROPERTY(f.name, 'SpaceUsed') AS BIGINT) * 8 / 1024.0 AS DECIMAL(18,2)) AS usado_mb,
                   CASE WHEN f.max_size = -1 THEN CAST(-1 AS DECIMAL(18,2))
                        ELSE CAST(CAST(f.max_size AS BIGINT) * 8 / 1024.0 AS DECIMAL(18,2)) END AS max_mb,
                   CASE WHEN f.is_percent_growth = 1 THEN CAST(f.growth AS DECIMAL(18,2))
                        ELSE CAST(CAST(f.growth AS BIGINT) * 8 / 1024.0 AS DECIMAL(18,2)) END AS crecimiento,
                   f.is_percent_growth AS crecimiento_porcentual
            FROM sys.database_files AS f
            LEFT JOIN sys.filegroups AS fg ON f.data_space_id = fg.data_space_id
            ORDER BY f.type, f.name
            """;

    private static final String SQL_INSERTAR_SNAPSHOT = """
            INSERT INTO dbo.AlmacenamientoSnapshot (BaseDatos, TamanoDatosMB, TamanoLogMB)
            SELECT x.base_datos, x.datos_mb, x.log_mb
            FROM (
                SELECT DB_NAME() AS base_datos,
                       CAST(SUM(CASE WHEN type = 0 THEN CAST(size AS BIGINT) ELSE 0 END) * 8 / 1024.0 AS DECIMAL(18,2)) AS datos_mb,
                       CAST(SUM(CASE WHEN type = 1 THEN CAST(size AS BIGINT) ELSE 0 END) * 8 / 1024.0 AS DECIMAL(18,2)) AS log_mb
                FROM sys.database_files
            ) AS x
            WHERE NOT EXISTS (
                    SELECT 1
                    FROM dbo.AlmacenamientoSnapshot AS s
                    WHERE s.BaseDatos = x.base_datos
                      AND s.FechaCaptura > DATEADD(MINUTE, -30, SYSDATETIME()))
            """;

    private static final String SQL_LISTAR_SNAPSHOTS = """
            SELECT FechaCaptura, BaseDatos, TamanoDatosMB, TamanoLogMB
            FROM dbo.AlmacenamientoSnapshot
            WHERE BaseDatos = ?
            ORDER BY FechaCaptura
            """;

    private final JdbcTemplate jdbcTemplate;

    public AlmacenamientoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public TamanoBaseDatosDTO obtenerTamanoBaseDatos() {
        return jdbcTemplate.queryForObject(SQL_TAMANO_BD, (rs, i) -> new TamanoBaseDatosDTO(
                rs.getString("base_datos"),
                rs.getBigDecimal("datos_mb"),
                rs.getBigDecimal("log_mb"),
                rs.getBigDecimal("total_mb")));
    }

    public List<ArchivoBaseDatosDTO> listarArchivos() {
        return jdbcTemplate.query(SQL_ARCHIVOS, (rs, i) -> new ArchivoBaseDatosDTO(
                rs.getString("nombre_logico"),
                rs.getString("ruta_fisica"),
                rs.getString("tipo"),
                rs.getString("filegroup"),
                rs.getBigDecimal("tamano_mb"),
                rs.getBigDecimal("usado_mb"),
                rs.getBigDecimal("max_mb"),
                rs.getBigDecimal("crecimiento"),
                rs.getBoolean("crecimiento_porcentual")));
    }

    public int guardarSnapshot() {
        return jdbcTemplate.update(SQL_INSERTAR_SNAPSHOT);
    }

    public List<SnapshotAlmacenamientoDTO> listarSnapshots(String baseDatos) {
        return jdbcTemplate.query(SQL_LISTAR_SNAPSHOTS, (rs, i) -> new SnapshotAlmacenamientoDTO(
                rs.getTimestamp("FechaCaptura").toLocalDateTime(),
                rs.getString("BaseDatos"),
                rs.getBigDecimal("TamanoDatosMB"),
                rs.getBigDecimal("TamanoLogMB")), baseDatos);
    }
}