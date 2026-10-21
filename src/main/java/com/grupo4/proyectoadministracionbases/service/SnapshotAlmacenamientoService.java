package com.grupo4.proyectoadministracionbases.service;

import com.grupo4.proyectoadministracionbases.repository.AlmacenamientoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SnapshotAlmacenamientoService {

    private static final Logger log = LoggerFactory.getLogger(SnapshotAlmacenamientoService.class);

    private final AlmacenamientoRepository repository;

    public SnapshotAlmacenamientoService(AlmacenamientoRepository repository) {
        this.repository = repository;
    }

    @Scheduled(initialDelay = 10_000, fixedRate = 3_600_000)
    public void capturarSnapshot() {
        try {
            int filas = repository.guardarSnapshot();
            log.info("Snapshot de almacenamiento: {} bases registradas", filas);
        } catch (Exception e) {
            log.error("No se pudo guardar el snapshot de almacenamiento: {}", e.getMessage());
        }
    }
}