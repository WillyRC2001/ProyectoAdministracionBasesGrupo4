/*
 * Modulo 2 - Monitoreo de rendimiento.
 * Efectos de interfaz de panel-monitoreo-rendimiento: fondo animado,
 * acordeones, filtros de hallazgos, toasts, cajon de diagnostico y modal.
 *
 * Este modulo no consume datos del SGBD: solo presenta y anima. Expone su API
 * en window.MonitorRendimiento.efectos para que el modulo de datos la use.
 */
(function () {
    "use strict";

    window.MonitorRendimiento = window.MonitorRendimiento || {};
    var efectos = window.MonitorRendimiento.efectos = window.MonitorRendimiento.efectos || {};

    var INTERVALO_TOAST = 4200;
    var movimientoReducido = window.matchMedia &&
        window.matchMedia("(prefers-reduced-motion: reduce)").matches;

    /* ======================================================================
       1. Utilidades de apoyo
       ====================================================================== */

    function elemento(id) {
        return document.getElementById(id);
    }

    function porClase(raiz, selector) {
        return raiz ? raiz.querySelector(selector) : null;
    }

    /* ======================================================================
       2. Indicador de estado de la cabecera
       ====================================================================== */

    efectos.estado = function (texto, tipo) {
        var punto = elemento("rx-estado-punto");
        var etiqueta = elemento("rx-estado-texto");

        if (etiqueta && texto) {
            etiqueta.textContent = texto;
        }

        if (punto) {
            punto.className = "rx-indicador" + (tipo ? " rx-indicador--" + tipo : "");
        }
    };

    /* ======================================================================
       3. Notificaciones tipo toast
       ====================================================================== */

    efectos.notificar = function (titulo, mensaje, tipo) {
        var contenedor = elemento("rx-toasts");
        if (!contenedor) {
            return;
        }

        var tema = "rx-tema-acento";
        var icono = "bi-info-circle-fill";
        if (tipo === "exito") {
            tema = "rx-tema-ok";
            icono = "bi-check-circle-fill";
        } else if (tipo === "alerta") {
            tema = "rx-tema-critico";
            icono = "bi-exclamation-octagon-fill";
        } else if (tipo === "aviso") {
            tema = "rx-tema-aviso";
            icono = "bi-exclamation-triangle-fill";
        }

        var toast = document.createElement("div");
        toast.className = "rx-toast " + tema;

        var iconoEl = document.createElement("i");
        iconoEl.className = "bi " + icono;
        iconoEl.setAttribute("aria-hidden", "true");

        var cuerpo = document.createElement("div");
        var fuerte = document.createElement("span");
        fuerte.className = "rx-toast__titulo";
        fuerte.textContent = titulo || "Aviso";

        var texto = document.createElement("span");
        texto.textContent = mensaje || "";

        cuerpo.appendChild(fuerte);
        cuerpo.appendChild(texto);
        toast.appendChild(iconoEl);
        toast.appendChild(cuerpo);
        contenedor.appendChild(toast);

        window.setTimeout(function () {
            toast.classList.add("rx-toast--visible");
        }, 20);

        window.setTimeout(function () {
            toast.classList.remove("rx-toast--visible");
            window.setTimeout(function () {
                if (toast.parentNode) {
                    toast.parentNode.removeChild(toast);
                }
            }, 350);
        }, INTERVALO_TOAST);
    };

    /* ======================================================================
       4. Cajon de diagnostico y modal de alertas
       ====================================================================== */

    efectos.abrirCajon = function () {
        var cajon = elemento("rx-cajon");
        var capa = elemento("rx-capa-cajon");
        if (cajon) {
            cajon.classList.add("rx-cajon--visible");
            cajon.setAttribute("aria-hidden", "false");
        }
        if (capa) {
            capa.classList.add("rx-capa--visible");
        }
    };

    efectos.cerrarCajon = function () {
        var cajon = elemento("rx-cajon");
        var capa = elemento("rx-capa-cajon");
        if (cajon) {
            cajon.classList.remove("rx-cajon--visible");
            cajon.setAttribute("aria-hidden", "true");
        }
        if (capa) {
            capa.classList.remove("rx-capa--visible");
        }
    };

    efectos.abrirModal = function () {
        var modal = elemento("rx-modal-alertas");
        var capa = elemento("rx-capa-modal");
        if (modal) {
            modal.classList.add("rx-modal--visible");
            modal.setAttribute("aria-hidden", "false");
        }
        if (capa) {
            capa.classList.add("rx-capa--visible");
        }
    };

    efectos.cerrarModal = function () {
        var modal = elemento("rx-modal-alertas");
        var capa = elemento("rx-capa-modal");
        if (modal) {
            modal.classList.remove("rx-modal--visible");
            modal.setAttribute("aria-hidden", "true");
        }
        if (capa) {
            capa.classList.remove("rx-capa--visible");
        }
    };

    /* ======================================================================
       5. Acordeones de registros y filtros de hallazgos
       ====================================================================== */

    function alternarRegistro(boton) {
        var registro = boton.closest ? boton.closest("[data-abierto]") : null;
        if (!registro) {
            return;
        }
        var abierto = registro.getAttribute("data-abierto") === "true";
        registro.setAttribute("data-abierto", abierto ? "false" : "true");
        boton.setAttribute("aria-expanded", abierto ? "false" : "true");
    }

    function aplicarFiltro(nivel) {
        var lista = elemento("rx-lista-hallazgos");
        if (!lista) {
            return;
        }
        var items = lista.querySelectorAll(".rx-hallazgo");
        var visibles = 0;

        for (var i = 0; i < items.length; i++) {
            var coincide = nivel === "TODOS" || items[i].getAttribute("data-nivel") === nivel;
            items[i].classList.toggle("rx-oculto", !coincide);
            if (coincide) {
                visibles++;
            }
        }

        var vacio = porClase(lista, ".rx-vacio");
        if (vacio) {
            vacio.classList.toggle("rx-oculto", items.length > 0);
        }

        var resumen = elemento("rx-hallazgos-resumen");
        if (resumen) {
            resumen.textContent = nivel === "TODOS"
                ? resumen.getAttribute("data-total") || "Sin datos"
                : visibles + " de " + items.length + " hallazgos";
        }
    }

    function bindInterfaz() {
        document.querySelectorAll("[data-rx-registro]").forEach(function (boton) {
            boton.addEventListener("click", function () {
                alternarRegistro(boton);
            });
        });

        document.querySelectorAll("[data-rx-filtro]").forEach(function (boton) {
            boton.addEventListener("click", function () {
                document.querySelectorAll("[data-rx-filtro]").forEach(function (otro) {
                    otro.setAttribute("aria-pressed", otro === boton ? "true" : "false");
                });
                aplicarFiltro(boton.getAttribute("data-rx-filtro"));
            });
        });

        document.querySelectorAll("[data-rx-cerrar-cajon]").forEach(function (control) {
            control.addEventListener("click", efectos.cerrarCajon);
        });

        document.querySelectorAll("[data-rx-cerrar-modal]").forEach(function (control) {
            control.addEventListener("click", efectos.cerrarModal);
        });

        document.addEventListener("keydown", function (evento) {
            if (evento.key === "Escape") {
                efectos.cerrarCajon();
                efectos.cerrarModal();
            }
        });
    }

    /* ======================================================================
       6. Fondo de particulas (decorativo, desactivable por accesibilidad)
       ====================================================================== */

    function iniciarFondo() {
        if (movimientoReducido) {
            return;
        }

        var lienzo = elemento("rx-fondo-particulas");
        if (!lienzo || !lienzo.getContext) {
            return;
        }

        var contexto = lienzo.getContext("2d");
        if (!contexto) {
            return;
        }

        var escenario = lienzo.parentNode;
        var particulas = [];
        var raton = { x: null, y: null, radio: 140 };
        var animando = true;

        function dimensionar() {
            if (!escenario) {
                return;
            }
            lienzo.width = escenario.clientWidth;
            lienzo.height = escenario.clientHeight;
            sembrar();
        }

        function sembrar() {
            var cantidad = Math.min(Math.floor((lienzo.width * lienzo.height) / 26000), 40);
            particulas = [];
            for (var i = 0; i < cantidad; i++) {
                particulas.push({
                    x: Math.random() * lienzo.width,
                    y: Math.random() * lienzo.height,
                    r: Math.random() * 1.1 + 0.4,
                    vx: (Math.random() - 0.5) * 0.25,
                    vy: (Math.random() - 0.5) * 0.25,
                    color: "#8b949e"
                });
            }
        }

        window.addEventListener("resize", dimensionar);
        window.addEventListener("mousemove", function (evento) {
            raton.x = evento.clientX;
            raton.y = evento.clientY;
        });

        document.addEventListener("visibilitychange", function () {
            animando = !document.hidden;
            if (animando) {
                pintar();
            }
        });

        function pintar() {
            if (!animando) {
                return;
            }

            var desplazamiento = escenario ? escenario.getBoundingClientRect() : { top: 0, left: 0 };
            contexto.clearRect(0, 0, lienzo.width, lienzo.height);

            for (var i = 0; i < particulas.length; i++) {
                var p = particulas[i];
                p.x += p.vx;
                p.y += p.vy;

                if (p.x < 0 || p.x > lienzo.width) {
                    p.vx *= -1;
                }
                if (p.y < 0 || p.y > lienzo.height) {
                    p.vy *= -1;
                }

                var mx = raton.x - desplazamiento.left;
                var my = raton.y - desplazamiento.top;
                if (mx !== null) {
                    var dx = mx - p.x;
                    var dy = my - p.y;
                    if (Math.sqrt(dx * dx + dy * dy) < raton.radio) {
                        p.x -= dx * 0.012;
                        p.y -= dy * 0.012;
                    }
                }

                contexto.beginPath();
                contexto.arc(p.x, p.y, p.r, 0, Math.PI * 2);
                contexto.fillStyle = p.color;
                contexto.globalAlpha = 0.3;
                contexto.fill();

                for (var j = i + 1; j < particulas.length; j++) {
                    var q = particulas[j];
                    var lx = p.x - q.x;
                    var ly = p.y - q.y;
                    var distancia = Math.sqrt(lx * lx + ly * ly);
                    if (distancia < 96) {
                        contexto.beginPath();
                        contexto.strokeStyle = "#ffffff";
                        contexto.globalAlpha = (1 - distancia / 96) * 0.05;
                        contexto.lineWidth = 0.5;
                        contexto.moveTo(p.x, p.y);
                        contexto.lineTo(q.x, q.y);
                        contexto.stroke();
                    }
                }
            }

            contexto.globalAlpha = 1;
            window.requestAnimationFrame(pintar);
        }

        dimensionar();
        pintar();
    }

    /* ======================================================================
       7. Inicializacion
       ====================================================================== */

    document.addEventListener("DOMContentLoaded", function () {
        bindInterfaz();
        iniciarFondo();
    });
})();
