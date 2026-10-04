package com.grupo4.proyectoadministracionbases.repository;

import com.grupo4.proyectoadministracionbases.dto.EstadoInstanciaDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EstadoRepository {

    private final JdbcTemplate jdbcTemplate;

    public EstadoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public EstadoInstanciaDTO obtenerEstadoInstancia() {

        String sql = """
            SELECT
                SERVERPROPERTY('MachineName') AS servidor,
                SERVERPROPERTY('InstanceName') AS instancia,
                SERVERPROPERTY('ProductVersion') AS version,
                SERVERPROPERTY('Edition') AS edicion,
                sqlserver_start_time AS fecha_inicio,
                DATEDIFF(SECOND, sqlserver_start_time, GETDATE()) AS segundos_actividad,
                CAST(committed_kb / 1024.0 AS DECIMAL(12,2)) AS memoria_utilizada_mb,
                CAST(committed_target_kb / 1024.0 AS DECIMAL(12,2)) AS memoria_asignada_mb
            FROM sys.dm_os_sys_info
            """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {

            EstadoInstanciaDTO estado = new EstadoInstanciaDTO();

            estado.setServidor(rs.getString("servidor"));

            String instancia = rs.getString("instancia");

            if (instancia == null) {
                instancia = "MSSQLSERVER";
            }

            estado.setInstancia(instancia);

            estado.setVersion(rs.getString("version"));
            estado.setEdicion(rs.getString("edicion"));

            estado.setFechaInicio(
                    rs.getTimestamp("fecha_inicio").toLocalDateTime()
            );

            estado.setSegundosActividad(
                    rs.getLong("segundos_actividad")
            );

            estado.setMemoriaUtilizadaMB(
                    rs.getDouble("memoria_utilizada_mb")
            );

            estado.setMemoriaAsignadaMB(
                    rs.getDouble("memoria_asignada_mb")
            );

            return estado;
        });
    }

    public List<String> obtenerBasesDatos() {

        String sql = """
            SELECT
                name AS nombre_base_datos
            FROM sys.databases
            ORDER BY name
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) ->
                        rs.getString("nombre_base_datos")
        );
    }

    public String obtenerEstadoServicio() {

        String sql = """
            SELECT TOP 1
                status_desc
            FROM sys.dm_server_services
            WHERE servicename LIKE 'SQL Server (%'
            """;

        return jdbcTemplate.queryForObject(
                sql,
                String.class
        );
    }
}
