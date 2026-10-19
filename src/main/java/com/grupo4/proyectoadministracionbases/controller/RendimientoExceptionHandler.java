package com.grupo4.proyectoadministracionbases.controller;

import com.grupo4.proyectoadministracionbases.dto.rendimiento.RespuestaErrorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejo de errores del Módulo 2.
 *
 * <p>Se limita únicamente al {@link RendimientoApiController} para no
 * interferir con los demás módulos. Devuelve mensajes claros y seguros al
 * usuario, sin exponer detalles internos de la excepción.</p>
 */
@RestControllerAdvice(assignableTypes = RendimientoApiController.class)
public class RendimientoExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RendimientoExceptionHandler.class);

    /**
     * Errores de conexión o ejecución contra el SGBD.
     *
     * @param ex excepción de acceso a datos.
     * @return respuesta 503 con un mensaje seguro.
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<RespuestaErrorDTO> manejarErrorDeAcceso(DataAccessException ex) {
        log.error("Error de acceso a datos en el modulo de rendimiento", ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new RespuestaErrorDTO(
                        "SGBD_NO_DISPONIBLE",
                        "No fue posible consultar el SGBD. Verifique la conexión y los permisos del usuario."));
    }

    /**
     * Cualquier otro error inesperado del módulo.
     *
     * @param ex excepción inesperada.
     * @return respuesta 500 con un mensaje seguro.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaErrorDTO> manejarErrorInesperado(Exception ex) {
        log.error("Error inesperado en el modulo de rendimiento", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RespuestaErrorDTO(
                        "ERROR_INTERNO",
                        "Ocurrió un error inesperado al obtener las métricas de rendimiento."));
    }
}
