/*
 * Modulo 2 - Monitoreo de rendimiento.
 * Carga /rendimiento/api/panel y pinta los indicadores, tablas, hallazgos y
 * gráficos de panel-monitoreo-rendimiento.
 *
 * Reglas de seguridad de este archivo:
 *   - Solo lectura: no se envía nada al servidor (GET).
 *   - Todo texto proveniente del SGBD pasa por escapeHtml antes de entrar a
 *     innerHTML; los números usan textContent.
 *   - Los errores se muestran con el mensaje controlado del servidor, nunca
 *     con trazas ni detalles internos.
 */
(function () {
    "use strict";

    var INTERVALO_AUTOACTUALIZAR = 30000;

    var temaPorNivel = {
        OK: "rx-tema-ok",
        INFORMATIVO: "rx-tema-neutro",
        ADVERTENCIA: "rx-tema-aviso",
        CRITICO: "rx-tema-critico"
    };

    var iconoPorNivel = {
        OK: "bi-check-circle-fill",
        INFORMATIVO: "bi-info-circle-fill",
        ADVERTENCIA: "bi-exclamation-triangle-fill",
        CRITICO: "bi-x-octagon-fill"
    };

    var etiquetaPorNivel = {
        OK: "OK",
        INFORMATIVO: "INFO",
        ADVERTENCIA: "ADVERTENCIA",
        CRITICO: "CRÍTICO"
    };

    var temporizadorAutomatico = null;
    var hallazgosActuales = [];

    document.addEventListener("DOMContentLoaded", function () {
        var botonRefrescar = document.getElementById("rx-boton-refrescar");
        var botonAutomatico = document.getElementById("rx-boton-autoactualizar");
        var botonAlertas = document.getElementById("rx-boton-alertas");
        var botonDiagnostico = document.getElementById("rx-boton-diagnostico");

        if (botonRefrescar) {
            botonRefrescar.addEventListener("click", function () {
                cargarPanel({ manual: true });
            });
        }

        if (botonAutomatico) {
            botonAutomatico.addEventListener("click", function () {
                alternarActualizacionAutomatica(botonAutomatico);
            });
        }

        if (botonAlertas) {
            botonAlertas.addEventListener("click", function () {
                pintarModalAlertas();
                ejecutar("abrirModal");
            });
        }

        if (botonDiagnostico) {
            botonDiagnostico.addEventListener("click", function () {
                ejecutar("abrirCajon");
            });
        }

        cargarPanel({ manual: false });
    });

    /* ======================================================================
       1. Carga de la telemetría
       ====================================================================== */

    function cargarPanel(opciones) {
        var configuracion = opciones || {};
        var url = document.body.getAttribute("data-api-panel");
        var boton = document.getElementById("rx-boton-refrescar");

        if (!url) {
            mostrarError("No se configuró la dirección del servicio de rendimiento.");
            return;
        }

        if (boton) {
            boton.disabled = true;
        }
        estado("Consultando telemetría…", "carga");

        fetch(url, { headers: { Accept: "application/json" } })
            .then(function (respuesta) {
                if (!respuesta.ok) {
                    return respuesta.json()
                        .catch(function () { return null; })
                        .then(function (cuerpo) {
                            throw new Error(cuerpo && cuerpo.mensaje
                                ? cuerpo.mensaje
                                : "No fue posible obtener las métricas (HTTP " + respuesta.status + ").");
                        });
                }
                return respuesta.json();
            })
            .then(function (panel) {
                ocultarError();
                pintar(panel);
                if (configuracion.manual) {
                    var criticos = contarNivel("CRITICO");
                    var advertencias = contarNivel("ADVERTENCIA");
                    avisar("Telemetría actualizada",
                        criticos + " crítico(s) y " + advertencias + " advertencia(s) detectados.",
                        criticos > 0 ? "alerta" : "exito");
                }
            })
            .catch(function (error) {
                var mensaje = (error && error.message)
                    ? error.message
                    : "Error inesperado al cargar las métricas.";
                mostrarError(mensaje);
                estado("SGBD no disponible", "critico");
                avisar("SGBD no disponible", mensaje, "alerta");
            })
            .finally(function () {
                if (boton) {
                    boton.disabled = false;
                }
            });
    }

    function alternarActualizacionAutomatica(boton) {
        var activa = boton.getAttribute("aria-pressed") === "true";

        if (activa) {
            window.clearInterval(temporizadorAutomatico);
            temporizadorAutomatico = null;
            boton.setAttribute("aria-pressed", "false");
            avisar("Refresco automático", "Se ha detenido la actualización periódica.", "info");
            return;
        }

        boton.setAttribute("aria-pressed", "true");
        temporizadorAutomatico = window.setInterval(function () {
            cargarPanel({ manual: false });
        }, INTERVALO_AUTOACTUALIZAR);
        avisar("Refresco automático", "El panel se actualizará cada 30 segundos.", "exito");
    }

    /* ======================================================================
       2. Pintado del panel
       ====================================================================== */

    function pintar(panel) {
        pintarContexto(panel.contexto);
        pintarResumen(panel.resumen, panel.contexto);
        pintarHallazgos(panel.interpretacion);
        pintarTablaConsultas(panel.consultasMayorCpu);
        pintarTablaSesiones(panel.sesiones);
        pintarTablaBloqueos(panel.bloqueos);
        pintarDetalleConsultaTop(panel.resumen);

        var graficosApi = window.MonitorRendimiento && window.MonitorRendimiento.graficos;
        if (graficosApi && graficosApi.dibujar) {
            graficosApi.dibujar(panel);
        }

        texto("rx-ultima-actualizacion", panel.generadoEn
            ? new Date(panel.generadoEn).toLocaleString()
            : new Date().toLocaleString());
    }

    function pintarContexto(contexto) {
        if (!contexto) {
            return;
        }
        texto("rx-ctx-version", contexto.versionProducto || "—");
        texto("rx-ctx-nucleos", contexto.nucleosCpu != null ? contexto.nucleosCpu : "—");
        texto("rx-ctx-memoria", contexto.memoriaServidorMb ? formatoMb(contexto.memoriaServidorMb) : "—");
        texto("rx-ctx-conexiones", contexto.conexionesMaximas > 0
            ? numero(contexto.conexionesMaximas)
            : "Sin límite");
    }

    function pintarResumen(resumen, contexto) {
        if (!resumen) {
            return;
        }

        texto("rx-kpi-total-sesiones", numero(resumen.totalSesiones));
        if (contexto && contexto.conexionesMaximas > 0) {
            texto("rx-kpi-total-sesiones-pie", "Máximo permitido: " + numero(contexto.conexionesMaximas));
        }

        var activas = resumen.sesionesActivas || 0;
        var total = resumen.totalSesiones || 0;
        texto("rx-kpi-sesiones-activas", numero(activas));
        texto("rx-kpi-sesiones-activas-pie", total > 0
            ? Math.round((activas / total) * 100) + " % de " + numero(total) + " sesiones"
            : "Sin sesiones registradas");

        var bloqueadas = resumen.sesionesBloqueadas || 0;
        texto("rx-kpi-sesiones-bloqueadas", numero(bloqueadas));
        texto("rx-kpi-sesiones-bloqueadas-pie",
            bloqueadas > 0 ? "Contención de recursos detectada" : "Sin contención de recursos");
        resaltarAlerta("rx-kpi-sesiones-bloqueadas", bloqueadas > 0);

        texto("rx-kpi-consultas-cache", numero(resumen.totalConsultasEnCache));
        texto("rx-kpi-cpu-total", formatoMs(resumen.cpuTotalMs));

        var tiempoPromedio = resumen.tiempoPromedioGlobalMs || 0;
        texto("rx-kpi-tiempo-promedio", formatoMs(tiempoPromedio));
        resaltarAlerta("rx-kpi-tiempo-promedio", tiempoPromedio > 1000);

        var memoria = resumen.memoriaTotalMb || 0;
        texto("rx-kpi-memoria-sesiones", formatoMb(memoria));
        if (contexto && contexto.memoriaServidorMb > 0) {
            var porcentaje = (memoria / contexto.memoriaServidorMb) * 100;
            texto("rx-kpi-memoria-sesiones-pie",
                porcentaje.toFixed(1) + " % de " + formatoMb(contexto.memoriaServidorMb));
            resaltarAlerta("rx-kpi-memoria-sesiones", porcentaje >= 60);
        }

        var cpuTop = resumen.porcentajeCpuConsultaTop || 0;
        texto("rx-kpi-cpu-top", cpuTop.toFixed(1) + " %");
        resaltarAlerta("rx-kpi-cpu-top", cpuTop >= 50);
    }

    function pintarDetalleConsultaTop(resumen) {
        if (!resumen) {
            return;
        }
        var base = resumen.consultaMasCostosa;
        var porcentaje = resumen.porcentajeCpuConsultaTop || 0;

        texto("rx-detalle-consulta-top", base
            ? "Concentra el " + porcentaje.toFixed(1) + " % del CPU acumulado en " + base + "."
            : "Sin consultas registradas en la caché de planes.");
    }

    /* ======================================================================
       3. Hallazgos de interpretación
       ====================================================================== */

    function pintarHallazgos(hallazgos) {
        var lista = document.getElementById("rx-lista-hallazgos");
        hallazgosActuales = hallazgos || [];

        if (!lista) {
            return;
        }

        if (hallazgosActuales.length === 0) {
            lista.innerHTML = '<li class="rx-vacio">Sin hallazgos para mostrar.</li>';
        } else {
            lista.innerHTML = hallazgosActuales.map(itemHallazgo).join("");
        }

        contarNiveles();
        pintarResumenHallazgos();
    }

    function itemHallazgo(hallazgo) {
        var nivel = hallazgo.nivel || "INFORMATIVO";
        var tema = temaPorNivel[nivel] || "rx-tema-acento";
        var icono = iconoPorNivel[nivel] || "bi-info-circle-fill";
        var etiqueta = etiquetaPorNivel[nivel] || "INFO";

        return '<li class="rx-hallazgo ' + tema + '" data-nivel="' + escapeHtml(nivel) + '">' +
            '<span class="rx-hallazgo__nivel">[' + escapeHtml(etiqueta) + '] ' +
            '<i class="bi ' + icono + '"></i></span>' +
            '<div>' +
            '<span class="rx-hallazgo__titulo">' + escapeHtml(hallazgo.titulo) + '</span>' +
            '<p class="rx-hallazgo__mensaje">' + escapeHtml(hallazgo.mensaje) + '</p>' +
            (hallazgo.recomendacion
                ? '<p class="rx-hallazgo__recomendacion"><i class="bi bi-lightbulb-fill"></i>' +
                '<span>' + escapeHtml(hallazgo.recomendacion) + '</span></p>'
                : '') +
            '</div>' +
            '</li>';
    }

    function contarNivel(nivel) {
        var total = 0;
        for (var i = 0; i < hallazgosActuales.length; i++) {
            if (hallazgosActuales[i].nivel === nivel) {
                total++;
            }
        }
        return total;
    }

    function contarNiveles() {
        var ok = contarNivel("OK");
        var info = contarNivel("INFORMATIVO");
        var aviso = contarNivel("ADVERTENCIA");
        var critico = contarNivel("CRITICO");

        texto("rx-nivel-ok", ok);
        texto("rx-nivel-informativo", info);
        texto("rx-nivel-advertencia", aviso);
        texto("rx-nivel-critico", critico);

        var riesgo = aviso + critico;
        texto("rx-alertas-contador", riesgo);
        resaltarAlerta("rx-alertas-contador", riesgo > 0);

        if (critico > 0) {
            estado("Riesgo crítico detectado", "critico");
        } else if (aviso > 0) {
            estado("Atención: condiciones de riesgo", "aviso");
        } else {
            estado("Sistema nominal // operativo", "ok");
        }
    }

    function pintarResumenHallazgos() {
        var resumen = hallazgosActuales.length + " hallazgo(s) · " +
            contarNivel("ADVERTENCIA") + " advertencia(s) · " +
            contarNivel("CRITICO") + " crítico(s)";

        var contenedor = document.getElementById("rx-hallazgos-resumen");
        if (contenedor) {
            contenedor.setAttribute("data-total", resumen);
            contenedor.textContent = resumen;
        }
    }

    function pintarModalAlertas() {
        var lista = document.getElementById("rx-modal-alertas-lista");
        if (!lista) {
            return;
        }

        var riesgos = hallazgosActuales.filter(function (hallazgo) {
            return hallazgo.nivel === "CRITICO" || hallazgo.nivel === "ADVERTENCIA";
        });

        if (riesgos.length === 0) {
            lista.innerHTML = '<li class="rx-vacio">Sin hallazgos de riesgo. Rendimiento estable.</li>';
            return;
        }

        lista.innerHTML = riesgos.map(itemHallazgo).join("");
    }

    /* ======================================================================
       4. Tablas
       ====================================================================== */

    function pintarTablaConsultas(consultas) {
        var cuerpo = document.getElementById("rx-tabla-consultas");
        if (!cuerpo) {
            return;
        }

        texto("rx-contador-consultas", consultas ? consultas.length : 0);

        if (!consultas || consultas.length === 0) {
            cuerpo.innerHTML = '<tr><td colspan="10" class="rx-vacio">' +
                'No hay consultas registradas en la caché de planes.</td></tr>';
            return;
        }

        cuerpo.innerHTML = consultas.map(function (consulta, indice) {
            return "<tr>" +
                "<td>" + (indice + 1) + "</td>" +
                "<td>" + escapeHtml(consulta.baseDatos || "—") + "</td>" +
                '<td><span class="rx-sql">' + escapeHtml(consulta.consulta) + "</span></td>" +
                '<td class="rx-num">' + numero(consulta.ejecuciones) + "</td>" +
                '<td class="rx-num">' + formatoMs(consulta.cpuTotalMs) + "</td>" +
                '<td class="rx-num">' + formatoMs(consulta.cpuPromedioMs) + "</td>" +
                '<td class="rx-num">' + (consulta.porcentajeCpu || 0).toFixed(1) + " %</td>" +
                '<td class="rx-num">' + numero(consulta.lecturasLogicas) + "</td>" +
                '<td class="rx-num">' + formatoMs(consulta.cpuPromedioMs) + "</td>" +
                '<td class="rx-num">' + formatoMs(consulta.tiempoPromedioMs) + "</td>" +
                '<td class="rx-num">' + formatoMs(consulta.tiempoTotalMs) + "</td>" +
                '<td class="rx-num">' + (consulta.porcentajeCpu || 0).toFixed(1) + " %</td>" +
                "</tr>";
        }).join("");
    }

    function pintarTablaSesiones(sesiones) {
        var cuerpo = document.getElementById("rx-tabla-sesiones");
        if (!cuerpo) {
            return;
        }

        texto("rx-contador-sesiones", sesiones ? sesiones.length : 0);

        if (!sesiones || sesiones.length === 0) {
            cuerpo.innerHTML = '<tr><td colspan="9" class="rx-vacio">No hay sesiones de usuario.</td></tr>';
            return;
        }

        cuerpo.innerHTML = sesiones.map(function (sesion) {
            var activa = sesion.estado && sesion.estado.toLowerCase() === "running";
            var tema = activa ? "rx-tema-ok" : "rx-tema-acento";
            return "<tr>" +
                "<td>" + sesion.sessionId + "</td>" +
                "<td>" + escapeHtml(sesion.login || "—") + "</td>" +
                "<td>" + escapeHtml(sesion.host || "—") + "</td>" +
                "<td>" + escapeHtml(sesion.programa || "—") + "</td>" +
                "<td>" + escapeHtml(sesion.baseDatos || "—") + "</td>" +
                '<td><span class="rx-sello ' + tema + '">' +
                escapeHtml(sesion.estado || "—") + "</span></td>" +
                '<td class="rx-num">' + numero(sesion.cpuMs) + "</td>" +
                '<td class="rx-num">' + formatoMb(sesion.memoriaMb) + "</td>" +
                "<td>" + escapeHtml(sesion.ultimaPeticion || "—") + "</td>" +
                "</tr>";
        }).join("");
    }

    function pintarTablaBloqueos(bloqueos) {
        var cuerpo = document.getElementById("rx-tabla-bloqueos");
        if (!cuerpo) {
            return;
        }

        texto("rx-contador-bloqueos", bloqueos ? bloqueos.length : 0);

        if (!bloqueos || bloqueos.length === 0) {
            cuerpo.innerHTML = '<tr><td colspan="7" class="rx-vacio">' +
                "No se detectaron sesiones bloqueadas.</td></tr>";
            return;
        }

        cuerpo.innerHTML = bloqueos.map(function (bloqueo) {
            return "<tr>" +
                "<td>" + bloqueo.sessionId + "</td>" +
                '<td><span class="rx-sello rx-tema-critico">' +
                bloqueo.sesionBloqueante + "</span></td>" +
                "<td>" + escapeHtml(bloqueo.tipoEspera || "—") + "</td>" +
                '<td class="rx-num">' + numero(bloqueo.tiempoEsperaMs) + "</td>" +
                "<td>" + escapeHtml(bloqueo.estado || "—") + "</td>" +
                "<td>" + escapeHtml(bloqueo.comando || "—") + "</td>" +
                "<td>" + escapeHtml(bloqueo.baseDatos || "—") + "</td>" +
                "</tr>";
        }).join("");
    }

    /* ======================================================================
       5. Mensajes de estado y utilidades
       ====================================================================== */

    function mostrarError(mensaje) {
        var contenedor = document.getElementById("rx-alerta-error");
        texto("rx-alerta-error-texto", mensaje);
        if (contenedor) {
            contenedor.classList.remove("rx-oculto");
        }
    }

    function ocultarError() {
        var contenedor = document.getElementById("rx-alerta-error");
        if (contenedor) {
            contenedor.classList.add("rx-oculto");
        }
    }

    function texto(id, valor) {
        var elemento = document.getElementById(id);
        if (elemento) {
            elemento.textContent = valor;
        }
    }

    function resaltarAlerta(id, activo) {
        var elemento = document.getElementById(id);
        if (elemento) {
            elemento.classList.toggle("rx-kpi__valor--alerta", !!activo);
        }
    }

    function estado(textoEstado, tipo) {
        var api = window.MonitorRendimiento && window.MonitorRendimiento.efectos;
        if (api && api.estado) {
            api.estado(textoEstado, tipo);
        }
    }

    function avisar(titulo, mensaje, tipo) {
        var api = window.MonitorRendimiento && window.MonitorRendimiento.efectos;
        if (api && api.notificar) {
            api.notificar(titulo, mensaje, tipo);
        }
    }

    function ejecutar(metodo) {
        var api = window.MonitorRendimiento && window.MonitorRendimiento.efectos;
        if (api && typeof api[metodo] === "function") {
            api[metodo]();
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
