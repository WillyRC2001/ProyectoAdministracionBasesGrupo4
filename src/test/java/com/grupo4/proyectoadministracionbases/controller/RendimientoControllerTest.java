package com.grupo4.proyectoadministracionbases.controller;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Prueba del controlador de vista del Módulo 2.
 */
class RendimientoControllerTest {

    private final RendimientoController controlador = new RendimientoController();

    @Test
    void panelDevuelveLaVistaDelModuloYDatosBase() {
        Model model = new ExtendedModelMap();

        String vista = controlador.panel(model);

        assertEquals("rendimiento/panel-monitoreo-rendimiento", vista);
        assertEquals("Monitoreo de rendimiento", model.getAttribute("tituloModulo"));
    }
}
