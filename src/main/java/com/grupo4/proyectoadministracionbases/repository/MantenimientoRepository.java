package com.grupo4.proyectoadministracionbases.repository;

import com.grupo4.proyectoadministracionbases.dto.mantenimiento.EstadisticaTablaDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class MantenimientoRepository {

    private static final String SQL_ESTADISTICAS = """
            SELECT s.name AS esquema,
                   t.name AS tabla,
                   COUNT(st.stats_id) AS cantidad_estadisticas,
                   MIN(sp.last_updated) AS ultima_actualizacion,
                   ISNULL(MAX(sp.rows), 0) AS filas,
                   ISNULL(MAX(sp.modification_counter), 0) AS modificaciones
            FROM sys.tables AS t
            JOIN sys.schemas AS s ON s.schema_id = t.schema_id
            LEFT JOIN sys.stats AS st ON st.object_id = t.object_id
            OUTER APPLY sys.dm_db_stats_properties(st.object_id, st.stats_id) AS sp
            WHERE t.is_ms_shipped = 0
            GROUP BY s.name, t.name
            ORDER BY ISNULL(MAX(sp.modification_counter), 0) DESC, s.name, t.name
            """;

    // Los nombres se devuelven ya delimitados con QUOTENAME y salen del catálogo,
    // nunca del texto que llega desde el navegador.
    private static final String SQL_RESOLVER_TABLA = """
            SELECT QUOTENAME(s.name) + N'.' + QUOTENAME(t.name)
            FROM sys.tables AS t
            JOIN sys.schemas AS s ON s.schema_id = t.schema_id
            WHERE s.name = ? AND t.name = ? AND t.is_ms_shipped = 0
            """;

    private static final String SQL_RESOLVER_MODULO = """
            SELECT QUOTENAME(s.name) + N'.' + QUOTENAME(o.name)
            FROM sys.objects AS o
            JOIN sys.schemas AS s ON s.schema_id = o.schema_id
            WHERE s.name = ? AND o.name = ?
              AND o.type IN ('P', 'V', 'FN', 'IF', 'TF', 'TR')
              AND o.is_ms_shipped = 0
            """;

    private static final String SQL_RESOLVER_INDICE = """
            SELECT QUOTENAME(i.name) + N' ON ' + QUOTENAME(s.name) + N'.' + QUOTENAME(o.name)
            FROM sys.indexes AS i
            JOIN sys.objects AS o ON o.object_id = i.object_id
            JOIN sys.schemas AS s ON s.schema_id = o.schema_id
            WHERE s.name = ? AND o.name = ? AND i.name = ?
              AND i.type > 0
              AND o.type IN ('U', 'V')
              AND o.is_ms_shipped = 0
            """;

    private final JdbcTemplate jdbcTemplate;

    public MantenimientoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<EstadisticaTablaDTO> listarEstadisticasPorTabla() {
        return jdbcTemplate.query(SQL_ESTADISTICAS, (rs, i) -> {
            Timestamp ultima = rs.getTimestamp("ultima_actualizacion");
            return new EstadisticaTablaDTO(
                    rs.getString("esquema"),
                    rs.getString("tabla"),
                    rs.getInt("cantidad_estadisticas"),
                    ultima == null ? null : ultima.toLocalDateTime(),
                    rs.getLong("filas"),
                    rs.getLong("modificaciones"));
        });
    }

    public Optional<String> resolverTabla(String esquema, String tabla) {
        return primero(SQL_RESOLVER_TABLA, esquema, tabla);
    }

    public Optional<String> resolverModulo(String esquema, String objeto) {
        return primero(SQL_RESOLVER_MODULO, esquema, objeto);
    }

    public Optional<String> resolverIndice(String esquema, String tabla, String indice) {
        return primero(SQL_RESOLVER_INDICE, esquema, tabla, indice);
    }

    public void actualizarEstadisticas(String tablaDelimitada) {
        jdbcTemplate.execute("UPDATE STATISTICS " + tablaDelimitada);
    }

    public void refrescarModulo(String moduloDelimitado) {
        jdbcTemplate.update("EXEC sys.sp_refreshsqlmodule @name = ?", moduloDelimitado);
    }

    public void reconstruirIndice(String indiceYTablaDelimitados) {
        jdbcTemplate.execute("ALTER INDEX " + indiceYTablaDelimitados + " REBUILD");
    }

    private Optional<String> primero(String sql, Object... parametros) {
        List<String> filas = jdbcTemplate.query(sql, (rs, i) -> rs.getString(1), parametros);
        return filas.isEmpty() ? Optional.empty() : Optional.of(filas.get(0));
    }
}