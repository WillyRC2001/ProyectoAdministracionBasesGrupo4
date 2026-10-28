package com.grupo4.proyectoadministracionbases.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RespaldoController {

    @GetMapping("/respaldo")
    public String panel() {
        return "respaldo/index";
    }
}