package Model;

import Util.Validator;

import java.math.BigDecimal;

/**
 * Clase "Instalacion"
 * Siguiendo con el modelo relacional, donde están definidos los datos y su tipo, esta clase se estructura de la siguiente manera:
 * id_instalacion = la llave primaria creada por el sistema, identifica unívocamente cada instalacion
 * (obligatorio) nombre_instalacion = el nombre de la instalacion (dentro del sistema está con restriccón UNIQUE) esto significa que el nombre de una instalación será unica, no puede existir otra instalacion con el mismo nombre
 * (obligatorio) tipo_instalacion = ENUM con los siguientes valores definidos BAR', 'SALON_EVENTOS', 'PISCINA', 'GIMNASIO', 'PADEL', 'BARBACOA'
 * (obligatorio) capacidad = indica la capacidad de una instalacion, un numero entero positivo
 * (obligatorio) precio_hora = una tarifa plana de coste para calcular en reserva el precio de una reserva (precio_hora * duracion)
 * (obligatorio, por defecto "true") activa = valor booleano que sirve para indicar si una instalación se encuentra activa o no, sirve para bloquear un recurso dependiendo de las necesidades del usuario
 * */

public class Instalacion {
    private Integer idInstalacion;
    private String nombreInstalacion;
    private TipoInstalacion tipoInstalacion;
    private Integer capacidad;
    private BigDecimal precioHora;
    private boolean activa;

    /**
     * Constructor completo — para recuperar registros de la base de datos (SELECT)
     * @param idInstalacion id generado por la base de datos
     * @param nombreInstalacion nombre único de la instalación (ej: "Pista 1")
     * @param tipoInstalacion tipo de instalación del catálogo
     * @param capacidad aforo máximo de personas simultáneas
     * @param precioHora precio por hora de uso
     * @param activa estado de la instalación (true = disponible)
     */

    public Instalacion(Integer idInstalacion, String nombreInstalacion, TipoInstalacion tipoInstalacion, Integer capacidad, BigDecimal precioHora, boolean activa) {

        this.idInstalacion = idInstalacion;
        this.nombreInstalacion = nombreInstalacion;
        this.tipoInstalacion = tipoInstalacion;
        this.capacidad = capacidad;
        this.precioHora = precioHora;
        this.activa = activa;
    }

    /**
     * Constructor para realizar inserciones en la base de datos
     * El id lo genera automáticamente la base de datos (no hay que validar nada).
     * activa = true por defecto al crear una instalación nueva.
     * @param nombreInstalacion nombre único de la instalación
     * @param tipoInstalacion tipo de instalación del catálogo
     * @param capacidad aforo máximo de personas simultáneas
     * @param precioHora precio por hora de uso
     */
    public Instalacion(String nombreInstalacion, TipoInstalacion tipoInstalacion, Integer capacidad, BigDecimal precioHora) {
        this.nombreInstalacion = nombreInstalacion;
        this.tipoInstalacion = tipoInstalacion;
        this.capacidad = capacidad;
        this.precioHora = precioHora;
        this.activa = true;
    }

    // Getters
    public Integer getIdInstalacion() {
        return idInstalacion;
    }

    public String getNombreInstalacion() {
        return nombreInstalacion;
    }

    public TipoInstalacion getTipoInstalacion() {
        return tipoInstalacion;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public BigDecimal getPrecioHora() {
        return precioHora;
    }

    public boolean isActiva() {
        return activa;
    }

    // Setters
    public void setNombreInstalacion(String nombreInstalacion) {
        this.nombreInstalacion = nombreInstalacion;
    }

    public void setTipoInstalacion(TipoInstalacion tipoInstalacion) {
        this.tipoInstalacion = tipoInstalacion;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public void setPrecioHora(BigDecimal precioHora) {
        this.precioHora = precioHora;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }
}