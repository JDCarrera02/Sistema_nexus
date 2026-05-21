package Util;

import java.sql.SQLException;

/**
 * Clase utilitaria para traducir codigos de error SQL
 * en mensajes legibles para el usuario
 * Centraliza  la logica que antes estaba duplicada en cada controlador
 * */
public class MensajeSQL {

    private MensajeSQL(){}

    /**
     * Traduce un error SQL con mensaje genérico.
     */
    public static String traducir(SQLException e) {
        return traducir(e, null);
    }

    public static String traducir(SQLException e, String contexto){
        return switch (e.getErrorCode()){
            case 1062 -> contexto!=null ? "Ya existe un registro con ese "+contexto+"."
            : "Ya existe un registro con esos datos únicos.";
            case 1451 -> contexto !=null ? "No se puede eliminar "+contexto+
            " porque  tiene registros asociados. "
            : "No se puede eliminar porque tiene registros asociados.";
            case 1452 -> "Error de integridad: algún dato referenciado no existe.";
            default   -> "Error en la base de datos: " + e.getMessage();
        };
    }
}
