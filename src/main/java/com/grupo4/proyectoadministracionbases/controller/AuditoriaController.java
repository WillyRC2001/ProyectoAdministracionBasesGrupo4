package com.grupo4.proyectoadministracionbases.controller;

import com.grupo4.proyectoadministracionbases.service.AuditoriaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuditoriaController {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaController.class);

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping("/auditoria")
    public String mostrarAuditoria(Model model) {
        try {
            model.addAttribute("panel", auditoriaService.construirPanel());
        } catch (DataAccessException ex) {
            log.error("Error de acceso a datos en el módulo de auditoría", ex);
            model.addAttribute("error",
                    "No fue posible consultar el SGBD. Verifique la conexión y los permisos del usuario.");
        }
        return "auditoria/index";
    }
}