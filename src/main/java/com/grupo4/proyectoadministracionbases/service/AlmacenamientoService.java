package com.grupo4.proyectoadministracionbases.service;

import com.grupo4.proyectoadministracionbases.dto.almacenamiento.ArchivoBaseDatosDTO;
import com.grupo4.proyectoadministracionbases.dto.almacenamiento.CrecimientoDTO;
import com.grupo4.proyectoadministracionbases.dto.almacenamiento.FilegroupDTO;
import com.grupo4.proyectoadministracionbases.dto.almacenamiento.PanelAlmacenamientoDTO;
import com.grupo4.proyectoadministracionbases.dto.almacenamiento.SnapshotAlmacenamientoDTO;
import com.grupo4.proyectoadministracionbases.dto.almacenamiento.TamanoBaseDatosDTO;
import com.grupo4.proyectoadministracionbases.repository.AlmacenamientoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlmacenamientoService {

    private final AlmacenamientoRepository repository;

    public AlmacenamientoService(AlmacenamientoRepository repository) {
        this.repository = repository;
    }

    public PanelAlmacenamientoDTO construirPanel() {
        TamanoBaseDatosDTO tamano = repository.obtenerTamanoBaseDatos();
        List<FilegroupDTO> filegroups = repository.listarFilegroups();
        List<ArchivoBaseDatosDTO> archivos = repository.listarArchivos();

        return new PanelAlmacenamientoDTO(
                tamano,
                filegroups,
                archivos,
                repository.listarObjetosMayores(),
                InterpretacionAlmacenamiento.generarHallazgos(filegroups, archivos));
    }

    public CrecimientoDTO construirCrecimiento() {
        String baseDatos = repository.obtenerTamanoBaseDatos().baseDatos();
        List<SnapshotAlmacenamientoDTO> historial = repository.listarSnapshots(baseDatos);
        return InterpretacionAlmacenamiento.calcularCrecimiento(baseDatos, historial);
    }
}