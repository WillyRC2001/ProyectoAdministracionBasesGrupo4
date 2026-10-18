package com.grupo4.proyectoadministracionbases.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador de vista del Módulo 2 (Monitoreo de rendimiento).
 *
 * <p>Sirve la página del panel. Los datos se cargan desde el endpoint JSON
 * expuesto por {@link RendimientoApiController}, por lo que esta capa no
 * contiene lógica de negocio.</p>
 */
@Controller
public class RendimientoController {

    /**
     * Muestra el panel de rendimiento.
     *
     * @param model modelo de la vista.
     * @return nombre de la plantilla Thymeleaf del módulo.
     */
    @GetMapping("/rendimiento")
    public String panel(Model model) {
        model.addAttribute("tituloModulo", "Monitoreo de rendimiento");
        return "rendimiento/index";
    }
}
