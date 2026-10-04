package com.grupo4.proyectoadministracionbases.service;

import com.grupo4.proyectoadministracionbases.dto.EstadoInstanciaDTO;
import com.grupo4.proyectoadministracionbases.repository.EstadoRepository;
import org.springframework.stereotype.Service;

@Service
public class EstadoService {

    private final EstadoRepository estadoRepository;

    public EstadoService(EstadoRepository estadoRepository) {
        this.estadoRepository = estadoRepository;
    }

    public EstadoInstanciaDTO obtenerEstadoInstancia() {


        EstadoInstanciaDTO estado =
                estadoRepository.obtenerEstadoInstancia();

        String estadoServicio =
                estadoRepository.obtenerEstadoServicio();

        estado.setEstado(estadoServicio);

        estado.setTipoSGBD("Microsoft SQL Server");

        estado.setTiempoActividad(
                convertirTiempo(
                        estado.getSegundosActividad()
                )
        );

        if (estado.getMemoriaAsignadaMB() > 0) {

            double porcentaje =
                    (estado.getMemoriaUtilizadaMB()
                            / estado.getMemoriaAsignadaMB()) * 100;

            estado.setPorcentajeMemoria(
                    Math.round(porcentaje * 100.0) / 100.0
            );
        }

        estado.setBasesDatos(
                estadoRepository.obtenerBasesDatos()
        );

        return estado;
    }

    private String convertirTiempo(long segundos) {

        long dias = segundos / 86400;
        segundos %= 86400;

        long horas = segundos / 3600;
        segundos %= 3600;

        long minutos = segundos / 60;

        return dias + " días, "
                + horas + " horas, "
                + minutos + " minutos";
    }
}