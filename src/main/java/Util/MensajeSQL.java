package Util;

import java.sql.SQLException;

/**
 * Clase utilitaria para traducir codigos de error SQL
 * en mensajes legibles para el usuario
 * Centraliza  la logica que antes estaba duplicada en cada controlador
 * */
public class MensajeSQL {

    private MensajeSQL(){}

    public static String traducir(SQLException e){
        return switch (e.getErrorCode()){
            case 1062 -> "Ya existe un registro con esos datos únicos (valor duplicado).";
            case 1451 -> "No se puede eliminar porque tiene registros asociados.";
            case 1452 -> "Error de integridad: algún dato referenciado no existe.";
            default   -> "Error en la base de datos: " + e.getMessage();
        };
    }
}
