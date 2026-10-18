/*
 * Modulo 2 - Monitoreo de rendimiento.
 * Consume el endpoint JSON /rendimiento/api/panel y renderiza indicadores,
 * graficos (Chart.js) y tablas. La logica de negocio vive en el servidor;
 * este archivo solo presenta y da formato a los datos.
 */
(function () {
    "use strict";

    var COLORES = {
        azul: "#0d6efd",
        cian: "#0dcaf0",
        verde: "#198754",
        amarillo: "#ffc107",
        rojo: "#dc3545",
        gris: "#adb5bd"
    };

    var graficos = {};

    document.addEventListener("DOMContentLoaded", function () {
        var apiUrl = document.body.getAttribute("data-api-panel");
        var boton = document.getElementById("boton-refrescar");

        if (boton) {
            boton.addEventListener("click", function () {
                cargarPanel(apiUrl, boton);
            });
        }

        cargarPanel(apiUrl, boton);
    });

    function cargarPanel(apiUrl, boton) {
        if (!apiUrl) {
            mostrarError("No se configuro la direccion del servicio de rendimiento.");
            return;
        }
        if (boton) {
            boton.disabled = true;
        }

        fetch(apiUrl, { headers: { "Accept": "application/json" } })
            .then(function (respuesta) {
                if (!respuesta.ok) {
                    return respuesta.json()
                        .catch(function () { return null; })
                        .then(function (cuerpo) {
                            var mensaje = cuerpo && cuerpo.mensaje
                                ? cuerpo.mensaje
                                : "No fue posible obtener las metricas (HTTP " + respuesta.status + ").";
                            throw new Error(mensaje);
                        });
                }
                return respuesta.json();
            })
            .then(function (panel) {
                ocultarError();
                renderizar(panel);
            })
            .catch(function (error) {
                mostrarError(error && error.message ? error.message : "Error inesperado al cargar las metricas.");
            })
            .finally(function () {
                if (boton) {
                    boton.disabled = false;
                }
            });
    }

    function renderizar(panel) {
        renderizarContexto(panel.contexto);
        renderizarResumen(panel.resumen);
        renderizarHallazgos(panel.interpretacion);
        renderizarTablaConsultas(panel.consultasMayorCpu);
        renderizarTablaSesiones(panel.sesiones);
        renderizarTablaBloqueos(panel.bloqueos);
        renderizarGraficos(panel);

        var marca = document.getElementById("ultima-actualizacion");
        if (marca) {
            marca.textContent = panel.generadoEn ? new Date(panel.generadoEn).toLocaleString() : new Date().toLocaleString();
        }
    }

    function renderizarContexto(contexto) {
        if (!contexto) {
            return;
        }
        texto("ctx-version", contexto.versionProducto || "—");
        texto("ctx-nucleos", contexto.nucleosCpu != null ? contexto.nucleosCpu : "—");
        texto("ctx-memoria", contexto.memoriaServidorMb ? formatoMb(contexto.memoriaServidorMb) : "—");
        texto("ctx-conexiones", contexto.conexionesMaximas > 0 ? contexto.conexionesMaximas : "Sin límite");
    }

    function renderizarResumen(resumen) {
        if (!resumen) {
            return;
        }
        texto("sum-total-sesiones", resumen.totalSesiones);
        texto("sum-activas", resumen.sesionesActivas);
        var bloqueadas = document.getElementById("sum-bloqueadas");
        texto("sum-bloqueadas", resumen.sesionesBloqueadas);
        if (bloqueadas) {
            bloqueadas.classList.toggle("text-danger", resumen.sesionesBloqueadas > 0);
            bloqueadas.classList.toggle("text-success", resumen.sesionesBloqueadas === 0);
        }
        texto("sum-consultas", resumen.totalConsultasEnCache);
        texto("sum-cpu", formatoMs(resumen.cpuTotalMs));
        texto("sum-tiempo", formatoMs(resumen.tiempoPromedioGlobalMs));
        texto("sum-memoria", formatoMb(resumen.memoriaTotalMb));
        texto("sum-cpu-top", (resumen.porcentajeCpuConsultaTop || 0).toFixed(1) + " %");
    }

    function renderizarHallazgos(hallazgos) {
        var contenedor = document.getElementById("lista-hallazgos");
        if (!contenedor) {
            return;
        }
        if (!hallazgos || hallazgos.length === 0) {
            contenedor.innerHTML = '<div class="text-muted">Sin hallazgos para mostrar.</div>';
            return;
        }

        var iconos = {
            OK: "bi-check-circle-fill",
            INFORMATIVO: "bi-info-circle-fill",
            ADVERTENCIA: "bi-exclamation-triangle-fill",
            CRITICO: "bi-x-octagon-fill"
        };
        var clasesTexto = {
            OK: "text-success",
            INFORMATIVO: "text-info",
            ADVERTENCIA: "text-warning",
            CRITICO: "text-danger"
        };

        var html = hallazgos.map(function (h) {
            var icono = iconos[h.nivel] || "bi-info-circle";
            var claseTexto = clasesTexto[h.nivel] || "text-secondary";
            return '' +
                '<div class="hallazgo nivel-' + escapeHtml(h.nivel) + ' bg-light rounded p-3">' +
                '  <div class="d-flex align-items-center mb-1">' +
                '    <i class="bi ' + icono + ' ' + claseTexto + ' me-2"></i>' +
                '    <strong>' + escapeHtml(h.titulo) + '</strong>' +
                '    <span class="badge text-bg-secondary ms-2">' + escapeHtml(h.nivel) + '</span>' +
                '  </div>' +
                '  <div>' + escapeHtml(h.mensaje) + '</div>' +
                '  <div class="text-muted small"><i class="bi bi-lightbulb me-1"></i>' + escapeHtml(h.recomendacion) + '</div>' +
                '</div>';
        }).join("");

        contenedor.innerHTML = html;
    }

    function renderizarTablaConsultas(consultas) {
        var cuerpo = document.getElementById("tabla-consultas");
        if (!cuerpo) {
            return;
        }
        if (!consultas || consultas.length === 0) {
            cuerpo.innerHTML = '<tr><td colspan="8" class="text-muted">No hay consultas registradas en la caché de planes.</td></tr>';
            return;
        }
        cuerpo.innerHTML = consultas.map(function (c, indice) {
            return '<tr>' +
                '<td>' + (indice + 1) + '</td>' +
                '<td>' + escapeHtml(c.baseDatos || "—") + '</td>' +
                '<td><code>' + escapeHtml(c.consulta) + '</code></td>' +
                '<td class="text-end">' + numero(c.ejecuciones) + '</td>' +
                '<td class="text-end">' + formatoMs(c.cpuTotalMs) + '</td>' +
                '<td class="text-end">' + formatoMs(c.cpuPromedioMs) + '</td>' +
                '<td class="text-end">' + (c.porcentajeCpu || 0).toFixed(1) + ' %</td>' +
                '<td class="text-end">' + numero(c.lecturasLogicas) + '</td>' +
                '</tr>';
        }).join("");
    }

    function renderizarTablaSesiones(sesiones) {
        var cuerpo = document.getElementById("tabla-sesiones");
        if (!cuerpo) {
            return;
        }
        if (!sesiones || sesiones.length === 0) {
            cuerpo.innerHTML = '<tr><td colspan="9" class="text-muted">No hay sesiones de usuario.</td></tr>';
            return;
        }
        cuerpo.innerHTML = sesiones.map(function (s) {
            var esActiva = s.estado && s.estado.toLowerCase() === "running";
            var claseEstado = esActiva ? "text-bg-success" : "text-bg-secondary";
            return '<tr>' +
                '<td>' + s.sessionId + '</td>' +
                '<td>' + escapeHtml(s.login || "—") + '</td>' +
                '<td>' + escapeHtml(s.host || "—") + '</td>' +
                '<td>' + escapeHtml(s.programa || "—") + '</td>' +
                '<td>' + escapeHtml(s.baseDatos || "—") + '</td>' +
                '<td><span class="badge ' + claseEstado + '">' + escapeHtml(s.estado || "—") + '</span></td>' +
                '<td class="text-end">' + numero(s.cpuMs) + '</td>' +
                '<td class="text-end">' + formatoMb(s.memoriaMb) + '</td>' +
                '<td>' + escapeHtml(s.ultimaPeticion || "—") + '</td>' +
                '</tr>';
        }).join("");
    }

    function renderizarTablaBloqueos(bloqueos) {
        var cuerpo = document.getElementById("tabla-bloqueos");
        if (!cuerpo) {
            return;
        }
        if (!bloqueos || bloqueos.length === 0) {
            cuerpo.innerHTML = '<tr><td colspan="7" class="text-success">No se detectaron sesiones bloqueadas.</td></tr>';
            return;
        }
        cuerpo.innerHTML = bloqueos.map(function (b) {
            return '<tr>' +
                '<td>' + b.sessionId + '</td>' +
                '<td><span class="badge text-bg-danger">' + b.sesionBloqueante + '</span></td>' +
                '<td>' + escapeHtml(b.tipoEspera || "—") + '</td>' +
                '<td class="text-end">' + numero(b.tiempoEsperaMs) + '</td>' +
                '<td>' + escapeHtml(b.estado || "—") + '</td>' +
                '<td>' + escapeHtml(b.comando || "—") + '</td>' +
                '<td>' + escapeHtml(b.baseDatos || "—") + '</td>' +
                '</tr>';
        }).join("");
    }

    function renderizarGraficos(panel) {
        if (typeof Chart === "undefined") {
            return;
        }
        Chart.defaults.font.family = "'Segoe UI', system-ui, sans-serif";
        Chart.defaults.color = "#495057";

        dibujar("grafico-cpu", {
            type: "bar",
            data: {
                labels: etiquetasConsultas(panel.consultasMayorCpu),
                datasets: [{
                    label: "CPU total (ms)",
                    data: (panel.consultasMayorCpu || []).map(function (c) { return c.cpuTotalMs; }),
                    backgroundColor: COLORES.azul
                }]
            },
            options: opcionesBarras(true, "ms")
        });

        dibujar("grafico-tiempo", {
            type: "bar",
            data: {
                labels: etiquetasConsultas(panel.consultasMayorTiempo),
                datasets: [{
                    label: "Tiempo promedio (ms)",
                    data: (panel.consultasMayorTiempo || []).map(function (c) { return c.tiempoPromedioMs; }),
                    backgroundColor: COLORES.cian
                }]
            },
            options: opcionesBarras(true, "ms")
        });

        var total = panel.resumen ? panel.resumen.totalSesiones : 0;
        var activas = panel.resumen ? panel.resumen.sesionesActivas : 0;
        var bloqueadas = panel.resumen ? panel.resumen.sesionesBloqueadas : 0;
        var inactivas = Math.max(total - activas - bloqueadas, 0);

        dibujar("grafico-sesiones", {
            type: "doughnut",
            data: {
                labels: ["Activas", "Bloqueadas", "Inactivas"],
                datasets: [{
                    data: [activas, bloqueadas, inactivas],
                    backgroundColor: [COLORES.verde, COLORES.rojo, COLORES.gris]
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { position: "bottom" } }
            }
        });

        dibujar("grafico-consumo", {
            type: "bar",
            data: {
                labels: (panel.consumoPorSesion || []).map(function (s) {
                    return "S" + s.sessionId + (s.login ? " (" + s.login + ")" : "");
                }),
                datasets: [{
                    label: "CPU por sesión (ms)",
                    data: (panel.consumoPorSesion || []).map(function (s) { return s.cpuMs; }),
                    backgroundColor: COLORES.amarillo
                }]
            },
            options: opcionesBarras(true, "ms")
        });
    }

    function opcionesBarras(horizontal, unidad) {
        return {
            indexAxis: horizontal ? "y" : "x",
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: { callbacks: { label: function (ctx) { return ctx.parsed[horizontal ? "x" : "y"] + " " + unidad; } } }
            },
            scales: {
                x: { beginAtZero: true, title: { display: true, text: unidad } }
            }
        };
    }

    function etiquetasConsultas(consultas) {
        return (consultas || []).map(function (c, indice) {
            var base = c.baseDatos ? c.baseDatos + ": " : "";
            return base + resumir(c.consulta, 42, indice);
        });
    }

    function resumir(texto, max, indice) {
        if (!texto) {
            return "Consulta " + (indice + 1);
        }
        var limpio = texto.replace(/\s+/g, " ").trim();
        return limpio.length > max ? limpio.substring(0, max) + "…" : limpio;
    }

    function dibujar(idCanvas, configuracion) {
        var lienzo = document.getElementById(idCanvas);
        if (!lienzo) {
            return;
        }
        if (graficos[idCanvas]) {
            graficos[idCanvas].destroy();
        }
        graficos[idCanvas] = new Chart(lienzo, configuracion);
    }

    function mostrarError(mensaje) {
        var contenedor = document.getElementById("contenedor-error");
        texto("mensaje-error", mensaje);
        if (contenedor) {
            contenedor.classList.remove("d-none");
        }
    }

    function ocultarError() {
        var contenedor = document.getElementById("contenedor-error");
        if (contenedor) {
            contenedor.classList.add("d-none");
        }
    }

    function texto(id, valor) {
        var elemento = document.getElementById(id);
        if (elemento) {
            elemento.textContent = valor;
        }
    }

    function numero(valor) {
        return (valor == null ? 0 : valor).toLocaleString("es-CR");
    }

    function formatoMs(valor) {
        var v = valor == null ? 0 : valor;
        return v.toLocaleString("es-CR", { maximumFractionDigits: 2 }) + " ms";
    }

    function formatoMb(valor) {
        var v = valor == null ? 0 : valor;
        if (v >= 1024) {
            return (v / 1024).toLocaleString("es-CR", { maximumFractionDigits: 2 }) + " GB";
        }
        return v.toLocaleString("es-CR", { maximumFractionDigits: 2 }) + " MB";
    }

    function escapeHtml(valor) {
        if (valor == null) {
            return "";
        }
        return String(valor)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#39;");
    }
})();
