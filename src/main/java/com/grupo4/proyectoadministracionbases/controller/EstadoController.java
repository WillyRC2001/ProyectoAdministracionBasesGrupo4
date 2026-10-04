package com.grupo4.proyectoadministracionbases.controller;

import com.grupo4.proyectoadministracionbases.dto.EstadoInstanciaDTO;
import com.grupo4.proyectoadministracionbases.service.EstadoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EstadoController {

    private final EstadoService estadoService;

    public EstadoController(EstadoService estadoService) {
        this.estadoService = estadoService;
    }

    @GetMapping("/estado")
    public String mostrarEstado(Model model) {

        EstadoInstanciaDTO estado = estadoService.obtenerEstadoInstancia();

        model.addAttribute("estado", estado);

        return "estado/index";
    }
}
