package Controller;

import DAO.ReservaDAO;
import DAO.SocioDAO;
import Model.EstadoReserva;
import Model.Reserva;
import Model.Socio;
import Model.SocioDetalle;
import Util.Login;
import Util.Validator;
import View.VistaCliente;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Clase SocioController.
 * En esta clase se llamaran los metodos del SocioDAO, haciendo validaciones necesarias para poder construir objetos
 * de manera correcta, y con ello, realizar el CRUD a la tabla correspondiente.
 * Utilizacion de ReservaDAO para usar los metodos de Reserva, cuando un socio desee reservar, se haran comprobaciones de antelacion y validez
 */
public class SocioController {

    private final VistaCliente vista;
    private final SocioDAO socioDAO;
    private final ReservaDAO reservaDAO;

    // Constructor que inicializa los objetos del controlador
    public SocioController(VistaCliente vista) {
        this.socioDAO = new SocioDAO();
        this.reservaDAO = new ReservaDAO();
        this.vista = vista;
    }

    /**
     * Metodo para insertar un Socio, llama al metodo del DAO insertar()
     * Realiza comprobaciones previas, sobre las entradas y busca si existe como cliente para poder insertarse como socio
     *
     * @param dni         el dni del cliente (para dar de alta como socio)
     * @param fechaAlta   la fecha de alta en el sistema
     * @param idMembresia la membresia que tendrá el socio
     * @param referidoPor El num_socio que refiere al cliente (socio)
     * @return "true" si realiza la insercion correctamente, o "false" si no se logra realizar
     */
    public boolean insertar(String dni, LocalDate fechaAlta, Integer idMembresia, String referidoPor) {
        // Validacion de entradas
        try {
            Validator.validarDni(dni);

            if (fechaAlta == null) {
                throw new IllegalArgumentException("La fecha de alta no puede estar vacia");
            }

            if (idMembresia == null) {
                throw new IllegalArgumentException("Debe seleccionar una membresia");
            }

            // Como referido_por es opcional, solo se valida su formato cuando tiene informacion
            if (referidoPor != null && !referidoPor.isBlank()) {
                Validator.validarNumSocio(referidoPor);
            }
        } catch (IllegalArgumentException e) {
            vista.mostrarError(e.getMessage());
            return false; // Si alguno de los parametros de entrada no son validos, no se inserta el socio
        }

        // Comprobacion si el socio a insertar está como socio o no
        try {
            // Buscar al socio a traves del metodo del DAO, buscarPorDni()
            Socio socioExistente = socioDAO.buscarPorDni(dni);

            if (socioExistente != null) { // Si el socio en cuestion ya existe
                vista.mostrarError("Este cliente ya está dado de alta como socio: " + socioExistente.getNumSocio());
                return false; // No se puede insertar un socio que ya está como socio en el sistema
            }

            Socio socio = new Socio(dni, fechaAlta, idMembresia, referidoPor);
            socioDAO.insertar(socio);

            Login.log("Alta de socio - DNI: " + dni);
            vista.mostrarMensaje("Socio dado de alta correctamente.");
            return true; // Se ha realizado correctamente la insercion del socio en el sistema.

        } catch (SQLException e) {
            Login.error("Error al dar de alta socio: " + e.getMessage());
            vista.mostrarError(gestionarErrorSQL(e));
            return false; // No se puede insertar si ocurre algun error en la base de datos
        }
    }

    /**
     * Metodo para desactivar un socio, o darlo de baja
     *
     * @param numSocio el criterio de busqueda para encontrar el socio que se quiere dar de baja
     * @return "true" en el caso que se pueda desactivar o "false" si no se puede.
     */
    public boolean desactivar(String numSocio) {
        // Validacion de entrada
        try {
            Validator.validarNumSocio(numSocio);
        } catch (IllegalArgumentException e) {
            vista.mostrarError(e.getMessage());
            return false; // No se puede desactivar un socio si el num_socio no es valido para buscarlo en el sistema
        }

        // Verificar si existe y está activo
        try {
            Socio socio = socioDAO.buscarPorId(numSocio);

            if (socio == null) {
                vista.mostrarError("No existe ningun socio con numero: " + numSocio);
                return false; // No se puede desactivar un socio que no existe.
            }

            if (!socio.isActivo()) {
                vista.mostrarError("El socio: " + numSocio + " ya se encuentra inactivo.");
                return false; // No se puede desactivar un socio que ya se encuentra inactivo.
            }

            // Desactivar por medio del setter y actualizar estado del socio
            socio.setActivo(false);
            socioDAO.actualizar(socio);
            Login.log("Socio desactivado: -- " + numSocio);
            vista.mostrarMensaje("Socio desactivado correctamente");
            return true;
        } catch (SQLException e) {
            Login.error("Error al desactivar el socio: " + e.getMessage());
            vista.mostrarError(gestionarErrorSQL(e));
            return false;
        }
    }

    /**
     * Metodo para buscar un socio por medio de su numero de socio
     *
     * @param numSocio el numero del socio a buscar en la base de datos
     * @return El objeto Socio con los resultados encontrados, o null si no o hay algun error en el proceso
     */
    public Socio buscarPorNumSocio(String numSocio) {
        try {
            Validator.validarNumSocio(numSocio);
        } catch (IllegalArgumentException e) {
            vista.mostrarError(e.getMessage());
            return null;
        }

        try {
            Socio socio = socioDAO.buscarPorId(numSocio);

            if (socio == null) {
                vista.mostrarError("No existe ningun socio con numero: " + numSocio);
            }

            return socio;
        } catch (SQLException e) {
            Login.error("Error al buscar socio: " + e.getMessage());
            vista.mostrarError(gestionarErrorSQL(e));
            return null;
        }
    }

    /**
     * Metodo para activar un socio, comprueba primero que el socio esté en la base de datos y se encuentre en estado activo primero para desactivarlo
     *
     * @param numSocio el numero de socio a buscar
     * @return "true" si se logra desactivar, o "false" si no se puede.
     */
    public boolean activar(String numSocio) {
        // Validar entrada
        try {
            Validator.validarNumSocio(numSocio);
        } catch (IllegalArgumentException e) {
            vista.mostrarError(e.getMessage());
            return false; // No se puede desactivar un socio si el parametro de busqueda no es valido.
        }

        try {
            // Recuperar socio de la base de datos
            Socio socio = socioDAO.buscarPorId(numSocio);

            // Si no lo encuentra
            if (socio == null) {
                vista.mostrarError("No existe ningun socio con numero: " + numSocio);
                return false; // No se puede activar un socio si no existe
            }

            // Si lo encuentra y el socio ya se encuentra activo
            if (socio.isActivo()) {
                vista.mostrarError("El socio: " + numSocio + " ya se encuentra activo.");
                return false; // No se puede activar un socio si ya se encuentra activo (no tiene sentido).
            }

            // Si es valida la activacion, por medio del setter se asigna el nuevo valor
            socio.setActivo(true);
            socioDAO.actualizar(socio);
            Login.log("Socio activado -- " + numSocio);
            vista.mostrarMensaje("Socio activado correctamente.");
            return true; // Socio activado exitosamente

        } catch (SQLException e) {
            Login.error("Error al activar socio: " + e.getMessage());
            vista.mostrarError(gestionarErrorSQL(e));
            return false; // No se puede activar un socio si hay algun error con la base de datos.
        }
    }

    // Listar todos los socios
    public List<Socio> listarTodos() {
        try {
            List<Socio> socios = socioDAO.listarTodos();
            if (socios.isEmpty()) {
                vista.mostrarMensaje("No hay socios registrados en el sistema.");
            }
            return socios;
        } catch (SQLException e) {
            Login.error("Error al listar socios: " + e.getMessage());
            return null;
        }
    }

    /**
     * Metodo para verificar el limite de reservas
     * <p>
     * Comprueba si el socio puede hacer mas reservas segun su membresia, llama al controlador de membresia antes de insertar una reserva (o reservar en pocas palabras)
     */
    public boolean puedeReservar(String dni, Integer maxReservas) {
        // El maximo de reservas es igual a 0 por defecto
        if (maxReservas == 0) return true; // Puede reservar

        try {
            // Listar reservas activas, utilizando stream, para listar solamente las reservas activas que tiene un socio (buscar por dni)
            List<Reserva> reservasActivas = reservaDAO.listarPorCliente(dni)
                    .stream()
                    .filter(r -> r.getEstado()
                            == EstadoReserva.CONFIRMADA)
                    .toList();

            if (reservasActivas.size() >= maxReservas) {
                vista.mostrarError("Has alcanzado el limite de " + maxReservas + " reservas activas de tu membresia.");
                return false;
            }

            return true;

        } catch (SQLException e) {
            Login.error("Error al verificar limite de reservas: " + e.getMessage());
            vista.mostrarError(gestionarErrorSQL(e));
            return false;
        }
    }

    /**
     * Metodo controlador que llama al metodo del DAO, listarDetalles(), que lista los detalles de los socios (mejor visualmente)
     *
     * @return La lista con los detalles, o null si no o si hay algun error con la base de datos
     */
    public List<SocioDetalle> listarDetalles() {
        try {
            return socioDAO.listarDetalles();
        } catch (SQLException e) {
            Login.error("Error al listar los detalles: " + e.getMessage());
            vista.mostrarError(gestionarErrorSQL(e));
            return null;
        }
    }

    /**
     * Metodo controlador para buscar los detalles del socio a partir del termino de busqueda
     *
     * @param termino el criterio de busqueda
     * @return los detalles del socio o solo los detalles de todos los socios si no encuentra el socio especificado por el parametro de entrada
     */
    public List<SocioDetalle> buscarDetalles(String termino) {
        // Validar si el termino es null o esta vacio
        if (termino == null || termino.isBlank()) {
            return listarDetalles();
        }

        try {
            List<SocioDetalle> detalles = socioDAO.buscarDetalles(termino);
            if (detalles.isEmpty())
                vista.mostrarMensaje(
                        "No se encontraron socios con: " + termino
                );
            return detalles;
        } catch (SQLException e) {
            Login.error("Error al buscar socios: " + e.getMessage());
            vista.mostrarError(gestionarErrorSQL(e));
            return null;
        }
    }

    /**
     * Metodo para actualizar una membresia (correspondiente a un socio)
     *
     * @param numSocio    el numero de socio a buscar, si existe
     * @param idMembresia el id de la membresia del socio correspondiente
     * @return "true" si actualiza correctamente su membresia, o "false" si no
     */
    public boolean actualizarMembresia(String numSocio, Integer idMembresia) {
        try {
            // Buscar socio si existe
            Socio socio = socioDAO.buscarPorId(numSocio);
            // Si no lo encuentra
            if (socio == null) {
                vista.mostrarError("No existe ningún socio con número: " + numSocio); // Mostrar mensaje al usuario
                return false;
            }
            // Actualizar membresia del socio por medio de los setter
            socio.setIdMembresia(idMembresia);
            socioDAO.actualizar(socio); // Llamar al metodo del dao
            Login.log("Membresia actualizada — socio: " + numSocio);
            return true;

        } catch (SQLException e) {
            Login.error("Error al actualizar membresía del socio: " + e.getMessage());
            vista.mostrarError(gestionarErrorSQL(e));
            return false;
        }
    }

    private String gestionarErrorSQL(SQLException e) {
        return switch (e.getErrorCode()) {
            case 1062 -> "Este cliente ya está registrado como socio.";
            case 1451 -> "No se puede eliminar el socio porque tiene registros asociados.";
            case 1452 -> "Error de integridad: el cliente o membresía referenciada no existe.";
            default -> "Error en la base de datos: " + e.getMessage();
        };
    }
}
