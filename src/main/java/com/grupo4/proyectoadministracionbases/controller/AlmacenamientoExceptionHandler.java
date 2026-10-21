package com.grupo4.proyectoadministracionbases.controller;

import com.grupo4.proyectoadministracionbases.dto.almacenamiento.ErrorAlmacenamientoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice(assignableTypes = AlmacenamientoApiController.class)
public class AlmacenamientoExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AlmacenamientoExceptionHandler.class);

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorAlmacenamientoDTO> manejarErrorDeAcceso(DataAccessException ex) {
        log.error("Error de acceso a datos en el modulo de almacenamiento", ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorAlmacenamientoDTO(
                        "SGBD_NO_DISPONIBLE",
                        "No fue posible consultar el SGBD. Verifique la conexión y los permisos del usuario."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorAlmacenamientoDTO> manejarErrorInesperado(Exception ex) {
        log.error("Error inesperado en el modulo de almacenamiento", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorAlmacenamientoDTO(
                        "ERROR_INTERNO",
                        "Ocurrió un error inesperado al obtener la información de almacenamiento."));
    }
}