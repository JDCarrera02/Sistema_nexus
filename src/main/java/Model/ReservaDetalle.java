package Model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/*
* Modelo DTO para mostrar en la vista (Solo vista, nada de insercion ni actualizacion)
* Combina datos de la tabla Reservas e Instalaciones mediante JOIN
* */
public class ReservaDetalle {
    private Integer idReserva;
    private String nombreInstalacion;
    private String tipoInstalacion;
    private LocalDate fechaReserva;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private BigDecimal precio;
    private String estado;
    private String dniCliente; // Atributo que solamente sera usado en la vista del admin

    // Constructor por defecto con todos los atributos, para la vista del socio
    public ReservaDetalle(Integer idReserva, String nombreInstalacion, String tipoInstalacion, LocalDate fechaReserva, LocalTime horaInicio, LocalTime horaFin, BigDecimal precio, String estado) {
        this.idReserva        = idReserva;
        this.nombreInstalacion = nombreInstalacion;
        this.tipoInstalacion  = tipoInstalacion;
        this.fechaReserva     = fechaReserva;
        this.horaInicio       = horaInicio;
        this.horaFin          = horaFin;
        this.precio           = precio;
        this.estado           = estado;
    }

    // Constructor para la vista del admin
    public ReservaDetalle(Integer idReserva, String nombreInstalacion, String tipoInstalacion, LocalDate fechaReserva, LocalTime horaInicio, LocalTime horaFin, BigDecimal precio, String estado, String dniCliente){
        this.idReserva         = idReserva;
        this.nombreInstalacion = nombreInstalacion;
        this.tipoInstalacion   = tipoInstalacion;
        this.fechaReserva      = fechaReserva;
        this.horaInicio        = horaInicio;
        this.horaFin           = horaFin;
        this.precio            = precio;
        this.estado            = estado;
        this.dniCliente        = dniCliente;
    }

    public Integer getIdReserva() {
        return idReserva;
    }

    public String getNombreInstalacion(){
        return nombreInstalacion;
    }

    public String getTipoInstalacion(){
        return tipoInstalacion;
    }

    public LocalDate getFechaReserva(){
        return fechaReserva;
    }

    public LocalTime getHoraInicio(){
        return horaInicio;
    }

    public LocalTime getHoraFin(){
        return horaFin;
    }

    public BigDecimal getPrecio(){
        return precio;
    }

    public String getEstado(){
        return estado;
    }

    public String getDniCliente(){
        return dniCliente;
    }
}
