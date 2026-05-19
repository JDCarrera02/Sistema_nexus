package Util;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Clase utilitaria que ejecuta operaciones dentro de una transaccion.
 * Gestiona automaticamente autocommit, commit, rollback y cierre de conexion
 * */
public class GestorTransaccion {

    private GestorTransaccion(){}

    /**
     * Implementa el metodo de la interfaz OperacionTransaccion ejecutar().
     * Ejecuta una operacion dentro de una transaccion controlada.
     * Siempre restaura autoCommit y cierra la conexion.
     *
     * @param operacion la operacion a ejecutar
     * @throws SQLException si ocurre algun error en la base de datos
     * */
    public static void ejecutar(OperacionTransaccional operacion)throws SQLException{

        Connection conexion = null; // Crear e inicializar variable de conexion con la base de datos

        try {
            conexion = DataBaseConnection.getConnection(); // Establecer conexion
            conexion.setAutoCommit(false); // Desactivar autoCommit de la base de datos, iniciar la transaccion
            operacion.ejecutar(conexion); // Llamar al metodo de la interfaz, para ejecutar la operacion (metodos de la variable conexion) y asignacion
            conexion.commit(); // Hacer commit si todo sale bien
        } catch (SQLException e) {
            if (conexion != null) conexion.rollback(); // Si hay algun error, deshacer cambios
            throw e; // Lanzar excepcion
        } finally {
            if (conexion != null){
                conexion.setAutoCommit(true); // Siempre finalizar operacion restableciendo el autoCommit de la base de datos
                conexion.close(); // Cerrar la conexion
            }
        }
    }

}
