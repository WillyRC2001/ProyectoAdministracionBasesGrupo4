package com.grupo4.proyectoadministracionbases.controller;

import com.grupo4.proyectoadministracionbases.dto.rendimiento.RespuestaErrorDTO;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Pruebas del manejo de errores del Módulo 2. Verifican que las excepciones se
 * traduzcan a respuestas seguras sin exponer detalles internos.
 */
class RendimientoExceptionHandlerTest {

    private final RendimientoExceptionHandler manejador = new RendimientoExceptionHandler();

    @Test
    void errorDeAccesoDevuelveServicioNoDisponible() {
        ResponseEntity<RespuestaErrorDTO> respuesta =
                manejador.manejarErrorDeAcceso(new DataAccessResourceFailureException("conexion rechazada"));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals("SGBD_NO_DISPONIBLE", respuesta.getBody().codigo());
    }

    @Test
    void errorInesperadoDevuelveErrorInterno() {
        ResponseEntity<RespuestaErrorDTO> respuesta =
                manejador.manejarErrorInesperado(new IllegalStateException("boom"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals("ERROR_INTERNO", respuesta.getBody().codigo());
    }
}
