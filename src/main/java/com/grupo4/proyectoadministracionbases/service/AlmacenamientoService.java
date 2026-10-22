package com.grupo4.proyectoadministracionbases.service;

import com.grupo4.proyectoadministracionbases.dto.almacenamiento.PanelAlmacenamientoDTO;
import com.grupo4.proyectoadministracionbases.repository.AlmacenamientoRepository;
import org.springframework.stereotype.Service;

@Service
public class AlmacenamientoService {

    private final AlmacenamientoRepository repository;

    public AlmacenamientoService(AlmacenamientoRepository repository) {
        this.repository = repository;
    }

    public PanelAlmacenamientoDTO construirPanel() {
        return new PanelAlmacenamientoDTO(
                repository.obtenerTamanoBaseDatos(),
                repository.listarFilegroups(),
                repository.listarArchivos());
    }
}