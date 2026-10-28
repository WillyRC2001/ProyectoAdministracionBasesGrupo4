package com.grupo4.proyectoadministracionbases.controller;

import com.grupo4.proyectoadministracionbases.dto.respaldo.ErrorRespaldoDTO;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class RespaldoExceptionHandlerTest {

    private final RespaldoExceptionHandler manejador = new RespaldoExceptionHandler();

    @Test
    void errorDeAccesoDevuelve503SinExponerDetalles() {
        ResponseEntity<ErrorRespaldoDTO> r =
                manejador.manejarErrorDeAcceso(new DataAccessResourceFailureException("conexion rechazada"));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, r.getStatusCode());
        assertEquals("SGBD_NO_DISPONIBLE", r.getBody().codigo());
        assertFalse(r.getBody().mensaje().contains("conexion rechazada"));
    }

    @Test
    void errorInesperadoDevuelve500() {
        ResponseEntity<ErrorRespaldoDTO> r = manejador.manejarErrorInesperado(new IllegalStateException("x"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, r.getStatusCode());
        assertEquals("ERROR_INTERNO", r.getBody().codigo());
    }
}