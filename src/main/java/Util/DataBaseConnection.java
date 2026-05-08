package Util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/*
* Clase DataBaseConnection
* Esta clase será utilizada por los dao, para realizar la conexion respectiva con la base de datos
* Inserciones, modificaciones, eliminaciones, consultas y busquedas rapidas especificas de cada clase
*
* Siguiendo con el estandar, se declaran 3 constantes que indican la url de la base de datos, el usuario y la contraseña para poder acceder
* */
public class DataBaseConnection {

    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/sistema_nexus";
    private static final String DATABASE_USER = "root";
    private static final String PASSWORD = "1234";

    // Constructor para evitar que esta clase sea instanciada
    private DataBaseConnection(){}

    /**
     * Metodo que devuelve una conexion activa a la base de datos
     * Esto para que cada clase DAO la utilice (controlando las excepciones)
     * @throws SQLException en el caso de ocurra un error al conectarse a la base de datos
     * */
    public static Connection getConnection()throws SQLException {
        return DriverManager.getConnection(DATABASE_URL,DATABASE_USER,PASSWORD);
    }
}
