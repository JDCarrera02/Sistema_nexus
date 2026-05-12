package DAO;

import Model.Instalacion;
import Model.TipoInstalacion;
import Util.DataBaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase InstalacionDAO
 * @author Juan Diego Carrera
 * @version 1.0
 *
 * Esta clase implementa la interfaz DAO<T> y sus metodos genericos.
 * Siguiendo con el modelo relacional, se utiliza la siguiente estructura de datos definida para la base de datos:
 * id_instalacion = Llave primaria de tipo Integer
 * nombre_instalacion = cadena de caracteres "UNIQUE" no puede existir más de una instalacion con el mismo nombre
 * tipo_instalacion = ENUM con los siguientes valores = 'BAR', 'SALON_EVENTOS', 'PISCINA', 'GIMNASIO', 'PADEL', 'BARBACOA'
 * capacidad = Valor Integer, idica la capacidad o aforo de una instalacion
 * precio_hora = valor calculado DECIMAL(8,2)
 * activa = BOOLEAN, indica si una instalacion se encuentra activa, por defecto su valor es 'true'
 * */

public class InstalacionDAO implements DAO<Instalacion>{
    /**
     * Metodo para insertar una instalacion
     * @param instalacion el objeto de entrada a insertar
     * @throws SQLException si hay algun error con la base de datos
     * */
    @Override
    public void insertar(Instalacion instalacion) throws SQLException {
        // Comprobar si el objeto Instalacion es nulo
        if (instalacion == null) return; // Salir del procedimiento

        // Preparar sql
        String sql = "INSERT INTO instalaciones (nombre_instalacion, tipo_instalacion, capacidad, precio_hora, activa) " +
                "VALUES (?, ?, ?, ?, ?)";

        // Crear e inicializar variable de conexion
        Connection connection = null;

        try {
            // Establecer conexion con la base de datos
            connection = DataBaseConnection.getConnection();
            connection.setAutoCommit(false); // Iniciar transaccion

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, instalacion.getNombreInstalacion());
                // TipoInstalacion es un enum, tanto en el programa como en la base de datos; para poder pasar su valor correctamente, se utiliza su metodo .name(), que pasa su valor como String
                ps.setString(2, instalacion.getTipoInstalacion().name());
                ps.setInt(3, instalacion.getCapacidad());
                ps.setBigDecimal(4, instalacion.getPrecioHora());
                ps.setBoolean(5, instalacion.isActiva());

                ps.executeUpdate(); // Ejecutar consulta
                connection.commit(); // Si no hay errores, efectuar cambios en la base de datos
            }

        } catch (SQLException e) {
            if (connection != null) connection.rollback();
            throw e;

        } finally {
            if (connection != null) {
                connection.setAutoCommit(true);
                connection.close();
            }
        }
    }

    /**
     * Metodo para actualizar una instalacion
     * @param instalacion objeto Instalacion actualizado
     * @throws SQLException si ocurre algun error con la base de datos
     * */
    @Override
    public void actualizar(Instalacion instalacion) throws SQLException {
        // Comprobar si el objeto Instalacion de ingreso es nulo
        if (instalacion == null) return; // Si lo es, salir del procedimiento

        // Preparar sql para modificacion
        String sql = "UPDATE instalaciones SET nombre_instalacion = ?, tipo_instalacion = ?, capacidad = ?, precio_hora = ?, activa = ? " +
                "WHERE id_instalacion = ?";
        // Crear e inicializar variable de conexion
        Connection conexion = null;

        try {
            // Establecer conexion
            conexion = DataBaseConnection.getConnection();
            conexion.setAutoCommit(false); // Iniciar transaccion

            // Preprar PreparedStatement
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                // Configurar PreparedStatement
                ps.setString(1, instalacion.getNombreInstalacion());
                ps.setString(2, instalacion.getTipoInstalacion().name());
                ps.setInt(3, instalacion.getCapacidad());
                ps.setBigDecimal(4, instalacion.getPrecioHora());
                ps.setBoolean(5, instalacion.isActiva());
                ps.setInt(6, instalacion.getIdInstalacion()); // Se pasa el id de la instalacion que se va a modificar

                ps.executeUpdate(); // Ejecutar consulta
                conexion.commit(); // Efectuar cambios
            }

        } catch (SQLException e) {
            if (conexion != null) conexion.rollback();
            throw e;

        } finally {
            if (conexion != null) {
                conexion.setAutoCommit(true);
                conexion.close();
            }
        }
    }

    @Override
    public void eliminar(String id) throws SQLException {
        eliminarPorId(Integer.parseInt(id));
    }

    /**
     * Metodo que elimina una instalacion por su clave primaria "id_instalacion"
     * Metodo adicional para evitar coneversiones innecesarias de String a Integer cuando se llame desde el controlador, se trabajará con un entero directamente
     * */
    public void eliminarPorId(Integer idInstalacion) throws SQLException{
        // Preparar sql para para eliminacion
        String sql = "DELETE FROM instalaciones WHERE id_instalacion = ?";

        Connection connection = null;

        try {
            connection = DataBaseConnection.getConnection();
            connection.setAutoCommit(false); // Iniciar transaccion

            try (PreparedStatement ps = connection.prepareStatement(sql)){
                ps.setInt(1, idInstalacion);
                ps.executeUpdate();
                connection.commit();
            }

        } catch (SQLException e) {
            if (connection != null) connection.rollback();
            throw e;

        } finally {
            if (connection != null) {
                connection.setAutoCommit(true);
                connection.close();
            }
        }
    }

    /**
     * Metodo que llama a buscarInstalacionPorId (para buscar una instalacion por su llave primaria, id)
     * esto es para pasar directamente un entero a la operacion, y evitar hacer conversiones innecesarias
     * @param id Valor de entrada a convertir, el id de la instalacion a buscar
     * @return un objeto Instalacion a través de buscarInstalacionPorId()
     * @throws SQLException si ocurre algun error con la base de datos
     * */
    @Override
    public Instalacion buscarPorId(String id) throws SQLException {
        return buscarInstalacionPorId(Integer.parseInt(id));
    }

    /**
     * Metodo para buscar una instalacion por su clave
     * Mismo objetivo que con eliminarPorId() - se evitan conversiones innecesarias
     * @param idInstalacion el id de la instalacion a buscar
     * @return Un objeto Instalacion si lo encuentra, o null si no lo encuentra
     * @throws SQLException si ocurre algun error con la base de datos
     * */

    public Instalacion buscarInstalacionPorId(Integer idInstalacion) throws SQLException{
        // Preparar sql para busqueda
        String sql = "SELECT * FROM instalaciones WHERE id_instalacion = ?";

        // Establecer conexion con la base de datos
        try (Connection connection = DataBaseConnection.getConnection();
             // Crear PreparedStatement
             PreparedStatement ps = connection.prepareStatement(sql)
        ){
            // Configurar PreparedStatement
            ps.setInt(1, idInstalacion);

            // Crear ResultSet y ejecucion de consulta
            try (ResultSet rs = ps.executeQuery()){
                // Llamar metodo de construccion de objeto Instalacion y retornar objeto Instalacion
                if (rs.next()){
                    return construirInstalacion(rs);
                }
            }

        }
        return null; // Si no lo encuentra
    }

    /**
     * Metodo especifico de este DAO.
     * Devuelve todas las instalaciones de un tipo en concreto, por ejemplo, las de PADEL, o BARBACOA, o GYMNASIO, etc...
     * @param tipo un objeto Enum con el tipo de instalacion a buscar o consultar
     * @return una lista con los resultados de busqueda
     * @throws SQLException si ocurre algun error con la base de datos
     * */
    public List<Instalacion> listarPorTipo(TipoInstalacion tipo) throws SQLException {
        // Preparar consulta
        String sql = "SELECT * FROM instalaciones WHERE tipo_instalacion = ? ORDER BY nombre_instalacion";
        // Preparar lista de retorno
        List<Instalacion> instalaciones = new ArrayList<>();

        try (Connection conexion = DataBaseConnection.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            // tipo es un Enum, entonces se trata con el metodo .name() para pasarlo correctamente a la consulta y por ende, ser correctamente leido por el sistema gestor de base de datos
            ps.setString(1, tipo.name());

            try (ResultSet rs = ps.executeQuery()) { // Ejecutar consulta
                while (rs.next()) { // Guardar cada resultado en la lista
                    instalaciones.add(construirInstalacion(rs));
                }
            }
        }
        return instalaciones;
    }

    /**
     * Metodo generico para listar todas las instalaciones
     * @throws SQLException si hay algun error con la base de datos
     * @return una lista con todas las instalaciones presentes en la base de datos, ordenadas por el nombre
     * */
    @Override
    public List<Instalacion> listarTodos() throws SQLException {
        // Preparar consulta
        String sql = "SELECT * FROM instalaciones ORDER BY nombre_instalacion";
        // Preparar lista de retorno
        List<Instalacion> instalaciones = new ArrayList<>();

        try (Connection conexion = DataBaseConnection.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql);
             // Crear ResultSet
             ResultSet rs = ps.executeQuery() // Ejecutar consulta
             ) {
            while (rs.next()) { // Guardar cada resultado en la lista
                instalaciones.add(construirInstalacion(rs));
            }
        }
        return instalaciones; // Retornar resultados
    }

    /**
     * Metodo para buscar una instalacion o varias por el nombre
     * @param termino el criterio de busqueda
     * @return la lista con los resultados encontrados
     * @throws SQLException si hay algun error con la base de datos
     * */
    public List<Instalacion>buscarPorNombre(String termino) throws SQLException{
        String sql = "SELECT id_instalacion, nombre_instalacion, tipo_instalacion, capacidad, precio_hora, activa "+
                "FROM instalaciones WHERE nombre_instalacion LIKE ? "+
                "ORDER BY nombre_instalacion";

        List<Instalacion> instalaciones = new ArrayList<>();
        String like = "%"+termino+"%";
        // Establecer conexion y crear PreparedStatement
        try (Connection conexion = DataBaseConnection.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql);
        ){
            // Configurar prepareStatement
            ps.setString(1, like);

            // Crear ResultSet y ejecutar consultas
            try (ResultSet rs = ps.executeQuery()){
                while (rs.next()){
                    instalaciones.add(construirInstalacion(rs));
                }
            }
        }
        return instalaciones;
    }

    /**
     * Metodo privado para construir objeto Intalacion a partir del ResultSet
     * @param rs ResultSet que viene con el registro de la base de datos
     * @return un objeto Instalacion valido
     * @throws SQLException si hay errores con la base de datos
     * */
private Instalacion construirInstalacion(ResultSet rs) throws SQLException{

        Integer idInstalacion  = rs.getInt("id_instalacion");
        String nombre          = rs.getString("nombre_instalacion");
        TipoInstalacion tipo   = TipoInstalacion.valueOf(rs.getString("tipo_instalacion"));
        Integer capacidad      = rs.getInt("capacidad");
        BigDecimal precioHora  = rs.getBigDecimal("precio_hora");
        boolean activa         = rs.getBoolean("activa");

        return new Instalacion(idInstalacion, nombre, tipo, capacidad, precioHora, activa);
    }
}
