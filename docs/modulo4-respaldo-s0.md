# Módulo 4: Respaldo y recuperación. S0 (Preparación)

**Fecha:** 2026-10-22

## Decisiones tomadas

1. **Fuente de datos:** `msdb.dbo.backupset` (historial de respaldos) y `msdb.dbo.backupmediafamily` (dispositivo), filtrados por `DB_NAME()`.
2. **Permisos mínimos:** `proyecto_grupo4` recibe solo `SELECT` sobre esas dos tablas de `msdb`, sin `db_datareader` completo. El script `database/scripts/modulo4_permisos.sql` lo ejecuta un administrador.
3. **Estructura:** misma organización que los módulos 2 y 3 (`dto/respaldo`, repository, service, controller de vista + API JSON, manejador de errores propio).
4. **Seguridad:** solo catálogos de SQL Server, sin SQL dinámico con entrada de usuario, errores sin trazas internas.

## Limitación conocida

`backupset` solo registra respaldos finalizados. Un respaldo que falla antes de terminar no genera fila.
Indicadores equivalentes documentados:

- `is_damaged = 1`: el respaldo se registró como dañado, estado `DANADO`.
- Antigüedad del último respaldo exitoso por tipo.
- Modelo de recuperación de la base (`sys.databases.recovery_model_desc`): en `FULL` sin respaldos de log hay riesgo de crecimiento del log y de pérdida de datos.

## Tipos de respaldo (`backupset.type`)

D = Completo, I = Diferencial, L = Log de transacciones, F = Archivos, G = Diferencial de archivos, P = Parcial, Q = Diferencial parcial.

## Plan de secciones

S0 (este documento), S1 DTOs, S2 Repositorio, S3 Servicio + interpretación, S4 Controladores + API + ExceptionHandler, S5 Vista, S6 Pruebas + documentación.