/*
 * Modulo 2 - Monitoreo de rendimiento.
 * Graficos del panel (Chart.js): consumo de CPU, tiempo promedio, estado de
 * sesiones y CPU por sesion. La logica de obtencion de datos vive en el modulo
 * de datos; aqui solo se transforma en visuales.
 *
 * API: window.MonitorRendimiento.graficos.dibujar(panel)
 */
(function () {
    "use strict";

    window.MonitorRendimiento = window.MonitorRendimiento || {};
    var graficos = window.MonitorRendimiento.graficos = window.MonitorRendimiento.graficos || {};

    var PALETA = {
        acero: "#9fadba",
        medio: "#8b949d",
        claro: "#c6d0d9",
        oscuro: "#4c545c",
        aviso: "#a68a4e",
        critico: "#a4635c"
    };

    var FUENTE = "'JetBrains Mono', Consolas, 'Courier New', monospace";
    var LIENZOS = {};

    graficos.dibujar = function (panel) {
        if (typeof Chart === "undefined" || !panel) {
            return;
        }

        Chart.defaults.font.family = FUENTE;
        Chart.defaults.color = "#838a92";

        dibujar("rx-grafico-cpu", configuracionBarras(
            etiquetas(panel.consultasMayorCpu),
            (panel.consultasMayorCpu || []).map(function (c) { return c.cpuTotalMs; }),
            "CPU total (ms)",
            PALETA.acero
        ));

        dibujar("rx-grafico-tiempo", configuracionBarras(
            etiquetas(panel.consultasMayorTiempo),
            (panel.consultasMayorTiempo || []).map(function (c) { return c.tiempoPromedioMs; }),
            "Tiempo promedio (ms)",
            PALETA.medio
        ));

        dibujar("rx-grafico-sesiones", configuracionDona(panel));

        dibujar("rx-grafico-consumo", configuracionBarras(
            (panel.consumoPorSesion || []).map(function (s) {
                return "S" + s.sessionId + (s.login ? " · " + s.login : "");
            }),
            (panel.consumoPorSesion || []).map(function (s) { return s.cpuMs; }),
            "CPU por sesión (ms)",
            PALETA.acero
        ));
    };

    /* ======================================================================
       Configuraciones
       ====================================================================== */

    function configuracionBarras(etiquetas, valores, etiquetaSerie, color) {
        return {
            type: "bar",
            data: {
                labels: etiquetas,
                datasets: [{
                    label: etiquetaSerie,
                    data: valores,
                    backgroundColor: color + "cc",
                    hoverBackgroundColor: color,
                    borderColor: color,
                    borderWidth: 0,
                    borderRadius: 3,
                    barPercentage: 0.78,
                    categoryPercentage: 0.82
                }]
            },
            options: {
                indexAxis: "y",
                responsive: true,
                maintainAspectRatio: false,
                animation: { duration: 700 },
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: "rgba(20, 23, 27, 0.97)",
                        borderColor: color,
                        borderWidth: 1,
                        titleColor: color,
                        bodyColor: "#cfd4d9",
                        titleFont: { family: FUENTE, size: 11 },
                        bodyFont: { family: FUENTE, size: 11 },
                        padding: 10,
                        displayColors: false,
                        callbacks: {
                            label: function (contexto) {
                                var valor = contexto.parsed.x;
                                return valor.toLocaleString("es-CR", { maximumFractionDigits: 2 }) + " ms";
                            }
                        }
                    }
                },
                scales: {
                    x: {
                        beginAtZero: true,
                        grid: { color: "rgba(255, 255, 255, 0.06)" },
                        border: { color: "rgba(255, 255, 255, 0.12)" },
                        ticks: {
                            color: "#6b737b",
                            font: { family: FUENTE, size: 10 },
                            maxTicksLimit: 6
                        },
                        title: {
                            display: true,
                            text: "milisegundos",
                            color: "#6b737b",
                            font: { family: FUENTE, size: 10 }
                        }
                    },
                    y: {
                        grid: { display: false },
                        border: { display: false },
                        ticks: {
                            color: "#a7aeb5",
                            font: { family: FUENTE, size: 10 },
                            callback: function (valor) {
                                var texto = this.getLabelForValue(valor);
                                return texto && texto.length > 34 ? texto.substring(0, 34) + "…" : texto;
                            }
                        }
                    }
                }
            }
        };
    }

    function configuracionDona(panel) {
        var resumen = panel.resumen || {};
        var total = resumen.totalSesiones || 0;
        var activas = resumen.sesionesActivas || 0;
        var bloqueadas = resumen.sesionesBloqueadas || 0;
        var inactivas = Math.max(total - activas - bloqueadas, 0);

        return {
            type: "doughnut",
            data: {
                labels: ["Activas", "Bloqueadas", "Inactivas"],
                datasets: [{
                    data: [activas, bloqueadas, inactivas],
                    backgroundColor: [PALETA.claro + "d9", PALETA.critico + "d9", PALETA.oscuro + "d9"],
                    hoverBackgroundColor: [PALETA.claro, PALETA.critico, PALETA.oscuro],
                    borderColor: "rgba(13, 15, 18, 0.95)",
                    borderWidth: 3,
                    hoverOffset: 8
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: "66%",
                animation: { duration: 700 },
                plugins: {
                    legend: {
                        position: "bottom",
                        labels: {
                            color: "#bcc2c8",
                            font: { family: FUENTE, size: 11 },
                            boxWidth: 10,
                            boxHeight: 10,
                            padding: 14,
                            usePointStyle: true,
                            pointStyle: "rectRounded"
                        }
                    },
                    tooltip: {
                        backgroundColor: "rgba(20, 23, 27, 0.97)",
                        borderColor: PALETA.acero,
                        borderWidth: 1,
                        titleColor: PALETA.acero,
                        bodyColor: "#cfd4d9",
                        titleFont: { family: FUENTE, size: 11 },
                        bodyFont: { family: FUENTE, size: 11 },
                        padding: 10,
                        displayColors: false
                    }
                }
            }
        };
    }

    /* ======================================================================
       Utilidades
       ====================================================================== */

    function etiquetas(consultas) {
        return (consultas || []).map(function (c, indice) {
            var base = c.baseDatos ? c.baseDatos + ": " : "";
            return base + resumir(c.consulta, 40, indice);
        });
    }

    function resumir(texto, maximo, indice) {
        if (!texto) {
            return "Consulta " + (indice + 1);
        }
        var limpio = String(texto).replace(/\s+/g, " ").trim();
        return limpio.length > maximo ? limpio.substring(0, maximo) + "…" : limpio;
    }

    function dibujar(id, configuracion) {
        var lienzo = document.getElementById(id);
        if (!lienzo) {
            return;
        }
        if (LIENZOS[id]) {
            LIENZOS[id].destroy();
        }
        LIENZOS[id] = new Chart(lienzo, configuracion);
    }
})();
