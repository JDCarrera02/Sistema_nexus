package DAO;


import Model.Membresia;
import Model.TipoMembresia;
import Util.DataBaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase MembresiaDAO
 * Esta clase implementa la interfaz DAO<T> que tiene los metodos genericos CRUD para la tabla Membresias.
 * Siguiendo con el modelo relacional, los datos definidos con los que esta clase trabaja son los siguientes:
 * id_membresia = el identificador unico de cada membresia (es creado internamente por la base de datos).
 * nombre_membresia = de tipo Enum que indica el nombre de una membresia, son los siguientes: 'ESSENTIAL', 'VIP', 'PREMIUM'.
 * precio_mensual = de tipo BigDecimal en Java, DECIMAL en la base de datos, indica el precio de lo que vale de una membresia mensualmente
 * max_reserva = de tipo Integer, indica el maximo de reservas que tiene un socio dependiendo del tipo de membresia, esto puede cambiar, pero va ligado a un tipo de membresia para poder definirlo, por defecto es cero.
 * descripcion = de tipo String, en la base de datos TEXT, indica una descripcion breve de una membresia, o anotaciones posibles de la misma, lo que se requiera poner en este campo, netamente indicativo y sirve para consulta.
 **/
public class MembresiaDAO implements DAO<Membresia> {
    /**
     * Metodo para insertar una membresia
     *
     * @param membresia el objeto Membresia a insertar
     * @throws SQLException si hay algun error con la base de datos
     */
    @Override
    public void insertar(Membresia membresia) throws SQLException {
        // Comprobar si el objeto actualizado es null o vacio
        if (membresia == null) return; // Si lo es, salir del procedimiento

        // Preparar sql para insercion
        String sql = "INSERT INTO membresias (nombre_membresia, precio_mensual, max_reservas, descripcion) " +
                "VALUES (?, ?, ?, ?)";
        // Crear e inicializar variable de conexion
        Connection conexion = null;

        try {
            // Establecer conexion con la base de datos
            conexion = DataBaseConnection.getConnection();
            conexion.setAutoCommit(false); // Iniciar transaccion
            // Crear PreparedStatement
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                // Configurar prepareStatement
                ps.setString(1, membresia.getNombre().name());
                ps.setBigDecimal(2, membresia.getPrecioMensual());
                ps.setInt(3, membresia.getMaxReservas());

                // descripcion es opcional
                if (membresia.getDescripcion() != null)
                    ps.setString(4, membresia.getDescripcion());
                else
                    ps.setNull(4, Types.VARCHAR);

                ps.executeUpdate(); // Ejecutar consulta
                conexion.commit(); // Efectuar cambios en la base de datos
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

    /**
     * Metodo para actualizar registros Membresia de la base de datos
     * Para respetar la integridad de los datos, no se permite modificar el id de una membresia
     *
     * @param membresia el objeto Membresia actualizado
     * @throws SQLException si hay algun error con la base de datos
     */
    @Override
    public void actualizar(Membresia membresia) throws SQLException {
        // Comprobar si el objeto actualizado es null o vacio
        if (membresia == null) return; // Si lo es, salir del procedimiento

        // Preparar SQl para actualizacion
        String sql = "UPDATE membresias SET nombre_membresia = ?, precio_mensual = ?, " +
                "max_reservas = ?, descripcion = ? WHERE id_membresia = ?";
        // Crear e inicializar variable de conexion
        Connection conexion = null;

        try {
            // Establecer conexion con la base de datos
            conexion = DataBaseConnection.getConnection();
            conexion.setAutoCommit(false); // Iniciar transaccion

            // Crear PreparedStatement
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                // Configurar prepareStatement
                ps.setString(1, membresia.getNombre().name());
                ps.setBigDecimal(2, membresia.getPrecioMensual());
                ps.setInt(3, membresia.getMaxReservas());

                // Comprobacion del campo descripcion, ya que es opcional
                if (membresia.getDescripcion() != null) {
                    ps.setString(4, membresia.getDescripcion()); // Si no es null, se asigna el valor
                } else {
                    ps.setNull(4, Types.VARCHAR); // En el caso de que sea null, se asigna el null del varchar (para que sql entienda que es NULL para este tipo de dato
                }

                ps.setInt(5, membresia.getIdMembresia());

                ps.executeUpdate(); // Ejecutar consulta
                conexion.commit(); // Efectuar cambios en la base de datos
            }

        } catch (SQLException e) {
            if (conexion != null) conexion.rollback(); // Si hay algun error, revertir cambios
            throw e; // Lanzar excepcion

        } finally { // Salga bien o no la operacion, finalizar siempre
            if (conexion != null) {
                conexion.setAutoCommit(true); // Restaurar el autoCommit de la base de datos
                conexion.close(); // Cerrar conexion
            }
        }
    }

    /**
     * Metodo para convertir el String id a entero, y ser utilizado en el metodo de eliminacion
     * para no hacer conversiones innecesarias desde el controlador
     *
     * @param id el String que representa el id de la membresia a eliminar, se pasa a Integer para ser utilizado por el metodo que realizarà la eliminacion
     * @throws SQLException si hay algun error con la base de datos
     */
    @Override
    public void eliminar(String id) throws SQLException {
        eliminarPorId(Integer.parseInt(id));
    }

    /**
     * Metodo para eliminar una membresia a partir de su id
     * La base de datos lanzara excepcion si hay socios asignados a una membresia, por el CONSTRAINT definidd
     * ON DELETE RESTRICT que garantiza la integridad referencial
     *
     * @param idMembresia el id de la membresia a eliminar
     * @throws SQLException si ocurre algun error con la base de datos
     */
    public void eliminarPorId(Integer idMembresia) throws SQLException {
        // Preparar el sql para la eliminacion
        String sql = "DELETE FROM membresias WHERE id_membresia = ?";

        // Crear e inicializar la variable de conexion
        Connection conexion = null;

        try {
            // Establecer conexion con la base de datos
            conexion = DataBaseConnection.getConnection();
            // Iniciar transaccion
            conexion.setAutoCommit(false);

            // Crear PreparedStatement
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                // Configuracion del prepareStatement
                ps.setInt(1, idMembresia);
                ps.executeUpdate(); // Ejecutar consulta
                conexion.commit(); // Efectuar cambios a la base de datos
            }

        } catch (SQLException e) {
            // Si hay algun error
            if (conexion != null) conexion.rollback(); // Hacer rollback, revertir cambios
            throw e; // Lanzar excepcion

        } finally {
            // Si sale todo bien, o no, finalizar siempre
            if (conexion != null) {
                conexion.setAutoCommit(true); // Restaurar autoCommit
                conexion.close(); // Cerrar conexion
            }
        }

    }

    /**
     * Metodo para buscar por el id de la membresia, que parsea el String y llama al metodo de buscar que ejecuta el SELECT y retorna un objeto Membresia
     *
     * @param id el id_membresia de la membresia a buscar
     * @throws SQLException si ocurre algun error con la base de datos
     */

    @Override
    public Membresia buscarPorId(String id) throws SQLException {
        return busquedaMembresiaPorId(Integer.parseInt(id));
    }

    /**
     * Metodo para buscar una membresia por su id
     *
     * @param idMembresia el entero "id" interno para buscar la membresia
     * @throws SQLException si ocurre algun error con la base de datos
     */
    public Membresia busquedaMembresiaPorId(Integer idMembresia) throws SQLException {

        // Preparar SQl para busqueda
        String sql = "SELECT * FROM membresias WHERE id_membresia = ?";
        // Establecer conexion con la base de datos
        try (Connection conexion = DataBaseConnection.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) { // Crear PreparedStatement
            // Configurar prepareStatement
            ps.setInt(1, idMembresia);

            // Ejecutar consulta
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return construirMembresia(rs); // Construir membresia (si la encuentra), y retorna el objeto Membresia
                }
            }
        }

        return null; // Si no encuentra la membresia
    }

    /**
     * Metodo exclusivo de membresias, para buscar una membresia por tipo: ESSENTIAL, VIP, PREMIUM
     * Este metodo llama al metodo privado construirMembresia para crear un objeto Membresia y asi trabajar
     *
     * @param tipo el Enum con el nombre de la membresia a buscar
     * @throws SQLException si hay un error con la base de datos
     */
    public Membresia consultarPorTipo(TipoMembresia tipo) throws SQLException {
        // Preparar sql para consulta
        String sql = "SELECT * FROM membresias WHERE nombre_membresia = ?";

        // Establecer conexion con la base de datos, y crear PreparedStatement
        try (Connection conexion = DataBaseConnection.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            // El parametro de entrada es un Enum, se utiliza el metodo name() para obtener el valor del Enum("ESSENTIAL, VIP, PREMIUM")
            ps.setString(1, tipo.name());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return construirMembresia(rs);
                }
            }
        }
        return null; // Si no lo encuentra
    }


    /**
     * Metodo para listar o recuperar las membresias existentes.
     * Llama al metodo construirMembresia() para crear objetos Membresia a partir del ResultSet (SELECT)
     *
     * @return la lista con las membresias o null si no hay membresias
     * @throws SQLException si hay algun error con la base de datos
     */
    @Override
    public List<Membresia> listarTodos() throws SQLException {
        // Preparacion del sql para consulta
        String sql = "SELECT * FROM membresias ORDER BY precio_mensual";

        // Crear e inicializar lisa de retorno
        List<Membresia> resultados = new ArrayList<>();

        // Establecer conexion y PreparedStatement y ResultSet
        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery() // Ejecucion del prepareStatement
        ) {
            while (rs.next()) {
                resultados.add(construirMembresia(rs));
            }
        }

        return resultados;
    }

    /**
     * Metodo privado para construir un objeto Membresia a partir de un ResultSet de una consulta
     *
     * @param rs el ResultSet o la informacion de la base de datos, como tal el registro Membresia encontrado
     * @return el objeto Membresia definido en el Modelo
     * @throws SQLException si ocurre algun error con la base de datos
     */
    private Membresia construirMembresia(ResultSet rs) throws SQLException {
        Integer idMembresia = rs.getInt("id_membresia");
        // Conversion del Enum recibido
        TipoMembresia nombreMembresia = TipoMembresia.valueOf(rs.getString("nombre_membresia"));
        BigDecimal precioMensual = rs.getBigDecimal("precio_mensual");
        Integer maxReservas = rs.getInt("max_reservas");
        // Descripcion al ser opcional, no se comprueba su contenido
        String descripcion = (rs.getString("descripcion").isBlank() ? "No hay descripcion para esta membresia" : rs.getString("descripcion"));

        // Retornar el nuevo objeto membresia
        return new Membresia(idMembresia, nombreMembresia, precioMensual, maxReservas, descripcion);
    }
}