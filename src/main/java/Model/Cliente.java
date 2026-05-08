package Model;

import Util.Validator;

import java.time.LocalDate;

/**
 * Clase "Cliente"
 * @author Juan Diego Carrera
 * @version 1.0
 *
 * Los atributos de la clase Cliente:
 * dni = llave primaria de 9 caracteres donde el ultimo de ellos debe ser una letra mayuscula
 * nombre = se refiere al nombre del cliente (puede incluir uno o varios nombres)
 * apellidos = los apellidos del cliente (puede incluir uno o varios apellidos)
 * email = el correo electronico del cliente, que en el sistema será unico(no puede existir dos clientes con el mismo correo electronico)
 * telefono = el numero de telefono del cliente (en este caso,  se manejará formato de telefono de españa, son 9 caracteres)
 * fechaNacimiento = se refiere a la fecha en que nació el cliente (controlar formato yy-MM-dd)
 * */

public class Cliente {
    private String dni,nombre, apellidos;
    private String email, telefono; // El email debe ser unico en todo el sistema
    private LocalDate fechaNacimiento;

    /*
     * En el modelo relacional, se tiene lo siguiente:
     *
     * Los atributos: dni, nombre, apellidos, email están estandarizados, no pueden ser NULL,
     * así que para las inserciones en la base de datos, se realizarán comprobaciones antes de hacer cualquier inserción
     * */

    /**
     * Este constructor se utilizará para recuperar registros de la base de datos, también para
     * hacer inserciones, por lo que se realizarán comprobaciones en el controlador para evitar errores y guardar datos
     * inválidos o vacios
     * @param dni el dni del cliente (debe ser 9 caracteres incluyendo una letra mayuscula al final: 2345678L)
     * @param nombre el nombre del cliente (permitir uno o varios nombres, solo letras)
     * @param apellidos el apellido del cliente (permitir uno o varios apellidos, solo letras)
     * @param email el correo electronico del cliente (es unico en el sistema, no puede existir otro cliente con el mismo email)
     * @param telefono el numero de telefono del cliente (en este sistema se usará el formato de españa, 9 caracteres numericos separados por guiones '-')
     * @param fechaNacimiento la fecha de nacimiento del cliente (con formato yy/MM/dd)
     * */
    public Cliente(String dni, String nombre, String apellidos, String email, String telefono, LocalDate fechaNacimiento) {
        // Siguiendo con el modelo relacional, los campos que son obligatorios "NOT NULL" serán comprobados, si alguno falla, se lanzará excepcion
        this.dni = dni;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
    }

    // Getters
    public String getDni() {
        return dni;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefono() {
        return telefono;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    // Setters de la clase Cliente (controlados)
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }
}