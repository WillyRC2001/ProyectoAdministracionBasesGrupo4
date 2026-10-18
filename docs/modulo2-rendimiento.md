# Módulo 2 — Monitoreo de rendimiento

Documentación técnica del Módulo 2 del proyecto EIF402 (Administración de Bases de Datos).
Instancia monitoreada: **Microsoft SQL Server** (base `DB_ProyectoGrupo4`).

---

## 1. Objetivo y alcance

Facilitar la identificación de problemas de rendimiento del sistema gestor de bases de datos,
presentando métricas reales obtenidas de las vistas de administración dinámica (DMV) de SQL Server,
con indicadores, tablas, gráficos e interpretación de resultados.

Funcionalidades cubiertas (rúbrica del Módulo 2):

| Funcionalidad | Criterio de aceptación | Dónde se cumple |
|---|---|---|
| Consultas de mayor consumo | Ranking ordenado por consumo | `consultasMayorCpu` / `consultasMayorTiempo` |
| Tiempo de ejecución | Promedio y acumulado | Resumen + ranking por tiempo promedio |
| Sesiones activas | Presentes con información relevante | Tabla de sesiones + indicador |
| Sesiones bloqueadas | Detectadas o informadas como ausentes | Tabla de bloqueos + hallazgo |
| Consumo de CPU/memoria | Por consulta, sesión y proceso | Ranking CPU + gráfico por sesión |
| Interpretación de resultados | Nombres, unidades, filtros, ordenamiento e indicadores | Hallazgos + unidades normalizadas |

> Este módulo **no** ejecuta acciones de escritura sobre el SGBD. Todas las consultas son de lectura.

---

## 2. Arquitectura (capas)

```
SQL Server (DMVs)
  → repository/RendimientoRepository      (JdbcTemplate, solo lectura)
  → dto/rendimiento/*.java                (transporte de datos inmutable)
  → service/RendimientoService            (agregación, unidades, porcentajes)
  → service/InterpretacionRendimiento     (umbrales, hallazgos)
  → controller/RendimientoApiController   (GET JSON /rendimiento/api/panel)
  → controller/RendimientoController      (GET /rendimiento → vista)
  → templates/rendimiento/index.html + static/js/rendimiento/charts.js (Chart.js)
```

### Archivos del módulo (todos nuevos y exclusivos del Módulo 2)

| Archivo | Responsabilidad |
|---|---|
| `repository/RendimientoRepository.java` | Consultas administrativas (DMV) |
| `dto/rendimiento/ConsultaConsumoDTO.java` | Consulta + métricas de consumo |
| `dto/rendimiento/SesionDTO.java` | Sesión de usuario |
| `dto/rendimiento/BloqueoDTO.java` | Sesión bloqueada |
| `dto/rendimiento/ResumenRendimientoDTO.java` | Métricas consolidadas |
| `dto/rendimiento/ContextoInstanciaDTO.java` | CPU, memoria y versión del servidor |
| `dto/rendimiento/HallazgoDTO.java` + `NivelHallazgo.java` | Interpretación |
| `dto/rendimiento/PanelRendimientoDTO.java` | Respuesta JSON consolidada |
| `dto/rendimiento/RespuestaErrorDTO.java` | Respuesta de error controlada |
| `service/RendimientoService.java` | Lógica y normalización |
| `service/InterpretacionRendimiento.java` | Umbrales y hallazgos |
| `controller/RendimientoController.java` | Vista del panel |
| `controller/RendimientoApiController.java` | API JSON |
| `controller/RendimientoExceptionHandler.java` | Manejo de errores acotado al módulo |
| `templates/rendimiento/index.html` | Interfaz del panel |
| `static/js/rendimiento/charts.js` | Gráficos y tablas (Chart.js) |

### Dependencias agregadas (`pom.xml`)

Se incorporan como **WebJars** (servidos localmente, sin CDN):

- `org.webjars:bootstrap:5.3.8`
- `org.webjars.npm:chart.js:4.5.1`
- `org.webjars.npm:bootstrap-icons:1.13.2`
- `org.webjars:webjars-locator-lite:1.1.5`

---

## 3. Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/rendimiento` | Página del panel (Thymeleaf). |
| GET | `/rendimiento/api/panel` | JSON con todas las métricas e interpretación. |

Respuestas ante error (JSON, sin exponer detalles internos):

- `503 SGBD_NO_DISPONIBLE`: no se pudo consultar SQL Server (conexión o permisos).
- `500 ERROR_INTERNO`: error inesperado.

---

## 4. Consultas administrativas (DMV) y permisos

Todas las consultas utilizan vistas de administración dinámica y vistas del sistema:

| Métrica | Fuente |
|---|---|
| Consultas de mayor consumo / tiempo promedio | `sys.dm_exec_query_stats` + `sys.dm_exec_sql_text` |
| Sesiones activas y consumo | `sys.dm_exec_sessions` |
| Sesiones bloqueadas | `sys.dm_exec_requests` + `sys.dm_exec_sql_text` |
| Contexto del servidor | `sys.dm_os_sys_info`, `sys.configurations` |

**Permisos requeridos** (otorgados por `BaseGrupo4.sql`):

- `VIEW SERVER STATE` (para DMV de servidor).
- `VIEW DATABASE STATE` (para DMV de base de datos).

El texto SQL de las consultas se **trunca** (máximo 600 caracteres) antes de exponerse.

---

## 5. Unidades y normalización

Todas las unidades se normalizan en la capa de servicio/repositorio para una lectura consistente:

| Dato original | Normalización |
|---|---|
| `total_worker_time` / `total_elapsed_time` (µs) | Milisegundos (`/ 1000`) |
| `memory_usage` de sesión (páginas de 8 KB) | Megabytes (`* 8 / 1024`) |
| `cpu_time` de sesión/request (ms) | Milisegundos |
| Porcentaje de CPU por consulta | `cpuConsulta / cpuTotal * 100` |

En la interfaz, los valores de memoria se muestran en MB o GB automáticamente.

---

## 6. Umbrales de interpretación

Definidos como constantes en `InterpretacionRendimiento` (ajustables en un solo punto):

| Regla | Umbral | Nivel |
|---|---|---|
| Sesiones bloqueadas con espera ≥ umbral | `5 000 ms` | CRÍTICO |
| Sesiones bloqueadas con espera < umbral | — | ADVERTENCIA |
| Consulta que concentra el CPU acumulado | `≥ 50 %` | ADVERTENCIA |
| Tiempo de ejecución promedio global | `> 1 000 ms` | ADVERTENCIA |
| Memoria de sesiones sobre memoria del servidor | `≥ 60 %` | ADVERTENCIA |
| Sin consultas en caché | — | INFORMATIVO |
| Sin riesgos detectados | — | OK (Rendimiento estable) |

---

## 7. Gráficos (Chart.js)

| Gráfico | Tipo | Métrica |
|---|---|---|
| Top consultas por CPU | Barras | `cpuTotalMs` |
| Top consultas por tiempo promedio | Barras | `tiempoPromedioMs` |
| Estado de las sesiones | Dona | Activas / Bloqueadas / Inactivas |
| Consumo de CPU por sesión | Barras | `cpuMs` por sesión |

---

## 8. Seguridad

- **Solo lectura**: ninguna operación `INSERT/UPDATE/DELETE/DDL`.
- **Sin SQL dinámico con entrada del usuario**: los únicos parámetros son límites numéricos de la
  aplicación, enlazados por `JdbcTemplate` (`TOP (?)`).
- **Escape de salida**: la interfaz escapa todo texto proveniente del SGBD (`escapeHtml` en el JS),
  evitando XSS desde el texto de las consultas.
- **Credenciales externas**: `DB_USER` / `DB_PASSWORD` por variables de entorno (no en el repositorio).
- **Errores seguros**: mensajes claros sin trazas ni detalles internos hacia el usuario.
- **Dependencias locales**: WebJars servidos por la propia aplicación (sin llamadas a CDN externos).

---

## 9. Pruebas

### Pruebas automáticas

```
DB_USER=tester DB_PASSWORD=tester ./mvnw test
```

| Clase | Qué valida |
|---|---|
| `InterpretacionRendimientoTest` (8) | Umbrales y niveles de los hallazgos |
| `RendimientoServiceTest` (3) | Agregación, porcentaje de CPU y límite de ranking |
| `RendimientoApiControllerTest` (1) | Delegación del endpoint JSON |
| `RendimientoControllerTest` (1) | Vista y atributo base |
| `RendimientoExceptionHandlerTest` (2) | Traducción de excepciones a respuestas seguras |
| `RendimientoRepositoryTest` (3) | Saneamiento/truncado de texto |

Total: **19 pruebas**, todas en verde.

### Prueba de integración real (opt-in)

Requiere una instancia SQL Server accesible:

```
RENDIMIENTO_IT=true DB_USER=<usuario> DB_PASSWORD=<clave> ./mvnw test -Dtest=RendimientoRepositoryIT
```

### Evidencia de métricas (manual)

Ejecutar las consultas de `docs/sql/modulo2-rendimiento-consultas.sql` en SSMS/SQLCMD, medir el
tiempo de respuesta de cada consulta y registrar los resultados como evidencia de las pruebas de métricas.

---

## 10. Cómo ejecutar

1. Definir credenciales (no se suben al repositorio):

   ```powershell
   $env:DB_USER="proyecto_grupo4"
   $env:DB_PASSWORD="********"
   ```

2. Ejecutar la aplicación:

   ```
   ./mvnw spring-boot:run
   ```

3. Abrir `http://localhost:8080/rendimiento`.

Si SQL Server no está disponible, el panel muestra un mensaje controlado (HTTP 503) sin romper la aplicación.

---

## 11. Coordinación pendiente (integración)

- **Layout común**: la vista `templates/rendimiento/index.html` es autocontenida porque el layout
  compartido (Integrante 1) aún no existe. Cuando esté disponible, se migra el contenido al layout y
  se elimina el HTML repetido, sin tocar el resto del módulo.
- **Enlace en el menú**: solicitar al Integrante 1 que agregue la entrada que apunta a `/rendimiento`.
- **`pom.xml`**: las dependencias WebJars son el único cambio en un archivo compartido.
