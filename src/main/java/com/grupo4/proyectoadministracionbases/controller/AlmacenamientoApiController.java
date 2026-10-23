package com.grupo4.proyectoadministracionbases.controller;

import com.grupo4.proyectoadministracionbases.dto.almacenamiento.CrecimientoDTO;
import com.grupo4.proyectoadministracionbases.dto.almacenamiento.PanelAlmacenamientoDTO;
import com.grupo4.proyectoadministracionbases.service.AlmacenamientoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/almacenamiento/api")
public class AlmacenamientoApiController {

    private final AlmacenamientoService almacenamientoService;

    public AlmacenamientoApiController(AlmacenamientoService almacenamientoService) {
        this.almacenamientoService = almacenamientoService;
    }

    @GetMapping("/panel")
    public PanelAlmacenamientoDTO panel() {
        return almacenamientoService.construirPanel();
    }

    @GetMapping("/crecimiento")
    public CrecimientoDTO crecimiento() {
        return almacenamientoService.construirCrecimiento();
    }
}