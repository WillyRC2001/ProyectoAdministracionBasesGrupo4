package com.grupo4.proyectoadministracionbases.repository;

import com.grupo4.proyectoadministracionbases.service.RendimientoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba de integración del Módulo 2 contra una instancia real de SQL Server.
 *
 * <p>No se ejecuta por defecto para no depender de una base de datos
 * disponible. Para habilitarla, exporte las variables de entorno de conexión
 * ({@code DB_USER}, {@code DB_PASSWORD}) y defina {@code RENDIMIENTO_IT=true}.</p>
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "RENDIMIENTO_IT", matches = "true")
class RendimientoRepositoryIT {

    @Autowired
    private RendimientoService rendimientoService;

    @Test
    void obtieneMetricasRealesDeLaInstancia() {
        var panel = rendimientoService.construirPanel();

        assertNotNull(panel);
        assertNotNull(panel.resumen());
        assertNotNull(panel.contexto());
        assertTrue(panel.resumen().totalSesiones() >= 0);
    }
}
