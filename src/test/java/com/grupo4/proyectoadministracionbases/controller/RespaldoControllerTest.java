package com.grupo4.proyectoadministracionbases.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RespaldoControllerTest {

    @Test
    void devuelveLaVistaDeRespaldo() {
        assertEquals("respaldo/index", new RespaldoController().panel());
    }
}