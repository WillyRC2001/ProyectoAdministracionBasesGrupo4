package com.grupo4.proyectoadministracionbases.controller;

import com.grupo4.proyectoadministracionbases.dto.respaldo.PanelRespaldoDTO;
import com.grupo4.proyectoadministracionbases.service.RespaldoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/respaldo/api")
public class RespaldoApiController {

    private final RespaldoService respaldoService;

    public RespaldoApiController(RespaldoService respaldoService) {
        this.respaldoService = respaldoService;
    }

    @GetMapping("/panel")
    public PanelRespaldoDTO panel() {
        return respaldoService.construirPanel();
    }
}