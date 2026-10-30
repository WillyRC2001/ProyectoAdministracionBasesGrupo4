package com.grupo4.proyectoadministracionbases.repository;

import com.grupo4.proyectoadministracionbases.dto.auditoria.AsignacionRolDTO;
import com.grupo4.proyectoadministracionbases.dto.auditoria.ObjetoInvalidoDTO;
import com.grupo4.proyectoadministracionbases.dto.auditoria.PermisoDTO;
import com.grupo4.proyectoadministracionbases.dto.auditoria.PrincipalDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AuditoriaRepository {

    private static final String SQL_BASE_DATOS = "SELECT DB_NAME()";

    private static final String SQL_LOGINS = """
            SELECT name,
                   type_desc,
                   is_disabled,
                   create_date,
                   default_database_name
            FROM sys.server_principals
            WHERE type IN ('S', 'U', 'G', 'E', 'X')
              AND name NOT LIKE N'##%'
            ORDER BY is_disabled, name
            """;

    private static final String SQL_USUARIOS_BD = """
            SELECT dp.name,
                   dp.type_desc,
                   dp.default_schema_name,
                   dp.create_date,
                   sp.name AS login_asociado
            FROM sys.database_principals AS dp
            LEFT JOIN sys.server_principals AS sp ON sp.sid = dp.sid
            WHERE dp.type IN ('S', 'U', 'G', 'E', 'X')
              AND dp.name NOT IN (N'INFORMATION_SCHEMA', N'sys')
            ORDER BY dp.name
            """;

    private static final String SQL_ROLES = """
            SELECT N'SERVIDOR' AS ambito,
                   m.name AS miembro,
                   r.name AS rol
            FROM sys.server_role_members AS srm
            JOIN sys.server_principals AS r ON r.principal_id = srm.role_principal_id
            JOIN sys.server_principals AS m ON m.principal_id = srm.member_principal_id
            UNION ALL
            SELECT N'BASE DE DATOS',
                   u.name,
                   r.name
            FROM sys.database_role_members AS drm
            JOIN sys.database_principals AS r ON r.principal_id = drm.role_principal_id
            JOIN sys.database_principals AS u ON u.principal_id = drm.member_principal_id
            ORDER BY 1, 2, 3
            """;

    private static final String SQL_PERMISOS = """
            SELECT TOP (500) p.ambito, p.principal, p.tipo_principal, p.estado, p.permiso, p.objeto
            FROM (
                SELECT N'BASE DE DATOS' AS ambito,
                       pr.name AS principal,
                       pr.type_desc AS tipo_principal,
                       pe.state_desc AS estado,
                       pe.permission_name AS permiso,
                       CASE pe.class
                           WHEN 0 THEN N'Base de datos ' + DB_NAME()
                           WHEN 3 THEN N'Esquema ' + SCHEMA_NAME(pe.major_id)
                           WHEN 1 THEN COALESCE(OBJECT_SCHEMA_NAME(pe.major_id) + N'.' + OBJECT_NAME(pe.major_id),
                                                N'Objeto ' + CAST(pe.major_id AS NVARCHAR(20)))
                           ELSE pe.class_desc
                       END AS objeto
                FROM sys.database_permissions AS pe
                JOIN sys.database_principals AS pr ON pr.principal_id = pe.grantee_principal_id
                WHERE pr.name <> N'public'
                  AND (pe.class <> 1 OR OBJECTPROPERTY(pe.major_id, 'IsMSShipped') = 0)
                UNION ALL
                SELECT N'SERVIDOR',
                       pr.name,
                       pr.type_desc,
                       pe.state_desc,
                       pe.permission_name,
                       N'Servidor'
                FROM sys.server_permissions AS pe
                JOIN sys.server_principals AS pr ON pr.principal_id = pe.grantee_principal_id
                WHERE pe.class = 100
                  AND pr.name <> N'public'
                  AND pr.name NOT LIKE N'##%'
            ) AS p
            ORDER BY p.ambito, p.principal, p.permiso
            """;

    private static final String SQL_OBJETOS_INVALIDOS = """
            SELECT s.name AS esquema,
                   o.name AS objeto,
                   o.type_desc AS tipo,
                   N'REFERENCIA_ROTA' AS categoria,
                   COALESCE(d.referenced_schema_name + N'.', N'') + d.referenced_entity_name AS elemento,
                   CASE WHEN d.is_ambiguous = 1
                        THEN N'Referencia ambigua: el objeto referenciado no se puede resolver de forma única'
                        ELSE N'Referencia a un objeto que no existe'
                   END AS problema
            FROM sys.sql_expression_dependencies AS d
            JOIN sys.objects AS o ON o.object_id = d.referencing_id
            JOIN sys.schemas AS s ON s.schema_id = o.schema_id
            WHERE d.referencing_class = 1
              AND d.referenced_id IS NULL
              AND d.referenced_server_name IS NULL
              AND d.referenced_database_name IS NULL
              AND o.is_ms_shipped = 0
            UNION ALL
            SELECT s.name,
                   o.name,
                   o.type_desc,
                   N'INDICE_DESHABILITADO',
                   i.name,
                   N'Índice deshabilitado: no se usa en las consultas hasta que se reconstruya'
            FROM sys.indexes AS i
            JOIN sys.objects AS o ON o.object_id = i.object_id
            JOIN sys.schemas AS s ON s.schema_id = o.schema_id
            WHERE i.is_disabled = 1
              AND o.is_ms_shipped = 0
              AND o.type IN ('U', 'V')
            UNION ALL
            SELECT s.name,
                   o.name,
                   o.type_desc,
                   N'RESTRICCION_NO_CONFIABLE',
                   k.name,
                   CASE WHEN k.is_disabled = 1
                        THEN N'Restricción deshabilitada'
                        ELSE N'Restricción no confiable: el optimizador no puede usarla'
                   END
            FROM (
                SELECT name, parent_object_id, is_disabled, is_not_trusted FROM sys.foreign_keys
                UNION ALL
                SELECT name, parent_object_id, is_disabled, is_not_trusted FROM sys.check_constraints
            ) AS k
            JOIN sys.objects AS o ON o.object_id = k.parent_object_id
            JOIN sys.schemas AS s ON s.schema_id = o.schema_id
            WHERE (k.is_disabled = 1 OR k.is_not_trusted = 1)
              AND o.is_ms_shipped = 0
            UNION ALL
            SELECT s.name,
                   o.name,
                   o.type_desc,
                   N'TRIGGER_DESHABILITADO',
                   o.name,
                   N'Trigger deshabilitado'
            FROM sys.triggers AS tr
            JOIN sys.objects AS o ON o.object_id = tr.object_id
            JOIN sys.schemas AS s ON s.schema_id = o.schema_id
            WHERE tr.is_disabled = 1
              AND tr.parent_class = 1
              AND o.is_ms_shipped = 0
            ORDER BY 1, 2, 4
            """;

    private final JdbcTemplate jdbcTemplate;

    public AuditoriaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String obtenerNombreBaseDatos() {
        return jdbcTemplate.queryForObject(SQL_BASE_DATOS, String.class);
    }

    public List<PrincipalDTO> listarLogins() {
        return jdbcTemplate.query(SQL_LOGINS, (rs, i) -> new PrincipalDTO(
                rs.getString("name"),
                rs.getString("type_desc"),
                rs.getBoolean("is_disabled") ? "DESHABILITADO" : "HABILITADO",
                rs.getTimestamp("create_date").toLocalDateTime(),
                "Base de datos por defecto: " + rs.getString("default_database_name")));
    }

    public List<PrincipalDTO> listarUsuariosBaseDatos() {
        return jdbcTemplate.query(SQL_USUARIOS_BD, (rs, i) -> {
            String login = rs.getString("login_asociado");
            return new PrincipalDTO(
                    rs.getString("name"),
                    rs.getString("type_desc"),
                    "ACTIVO",
                    rs.getTimestamp("create_date").toLocalDateTime(),
                    "Esquema: " + rs.getString("default_schema_name")
                            + " | Login: " + (login == null ? "sin login asociado" : login));
        });
    }

    public List<AsignacionRolDTO> listarAsignacionesRoles() {
        return jdbcTemplate.query(SQL_ROLES, (rs, i) -> new AsignacionRolDTO(
                rs.getString("ambito"),
                rs.getString("miembro"),
                rs.getString("rol")));
    }

    public List<PermisoDTO> listarPermisos() {
        return jdbcTemplate.query(SQL_PERMISOS, (rs, i) -> new PermisoDTO(
                rs.getString("ambito"),
                rs.getString("principal"),
                rs.getString("tipo_principal"),
                rs.getString("estado"),
                rs.getString("permiso"),
                rs.getString("objeto")));
    }

    public List<ObjetoInvalidoDTO> listarObjetosInvalidos() {
        return jdbcTemplate.query(SQL_OBJETOS_INVALIDOS, (rs, i) -> new ObjetoInvalidoDTO(
                rs.getString("esquema"),
                rs.getString("objeto"),
                rs.getString("tipo"),
                rs.getString("categoria"),
                rs.getString("elemento"),
                rs.getString("problema")));
    }
}