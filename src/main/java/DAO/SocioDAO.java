package DAO;

import Model.Socio;
import Model.SocioDetalle;
import Model.TipoMembresia;
import Util.DataBaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * CLase SocioDAO
 * Esta clase implementa la interfaz DAO que tiene metodos genericos para realizar un CRUD basico:
 * Consultar, modificar, eliminar, e insertar.
 * Siguiendo la estructura de tabla de entidad y la tipologia de los datos, se utiliza exclusivamente la clase Socio, definida en el modelo
 * atributos definidos en el modelo relacional:
 * num_socio = String que es la llave primaria de socio
 * dni = llave foranea que referencia a Clientes
 * fecha_alta = de tipo Date, indica la fecha en que se dió de alta el socio
 * activo = de tipo Boolean que indica si un socio está activo o no
 * id_membresia = llave foranea de tipo Integer que referencia a Membresias
 * referido_por = llave foranea de tipo String que referencia a Socio, siguiendo la lógica, un socio puede ser referido por otro, así que, en este campo va el num_socio del que lo referencia
 */
public class SocioDAO implements DAO<Socio> {

    /*
     * Para hacer una insercion de socio de manera correcta, el num_socio debe ser generado por el sistema, ya que se trata de la llave primaria de la tabla Socio
     * se añade un metodo privado adicional para comprobar el ultimo num_socio registrado, toma el numero y le suma uno
     * La estructura del num_socio debe ser: S-001, S-002, etc...
     * */

    /**
     * Metodo privado que genera el num_socio con la estructura definida
     * Consulta el ultimo num_socio registrado y le suma 1
     *
     * @param conexion la conexion para consultar el num_socio
     * @return El codigo con el formato de tres digitos o si no hay socios registrados, devuelve por defecto el primero = "S-001"
     */
    private String generarNumSocio(Connection conexion) throws SQLException {
        String sql = "SELECT num_socio FROM socios ORDER BY num_socio DESC LIMIT 1";

        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                // Extraer el ultimo numero del formato y sumarle 1
                String ultimo = rs.getString("num_socio");
                int numero = Integer.parseInt(ultimo.substring(2));
                // Formateo del codigo con tres cifras mínimo = "S-002"
                return String.format("S-%03d", numero + 1);
            }
            // Si no hay socios aún, el primero es S-001
            return "S-001";
        }
    }

    /**
     * Metodo privado para verificar que el dni existe en Clientes antes de dar un socio de alta
     * Ya que un socio no puede insertarse sin ser un cliente primero
     *
     * @param dni      el dni del cliente a verificar
     * @param conexion la conexion de la base de datos para consultar
     * @return un booleano, si encuentra el cliente "true", o si no lo encuentra "false"
     * @throws SQLException si ocurre algun error con la base de datos
     */
    private boolean clienteExiste(String dni, Connection conexion) throws SQLException {
        String sql = "SELECT DNI FROM clientes WHERE DNI = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, dni);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Metodo para insertar un socio.
     * Primero, comprueba a través del metodo privado clienteExiste() si el socio que se va a insertar, existe como cliente,
     * ya que para dar de alta a un socio, debe estar registrado como cliente.
     * Luego de comprobar si el socio existe como cliente, antes de insertarlo, llama al otro metodo privado generarNumSocio para generar internamente el numero del socio;
     * ese metodo recibe una conexion y realiza una consulta a la tabla socios, que busca el ultimo socio creado que existe en la base de datos. Para evitar crear socios duplicados,
     * ya que toma el numero del ultimo socio y le suma 1.
     * Después de estas comprobaciones, se inserta el socio en la tabla.
     *
     * @param socio el socio a insertar
     * @throws SQLException si existe algun problema con la base de datos
     */
    @Override
    public void insertar(Socio socio) throws SQLException {
        // Comprobar si el objeto es null
        if (socio == null) return; // Si lo es, salir del procedimiento

        // Preparar sql para insercion
        String sql = "INSERT INTO socios (num_socio, dni, fecha_alta, id_membresia, referido_por) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        // Crear e inicializar variable de conexion
        Connection conexion = null;

        try {
            // Establecer conexion
            conexion = DataBaseConnection.getConnection();
            // Comenzar transaccion
            conexion.setAutoCommit(false);

            // Verificacion si el cliente existe antes de insertar
            if (!clienteExiste(socio.getDni(), conexion))
                // Si no está como cliente, se lanza excepcion con mensaje especifico
                throw new SQLException("No existe ningún cliente con DNI: " + socio.getDni() + ". Registre primero el cliente antes de darlo de alta como socio.");

            // Generar el num_socio dentro de la misma transacción

            // para evitar duplicados en caso de concurrencia
            String numSocio = generarNumSocio(conexion);
            // Creacion del PreparedStatement
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                // Configuracion del PreparedStatement
                ps.setString(1, numSocio);
                ps.setString(2, socio.getDni());
                ps.setDate(3, Date.valueOf(socio.getFechaAlta()));
                ps.setInt(5, socio.getIdMembresia());

                // referidoPor es opcional
                if (socio.getReferidoPor() != null) {
                    ps.setString(6, socio.getReferidoPor());
                } else {
                    ps.setNull(6, Types.VARCHAR);
                }

                ps.executeUpdate(); // Ejecutar consulta
                conexion.commit(); // Efectuar cambios en la base de datos
            }

        } catch (SQLException e) { // En el caso de que ocurra algun error con la insercion
            if (conexion != null) conexion.rollback(); // si la conexion no es nula, se hace rollback
            throw e; // Lanzar excepcion

        } finally {
            if (conexion != null) {
                conexion.setAutoCommit(true); // Salga o no bien la insercion, finalizar restaurando el autocommit de la base de datos
                conexion.close(); // Cerrar conexion
            }
        }
    }

    /**
     * Metodo para actualizar un Socio
     * Tener en cuenta lo siguiente, num_socio es PK, y el dni es UNIQUE, estos dos atributos no deben modificarse
     *
     * @param socio el objeto socio actualizado
     * @throws SQLException si ocurre algun error con la base de datos
     */
    @Override
    public void actualizar(Socio socio) throws SQLException {
        // Verificar si el objeto es nulo
        if (socio == null) return; // Salir del procedimiento

        // Preparar sql para actualizacion
        // Nota: num_socio es la clave primaria, y dni es unique, no se deben modificar
        String sql = "UPDATE socios SET activo = ?, id_membresia = ?, referido_por = ? " +
                "WHERE num_socio = ?";

        // Crear e inicializar variable de conexion
        Connection conexion = null;

        try {
            // Establecer conexion con la base de datos
            conexion = DataBaseConnection.getConnection();
            conexion.setAutoCommit(false); // Iniciar transaccion

            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setBoolean(1, socio.isActivo());
                ps.setInt(2, socio.getIdMembresia());

                if (socio.getReferidoPor() != null)
                    ps.setString(3, socio.getReferidoPor());
                else
                    ps.setNull(3, Types.VARCHAR);

                ps.setString(4, socio.getNumSocio());

                ps.executeUpdate();
                conexion.commit();
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
     * Metodo para eliminar un socio a partir de su num_socio
     *
     * @param numSocio el numero del socio a eliminar
     * @throws SQLException si ocurre algun error con la base de datos
     */
    @Override
    public void eliminar(String numSocio) throws SQLException {
        // Preparar sql para eliminacion
        String sql = "DELETE FROM socios WHERE num_socio = ?";

        // Crear e inicializar variable de conexion
        Connection conexion = null;

        try {
            // Establecer conexion con la base de datos
            conexion = DataBaseConnection.getConnection();
            // Comenzar transaccion
            conexion.setAutoCommit(false);

            // Crear PreparedStatement
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                // Configurar PreparedStatement
                ps.setString(1, numSocio);
                ps.executeUpdate(); // Ejecutar consulta
                conexion.commit(); // Efectuar cambios en la base de datos
            }

        } catch (SQLException e) {
            // Si la conexion no es nula, pero hubo error
            if (conexion != null) conexion.rollback(); // Revertir cambios
            throw e; // Lanzar excepcion

        } finally {
            // Salga o no salga bien la eliminacion, se restablece el autocommit de la base de datos
            if (conexion != null) {
                conexion.setAutoCommit(true);
                conexion.close(); // Cerrar conexion
            }
        }
    }

    /**
     * Metodo para buscar un socio a partir de su clave primaria num_socio
     *
     * @param numSocio el numero de socio a buscar
     * @return el objeto Socio o null si no lo encuentra
     * @throws SQLException si ocurre algun error con la base de datos
     */
    @Override
    public Socio buscarPorId(String numSocio) throws SQLException {
        String sql = "SELECT num_socio, dni, fecha_alta, activo, id_membresia, referido_por " +
                "FROM socios WHERE num_socio = ?";

        try (Connection conexion = DataBaseConnection.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, numSocio);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return construirSocio(rs);
                }
            }
        }
        return null;
    }

    /**
     * Metodo adicional para buscar un socio por su dni
     * funcional para busquedas rapidas
     *
     * @param dni el dni del socio a buscar
     * @return el objeto socio encontrado o null si no lo encuentra
     * @throws SQLException si hay algun error con la base de datos
     */
    public Socio buscarPorDni(String dni) throws SQLException {
        String sql = "SELECT num_socio, dni, fecha_alta, activo, id_membresia, referido_por " +
                "FROM socios WHERE dni = ?";

        try (Connection conexion = DataBaseConnection.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, dni);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return construirSocio(rs);
                }
            }
        }
        return null;
    }

    /**
     * Metodo para listar todos los socios registrados en la base de datos, ordenados por el num_socio
     *
     * @return los resultados encontrados en la consulta
     * @throws SQLException si ocurre algun error con la base de datos
     */
    @Override
    public List<Socio> listarTodos() throws SQLException {
        String sql = "SELECT num_socio, dni, fecha_alta, activo, id_membresia, referido_por " +
                "FROM socios ORDER BY num_socio";

        List<Socio> socios = new ArrayList<>();

        try (Connection conexion = DataBaseConnection.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                socios.add(construirSocio(rs));
            }
        }
        return socios;
    }

    /**
     * Metodo especial de esta clase DAO
     * Se utiliza para listar los socios en estado activo, ordenados por el num_socio
     *
     * @return el listado de socios activos, o nada si no encuentra
     * @throws SQLException si ocurre algun error con la base de datos
     */
    public List<Socio> listarActivos() throws SQLException {
        // Preparar consulta
        String sql = "SELECT num_socio, DNI, fecha_alta, activo, id_membresia, referido_por " +
                "FROM socios WHERE activo = true ORDER BY num_socio";
        // Preparar lista de retorno
        List<Socio> socios = new ArrayList<>();

        // Establecer conexion, crear PreparedStatement y ResultSet
        try (Connection conexion = DataBaseConnection.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                socios.add(construirSocio(rs)); // Construir objetos Socios con los resultados encontrados
            }
        }
        return socios;
    }

    /**
     * Metodo para devolver todos los socios con informacion enriquecida, utilizando las tablas CLIENTES y MEMBRESIAS
     * por medio de JOIN, y utilizarse para la tabla donde estarán estos registros, manipulada por el admin
     * @return La lista con los datos, o null si hay algun error o no encuentra nada.
     * @throws SQLException si hay algun error con la base de datos
     * */
    public List<SocioDetalle> listarDetalles() throws SQLException {
        // Preparar SQL para busqueda
        String sql =
                "SELECT s.num_socio, s.DNI, c.nombre, c.apellidos, c.email, c.telefono, s.fecha_alta, s.activo, m.nombre_membresia AS tipo_membresia, s.referido_por " +
                        "FROM socios s " +
                        "JOIN clientes c ON s.DNI = c.DNI " +
                        "JOIN membresias m ON s.id_membresia = m.id_membresia " +
                        "ORDER BY s.num_socio";

        // Crear e inicializar lista de retorno
        List<SocioDetalle> detalles = new ArrayList<>();

        // Crear y establecer conexion, crear PreparedStatement y ResultSet
        try (Connection conexion = DataBaseConnection.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // Conversion del Enum, para mostrar una cadena de texto
                TipoMembresia tipo = TipoMembresia.valueOf(rs.getString("tipo_membresia"));

                detalles.add(new SocioDetalle(
                        rs.getString("num_socio"),
                        rs.getString("DNI"),
                        rs.getString("nombre"),
                        rs.getString("apellidos"),
                        rs.getString("email"),
                        rs.getString("telefono"),
                        rs.getDate("fecha_alta").toLocalDate(),
                        rs.getBoolean("activo"),
                        tipo.getNombreMembresia(),
                        rs.getString("referido_por")
                ));
            }
        }
        return detalles;
    }

    /**
     * Metodo especial para buscar socios por dni o nombre o apellidos
     * @param termino el parametro o criterio de busqueda
     * @return la lista de los socios encontrados a partir del parametro de entrada, o null si no encuentra nada
     * @throws SQLException si hay algun error con la base de datos
     * */
    public List<SocioDetalle> buscarDetalles(String termino) throws SQLException{
        // Preparar SQL para busqueda
        String sql =
                "SELECT s.num_socio, s.DNI, c.nombre, c.apellidos, c.email, c.telefono, s.fecha_alta, s.activo, m.nombre_membresia AS tipo_membresia, s.referido_por " +
                        "FROM socios s " +
                        "JOIN clientes c ON s.DNI = c.DNI " +
                        "JOIN membresias m ON s.id_membresia = m.id_membresia " +
                        "WHERE s.num_socio LIKE ? OR s.DNI LIKE ? OR c.nombre LIKE ? OR c.apellidos LIKE ? " +
                        "ORDER BY s.num_socio";
        // Crear e inicializar lista de retorno
        List<SocioDetalle> detalles = new ArrayList<>();
        // Para configurar el Like de la consulta
        String like = "%" + termino + "%";

        // Crear conexion y PreparedStatement
        try (Connection conexion = DataBaseConnection.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            // Configurar prepareStatement con el termino de busqueda
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);

            // Crear ResultSet y ejecutar consulta
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TipoMembresia tipo = TipoMembresia.valueOf(rs.getString("tipo_membresia"));
                    detalles.add(new SocioDetalle(
                            rs.getString("num_socio"),
                            rs.getString("DNI"),
                            rs.getString("nombre"),
                            rs.getString("apellidos"),
                            rs.getString("email"),
                            rs.getString("telefono"),
                            rs.getDate("fecha_alta").toLocalDate(),
                            rs.getBoolean("activo"),
                            tipo.getNombreMembresia(),
                            rs.getString("referido_por")
                    ));
                }
            }
        }
        return detalles;
    }

    /**
     * Metodo privado para construir objetos Socio a partir de un ResultSet
     *
     * @param rs El resultado de la base de datos
     * @return el objeto Socio
     * @throws SQLException si ocurre algun error con la base de datos
     */
    private Socio construirSocio(ResultSet rs) throws SQLException {
        String numSocio = rs.getString("num_socio");
        String dni = rs.getString("DNI");
        LocalDate fechaAlta = rs.getDate("fecha_alta").toLocalDate();
        boolean activo = rs.getBoolean("activo");
        Integer idMembresia = rs.getInt("id_membresia");
        String referidoPor = rs.getString("referido_por"); // Puede ser null

        return new Socio(numSocio, dni, fechaAlta, activo, idMembresia, referidoPor);
    }
}
