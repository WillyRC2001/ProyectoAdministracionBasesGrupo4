# Módulo 4 – Consultas y reglas de interpretación

## Fuente de datos
Todo sale de `msdb`, donde SQL Server registra cada respaldo:
- `msdb.dbo.backupset`: un registro por respaldo (tipo, fechas, tamaño, `is_damaged`, `is_copy_only`).
- `msdb.dbo.backupmediafamily`: destino físico del respaldo (`physical_device_name`).
- `sys.databases.recovery_model_desc`: modelo de recuperación (FULL, SIMPLE, BULK_LOGGED).

El usuario de la aplicación solo tiene `SELECT` sobre esas dos tablas (`modulo4_permisos.sql`).

## Consultas
| Indicador | Consulta | Método |
|---|---|---|
| Último respaldo exitoso (por tipo) | `MAX(backup_finish_date)` agrupado por `type`, `is_damaged = 0` | `listarUltimosExitosos()` |
| Tipo de respaldo | `type`: D completo, I diferencial, L log | traducido en el repositorio |
| Estado | `is_damaged` → COMPLETADO / DANADO | `listarHistorial()` |
| Historial | `TOP (20)` ordenado por inicio descendente + `OUTER APPLY` a `backupmediafamily` | `listarHistorial()` |
| Modelo de recuperación | `sys.databases` de la base actual | `obtenerModeloRecuperacion()` |

## Interpretación administrativa (`InterpretacionRespaldo`)
| Condición | Nivel |
|---|---|
| Sin ningún respaldo exitoso | CRITICO |
| Hay respaldos pero ninguno completo | CRITICO |
| Respaldo completo con 30 días o más | CRITICO |
| Respaldo completo con 7 días o más | ADVERTENCIA |
| Modelo FULL sin respaldos de log | ADVERTENCIA |
| Modelo FULL con último log de 24 h o más | ADVERTENCIA |
| Respaldos dañados en el historial | CRITICO |
| Ninguna de las anteriores | OK |

## Limitación documentada
SQL Server Express no incluye SQL Agent, por lo que no se pueden programar respaldos desde el motor.
El indicador equivalente es el historial real de `msdb`, que refleja los respaldos hechos manualmente
o con tareas externas.