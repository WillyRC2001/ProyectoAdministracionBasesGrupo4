# Módulo 3: Gestión del almacenamiento. Consultas administrativas

Todas las consultas se ejecutan sobre la base monitoreada y no reciben parámetros del usuario.
Permisos requeridos para `proyecto_grupo4`: `db_datareader`, `db_datawriter` y `VIEW DATABASE STATE`.

| Funcionalidad | Fuente en SQL Server | Método del repositorio |
|---|---|---|
| Tamaño de la base de datos | `sys.database_files` (suma de `size`, 1 página = 8 KB, separado datos/log) | `obtenerTamanoBaseDatos` |
| Filegroups: usado y disponible | `sys.filegroups` + `sys.database_files` + `FILEPROPERTY(name, 'SpaceUsed')` | `listarFilegroups` |
| Archivos de datos y log | `sys.database_files` + `sys.filegroups` | `listarArchivos` |
| Objetos de mayor tamaño | `sys.tables` + `sys.schemas` + `sys.dm_db_partition_stats` | `listarObjetosMayores` |
| Crecimiento | Tabla `dbo.AlmacenamientoSnapshot`, alimentada cada hora por `SnapshotAlmacenamientoService` | `guardarSnapshot`, `listarSnapshots` |

## Objetos de mayor tamaño

- Datos: `in_row_data_page_count + lob_used_page_count + row_overflow_used_page_count` de los índices 0 y 1 (heap o clustered).
- Índices: `used_page_count` total menos las páginas de datos.
- Reservado: `reserved_page_count` total. Se ordena por este valor y se muestran las 10 primeras tablas de usuario.

## Crecimiento

- Se compara el espacio realmente usado en datos entre la primera y la última captura.
- El ritmo en MB/día solo se calcula si el intervalo es de al menos una hora; con menos tiempo no es representativo.
- Anti-duplicados: no se inserta una captura si existe otra de los últimos 30 minutos.

## Interpretación

| Condición | Nivel |
|---|---|
| Uso de filegroup o archivo de 70 % o más | Advertencia |
| Uso de filegroup o archivo de 85 % o más | Crítico |
| Tamaño del archivo de 90 % o más de su máximo | Crítico |
| Autogrowth porcentual | Advertencia |
| Filegroup sin archivos | Advertencia |
| Ninguna de las anteriores | OK |

## Scripts

- `database/scripts/modulo3_snapshots.sql`: tabla de historial.
- `database/scripts/modulo3_filegroups.sql`: filegroup `FG_DATOS` con un archivo adicional.
- `database/scripts/modulo3_datos_prueba.sql`: tabla con datos reales para generar crecimiento y captura manual.