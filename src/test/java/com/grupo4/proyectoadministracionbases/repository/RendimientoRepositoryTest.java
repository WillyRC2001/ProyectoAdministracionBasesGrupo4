package com.grupo4.proyectoadministracionbases.repository;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del saneamiento de texto realizado por el repositorio. Al tratarse
 * de lógica pura no requiere conexión a la base de datos.
 */
class RendimientoRepositoryTest {

    @Test
    void truncaTextosLargosAgregandoElipsis() {
        String texto = "A".repeat(700);

        String resultado = RendimientoRepository.truncar(texto, 600);

        assertEquals(601, resultado.length());
        assertTrue(resultado.endsWith("…"));
    }

    @Test
    void conservaTextosCortosSinModificar() {
        assertEquals("SELECT 1", RendimientoRepository.truncar("  SELECT 1  ", 600));
    }

    @Test
    void textoNuloSeConvierteEnCadenaVacia() {
        assertEquals("", RendimientoRepository.truncar(null, 600));
    }
}
