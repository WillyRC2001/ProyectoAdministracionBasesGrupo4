package com.grupo4.proyectoadministracionbases.controller;

import com.grupo4.proyectoadministracionbases.dto.respaldo.ErrorRespaldoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = RespaldoApiController.class)
public class RespaldoExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RespaldoExceptionHandler.class);

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorRespaldoDTO> manejarErrorDeAcceso(DataAccessException ex) {
        log.error("Error de acceso a datos en el modulo de respaldo", ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorRespaldoDTO(
                        "SGBD_NO_DISPONIBLE",
                        "No fue posible consultar el historial de respaldos. Verifique la conexión y que el usuario "
                                + "tenga permiso de lectura sobre msdb (backupset y backupmediafamily)."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespaldoDTO> manejarErrorInesperado(Exception ex) {
        log.error("Error inesperado en el modulo de respaldo", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorRespaldoDTO(
                        "ERROR_INTERNO",
                        "Ocurrió un error inesperado al obtener la información de respaldos."));
    }
}