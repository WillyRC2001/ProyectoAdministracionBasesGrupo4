package com.grupo4.proyectoadministracionbases.dto;

import java.time.LocalDateTime;
import java.util.List;

public class EstadoInstanciaDTO {

    private String servidor;
    private String instancia;
    private String version;
    private String edicion;

    private String estado;
    private LocalDateTime fechaInicio;
    private String tiempoActividad;
    private long segundosActividad;

    private double memoriaUtilizadaMB;
    private double memoriaAsignadaMB;
    private double porcentajeMemoria;

    private List<String> basesDatos;

    private String tipoSGBD;

    public EstadoInstanciaDTO() {
    }

    public EstadoInstanciaDTO(
            String servidor,
            String instancia,
            String version,
            String edicion,
            String estado,
            LocalDateTime fechaInicio,
            String tiempoActividad
    ) {
        this.servidor = servidor;
        this.instancia = instancia;
        this.version = version;
        this.edicion = edicion;
        this.estado = estado;
        this.fechaInicio = fechaInicio;
        this.tiempoActividad = tiempoActividad;
    }

    public String getServidor() {
        return servidor;
    }

    public void setServidor(String servidor) {
        this.servidor = servidor;
    }

    public String getInstancia() {
        return instancia;
    }

    public void setInstancia(String instancia) {
        this.instancia = instancia;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getEdicion() {
        return edicion;
    }

    public void setEdicion(String edicion) {
        this.edicion = edicion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getTiempoActividad() {
        return tiempoActividad;
    }

    public void setTiempoActividad(String tiempoActividad) {
        this.tiempoActividad = tiempoActividad;
    }

    public long getSegundosActividad() {
        return segundosActividad;
    }

    public void setSegundosActividad(long segundosActividad) {
        this.segundosActividad = segundosActividad;
    }

    public double getMemoriaUtilizadaMB() {
        return memoriaUtilizadaMB;
    }

    public void setMemoriaUtilizadaMB(double memoriaUtilizadaMB) {
        this.memoriaUtilizadaMB = memoriaUtilizadaMB;
    }

    public double getMemoriaAsignadaMB() {
        return memoriaAsignadaMB;
    }

    public void setMemoriaAsignadaMB(double memoriaAsignadaMB) {
        this.memoriaAsignadaMB = memoriaAsignadaMB;
    }

    public double getPorcentajeMemoria() {
        return porcentajeMemoria;
    }

    public void setPorcentajeMemoria(double porcentajeMemoria) {
        this.porcentajeMemoria = porcentajeMemoria;
    }

    public List<String> getBasesDatos() {
        return basesDatos;
    }

    public void setBasesDatos(List<String> basesDatos) {
        this.basesDatos = basesDatos;
    }

    public String getTipoSGBD() {
        return tipoSGBD;
    }

    public void setTipoSGBD(String tipoSGBD) {
        this.tipoSGBD = tipoSGBD;
    }
}
