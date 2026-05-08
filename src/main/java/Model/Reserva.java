package Model;

import Util.Validator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Clase "Reserva"
 * @author Juan Diego Carrera
 * @version 1.0
 *
 * Atributos de la clase Reserva, definidos en el modelo relacional:
 * id_reserva = el identificador único para cada reserva
 * dni_cliente = llave foranea que referencia a Clientes, una reserva tiene asociado un cliente
 * id_instalacion = llave foranea que referencia a Intalaciones, una reserva tiene asociada una instalacion
 * fecha_reserva = dato de tipo Date que indica la fecha de la reserva
 * hora_inicio = dato de tipo Time que indica la hora de inicio de la reserva
 * hora_fin = dato de tipo Time que indica la hora de fin de la reserva
 * precio = valor calculado por el sistema
 * estado = dato tipo Enum que indica el estado de una reserva: 'CONFIRMADA', 'CANCELADA', 'COMPLETADA'
 * */

public class Reserva {
    private Integer idReserva;
    private String dniCliente;
    private Integer idInstalacion;
    private LocalDate fechaReserva;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private BigDecimal precio;
    private EstadoReserva estado;

    /**
     * Constructor completo, para recuperar registros de la base de datos
     * @param idReserva id generado por la base de datos
     * @param dniCliente DNI del cliente que realiza la reserva
     * @param idInstalacion id de la instalación reservada
     * @param fechaReserva fecha de la reserva
     * @param horaInicio hora de inicio de la reserva
     * @param horaFin hora de fin de la reserva
     * @param precio precio total calculado por el sistema
     * @param estado estado actual de la reserva
     * */
    public Reserva(Integer idReserva, String dniCliente, Integer idInstalacion, LocalDate fechaReserva, LocalTime horaInicio, LocalTime horaFin, BigDecimal precio, EstadoReserva estado) {

        this.idReserva = idReserva;
        this.dniCliente = dniCliente;
        this.idInstalacion = idInstalacion;
        this.fechaReserva = fechaReserva;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.precio = precio;
        this.estado = estado;
    }

    /**
     * Constructor sin idReserva — para realizar inserciones en la base de datos
     * El id lo genera automáticamente la base de datos.
     * El estado se establece como CONFIRMADA por defecto al crear una reserva nueva.
     * @param dniCliente DNI del cliente que realiza la reserva
     * @param idInstalacion id de la instalación reservada
     * @param fechaReserva fecha de la reserva
     * @param horaInicio hora de inicio de la reserva
     * @param horaFin hora de fin de la reserva
     * @param precio precio total calculado por el sistema
     */
    public Reserva(String dniCliente, Integer idInstalacion, LocalDate fechaReserva, LocalTime horaInicio, LocalTime horaFin, BigDecimal precio) {

        this.dniCliente = dniCliente;
        this.idInstalacion = idInstalacion;
        this.fechaReserva = fechaReserva;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.precio = precio;
        this.estado = EstadoReserva.CONFIRMADA;

    }

    // Getters
    public Integer getIdReserva() {
        return idReserva;
    }

    public String getDniCliente() {
        return dniCliente;
    }

    public Integer getIdInstalacion() {
        return idInstalacion;
    }

    public LocalDate getFechaReserva() {
        return fechaReserva;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    // Setters
    /*
    * Para la logica del proyecto y la integridad de los datos, sólo existirá un Setter
    * para esta clase, será setEstado. No debería cambiar:
    * - IdReserva = esto lo genera la Base de datos
    * - dniCliente = el cliente de una reserva no cambia
    * - idInstalacion = una instalacion reservada no cambia
    * - fechaReserva = más de lo mismo, la fecha de la reserva no cambia, modificarla implicaría cancelar y crear una nueva (esto sería lo logico)
    * - horaInicio y horaFin = más de lo mismo, igual que fechaReserva
    * - precio = esto es calculado por el sistema, no debe modificarse manualmente
    *
    * Con lo anterior, el único atributo que tiene lógica la posibilidad de ser cambiado o modificado
    * es "estado" porque una reserva dependiendo de la circunstancia, cambia de estado, puede confirmarse, o cancelarse
    * */

    // Metodo setter para cambiar el estado de la reserva
    public void setEstado(EstadoReserva estado){
        this.estado = estado;
    }
}