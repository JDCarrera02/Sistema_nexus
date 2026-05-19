package DAO;

import Model.Cliente;
import Util.DataBaseConnection;
import Util.GestorTransaccion;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase ClienteDAO
 * Esta clase implementa la interfaz DAO<T> para realizar el CRUD de la entidad clientes
 * Siguiendo con el modelo relacional, esta clase trabaja con los siguientes atributos
 * dni = de tipo String con longitud de 9 caracteres, indica la identificacion unica de un cliente, es la llave primaria de esta entidad
 * nombre = el nombre del cliente, formado solamente con letras
 * apellidos = el apellido o los apellidos de un cliente, formado solamente con letras, no se admiten valores numericos
 * email = indica el email de un cliente, es unico y no puede existir el mismo correo asociado a otro cliente
 * telefono = el numero de contacto de un cliente, tiene un formato especifico que se ve mejor en la clase Validator
 * fecha_nacimiento = la fecha de nacimiento de un cliente
 * <p>
 * Utiliza GestorTransaccion para encapsular la gestión de conexiones y transacciones
 * * en las operaciones de escritura, eliminando código repetido.
 */
public class ClienteDAO implements DAO<Cliente> {
    /**
     * Metodo para insertar un cliente
     *
     * @param cliente objeto cliente de entrada a insertar
     * @throws SQLException Si ocurre algún error con la base de datos
     */
    @Override
    public void insertar(Cliente cliente) throws SQLException {
        // Comprobar si el objeto de entrada es null, no se continúa con la insercion
        if (cliente == null) return;

        // Si el objeto es valido, se prepara el sql de insercion
        String sql = "INSERT INTO clientes(DNI, nombre, apellidos, email, telefono, fecha_nacimiento) " +
                "VALUES(?, ?, ?, ?, ?, ?)";
        // Comenzar transaccion
        GestorTransaccion.ejecutar(conexion -> {
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                // Configuracion del PreparedStatement
                ps.setString(1, cliente.getDni());
                ps.setString(2, cliente.getNombre());
                ps.setString(3, cliente.getApellidos());
                ps.setString(4, cliente.getEmail());

                // Verificacion de campos opcionales - Asignar null cuando no exista información
                if (cliente.getTelefono() != null) {
                    ps.setString(5, cliente.getTelefono());
                } else {
                    ps.setNull(5, Types.NULL); // Asignar Null
                }

                if (cliente.getFechaNacimiento() != null) {
                    ps.setDate(6, Date.valueOf(cliente.getFechaNacimiento()));
                } else {
                    ps.setNull(6, Types.DATE); // Asignar DATE NULL
                }

                ps.executeUpdate(); // Ejecutar consulta
            }
        });


    }

    // Metodo para modificar clientes
    @Override
    public void actualizar(Cliente cliente) throws SQLException {
        // Validar objeto
        if (cliente == null) return;// No se realiza la actualizacion

        // Preparar SQL
        String sql = "UPDATE clientes SET nombre = ?, apellidos = ?, email = ?, telefono = ?, fecha_nacimiento = ? WHERE dni = ?";

        // Comenzar transaccion
        GestorTransaccion.ejecutar(conexion -> {
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setString(1, cliente.getNombre());
                ps.setString(2, cliente.getApellidos());
                ps.setString(3, cliente.getEmail());

                if (cliente.getTelefono() != null) {
                    ps.setString(4, cliente.getTelefono());
                } else {
                    ps.setNull(4, Types.VARCHAR);
                }

                if (cliente.getFechaNacimiento() != null) {
                    ps.setDate(5, Date.valueOf(cliente.getFechaNacimiento()));
                } else {
                    ps.setNull(5, Types.DATE);
                }

                ps.setString(6, cliente.getDni());

                ps.executeUpdate(); // Ejecutar consulta
            }
        });
    }

    /**
     * Metodo para eliminar un cliente por su DNI
     * @param dni la clave primaria del cliente a eliminar
     * @throws SQLException si ocurre algun error con la base de datos
     * */
    @Override
    public void eliminar(String dni) throws SQLException {

        String sql = "DELETE FROM clientes WHERE dni = ?";

        GestorTransaccion.ejecutar(conexion -> {
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setString(1, dni);
                ps.executeUpdate();
            }
        });
    }

    /**
     * Metodo para buscar un cliente por DNI
     * @param dni la clave primaria del cliente a buscar
     * @return el objeto Cliente si lo encuentra, o null si no existe
     * @throws SQLException si ocurre algun error con la base de datos
     * */
    @Override
    public Cliente buscarPorId(String dni) throws SQLException {
        String sql = "SELECT * FROM clientes WHERE dni = ?";

        try (Connection conexion = DataBaseConnection.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, dni);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return construirCliente(rs);
                }
            }
        }
        // Si no encuentra el registro devuelve null
        return null;
    }

    // Metodo para devolver registros de clientes, listar (SELECT * FROM CLIENTES)
    @Override
    public List<Cliente> listarTodos() throws SQLException {
        // Preparar SQL para consulta
        String sql = "SELECT * FROM clientes ORDER BY apellidos, nombre";

        // Crear e inicializar lista de retorno
        List<Cliente> clientes = new ArrayList<>();

        // Conectar con la base de datos y configurar consulta
        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            // Mientras se encuentren resultados o contenido de lo consultado
            while (rs.next()) {
                clientes.add(construirCliente(rs));
            }
        }

        return clientes; // Retornar resultados
    }

    /**
     * Metodo para buscar un cliente por su email
     *
     * @param email el email del cliente a buscar
     * @return el registro encontrado a partir del email especificado, o null si no lo encuentra
     * @throws SQLException si hay algun error con la base de datos
     */
    public Cliente buscarPorEmail(String email) throws SQLException {
        // Preparar sql para busqueda
        String sql = "SELECT * FROM clientes WHERE email = ?";

        // Conectar con la base de datos y configurar consulta
        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return construirCliente(rs); // Si encuentra el cliente con el email especificado, se retorna el objeto Cliente
                }
            }
        }

        return null; // Si no lo encuentra
    }

    /**
     * Metodo para buscar clientes por DNI, nombre/apellidos
     *
     * @param termino El criterio de busqueda
     * @return la lista con los resultados, o null si no encuentra nada
     * @throws SQLException si hay algun error con la base de datos
     */
    public List<Cliente> buscarPorTermino(String termino) throws SQLException {
        // Preparar SQL para busqueda
        String sql =
                "SELECT DNI, nombre, apellidos, email, telefono, fecha_nacimiento " +
                        "FROM clientes " +
                        "WHERE DNI LIKE ? OR nombre LIKE ? OR apellidos LIKE ? " +
                        "ORDER BY apellidos, nombre";

        // Crear e inicializar lista de retorno
        List<Cliente> clientes = new ArrayList<>();

        // Configurar like de la consulta
        String like = "%" + termino + "%";

        // Crear conexion y PreparedStatement
        try (Connection conexion = DataBaseConnection.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            // Configurar prepareStatement
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);

            // Crear ResultSet y ejecutar consulta
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    clientes.add(construirCliente(rs));
                }
            }
        }
        return clientes;
    }

    /**
     * Metodo para construir un objeto Cliente a partir de un ResulSet
     *
     * @param rs el ResultSet de la base de datos
     * @return el objeto Cliente construido
     * @throws SQLException si ahy algun error con la base de datos
     */
    private Cliente construirCliente(ResultSet rs) throws SQLException {
        String dni = rs.getString("dni");
        String nombre = rs.getString("nombre");
        String apellidos = rs.getString("apellidos");
        String email = rs.getString("email");
        String telefono = rs.getString("telefono"); // Este campo puede ser null
        Date fechaSql = rs.getDate("fecha_nacimiento");
        LocalDate fechaNacimiento = (fechaSql != null) ? fechaSql.toLocalDate() : null; // Si la fecha capturada es null, así se guardará en el constructor, ya que este campo no es obligatorio, puede ser nulo, no se comprueba nada. Pero si tiene contenido, se parsea la entrada y se valida el formato

        return new Cliente(dni, nombre, apellidos, email, telefono, fechaNacimiento);
    }
}