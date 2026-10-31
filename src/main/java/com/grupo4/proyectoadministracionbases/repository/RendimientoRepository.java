package com.grupo4.proyectoadministracionbases.repository;

import com.grupo4.proyectoadministracionbases.dto.rendimiento.BloqueoDTO;
import com.grupo4.proyectoadministracionbases.dto.rendimiento.ConsultaConsumoDTO;
import com.grupo4.proyectoadministracionbases.dto.rendimiento.ContextoInstanciaDTO;
import com.grupo4.proyectoadministracionbases.dto.rendimiento.SesionDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Acceso a datos del Módulo 2 (Monitoreo de rendimiento).
 *
 * <p>Todas las consultas son de <strong>solo lectura</strong> y se ejecutan
 * directamente sobre las vistas de administración dinámica (DMV) de SQL
 * Server. No se construye SQL a partir de datos ingresados por el usuario:
 * los únicos parámetros son límites numéricos controlados por la aplicación.</p>
 */
@Repository
public class RendimientoRepository {

    /** Longitud máxima del texto de una consulta antes de truncarlo para la vista. */
    private static final int MAX_LONGITUD_CONSULTA = 600;

    private static final String SQL_CONSULTAS_BASE = """
            SELECT TOP (?)
                   COALESCE(DB_NAME(st.dbid),
                            DB_NAME((SELECT TOP (1) CAST(pa.value AS int)
                                     FROM sys.dm_exec_plan_attributes(qs.plan_handle) AS pa
                                     WHERE pa.attribute = N'dbid'))) AS base_datos,
                   CONVERT(nvarchar(max), SUBSTRING(st.text,
                        (qs.statement_start_offset / 2) + 1,
                        (CASE qs.statement_end_offset
                             WHEN -1 THEN DATALENGTH(st.text)
                             ELSE qs.statement_end_offset END
                         - qs.statement_start_offset) / 2 + 1)) AS consulta,
                   qs.execution_count AS ejecuciones,
                   qs.total_worker_time AS cpu_total_micros,
                   qs.total_worker_time * 1.0 / NULLIF(qs.execution_count, 0) AS cpu_promedio_micros,
                   qs.total_elapsed_time AS tiempo_total_micros,
                   qs.total_elapsed_time * 1.0 / NULLIF(qs.execution_count, 0) AS tiempo_promedio_micros,
                   qs.total_logical_reads AS lecturas_logicas,
                   qs.total_physical_reads AS lecturas_fisicas,
                   qs.total_logical_reads * 1.0 / NULLIF(qs.execution_count, 0) AS lecturas_promedio
            FROM sys.dm_exec_query_stats AS qs
            CROSS APPLY sys.dm_exec_sql_text(qs.sql_handle) AS st
            WHERE qs.execution_count > 0
             AND st.text NOT LIKE N'%dm_exec_query_stats%'
             AND st.text NOT LIKE N'%dm_exec_sessions%'
             AND st.text NOT LIKE N'%dm_exec_requests%'
             AND st.text NOT LIKE N'%dm_os_sys_info%'
            """;

    private static final String SQL_CONSULTAS_MAYOR_CPU =
            SQL_CONSULTAS_BASE + " ORDER BY qs.total_worker_time DESC";

    private static final String SQL_CONSULTAS_MAYOR_TIEMPO =
            SQL_CONSULTAS_BASE
                    + " ORDER BY qs.total_elapsed_time * 1.0 / NULLIF(qs.execution_count, 0) DESC";

    private static final String SQL_SESIONES = """
            SELECT s.session_id,
                   s.login_name,
                   s.host_name,
                   s.program_name,
                   DB_NAME(s.database_id) AS base_datos,
                   s.status,
                   s.cpu_time AS cpu_ms,
                   s.memory_usage * 8.0 / 1024.0 AS memoria_mb,
                   s.logical_reads AS lecturas_logicas,
                   s.reads AS lecturas_fisicas,
                   CONVERT(varchar(23), s.last_request_start_time, 121) AS ultima_peticion
            FROM sys.dm_exec_sessions AS s
            WHERE s.is_user_process = 1
            AND s.session_id <> @@SPID
            ORDER BY s.cpu_time DESC
            """;

    private static final String SQL_BLOQUEOS = """
            SELECT r.session_id,
                   r.blocking_session_id,
                   r.wait_type,
                   r.wait_time AS tiempo_espera_ms,
                   r.status,
                   r.command,
                   r.cpu_time AS cpu_ms,
                   DB_NAME(r.database_id) AS base_datos,
                   CONVERT(nvarchar(max), SUBSTRING(t.text,
                        (r.statement_start_offset / 2) + 1,
                        (CASE r.statement_end_offset
                             WHEN -1 THEN DATALENGTH(t.text)
                             ELSE r.statement_end_offset END
                         - r.statement_start_offset) / 2 + 1)) AS consulta
            FROM sys.dm_exec_requests AS r
            OUTER APPLY sys.dm_exec_sql_text(r.sql_handle) AS t
            WHERE r.blocking_session_id <> 0
              AND r.session_id <> @@SPID
            ORDER BY r.wait_time DESC
            """;

    private static final String SQL_CONTAR_CONSULTAS =
            "SELECT COUNT_BIG(*) FROM sys.dm_exec_query_stats";

    private static final String SQL_SUMA_CPU =
            "SELECT ISNULL(SUM(total_worker_time), 0) / 1000.0 FROM sys.dm_exec_query_stats";

    private static final String SQL_PROMEDIO_TIEMPO = """
           SELECT ISNULL(SUM(total_elapsed_time) * 1.0 / NULLIF(SUM(execution_count), 0), 0) / 1000.0
            FROM sys.dm_exec_query_stats
            WHERE execution_count > 0
            """;

    private static final String SQL_CONTEXTO = """
            SELECT CAST(SERVERPROPERTY('ProductVersion') AS varchar(50)) AS version,
                   si.cpu_count AS nucleos,
                   si.physical_memory_kb / 1024.0 AS memoria_mb,
                   ISNULL((SELECT CAST(value_in_use AS int)
                           FROM sys.configurations
                           WHERE name = N'user connections'), 0) AS conexiones
            FROM sys.dm_os_sys_info AS si
            """;

    private final JdbcTemplate jdbcTemplate;

    public RendimientoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Consultas con mayor consumo acumulado de CPU.
     *
     * @param limite número máximo de filas a devolver.
     */
    public List<ConsultaConsumoDTO> buscarConsultasMayorCpu(int limite) {
        return jdbcTemplate.query(SQL_CONSULTAS_MAYOR_CPU, new ConsultaConsumoRowMapper(), limite);
    }

    /**
     * Consultas con mayor tiempo de ejecución promedio.
     *
     * @param limite número máximo de filas a devolver.
     */
    public List<ConsultaConsumoDTO> buscarConsultasMayorTiempoPromedio(int limite) {
        return jdbcTemplate.query(SQL_CONSULTAS_MAYOR_TIEMPO, new ConsultaConsumoRowMapper(), limite);
    }

    /** Sesiones de usuario activas en la instancia, ordenadas por consumo de CPU. */
    public List<SesionDTO> buscarSesiones() {
        return jdbcTemplate.query(SQL_SESIONES, new SesionRowMapper());
    }

    /** Sesiones bloqueadas actualmente. */
    public List<BloqueoDTO> buscarBloqueos() {
        return jdbcTemplate.query(SQL_BLOQUEOS, new BloqueoRowMapper());
    }

    /** Cantidad de consultas registradas en la caché de planes. */
    public long contarConsultasEnCache() {
        Long total = jdbcTemplate.queryForObject(SQL_CONTAR_CONSULTAS, Long.class);
        return total == null ? 0L : total;
    }

    /** Suma del CPU acumulado por todas las consultas, en milisegundos. */
    public double sumaCpuTotalMs() {
        Double total = jdbcTemplate.queryForObject(SQL_SUMA_CPU, Double.class);
        return total == null ? 0d : total;
    }

    /** Tiempo de ejecución promedio global de las consultas, en milisegundos. */
    public double promedioTiempoEjecucionMs() {
        Double promedio = jdbcTemplate.queryForObject(SQL_PROMEDIO_TIEMPO, Double.class);
        return promedio == null ? 0d : promedio;
    }

    /** Contexto del servidor (versión, CPU, memoria, conexiones). */
    public ContextoInstanciaDTO obtenerContexto() {
        ContextoInstanciaDTO contexto = jdbcTemplate.queryForObject(SQL_CONTEXTO, new ContextoRowMapper());
        return contexto == null
                ? new ContextoInstanciaDTO("desconocida", 0, 0d, 0)
                : contexto;
    }

    /**
     * Trunca un texto largo para su presentación segura en la interfaz.
     *
     * @param texto texto original.
     * @param max   longitud máxima.
     * @return texto truncado con elipsis si excede el máximo.
     */
    static String truncar(String texto, int max) {
        if (texto == null) {
            return "";
        }
        String limpio = texto.strip();
        if (limpio.length() <= max) {
            return limpio;
        }
        return limpio.substring(0, max) + "…";
    }

    private static double microsAMs(double micros) {
        return micros / 1000.0;
    }

    /** RowMapper para las consultas de {@code sys.dm_exec_query_stats}. */
    private static final class ConsultaConsumoRowMapper implements RowMapper<ConsultaConsumoDTO> {
        @Override
        public ConsultaConsumoDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ConsultaConsumoDTO(
                    rs.getString("base_datos"),
                    truncar(rs.getString("consulta"), MAX_LONGITUD_CONSULTA),
                    rs.getLong("ejecuciones"),
                    microsAMs(rs.getDouble("cpu_total_micros")),
                    microsAMs(rs.getDouble("cpu_promedio_micros")),
                    microsAMs(rs.getDouble("tiempo_total_micros")),
                    microsAMs(rs.getDouble("tiempo_promedio_micros")),
                    rs.getLong("lecturas_logicas"),
                    rs.getLong("lecturas_fisicas"),
                    rs.getDouble("lecturas_promedio"),
                    0d);
        }
    }

    /** RowMapper para las sesiones de {@code sys.dm_exec_sessions}. */
    private static final class SesionRowMapper implements RowMapper<SesionDTO> {
        @Override
        public SesionDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new SesionDTO(
                    rs.getInt("session_id"),
                    rs.getString("login_name"),
                    rs.getString("host_name"),
                    rs.getString("program_name"),
                    rs.getString("base_datos"),
                    rs.getString("status"),
                    rs.getLong("cpu_ms"),
                    rs.getDouble("memoria_mb"),
                    rs.getLong("lecturas_logicas"),
                    rs.getLong("lecturas_fisicas"),
                    rs.getString("ultima_peticion"));
        }
    }

    /** RowMapper para los bloqueos de {@code sys.dm_exec_requests}. */
    private static final class BloqueoRowMapper implements RowMapper<BloqueoDTO> {
        @Override
        public BloqueoDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new BloqueoDTO(
                    rs.getInt("session_id"),
                    rs.getInt("blocking_session_id"),
                    rs.getString("wait_type"),
                    rs.getDouble("tiempo_espera_ms"),
                    rs.getString("status"),
                    rs.getString("command"),
                    rs.getLong("cpu_ms"),
                    rs.getString("base_datos"),
                    truncar(rs.getString("consulta"), MAX_LONGITUD_CONSULTA));
        }
    }

    /** RowMapper para el contexto de {@code sys.dm_os_sys_info}. */
    private static final class ContextoRowMapper implements RowMapper<ContextoInstanciaDTO> {
        @Override
        public ContextoInstanciaDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ContextoInstanciaDTO(
                    rs.getString("version"),
                    rs.getInt("nucleos"),
                    rs.getDouble("memoria_mb"),
                    rs.getInt("conexiones"));
        }
    }
}
