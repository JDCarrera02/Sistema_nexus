package Controller;

import DAO.InstalacionDAO;
import DAO.MembresiaDAO;
import DAO.ReservaDAO;
import DAO.SocioDAO;
import Model.*;
import Util.Login;
import Util.MensajeSQL;
import Util.Validator;
import View.VistaCliente;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Clase ReservaController, llamara los metodos respectivos del DAO ReservaDAO que haran el CRUD basico permitido para la tabla Reservas de la base de datos.
 * Esta clase utiliza los siguientes objetos:
 * - ReservaDAO = para hacer el CRUD a la tabla Reservas
 * - SocioDAO = para utilizar sus metodos como la busquedaPorDni() y hacer comprobaciones antes de asignar una reserva a un socio Cliente
 * - MembresiaDAO  = para utilizar sus metodos como busquedaMembresiaPorId() que retorna la respectiva membresia de un socio, antes de asignarse una reserva
 * - VistaCliente = los metodos para mostrar dialogos o mensajes en pantalla al usuario (para informar o señalar errores)
 */
public class ReservaController {
    private final ReservaDAO reservaDAO;
    private final SocioDAO socioDAO;
    private final MembresiaDAO membresiaDAO;
    private final InstalacionDAO instalacionDAO;
    private final VistaCliente vista;

    // Constructor para inicializar los objetos del controlador
    public ReservaController(VistaCliente vista) {
        this.reservaDAO = new ReservaDAO();
        this.socioDAO = new SocioDAO();
        this.membresiaDAO = new MembresiaDAO();
        this.instalacionDAO = new InstalacionDAO();
        this.vista = vista;
    }

    /**
     * Valida las entradas y crea una nueva reserva.
     * El precio se calcula automáticamente en el DAO.
     * Flujo de validación:
     * 1. Formato de los parámetros de entrada
     * 2. Que el cliente sea socio activo
     * 3. Que la fecha cumpla la antelación máxima
     * 4. Que no haya superado el límite de reservas de su membresía
     *
     * @param dniCliente    el dni del cliente (socio) que se le asignara a la reserva
     * @param idInstalacion el id de la instalacion a reservar
     * @param fechaReserva  la fecha de la reserva
     * @param horaInicio    la hora de inicion de la reserva
     * @param horaFin       la hora de finalizacion de la reserva
     * @return "true" si se inserta, o "false" si no.
     */
    public boolean insertar(String dniCliente, Integer idInstalacion, LocalDate fechaReserva, LocalTime horaInicio, LocalTime horaFin) {
        // Validacion de entradas
        try {
            Validator.validarDni(dniCliente);

            if (idInstalacion == null) {
                throw new IllegalArgumentException("Debe seleccionar una instalacion");
            }

            if (fechaReserva == null) {
                throw new IllegalArgumentException("La fecha de reserva no puede estar vacia");
            }

            if (horaInicio == null || horaFin == null) {
                throw new IllegalArgumentException("Las horas de inicio y fin no pueden estar vacias");
            }

            if (!horaFin.isAfter(horaInicio)) {
                throw new IllegalArgumentException("La hora de finalizacion debe ser posterior a la horan de inicio");
            }

        } catch (IllegalArgumentException e) {
            vista.mostrarError(e.getMessage());
            return false;
        }

        // Verificacion de que el cliente es un socio activo
        try {
            Socio socio = socioDAO.buscarPorDni(dniCliente);
            if (socio == null || !socio.isActivo()) { // Si el cliente no es socio, o si es socio y no está activo
                vista.mostrarError("Solo los socios activos pueden realizar reservas.");
                return false;
            }

            // Validacion de antelacion (reserva) segun la membresia
            if (!validarAntelacion(fechaReserva))
                return false; // Si la fecha de la reserva no es valida, no puede reservar

            // Recuperar membresia del socio correspondiente que desea hacer la reserva
            Membresia membresia = membresiaDAO.busquedaMembresiaPorId(socio.getIdMembresia());

            // Si no encuentra su membresia
            if (membresia == null) {
                vista.mostrarError("No se encontro la membresia del socio.");
                return false; // No puede reservar
            }

            // Verificacion del limite de reservas de la membresia
            if (!verificarLimiteReservas(dniCliente, membresia.getMaxReservas())) {
                return false; // Si el cliente correspondiente supera el limite de reservas, no puede reservar
            }

            Instalacion instalacion = instalacionDAO.buscarInstalacionPorId(idInstalacion);

            // Buscar la instalacion seleccionada
            if (instalacion == null){
                vista.mostrarError("No se encontro la instalacion seleccionada ");
                return false;
            }

            // Verificar si la instalacion se encuentra activa
            if (!instalacion.isActiva()){
                vista.mostrarError("La instalacion seleccionada no se encuentra activa para reservar ");
                return false;
            }

            // Construccion del objeto para insertar
            Reserva reserva = new Reserva(dniCliente, idInstalacion, fechaReserva, horaInicio, horaFin, BigDecimal.ZERO);

            reservaDAO.insertar(reserva); // Llamar al metodo del DAO para insertar reserva
            Login.log("Reserva creada -- DNI: " + dniCliente + " Instalacion: " + idInstalacion + " Fecha: " + fechaReserva);
            vista.mostrarMensaje("Reserva realizada correctamete.");
            return true;

        } catch (SQLException e) {
            Login.error("Error al insertar reserva: " + e.getMessage());
            vista.mostrarError(MensajeSQL.traducir(e));
            return false;
        }
    }

    /**
     * Verifica que la reserva existe y su estado permite el cambio,
     * y actualiza el estado de la reserva.
     * Una reserva completada o cancelada no puede modificarse.
     *
     * @param idReserva   el id de la reserva a actualizar
     * @param nuevoEstado el nuevo estado que tomara la reserva actual a modificar
     * @return "true" si se actualiza, o "false" si no.
     */
    public boolean actualizarEstado(Integer idReserva, EstadoReserva nuevoEstado) {
        // Validacion de entradas
        try {
            if (idReserva == null) {
                throw new IllegalArgumentException("El id de la reserva no puede estar vacio");
            }

            if (nuevoEstado == null) {
                throw new IllegalArgumentException("El estado no puede estar vacio");
            }
        } catch (IllegalArgumentException e) {
            vista.mostrarError(e.getMessage());
            return false;
        }

        // Verificacion de que existe la reserva a modificar y que el cambio de estado de la reserva es valido
        try {
            Reserva reserva = reservaDAO.buscarReservaPorId(idReserva);
            if (reserva == null) {
                vista.mostrarError("No existe ninguna reserva con id: " + idReserva);
                return false;
            }

            // Validar estado de la reserva, caso de ejemplo: si una reserva esta en estado "COMPLETADA", no tiene sentido modificarla
            if (reserva.getEstado() == EstadoReserva.COMPLETADA) {
                vista.mostrarError("No se puede modificar una reserva completada.");
                return false;
            }

            // Validar estado de la reserva, caso de ejemplo: si una reserva se encuentra en estado "CANCELADA" no tiene sentido modificarla.
            if (reserva.getEstado() == EstadoReserva.CANCELADA) {
                vista.mostrarError("No se puede modificar una reserva cancelada.");
                return false;
            }

            // Luego de verificar que el estado nuevo de la reserva sea valido, se actualiza el estado de la reserva, llamando al metodo del dao
            reservaDAO.actualizarEstado(idReserva, nuevoEstado);
            Login.log("Estado de reserva actualizado -- id: " + idReserva + " nuevo estado: " + nuevoEstado.getNombreVisible());
            vista.mostrarMensaje("Estado de la reserva actualizado correctamente.");
            return true;
        } catch (SQLException e) {
            Login.error("Error al actualizar estado de reserva: " + e.getMessage());
            vista.mostrarError(MensajeSQL.traducir(e));
            return false;
        }
    }

    /**
     * Metodo para listar reservas por cliente, llama al metodo del dao listarPorCliente
     *
     * @param dniCliente se refiere al parametro (dni) para listar reservas por el dni del cliente.
     * @return la lista de reservas del cliente, o null si no tiene reservas realizadas (o si el dni ingresado no es valido) o si hay alguna excepcion
     */
    public List<Reserva> listarPorCliente(String dniCliente) {
        try {
            Validator.validarDni(dniCliente);
        } catch (IllegalArgumentException e) {
            vista.mostrarError(e.getMessage());
            return null;
        }

        try {
            List<Reserva> reservas = reservaDAO.listarPorCliente(dniCliente);
            if (reservas.isEmpty()) {
                vista.mostrarMensaje("No tienes reservas registradas en el sistema.");
            }

            return reservas;
        } catch (SQLException e) {
            Login.error("Error al listar reservas por cliente: " + e.getMessage());
            vista.mostrarError(MensajeSQL.traducir(e));
            return null;
        }
    }

    /**
     * Metodo para listar todas las reservas (Utilizada por el administrador del sistema)
     *
     * @return una lista con las reservas registradas, null si hay algun error
     */
    public List<Reserva> listarTodos() {
        try {

            return reservaDAO.listarTodos();

        } catch (SQLException e) {
            Login.error("Error al listar reservas: " + e.getMessage());
            vista.mostrarError(MensajeSQL.traducir(e));
            return null;
        }
    }

    /**
     * Metodo privado para validar antelacion de reserva
     * Los socios: maximo 14 dias de antelacion
     *
     * @param fechaReserva la fecha de la reserva
     * @return si puede reservar o no, si se cumple con las dos condiciones "que sea despues de una fecha, y con un maximo de 2 semanas de antelacion"
     */
    private boolean validarAntelacion(LocalDate fechaReserva) {
        LocalDate hoy = LocalDate.now(); // Variable para capturar el dia actual
        LocalDate maxFecha = hoy.plusWeeks(2); // El max reserva es una variable de control que utiliza el metodo plusWeek(2) para añadirle dos semanas partiendo del dia actual, y manejar siempre el limite de reservas de 14 dias

        // Comprobar el parametro de entrada
        if (fechaReserva.isBefore(hoy)) { // Si la fecha de la reserva es anterior a la del dia actual
            vista.mostrarError("No se puede reservar en una fecha pasada."); // No es valido y no tiene sentido hacer una reserva una fecha que ya paso.
            return false;
        }


        if (fechaReserva.isAfter(maxFecha)) { // Si la fecha de la reserva es posterior al limite de reserva (14 dias)
            vista.mostrarError("Solo se puede reservar con un maximo de 2 semanas de antelacion. "); // no es valido reservar, porque supera el limite
            return false;
        }

        return true; // Si el parametro de entrada es valido, se puede reservar
    }

    /**
     * Metodo controlador para listar los detalles de las reservas filtradas por cliente
     * Llama al metodo del dao listarDetallesPorCliente() y retorna una lista
     * @param dniCliente el dni del cliente socio
     * @return la lista con los detalles filtrados por el socio correspondiente
     * */
    public List<ReservaDetalle> listarDetallesPorCliente(String dniCliente){
        // Validar entrada
        try {
            Validator.validarDni(dniCliente);
        } catch (IllegalArgumentException e){
            vista.mostrarError(e.getMessage());
            return null;
        }

        try {

            return reservaDAO.listarDetallesPorCliente(dniCliente);

        } catch (SQLException e) {
            Login.error("Error al listar detalles de reservas: "+e.getMessage());
            vista.mostrarError(MensajeSQL.traducir(e));
            return null;
        }

    }

    /**
     * Metodo que filtra las reservas por criterio de busqueda (nombre instalacion)
     * @param dniCliente el dni del socio en ese momento
     * @param termino el nombre de la instalacion
     * */
    public List<ReservaDetalle> buscarDetallesPorInstalacion(String dniCliente, String termino){

        if (termino == null || termino.isBlank()){ // Si el termino es null o no se especifica
            return listarDetallesPorCliente(dniCliente); // No se filtra
        }

        try {
            List<ReservaDetalle> detalles = reservaDAO.buscarDetallesPorInstalacion(dniCliente, termino);

            if (detalles.isEmpty()) {
                vista.mostrarMensaje("No se encontraron reservas con ese nombre de instalacion: "+termino);
            }

            return detalles;
        } catch (SQLException e){
            Login.error("Error al buscar reservas por instalacion: "+e.getMessage());
            vista.mostrarError(MensajeSQL.traducir(e));
            return null;
        }
    }

    /**
     * Metodo privado que verifica las reservas activas (el limite)
     *
     * @param dni         el dni del socio a validar sus reservas
     * @param maxReservas el maximo de reservas que puede realizar el socio
     * @throws SQLException si hay algun error con la base de datos
     */
    public boolean verificarLimiteReservas(String dni, Integer maxReservas) throws SQLException {
        if (maxReservas == 0) return true; // Si el maximo de reservas es cero, quiere decir que puede reservar

        // Recuperar reservas por cliente, a partir del dni (filtrado de reservas), y filtrar la lista por el Enum, con valor CONFIRMADA, es decir, listar las reservas activas del cliente
        long reservasActivas = reservaDAO.listarPorCliente(dni)
                .stream()
                .filter(r -> r.getEstado() == EstadoReserva.CONFIRMADA)
                .count();
        // Comprobar si la lista supera o es igual al maximo de reservas
        if (reservasActivas >= maxReservas) {
            vista.mostrarError("Has alcanzado el limite de " + maxReservas + " reservas activas de tu membresia. " + "Cancela alguna reserva para poder hacer una nueva.");
            return false; // No puede reservar
        }
        return true; // Si puede reservar
    }

    /**
     * Metodo controlador que llama al metodo del dao listarTodosDetalles(), para listar todos los detalles de las reservas
     * @return la lista con los resultados encontrados que devuelve el dao
     * */
    public List<ReservaDetalle> listarTodosDetalles(){
        try {
            // Cargar los detalles de las reservas
            List<ReservaDetalle> detalles = reservaDAO.listarTodosDetalles();

            if (detalles.isEmpty()){
                vista.mostrarMensaje("No hay reservas registradas en el sistema. ");
            }

            return detalles;
        } catch (SQLException e){
            Login.error("Error al listar todos los detalles de reservas: "+e.getMessage());
            vista.mostrarError(MensajeSQL.traducir(e));
            return null;
        }
    }

    /**
     * Metodo controlador que llama al metodo del dao buscarDetallesAdmin(), que lista todas las reservas de acuerdo con un termino de busqueda (nombre de la instalacion o dni del cliente)
     * @param termino el criterio de busqueda
     * @return la lista con los resultados encontrados, a partir de lo retornado del dao
     * */
    public List<ReservaDetalle> buscarDetallesAdmin(String termino){

        // Valida si el termino ingresado es null o tiene informacion vacia "Solo espacios"
        if (termino == null || termino.isBlank()){
            return listarTodosDetalles(); // Se listan todas las reservas con informacion enriquecida
        }

        try {
            // Preparar lista de retorno
            List<ReservaDetalle> detalles = reservaDAO.buscarDetallesAdmin(termino);

            if (detalles.isEmpty()){
                vista.mostrarMensaje("No se encontraron reservas con ese termino de busqueda: "+termino);
            }

            return detalles;
        } catch (SQLException e){
            Login.error("Error al buscar reservas: "+e.getMessage());
            vista.mostrarError(MensajeSQL.traducir(e));
            return null;
        }
    }
}
