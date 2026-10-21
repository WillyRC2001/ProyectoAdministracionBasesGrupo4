package com.grupo4.proyectoadministracionbases.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AlmacenamientoController {

    @GetMapping("/almacenamiento")
    public String panel() {
        return "almacenamiento/index";
    }
}