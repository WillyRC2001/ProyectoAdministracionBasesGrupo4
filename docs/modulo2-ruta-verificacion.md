# Módulo 2 — Ruta de verificación local (compilación y prueba incremental)

Esta guía define la **ruta a trazar** para validar el módulo de Monitoreo de
rendimiento de forma incremental, empezando por lo más barato (compilación) y
terminando en una prueba funcional mínima.

> No requiere SQL Server para los pasos 1–4. El paso 5 usa un servidor embebido;
> el paso 6 usa la base real (opcional).

## 0. Requisitos

- JDK 21, Maven (wrapper `mvnw` incluido).
- Directorio de trabajo: `Code/`.

## 1. Compilación

```
mvn -q -DskipTests compile
```

Resultado esperado: sin salida y `EXIT=0` (equivale a `BUILD SUCCESS`).

Diagnóstico rápido de errores de codificación (si aparecen caracteres
`\u0000`, `\ufffd` o `class, interface, enum, or record expected`):

- El archivo fuente no está en UTF-8 (probablemente quedó guardado en UTF-16).
- Causa típica: restaurar archivos con `git show ... > archivo` en PowerShell
  (la redirección `>` re-codifica a UTF-16LE).
- Solución: restaurar con `git checkout <commit> -- <ruta>` (copia bytes exactos).

## 2. Pruebas unitarias

```
mvn test
```

Resultado esperado: `Tests run: 19, Failures: 0, Errors: 0, Skipped: 0` y
`BUILD SUCCESS`. Las pruebas de integración (`RendimientoRepositoryIT`) están
deshabilitadas salvo que se active la variable de entorno:

```
set RENDIMIENTO_IT=true
mvn -Dtest=RendimientoRepositoryIT test
```

## 3. Empaquetado

```
mvn -q -DskipTests package
```

Resultado esperado: `target/Proyecto-administracion-bases-0.0.1-SNAPSHOT.jar`.

## 4. Verificación de recursos estáticos (sin BD)

Con la aplicación levantada (paso 5), comprobar que los WebJars y el CSS/JS
propios responden `200`:

| Recurso | URL |
|---|---|
| Chart.js | `/webjars/chart.js/4.5.1/dist/chart.umd.min.js` |
| Bootstrap Icons | `/webjars/bootstrap-icons/1.13.2/font/bootstrap-icons.min.css` |
| CSS del módulo | `/css/panel-monitoreo-rendimiento.css` |
| JS de datos | `/js/rendimiento/panel-monitoreo-rendimiento-datos.js` |
| JS de gráficos | `/js/rendimiento/panel-monitoreo-rendimiento-graficos.js` |
| JS de efectos | `/js/rendimiento/panel-monitoreo-rendimiento-efectos.js` |

> Las fuentes del diseño (Google Fonts) y Bootstrap (CDN jsdelivr) vienen del layout; si no hay
> red, la vista funciona con las fuentes del sistema (`Consolas`, `Segoe UI`).

## 5. Arranque sin base de datos (prueba funcional mínima)

Variables de entorno (valores ficticios para que arranque; la BD no responde):

```
set DB_HOST=localhost
set DB_PORT=1433
set DB_NAME=master
set DB_USER=sa
set DB_PASSWORD=x
```

Arranque con timeouts cortos para no bloquear:

```
java -jar target/Proyecto-administracion-bases-0.0.1-SNAPSHOT.jar ^
  --server.port=8099 ^
  --spring.datasource.hikari.connection-timeout=3000 ^
  --spring.datasource.hikari.initialization-fail-timeout=-1
```

Endpoints a verificar:

| Endpoint | Esperado sin BD |
|---|---|
| `GET /` | `200` (dashboard) |
| `GET /rendimiento` | `200` (vista del módulo con layout) |
| `GET /rendimiento/api/panel` | `503` (error controlado, sin trazas) |
| `GET /actuator/*` | n/a |

Ejemplo en PowerShell:

```powershell
Invoke-WebRequest http://localhost:8099/rendimiento            -UseBasicParsing | % StatusCode
Invoke-WebRequest http://localhost:8099/rendimiento/api/panel  -UseBasicParsing | % StatusCode
```

Resultado esperado: la vista contiene el sidebar común (marca *Rendimiento*
activo) y el panel muestra el aviso de error controlado.

## 6. Verificación con base de datos real (opcional)

Con SQL Server accesible y credenciales reales en `DB_*`:

- `GET /rendimiento/api/panel` debe devolver `200` con JSON del panel.
- Comprobar que el usuario tiene `VIEW SERVER STATE` y `VIEW DATABASE STATE`.

## 7. Criterio de aceptación

1. `mvn -q -DskipTests compile` → sin errores.
2. `mvn test` → 19/19 verdes.
3. `mvn -q -DskipTests package` → jar generado.
4. Recursos WebJars/CSS/JS → `200`.
5. `/rendimiento` → `200`; `/rendimiento/api/panel` → `503` sin BD, `200` con BD.
