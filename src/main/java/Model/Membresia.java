package Model;

import Util.Validator;

import java.math.BigDecimal;

/**
 * Clase Membresia
 * @author Juan Diego Carrera
 * @version 1.0
 *
 * Siguiendo el modelo relacional, la clase se estructura con los siguientes atributos:
 * idMembresia = el id interno (generado por la base de datos) que es la llave primaria, identificará cada registro de manera unica
 * nombre = el nombre de la membresia que es único, para evitar duplicados comerciales y diferenciar entre membresias
 * precioMensual = el precio base de la membresia
 * maxReservas = la cantidad maxima de reservas que tendrá un socio para realizar
 * descripcion = las especificaciones adicionales de la membresia, como detalles, precios especiales, etc
 * */

public class Membresia {
    private Integer idMembresia;
    private TipoMembresia nombre;
    private BigDecimal precioMensual;
    private Integer maxReservas;
    private String descripcion;

    /**
     * Constructor con todos los parametros (para recibir registros de la base de datos)
     * @param idMembresia el id de la membresia (creado internamente por la base de datos)
     * @param nombre el nombre de la membresia
     * @param precioMensual el precio de la membresia
     * @param maxReservas la cantidad maxima que tiene un socio para reservar una instalacion
     * @param descripcion las especificaciones adicionales de la membresia
     * */
    public Membresia(Integer idMembresia, TipoMembresia nombre, BigDecimal precioMensual, Integer maxReservas, String descripcion) {

        this.idMembresia = idMembresia;
        this.nombre = nombre;
        this.precioMensual = precioMensual;
        this.maxReservas = maxReservas;
        this.descripcion = descripcion;
    }

    /**
     * Constructor para realizar inserciones en la base de datos
     * El atributo idMembresia al ser primary key, no se realizará inserción manual de este campo, ya que el sistema internamente lo crea
     * Campos o atributos obligatorios (NOT NULL), siguiendo el modelo relacional:
     * nombre, precioMensual, maxReservas.
     * @param nombre atributo tipo Enum que indica el tipo de membresia
     * @param precioMensual el precio mensual de la membresia
     * @param maxReservas el maximo de reservas que tiene un socio
     * @param descripcion los detalles adicionales (si aplica) de la membresia de un socio.
     * */
    public Membresia(TipoMembresia nombre, BigDecimal precioMensual, Integer maxReservas, String descripcion) {
        this.nombre = nombre;
        this.precioMensual = precioMensual;
        this.maxReservas = maxReservas;
        this.descripcion = descripcion;
    }

    // Getters de los atributos
    public Integer getIdMembresia() {
        return idMembresia;
    }

    public TipoMembresia getNombre() {
        return nombre;
    }

    public BigDecimal getPrecioMensual() {
        return precioMensual;
    }

    public Integer getMaxReservas() {
        return maxReservas;
    }

    public String getDescripcion() {
        return descripcion;
    }

    // Setters

    public void setNombre(TipoMembresia nombre){
        this.nombre = nombre;
    }

    public void setPrecioMensual(BigDecimal precioMensual){
        this.precioMensual = precioMensual;
    }

    public void setMaxreservas(Integer maxReservas){
        this.maxReservas = maxReservas;
    }

    public void setDescripcion(String descripcion){
        this.descripcion = descripcion;
    }
}