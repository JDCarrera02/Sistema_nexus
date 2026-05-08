package Model;

import java.time.LocalDate;

/**
 * Modelo DTO - para vista enriquecida de un socio en la vista
 * Este modelo combina datos de SOCIOS, CLIENTES, y MEMBRESIAS Mediante JOIN
 * Solo se usará para mostrar informacion, no para actualizar, ni insertar, ni eliminar (Solo consulta)
 * */
public class SocioDetalle {
    private String numSocio;
    private String dni;
    private String nombre;
    private String apellidos;
    private String email;
    private String telefono;
    private LocalDate fechaAlta;
    private boolean activo;
    private String tipoMembresia;    // Nombre visible del Enum
    private String referidoPor;

    public SocioDetalle(String numSocio, String dni, String nombre, String apellidos, String email, String telefono, LocalDate fechaAlta, boolean activo, String tipoMembresia, String referidoPor) {
        this.numSocio     = numSocio;
        this.dni          = dni;
        this.nombre       = nombre;
        this.apellidos    = apellidos;
        this.email        = email;
        this.telefono     = telefono;
        this.fechaAlta    = fechaAlta;
        this.activo       = activo;
        this.tipoMembresia = tipoMembresia;
        this.referidoPor  = referidoPor;
    }

    public String getNumSocio()      { return numSocio; }
    public String getDni()           { return dni; }
    public String getNombre()        { return nombre; }
    public String getApellidos()     { return apellidos; }
    public String getEmail()         { return email; }
    public String getTelefono()      { return telefono; }
    public LocalDate getFechaAlta()  { return fechaAlta; }
    public boolean isActivo()        { return activo; }
    public String getTipoMembresia() { return tipoMembresia; }
    public String getReferidoPor()   { return referidoPor; }
}
