/* ============================================================================
 * Modulo 2 - Monitoreo de rendimiento
 * Consultas administrativas sobre SQL Server (solo lectura).
 *
 * Uso: ejecutar en SSMS o SQLCMD sobre la instancia monitoreada para
 * verificar resultados y medir tiempos de respuesta (evidencia de pruebas).
 *
 * Permisos requeridos: VIEW SERVER STATE, VIEW DATABASE STATE.
 * ========================================================================== */

/* 1) Consultas con mayor consumo de CPU (top 15). -------------------------- */
SELECT TOP (15)
       DB_NAME(st.dbid) AS base_datos,
       SUBSTRING(st.text,
            (qs.statement_start_offset / 2) + 1,
            (CASE qs.statement_end_offset
                 WHEN -1 THEN DATALENGTH(st.text)
                 ELSE qs.statement_end_offset END
             - qs.statement_start_offset) / 2 + 1)                    AS consulta,
       qs.execution_count                                             AS ejecuciones,
       qs.total_worker_time / 1000.0                                  AS cpu_total_ms,
       qs.total_worker_time * 1.0 / NULLIF(qs.execution_count, 0) / 1000.0 AS cpu_promedio_ms,
       qs.total_elapsed_time / 1000.0                                 AS tiempo_total_ms,
       qs.total_elapsed_time * 1.0 / NULLIF(qs.execution_count, 0) / 1000.0 AS tiempo_promedio_ms,
       qs.total_logical_reads                                         AS lecturas_logicas,
       qs.total_physical_reads                                        AS lecturas_fisicas
FROM sys.dm_exec_query_stats AS qs
CROSS APPLY sys.dm_exec_sql_text(qs.sql_handle) AS st
WHERE qs.execution_count > 0
ORDER BY qs.total_worker_time DESC;

/* 2) Consultas con mayor tiempo de ejecucion promedio (top 15). ------------- */
SELECT TOP (15)
       DB_NAME(st.dbid) AS base_datos,
       SUBSTRING(st.text,
            (qs.statement_start_offset / 2) + 1,
            (CASE qs.statement_end_offset
                 WHEN -1 THEN DATALENGTH(st.text)
                 ELSE qs.statement_end_offset END
             - qs.statement_start_offset) / 2 + 1)                    AS consulta,
       qs.execution_count                                             AS ejecuciones,
       qs.total_elapsed_time * 1.0 / NULLIF(qs.execution_count, 0) / 1000.0 AS tiempo_promedio_ms
FROM sys.dm_exec_query_stats AS qs
CROSS APPLY sys.dm_exec_sql_text(qs.sql_handle) AS st
WHERE qs.execution_count > 0
ORDER BY qs.total_elapsed_time * 1.0 / NULLIF(qs.execution_count, 0) DESC;

/* 3) Sesiones de usuario y su consumo. ------------------------------------- */
SELECT s.session_id,
       s.login_name,
       s.host_name,
       s.program_name,
       DB_NAME(s.database_id) AS base_datos,
       s.status,
       s.cpu_time AS cpu_ms,
       s.memory_usage * 8.0 / 1024.0 AS memoria_mb,
       s.logical_reads,
       s.reads,
       CONVERT(varchar(23), s.last_request_start_time, 121) AS ultima_peticion
FROM sys.dm_exec_sessions AS s
WHERE s.is_user_process = 1
ORDER BY s.cpu_time DESC;

/* 4) Sesiones bloqueadas. --------------------------------------------------- */
SELECT r.session_id,
       r.blocking_session_id,
       r.wait_type,
       r.wait_time AS tiempo_espera_ms,
       r.status,
       r.command,
       r.cpu_time AS cpu_ms,
       DB_NAME(r.database_id) AS base_datos,
       SUBSTRING(t.text,
            (r.statement_start_offset / 2) + 1,
            (CASE r.statement_end_offset
                 WHEN -1 THEN DATALENGTH(t.text)
                 ELSE r.statement_end_offset END
             - r.statement_start_offset) / 2 + 1) AS consulta
FROM sys.dm_exec_requests AS r
OUTER APPLY sys.dm_exec_sql_text(r.sql_handle) AS t
WHERE r.blocking_session_id <> 0
  AND r.session_id <> @@SPID
ORDER BY r.wait_time DESC;

/* 5) Metricas agregadas. ---------------------------------------------------- */
SELECT COUNT_BIG(*) AS total_consultas_en_cache FROM sys.dm_exec_query_stats;

SELECT ISNULL(SUM(total_worker_time), 0) / 1000.0 AS cpu_total_ms
FROM sys.dm_exec_query_stats;

SELECT ISNULL(AVG(total_elapsed_time * 1.0 / NULLIF(execution_count, 0)), 0) / 1000.0 AS tiempo_promedio_global_ms
FROM sys.dm_exec_query_stats
WHERE execution_count > 0;

/* 6) Contexto del servidor. ------------------------------------------------- */
SELECT CAST(SERVERPROPERTY('ProductVersion') AS varchar(50)) AS version,
       si.cpu_count AS nucleos,
       si.physical_memory_kb / 1024.0 AS memoria_mb,
       ISNULL((SELECT CAST(value_in_use AS int)
               FROM sys.configurations
               WHERE name = N'user connections'), 0) AS conexiones
FROM sys.dm_os_sys_info AS si;
