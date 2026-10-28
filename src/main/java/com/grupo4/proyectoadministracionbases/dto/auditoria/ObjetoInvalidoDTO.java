package com.grupo4.proyectoadministracionbases.dto.auditoria;

import java.util.Set;

public record ObjetoInvalidoDTO(
        String esquema,
        String objeto,
        String tipo,
        String categoria,
        String elemento,
        String problema) {

    public static final String REFERENCIA_ROTA = "REFERENCIA_ROTA";
    public static final String INDICE_DESHABILITADO = "INDICE_DESHABILITADO";
    public static final String RESTRICCION_NO_CONFIABLE = "RESTRICCION_NO_CONFIABLE";
    public static final String TRIGGER_DESHABILITADO = "TRIGGER_DESHABILITADO";

    private static final Set<String> TIPOS_MODULO = Set.of(
            "SQL_STORED_PROCEDURE",
            "VIEW",
            "SQL_SCALAR_FUNCTION",
            "SQL_INLINE_TABLE_VALUED_FUNCTION",
            "SQL_TABLE_VALUED_FUNCTION",
            "SQL_TRIGGER");

    /** Se puede revalidar con sp_refreshsqlmodule (procedimientos, vistas, funciones y triggers). */
    public boolean recompilable() {
        return REFERENCIA_ROTA.equals(categoria) && TIPOS_MODULO.contains(tipo);
    }

    /** Se puede corregir con ALTER INDEX ... REBUILD. */
    public boolean reconstruible() {
        return INDICE_DESHABILITADO.equals(categoria);
    }
}