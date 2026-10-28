package com.grupo4.proyectoadministracionbases.service;

import com.grupo4.proyectoadministracionbases.dto.respaldo.PanelRespaldoDTO;
import com.grupo4.proyectoadministracionbases.dto.respaldo.RespaldoDTO;
import com.grupo4.proyectoadministracionbases.dto.respaldo.UltimoRespaldoDTO;
import com.grupo4.proyectoadministracionbases.repository.RespaldoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RespaldoService {

    private final RespaldoRepository repository;

    public RespaldoService(RespaldoRepository repository) {
        this.repository = repository;
    }

    public PanelRespaldoDTO construirPanel() {
        String baseDatos = repository.obtenerBaseDatos();
        String modeloRecuperacion = repository.obtenerModeloRecuperacion();
        List<UltimoRespaldoDTO> ultimos = repository.listarUltimosExitosos();
        List<RespaldoDTO> historial = repository.listarHistorial();

        return new PanelRespaldoDTO(
                baseDatos,
                modeloRecuperacion,
                ultimos,
                historial,
                InterpretacionRespaldo.generarHallazgos(modeloRecuperacion, ultimos, historial));
    }
}