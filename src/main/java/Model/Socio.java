package Model;

import java.time.LocalDate;

/**
 * Clase "Socio"
 *
 * @author Juan Diego Carrera
 * @version 1.0
 * Los atributos de la clase Socio, definidos en el modelo relacional:
 * numSocio = es un VARCHAR que será el codigo interno del socio, que será asignado por el sistema siguiendo una estructura = S-001...
 * (obligatorio) dni = es la llave foranea que referencia a la tabla CLiente, será unico, no puede existir dos socios con el mismo dni
 * (obligatorio) fecha_alta = tipo de dato DATE que indicará la fecha en el que se convirtió en socio
 * (obligatorio) activo = valor booleano que indica si un socio está activo o no
 * (obligatorio) id_membresia = llave foranea que referencia a Membresia, un socio tiene asignado una membresia
 * referido_por = llave foranea que referencia a Socio, un socio puede ser referido por otro (opcional)
 */

public class Socio {
    private String numSocio;
    private String dni;
    private LocalDate fechaAlta;
    private boolean activo;
    private Integer idMembresia;
    private String referidoPor;

    /**
     * Constructor para recibir Socios de la base de datos
     *
     * @param numSocio    codigo interno del socio, generado por el sistema con estructura especifica (ejemplo: S-001)
     * @param dni         el DNI del cliente asociado a este socio
     * @param fechaAlta   la fecha en que el cliente se convirtió en socio
     * @param activo      estado del socio (true = activo, false = baja)
     * @param idMembresia id de la membresía contratada
     * @param referidoPor num_socio del socio que lo recomendó, null si ninguno
     */

    public Socio(String numSocio, String dni, LocalDate fechaAlta, boolean activo, Integer idMembresia, String referidoPor) {

        this.numSocio = numSocio;
        this.dni = dni;
        this.fechaAlta = fechaAlta;
        this.activo = activo;
        this.idMembresia = idMembresia;
        this.referidoPor = referidoPor;
    }

    /**
     * Constructor para realizar inserciones a la base de datos
     * al ser para inserciones, el num_socio es generado por el sistema, y activo será "true" por defecto.
     *
     * @param dni         DNI del cliente que se convierte en socio
     * @param fechaAlta   fecha de alta en el club
     * @param idMembresia id de la membresía contratada
     * @param referidoPor num_socio del socio que lo recomendó, null si ninguno
     */
    public Socio(String dni, LocalDate fechaAlta, Integer idMembresia, String referidoPor) {

        this.dni = dni;
        this.fechaAlta = fechaAlta;
        this.activo = true;
        this.idMembresia = idMembresia;
        this.referidoPor = referidoPor;

    }

    // Getters
    public String getNumSocio() {
        return numSocio;
    }

    public String getDni() {
        return dni;
    }

    public LocalDate getFechaAlta() {
        return fechaAlta;
    }

    public boolean isActivo() {
        return activo;
    }

    public Integer getIdMembresia() {
        return idMembresia;
    }

    public String getReferidoPor() {
        return referidoPor;
    }

    // Setters

    // Activar o desactivar un socio (dar de alta o baja)
    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    // Actualizar la membresía contratada por el socio
    public void setIdMembresia(Integer idMembresia) {
        this.idMembresia = idMembresia;
    }

    // Actualiza el socio referidor
    public void setReferidoPor(String referidoPor) {
        this.referidoPor = referidoPor;
    }
}