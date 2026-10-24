# Módulo 2 — S0 (Preparación)

**Fecha:** 2026-10-17
**Rama:** feature/modulo-rendimiento

## Decisiones tomadas

1. **Entrega de datos a la vista (punto 4):** Opción **B** — API JSON `/rendimiento/api/panel` + vista Thymeleaf `/rendimiento`. El JS (Chart.js) consume la API mediante `fetch`. (Recomendado por seguridad y menor superficie de ataque).
2. **Dependencias:** Usar **WebJars** para Bootstrap 5.3.8, Chart.js 4.5.1, Bootstrap Icons 1.13.2 y `webjars-locator-lite 1.1.5`. Se agrega al `pom.xml` (archivo compartido) una sola vez.
3. **Seguridad (transversal):** Solo lectura (DMVs), sin SQL dinámico con entrada de usuario (parámetros controlados por `JdbcTemplate`), escape de salida en JS (`escapeHtml`), manejo de errores seguro sin exponer trazas internas, credenciales vía `${DB_USER}/${DB_PASSWORD}`, WebJars servidos localmente.
4. **Marco de trabajo:** Secciones S0–S9 con incrementos parciales. Cada sección será un hito para subir cambios.
5. **S7 sin layout:** Vista `templates/rendimiento/index.html` autocontenida (Bootstrap/Chart.js/Icons incluidos). Cuando exista el layout común del Integrante 1, se migra el contenido al layout; el resto del módulo no se toca. *(Cumplido: la vista se migró al layout común y se renombró a `templates/rendimiento/panel-monitoreo-rendimiento.html`.)*

## Alcance confirmado

- Módulo 2 completo + trabajos adicionales: gráficos principales, pruebas de métricas e interpretación de resultados.
- Exclusivamente Módulo 2.

## Contrato de datos (resumido)

- `ConsultaConsumoDTO`, `SesionDTO`, `BloqueoDTO`, `ResumenRendimientoDTO`, `ContextoInstanciaDTO`, `HallazgoDTO` (con `NivelHallazgo`), `PanelRendimientoDTO`, `RespuestaErrorDTO`.
- Unidades normalizadas: tiempo en ms, memoria en MB (convertida a GB para mostrar), porcentajes redondeados.
- Sesiones "activas" = `status = 'running'`. Bloqueos desde `sys.dm_exec_requests` con `blocking_session_id <> 0`.

## Endpoints

- `GET /rendimiento` → vista
- `GET /rendimiento/api/panel` → JSON (PanelRendimientoDTO)

## Umbrales iniciales (documentados)

- Bloqueos con espera ≥ 5000 ms → CRÍTICO; < 5000 ms → ADVERTENCIA
- Consulta dominante ≥ 50% CPU → ADVERTENCIA
- Tiempo promedio global > 1000 ms → ADVERTENCIA
- Memoria sesiones ≥ 60% memoria servidor → ADVERTENCIA
- Sin consultas en caché → INFORMATIVO
- Sin riesgos → OK

## Plan de secciones

S0 (este commit), S1 DTOs, S2–S3 Repositorio (DMVs), S4 Servicio, S5 Interpretación, S6 Controladores+API+ExceptionHandler, S7 Vista autocontenida, S8 Gráficos Chart.js, S9 Pruebas + Documentación.
