package com.grupo4.proyectoadministracionbases.controller;

import com.grupo4.proyectoadministracionbases.service.MantenimientoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.function.Supplier;

@Controller
public class MantenimientoController {

    private static final Logger log = LoggerFactory.getLogger(MantenimientoController.class);

    private final MantenimientoService mantenimientoService;

    public MantenimientoController(MantenimientoService mantenimientoService) {
        this.mantenimientoService = mantenimientoService;
    }

    @GetMapping("/mantenimiento")
    public String mostrarMantenimiento(Model model) {
        try {
            model.addAttribute("panel", mantenimientoService.construirPanel());
        } catch (DataAccessException ex) {
            log.error("Error de acceso a datos en el módulo de mantenimiento", ex);
            model.addAttribute("error",
                    "No fue posible consultar el SGBD. Verifique la conexión y los permisos del usuario.");
        }
        return "mantenimiento/index";
    }

    @PostMapping("/mantenimiento/estadisticas")
    public String actualizarEstadisticas(@RequestParam String esquema,
                                         @RequestParam String tabla,
                                         RedirectAttributes redirect) {
        return ejecutar(redirect, () -> mantenimientoService.actualizarEstadisticas(esquema, tabla));
    }

    @PostMapping("/mantenimiento/estadisticas/todas")
    public String actualizarEstadisticasTodas(RedirectAttributes redirect) {
        return ejecutar(redirect, mantenimientoService::actualizarEstadisticasTodas);
    }

    @PostMapping("/mantenimiento/recompilar")
    public String recompilar(@RequestParam String esquema,
                             @RequestParam String objeto,
                             RedirectAttributes redirect) {
        return ejecutar(redirect, () -> mantenimientoService.recompilarModulo(esquema, objeto));
    }

    @PostMapping("/mantenimiento/reconstruir-indice")
    public String reconstruirIndice(@RequestParam String esquema,
                                    @RequestParam String tabla,
                                    @RequestParam String indice,
                                    RedirectAttributes redirect) {
        return ejecutar(redirect, () -> mantenimientoService.reconstruirIndice(esquema, tabla, indice));
    }

    private String ejecutar(RedirectAttributes redirect, Supplier<String> accion) {
        try {
            redirect.addFlashAttribute("resultado", accion.get());
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        } catch (DataAccessException ex) {
            log.error("Error al ejecutar una acción de mantenimiento", ex);
            redirect.addFlashAttribute("error",
                    "La operación falló en SQL Server: " + ex.getMostSpecificCause().getMessage());
        }
        return "redirect:/mantenimiento";
    }
}