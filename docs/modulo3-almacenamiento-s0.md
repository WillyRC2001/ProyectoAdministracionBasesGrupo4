# Módulo 3 — S0 (Preparación)

**Fecha:** 2026-10-19
**Rama:** feature/modulo-almacenamiento

## Decisiones tomadas

1. **Historial de crecimiento:** tabla propia `dbo.AlmacenamientoSnapshot`, alimentada por una tarea
   programada (cada hora). Se inicia desde el primer día para contar con datos reales en la presentación.
2. **Anti-duplicados:** no se inserta un snapshot si ya hay uno de los últimos 30 minutos para esa base.
3. **Estructura:** misma organización que el Módulo 2 (`dto/almacenamiento`, repository, service,
   controller de vista + API JSON, manejador de errores propio).
4. **Seguridad:** solo catálogos de SQL Server, sin SQL dinámico con entrada de usuario, credenciales por
   variables de entorno, errores sin trazas internas.

## Pendiente de permisos

- `proyecto_grupo4` no puede crear tablas: el script `database/scripts/modulo3_snapshots.sql` se ejecuta
  con un usuario administrador.

## Plan de secciones

S0 (este commit), S1 DTOs, S2 Repositorio (tamaño, archivos, snapshots), S3 Filegroups, S4 Objetos de mayor
tamaño, S5 Servicio + interpretación, S6 Controladores + API + ExceptionHandler, S7 Vista, S8 Gráficos, S9 Pruebas + documentación.