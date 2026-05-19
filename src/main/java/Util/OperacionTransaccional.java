package Util;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Interfaz funcional para encapsular operaciones que requieren
 * una conexion a la base de datos dentro de una transaccion.
 * */
@FunctionalInterface
public interface OperacionTransaccional {
    // Esta función será implementada por una clase que gestiona las transacciones
    void ejecutar(Connection conexion)throws SQLException;


}
