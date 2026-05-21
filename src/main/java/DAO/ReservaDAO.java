package DAO;

import Model.EstadoReserva;
import Model.Reserva;
import Model.ReservaDetalle;
import Model.TipoInstalacion;
import Util.DataBaseConnection;
import Util.GestorTransaccion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase ReservaDAO
 * Esta clase implementa los metodos de la interfaz DAO<T> para realizar el CRUD de una reserva en la base de datos.
 * Siguiendo el modelo relacional, se trabaja con los siguientes atributos de la entidad reservas:
 * id_reserva = Llave primaria, generada por el sistema
 * dni_cliente = Llave foranea que referencia a clientes, esto para saber qué cliente ha realizado la reserva
 * id_instalacion = Llave foranea que referencia a instalaciones, para saber qué instalacion ha sido reservada
 * hora_inicio = de tipo TIME, indica la hora de inicio de una reserva
 * hora_fin = de tipo TIME, indica la hora de finalizacion de una reserva
 * precio = cantidad DECIMAL, calculada por el sistema, indica el precio de una reserva
 * estado = de tipo ENUM con los siguientes valores: ('CONFIRMADA', 'CANCELADA', 'COMPLETADA', indica los estados en los que puede estar una reserva
 * <p>
 * Utiliza GestorTransaccion para encapsular la gestión de conexiones y transacciones
 * en las operaciones de escritura, eliminando código repetido.
 * <p>
 * Una reserva es un registro histórico inmutable — solo puede cambiar su estado.
 * Los métodos actualizar() y eliminar() de la interfaz lanzan UnsupportedOperationException.
 */
public class ReservaDAO implements DAO<Reserva> {

    /*
     * Explicacion de lo que es el solapamiento en las reservas y así entender el porqué de los dos metodos privados que se ejecutan en la misma transaccion de insercion
     * Un solapamiento de la reserva ocurre cuando dos reservas comparte tiempo en la misma instalacion:
     * Por ejemplo, se tienen dos reservas, A y B:
     * Reserva A: |------10:00----11:30----|
     * Reserva B:        |----11:00--------12:30-----|
     * Puede apreciarse que las dos reservas tienen un rango de hora en la misma instalacion, hay que evitar eso; por lo que ninguna reserva puede hacerse en la misma instalacion en la misma franja de hora que Reserva A, por lo que Reserva B sería invalida
     * EL sistema detecta esto, por esa razón se realizan estos dos metodos privados, uno que valida lo primero, y la segunda hace el calculo del precio.
     *
     * Hay cuatro tipos de solapamientos en este caso de las reservas, partiendo del caso base de la Reserva A:
     * Que otra reserva empiece antes y termina en medio
     * Que está justo en medio de la franja horaria.
     * Que empieza en medio y termine despues.
     * Que envuelva toda la franja horaria, incluso termine despues.
     *
     * Luego de estudiar estos casos, existen dos casos que no son conflictos y que por ello, el mismo sistema detecta esto para insertar de manera correcta una reserva:
     *
     * Reserva A:    |------10:00----12:00----|
     * Reserva B: |---8:00--10:00---| (valida)   --- Termina justo cuando empieza la reserva A
     * Reserva C:                             |----12:00--14:00--| -- Empieza justo cuando termina B
     *
     * */

    /**
     * Metodo privado que calcula el precio de la reserva de esta manera:
     * precio = precio_hora * duracion en horas
     * este metodo se ejecuta de la misma transaccion en el metodo insertar
     * y retorna el valor calculado redondeado
     *
     * @param idInstalacion el id de la instalacion
     * @param horaInicio    la hora de inicio de la reserva
     * @param horaFin       la hora de fin de la reserva
     * @param conexion      la conexion de la base de datos para realizar la consulta
     * @return El precio de la instalacion redondeado
     * @throws SQLException si hay algun error con la base de datos
     */
    private BigDecimal calcularPrecio(Integer idInstalacion, LocalTime horaInicio, LocalTime horaFin, Connection conexion) throws SQLException {

        String sql = "SELECT precio_hora FROM instalaciones WHERE id_instalacion = ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idInstalacion);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("No existe ninguna instalacion con id: " + idInstalacion);
                }

                BigDecimal precioHora = rs.getBigDecimal("precio_hora");

                // Calculo de la duracion en minutos
                long minutos = java.time.Duration.between(horaInicio, horaFin).toMinutes();
                // Calcular precio a partir del resultado de las horas calculadas. Convertir minutos a horas
                BigDecimal duracion = BigDecimal.valueOf(minutos).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

                // Retorno del precio_hora calculado (redondeado)
                return precioHora.multiply(duracion).setScale(2, RoundingMode.HALF_UP);

            }
        }
    }

    /**
     * Metodo privado para validar si una reserva es valida,
     * valida si la hora de inicio ingresada es menor a la hora de fin de la reserva
     * y si la hora de fin ingresada es mayor a la hora de inicio de la reserva.
     * Todo esto es para garantizar la integridad de datos, y guardar un historico de reservas correctamente
     * Por ejemplo tenemos una reserva existente con el siguiente horario: 10:00 - 12:00
     * Caso 1 — nueva: 09:00 - 11:00
     *   10:00 < 11:00 AND 12:00 > 09:00 = CONFLICTO
     * Caso 2 — nueva: 10:30 - 11:30
     * 10:00 < 11:30 AND 12:00 > 10:30 = CONFLICTO
     * Caso 3 — nueva: 11:00 - 13:00
     * 10:00 < 13:00 AND 12:00 > 11:00 = CONFLICTO
     * Caso 4 — nueva: 09:00 - 13:00
     * 10:00 < 13:00 AND 12:00 > 09:00 = CONFLICTO
     * <p>
     * Casos validos partiendo del caso ejemplo de comparacion 10:00 - 12:00:
     * Nueva: 08:00 - 10:00
     * 10:00 < 10:00 = SIN CONFLICTO
     * Nueva: 12:00 - 14:00
     * 10:00 < 14:00 AND 12:00 > 12:00 = SIN CONFLICTO
     *
     * @param idInstalacion el id de la instalacion a validar
     * @param fecha         la fecha de la reserva
     * @param horaInicio    la hora de inicio de la reserva
     * @param horaFin       la hora de finalizacion de la reserva
     * @param conexion      la conexion de la base de datos
     * @return si hay conflicto = true, si no lo hay = false
     * @throws SQLException si hay algun error con la base de datos
     */
    private boolean hayConflicto(Integer idInstalacion, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, Connection conexion) throws SQLException {
        // Detecta cualquier reserva en estado CONFIRMADA que se solape con el rango solicitado (la hora de inicio y fin, sea valido)
        // Una reserva tiene conflicto o se solapa si su inicio es anterior al fin solicitado, y su hora de fin es posterior al inicio solicitado, para entender mejor, ir al principio de la documentacion de esta clase, que ahí se explica con mayor claridad.
        String sql = "SELECT id_reserva FROM reservas " +
                "WHERE id_instalacion = ? " +
                "AND fecha_reserva = ? " +
                "AND estado = 'CONFIRMADA' " +
                "AND hora_inicio < ? " +
                "AND hora_fin > ?";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idInstalacion);
            ps.setDate(2, Date.valueOf(fecha));
            ps.setTime(3, Time.valueOf(horaFin)); // En la consulta, se compara si la hora de inicio es menor a la hora de fin de la reserva
            ps.setTime(4, Time.valueOf(horaInicio)); // En la consulta, se compara si la hora de fin es mayor que la hora de inicio de la reserva

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // Devuelve true si existe algún conflicto o false si no
            }
        }
    }

    /**
     * Metodo que inserta una reserva, llamando al metodo de calculo de precio, y el de comprobacion de conflicto
     *
     * @param reserva la reserva a insertar o crear
     * @throws SQLException si hay algun error con la base de datos
     */
    @Override
    public void insertar(Reserva reserva) throws SQLException {
        // Comprobar si el objeto a insertar es nulo
        if (reserva == null) return; // Si lo es, salir del procedimiento

        // Preparacion de sql para insercion
        String sql = "INSERT INTO reservas (dni_cliente, id_instalacion, fecha_reserva, hora_inicio, hora_fin, precio, estado) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?)";

        // Comenzar transaccion
        GestorTransaccion.ejecutar(conexion -> {

            // Verificar si hay algun solapamiento de reserva
            if (hayConflicto(reserva.getIdInstalacion(), reserva.getFechaReserva(), reserva.getHoraInicio(), reserva.getHoraFin(), conexion)) {
                throw new SQLException("La instalacion ya tiene una reserva confirmada en ese horario.");
            }

            // Calcular precio
            BigDecimal precio = calcularPrecio(reserva.getIdInstalacion(),
                    reserva.getHoraInicio(),
                    reserva.getHoraFin(),
                    conexion);

            // Crear PreparedStatement
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                // Configuracion del prepareStatement
                ps.setString(1, reserva.getDniCliente());
                ps.setInt(2, reserva.getIdInstalacion());
                ps.setDate(3, Date.valueOf(reserva.getFechaReserva()));
                ps.setTime(4, Time.valueOf(reserva.getHoraInicio()));
                ps.setTime(5, Time.valueOf(reserva.getHoraFin()));
                ps.setBigDecimal(6, precio);
                ps.setString(7, reserva.getEstado().name());

                // Ejecutar consulta
                ps.executeUpdate();
            }
        });
    }

    @Override
    public void actualizar(Reserva reserva) throws SQLException {
        // Una reserva no puede editarse, solo puede cambiar su estado
        // Usar metodo actualizarEstado()
        throw new UnsupportedOperationException("Las reservas no se modifican. Sólo pueden cambiar de estado");
    }

    /**
     * Metodo que actualiza el estado de una reserva, ya que por integridad de datos, una reserva solo puede cambiar su estado, mas no los demás datos
     * Se trata de una entidad "historial" que guarda el historico de reservas realizadas por los clientes o socios en el sistema
     *
     * @param idReserva   el id de la reserva a actualizar
     * @param nuevoEstado el nuevo estado de la reserva "CANCELADA" O "CONFIRMADA"
     * @throws SQLException si hay algun error con la base de datos
     */
    public void actualizarEstado(Integer idReserva, EstadoReserva nuevoEstado) throws SQLException {
        // Preparar SQL para actualizar
        String sql = "UPDATE reservas SET estado = ? WHERE id_reserva = ?";

        // Comenzar transaccion
        GestorTransaccion.ejecutar(conexion -> {
            // Crear PreparedStatement
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                // Configurar prepareStatement
                ps.setString(1, nuevoEstado.name());
                ps.setInt(2, idReserva);

                ps.executeUpdate(); // Ejecutar consulta
            }
        });
    }

    @Override
    public void eliminar(String id) throws SQLException {
        // Una reserva no puede eliminarse, ya que se trata de una tabla "historial" que guarda el historico de cada reserva realizada por los usuarios
        throw new UnsupportedOperationException("Las reservas no se eliminan. Si desea invalidar una reserva, cambie el estado a CANCELADA");
    }

    @Override
    public Reserva buscarPorId(String id) throws SQLException {
        return buscarReservaPorId(Integer.parseInt(id));
    }

    /**
     * Metodo para buscar por el id de una reserva (utilizando el parametro de tipo Integer)
     *
     * @param idReserva el id de la reserva a buscar
     * @return el objeto reserva encontrado o null si no lo encuentra
     * @throws SQLException si hay algun error con la base de datos
     */
    public Reserva buscarReservaPorId(Integer idReserva) throws SQLException {
        // Preparar sql para busqueda
        String sql = "SELECT * FROM reservas WHERE id_reserva = ?";

        // Establecer conexion y crear PreparedStatement
        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            // Configurar PreparedStatemen
            ps.setInt(1, idReserva);

            // Crear ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return construirReserva(rs);
                }
            }

        }

        return null;
    }

    /**
     * Metodo para listar todas las reservas registradas, ordenadas por fecha_reserva (descendentemente) para mostrar desde la más reciente hasta las más antigua
     *
     * @return La lista con los resultados
     * @throws SQLException si hay algun error con la base de datos
     */
    @Override
    public List<Reserva> listarTodos() throws SQLException {
        // Preparar sql para busqueda
        String sql = "SELECT * FROM reservas ORDER BY fecha_reserva DESC";

        // Crear e inicializar lista de retorno
        List<Reserva> reservas = new ArrayList<>();

        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                reservas.add(construirReserva(rs));
            }
        }

        return reservas;
    }

    /**
     * Metodo exclusivo para listar reservas por cliente, ordenadas por fecha_reserva descendentemente y por hora de inicio ascendentemente
     *
     * @param dniCliente el dni del cliente que tiene reservas realizadas
     * @return la lista con los resultados encontrados o una lista null si no encuentra nada por ese dni de ciente
     * @throws SQLException si hay algun error con la base de datos
     */
    public List<Reserva> listarPorCliente(String dniCliente) throws SQLException {
        // Preparacion de sql para busqueda
        String sql = "SELECT * FROM reservas WHERE dni_cliente = ? ORDER BY fecha_reserva DESC, hora_inicio";

        // Crear e inicializar lista de retorno
        List<Reserva> reservas = new ArrayList<>();

        // Establecer conexion y crear PreparedStatement
        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            // Configurar prepareStatement
            ps.setString(1, dniCliente);

            // Crear ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reservas.add(construirReserva(rs));
                }
            }
        }

        return reservas;
    }

    /**
     * Metodo exclusivo para listar reservas por instalacion, por medio de su id
     *
     * @param idInstalacion el id de la instalacion a buscar sus reservas correspondientes
     * @return la lista con los resultados o una lista nula si no encuentra nada por ese id de instalacion
     * @throws SQLException Si hay algun error con la base de datos
     */
    public List<Reserva> listarPorInstalacion(Integer idInstalacion) throws SQLException {
        // Preparacion de sql para busqueda
        String sql = "SELECT * FROM reservas WHERE id_instalacion = ? ORDER BY fecha_reserva DESC, hora_inicio";

        // Crear e inicializar lista de retorno
        List<Reserva> reservas = new ArrayList<>();

        // Establecer conexion y crear PreparedStatement
        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            // Configurar prepareStatement
            ps.setInt(1, idInstalacion);

            // Crear ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reservas.add(construirReserva(rs));
                }
            }
        }

        return reservas;
    }

    /**
     * Metodo exclusivo para listar reservas por fecha_reserva
     *
     * @param fecha el criterio de fecha con el que se buscaran reservas
     * @return la lista con los resultados encontrados o una lista nula si no ha encontrado nada con ese criterio de fecha
     * @throws SQLException si hay algun error con la base de datos
     */
    public List<Reserva> listarPorFecha(LocalDate fecha) throws SQLException {
        // Preparacion de sql para busqueda
        String sql = "SELECT * FROM reservas WHERE fecha_reserva = ? ORDER BY hora_inicio";

        // Crear e inicializar lista de retorno
        List<Reserva> reservas = new ArrayList<>();

        // Establecer conexion y crear PreparedStatement
        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            // Configurar prepareStatement
            ps.setDate(1, Date.valueOf(fecha));

            // Crear ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reservas.add(construirReserva(rs));
                }
            }
        }

        return reservas;
    }

    /**
     * Metodo que devuelve las reservas de un cliente junto con las instalaciones reservadas, mediante JOIN
     *
     * @param dniCliente el dni del cliente(socio)
     * @return La lista con los detalles encontrados
     * @throws SQLException si hay algun error con la base de datos
     */
    public List<ReservaDetalle> listarDetallesPorCliente(String dniCliente) throws SQLException {
        // Preparar SQL para consulta
        String sql = "SELECT r.id_reserva, i.nombre_instalacion, i.tipo_instalacion, r.fecha_reserva, " +
                "r.hora_inicio, r.hora_fin, r.precio, r.estado FROM reservas r " +
                "JOIN instalaciones i ON r.id_instalacion = i.id_instalacion " +
                "WHERE r.dni_cliente = ? " +
                "ORDER BY r.fecha_reserva DESC, r.hora_inicio";

        // Preparar lista de retorno
        List<ReservaDetalle> detalles = new ArrayList<>();

        // Establecer conexion y Crear PreparedStatement
        try (Connection conexion = DataBaseConnection.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)
        ) {
            // Configurar prepareStatement
            ps.setString(1, dniCliente);

            // Crear ResultSet y ejecutar consulta
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    detalles.add(construirReservaDetalle(rs));
                }
            }
        }

        return detalles;
    }

    /**
     * Metodo para buscar reservas a partir del criterio de busqueda del cliente, se filtraran resultados a partir del nombre de una instalacion
     *
     * @param dniCliente el dni del cliente Socio correspondiente
     * @param termino    el nombre de la instalacion
     * @throws SQLException si hay algun error con la base de datos
     */
    public List<ReservaDetalle> buscarDetallesPorInstalacion(String dniCliente, String termino) throws SQLException {
        // Preparar SQL para consulta
        String sql =
                "SELECT r.id_reserva, i.nombre_instalacion, " +
                        "i.tipo_instalacion, r.fecha_reserva, " +
                        "r.hora_inicio, r.hora_fin, r.precio, r.estado " +
                        "FROM reservas r " +
                        "JOIN instalaciones i ON r.id_instalacion = i.id_instalacion " +
                        "WHERE r.dni_cliente = ? " +
                        "AND i.nombre_instalacion LIKE ? " +
                        "ORDER BY r.fecha_reserva DESC, r.hora_inicio";
        // Preparar lista de retorno
        List<ReservaDetalle> detalles = new ArrayList<>();

        // Configurar Like de la consulta
        String like = "%" + termino + "%";

        // Establecer conexion y crear PreparedStatement
        try (Connection conexion = DataBaseConnection.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)
        ) {
            // Configurar prepareStatement
            ps.setString(1, dniCliente);
            ps.setString(2, like);

            // Crear resultSet y ejecutar consulta
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    detalles.add(construirReservaDetalle(rs));
                }
            }
        }

        return detalles;
    }

    /**
     * Metodo para listar todas las reservas con informacion enriquecida, a partir de JOIN con instalaciones y clientes.
     *
     * @return la lista con la informacion enriquecida
     * @throws SQLException si hay algun error con la base de datos
     */
    public List<ReservaDetalle> listarTodosDetalles() throws SQLException {
        // Preparar sql para consulta
        String sql = "SELECT r.id_reserva, i.nombre_instalacion, i.tipo_instalacion, " +
                "r.fecha_reserva, r.hora_inicio, r.hora_fin, r.precio, r.estado, r.dni_cliente " +
                "FROM reservas r " +
                "JOIN instalaciones i ON r.id_instalacion = i.id_instalacion " +
                "ORDER BY r.fecha_reserva DESC, r.hora_inicio";

        // Crear lista de retorno
        List<ReservaDetalle> detalles = new ArrayList<>();

        // Establecer conexion, crear PreparedStatement y ResultSet para ejecutar la consulta
        try (Connection conexion = DataBaseConnection.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()
        ) {
            // Llenado de la lista
            while (rs.next()) {
                detalles.add(construirReservaDetalleAdmin(rs));
            }
        }

        return detalles;
    }

    /**
     * Metodo para buscar reservas por el dni del cliente o el nombre de la instalacion
     *
     * @param termino el criterio de busqueda, dni del cliente o el nombre de la instalacion
     * @return la lista con los resultados encontrados
     * @throws SQLException si hay algun error con la base de datos
     */
    public List<ReservaDetalle> buscarDetallesAdmin(String termino) throws SQLException {
        // Preparar sql para consulta
        String sql = "SELECT r.id_reserva, i.nombre_instalacion, i.tipo_instalacion, r.fecha_reserva, " +
                "r.hora_inicio, r.hora_fin, r.precio, r.estado, " +
                "r.dni_cliente " +
                "FROM reservas r " +
                "JOIN instalaciones i ON r.id_instalacion = i.id_instalacion " +
                "WHERE r.dni_cliente LIKE ? " +
                "OR i.nombre_instalacion LIKE ? " +
                "ORDER BY r.fecha_reserva DESC, r.hora_inicio";
        // Crear lista de retorno
        List<ReservaDetalle> detalles = new ArrayList<>();

        // Configurar Like de la consulta
        String like = "%" + termino + "%";

        // Establecer conexion y crear PreparedStatement
        try (Connection conexion = DataBaseConnection.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)
        ) {
            // Configurar prepareStatement
            ps.setString(1, like);
            ps.setString(2, like);

            // Crear ResultSet y ejecutar consulta configurada
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    detalles.add(construirReservaDetalleAdmin(rs));
                }
            }
        }

        return detalles;
    }

    /**
     * Metodo privado para construir objeto DetalleReserva pero con los detalles para la vista del admin
     * Incluye el DNI del cliente como campo adicional.
     *
     * @param rs El resultSet con las reservas con el JOIN(multitabla) de datos
     * @return El objeto DetalleReserva o null si no encuentra nada
     * @throws SQLException si hay algun error con la base de datos
     */
    private ReservaDetalle construirReservaDetalleAdmin(ResultSet rs) throws SQLException {
        TipoInstalacion tipo = TipoInstalacion.valueOf(rs.getString("tipo_instalacion"));
        EstadoReserva estado = EstadoReserva.valueOf(rs.getString("estado"));

        return new ReservaDetalle(
                rs.getInt("id_reserva"),
                rs.getString("nombre_instalacion"),
                tipo.getNombreInstalacion(),
                rs.getDate("fecha_reserva").toLocalDate(),
                rs.getTime("hora_inicio").toLocalTime(),
                rs.getTime("hora_fin").toLocalTime(),
                rs.getBigDecimal("precio"),
                estado.getNombreVisible(),
                rs.getString("dni_cliente")  // Campo extra para el administrador
        );
    }

    /**
     * Metodo privado para construir un objeto ReservaDetalle a partir de un ResultSet de la base de datos
     * No incluye el DNI del cliente
     *
     * @param rs el ResultSet
     * @return El objeto ReservaDetalle a partir de los resultados encontrados
     * @throws SQLException si hay algun error con la base de datos
     */
    private ReservaDetalle construirReservaDetalle(ResultSet rs) throws SQLException {
        TipoInstalacion tipo = TipoInstalacion.valueOf(rs.getString("tipo_instalacion"));
        EstadoReserva estado = EstadoReserva.valueOf(rs.getString("estado"));

        return new ReservaDetalle(
                rs.getInt("id_reserva"),
                rs.getString("nombre_instalacion"),
                tipo.getNombreInstalacion(),
                rs.getDate("fecha_reserva").toLocalDate(),
                rs.getTime("hora_inicio").toLocalTime(),
                rs.getTime("hora_fin").toLocalTime(),
                rs.getBigDecimal("precio"),
                estado.getNombreVisible()
        );
    }

    /**
     * Metodo privado para construir un objeto Reserva
     *
     * @param rs el ResultSet de la base de datos
     * @return el objeto Reserva
     * @throws SQLException si hay algun error con la base de datos
     */
    private Reserva construirReserva(ResultSet rs) throws SQLException {
        Integer idReserva = rs.getInt("id_reserva");
        String dniCliente = rs.getString("dni_cliente");
        Integer idInstalacion = rs.getInt("id_instalacion");
        LocalDate fechaReserva = rs.getDate("fecha_reserva").toLocalDate();
        LocalTime horaInicio = rs.getTime("hora_inicio").toLocalTime();
        LocalTime horaFin = rs.getTime("hora_fin").toLocalTime();
        BigDecimal precio = rs.getBigDecimal("precio");
        EstadoReserva estado = EstadoReserva.valueOf(rs.getString("estado"));

        return new Reserva(idReserva, dniCliente, idInstalacion, fechaReserva, horaInicio, horaFin, precio, estado);
    }
}