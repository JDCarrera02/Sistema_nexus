package Util;

import Model.EstadoReserva;
import Model.TipoInstalacion;
import Model.TipoMembresia;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Clase Validator
 *
 * @author Juan Diego Carrera
 * @version 1.0
 * <p>
 * En esta clase se tendrán los métodos que realizarán validaciones como Regex o patrones de diseño
 * que deben cumplir algunos de los atributos antes de hacer inserciones, modificaciones
 */
public class Validator {

    private static final String REGEX_DNI = "[0-9]{8}[A-Z]";
    private static final String REGEX_EMAIL = "^[a-zA-Z0-9._\\-*+]+@[a-zA-Z0-9._\\-+*]+\\.[a-zA-Z]{2,}$";
    private static final String REGEX_NOMBRE_COMPLETO = "\\p{Lu}\\p{Ll}+(\\s\\p{Lu}\\p{Ll}+)*";
    private static final String REGEX_TELEFONO = "\\d{3}-?\\d{3}-?\\d{3}";

    // Para validar el codigo del socio, tendrá una estructura a esta: S-001, S-002... tiene que comenzar con S-...
    private static final String REGEX_CODIGO_SOCIO = "S-\\d+";

    // Esta clase no debe instanciarse, por lo que se creará el constructor vacio, en el caso que se vaya a instanciar
    private Validator() {
    }

    /**
     * Este metodo validará el atributo DNI
     *
     * @param dni el dni del cliente (debe ser 9 caracteres incluyendo una letra mayuscula al final: 2345678L)
     * @throws IllegalArgumentException cuando el DNI está vacio
     * @throws IllegalArgumentException cuando el formato del DNI no es valido, no cumple con el regex
     */
    public static void validarDni(String dni) {
        if (dni == null || dni.isBlank())
            throw new IllegalArgumentException("El dni no puede estar vacio");
        if (!dni.trim().matches(REGEX_DNI))
            throw new IllegalArgumentException("Formato de DNI invalido" + dni);
    }

    /**
     * Este metodo valida texto que solo tenga letras (incluyendo tildes y ñ)
     * Con la primera letra de cada grupo en mayuscula, separados por espacios.
     * Ejemplos validos: "Francisco Javier, "María", "Ángel" () esto también es valido para los apellidos
     *
     * @param valor       la entrada de texto, que será el nombre o los apellidos
     * @param nombreCampo El nombre del campo en cuestion
     * @throws IllegalArgumentException En caso de que el campo esté vacio o NULL
     * @throws IllegalArgumentException Cuando el campo no cumpla con el formato especificado
     */

    public static void validarNombreCompleto(String valor, String nombreCampo) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("El campo: '" + nombreCampo + "' no puede estar vacio");
        if (!valor.matches(REGEX_NOMBRE_COMPLETO))
            throw new IllegalArgumentException("El campo: '" + nombreCampo + "' solo admite letras, con la primera letra de cada palabra en mayuscula. Ejemplo: Francisco Javier");
    }

    /**
     * Metodo para validar texto no vacio
     *
     * @param valor       La entrada de informacion, se validará que esta no esté vacia
     * @param nombreCampo Se refiere al nombre del campo ingresado, por ejemplo DNI o nombre, etc...
     * @throws IllegalArgumentException Cuando el valor de ingreso es NULL o está vacio
     */

    public static void validarTextoNoVacio(String valor, String nombreCampo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(nombreCampo + " No puede estar vacio");
        }
    }

    /**
     * Este metodo valida que un número sea positivo, para usarse con los IDs, cantidades, etc...
     *
     * @param valor       el valor numerico entero
     * @param nombreCampo el nombre del campo en cuestion, id, cantidad, etc...
     * @throws IllegalArgumentException cuando el campo numerico es negativo
     */

    public static void validarPositivo(int valor, String nombreCampo) {
        if (valor <= 0)
            throw new IllegalArgumentException("El campo '" + nombreCampo + "' debe ser un número positivo");
    }

    /**
     * Metodo para validar que un email tenga un formato basico valido
     *
     * @param email la entrada a comprobar
     * @throws IllegalArgumentException cuando el formato del email no es valido
     */
    public static void validarEmail(String email) {
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("El email no puede estar vacio");
        if (!email.trim().matches(REGEX_EMAIL))
            throw new IllegalArgumentException("Formato de email invalido: " + email);
    }

    /**
     * Este metodo valida el formato de telefono español.
     * Formato esperado: 9 digitos, puede ir separado por guiones
     * Este campo es opcional, segun el modelo relacional, puede ser NULL, en el caso que lo sea, no se comprobará nada
     * Explicacion del regex:
     * \\d{3} Primeros 3 digitos
     * [-]? guion opcional
     * \\d{3} siguientes tres digitos
     * [-]? guión opcional
     * \\d{3} ultimos 3 digitos
     *
     * @param telefono El numero de telefono ingresado
     */

    public static void validarTelefono(String telefono) {
        // Si está vacio, no se valida nada
        if (telefono == null || telefono.isBlank()) return;
        if (!telefono.trim().matches(REGEX_TELEFONO))
            throw new IllegalArgumentException("Formato de telefono invalido. Debe tener 9 digitos. Ejemplo: 612-345-678 o 612345678");
    }

    /**
     * Este metodo valida que el precio sea mayor que cero (positivo)
     * Se usa para campos de tipo BigDecimal que representan dinero
     *
     * @param precio      el valor numerico de ingreso
     * @param nombreCampo el nombre del campo en cuestión
     * @throws IllegalArgumentException Si el precio es NULL o vacio
     * @throws IllegalArgumentException Si el precio es menor o igual a cero (negativo)
     */
    public static void validarPrecio(BigDecimal precio, String nombreCampo) {
        if (precio == null)
            throw new IllegalArgumentException("El campo: '" + nombreCampo + "' no puede estar vacio");
        // Ahora, se comprueba que el numero sea mayor a cero
        // Al ser un objeto BigDecimal se utiliza el metodo compareTo()
        // Este metodo devuelve  lo siguiente: 0 si es igual(a cero en este caso). -1 si es menor. 1 si es mayor(positivo)
        if (precio.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("El campo: '" + nombreCampo + "' deber ser mayor que cero");
    }

    /**
     * Este metodo valida que un numero entero no sea negativo
     * Se utiliza para cantidades que pueden ser cero pero no un valor negativo
     * en el caso del atributo de Membresia "maxReservas" (0 significaria sin limite de reservas)
     *
     * @param valor       el valor numerico entero a comprobar
     * @param nombreCampo el nombre del campo en cuestion
     */
    public static void validarNoNegativo(Integer valor, String nombreCampo) {
        if (valor == null)
            throw new IllegalArgumentException("El campo: '" + nombreCampo + "' no puede estar vacio");
        if (valor < 0)
            throw new IllegalArgumentException("El campo: '" + nombreCampo + "' no puede ser negativo");
    }

    /**
     * Este metodo valida que el tipo de membresia no sea null
     * El Enum ya realiza la validacion de los valores.
     *
     * @param tipo el tipo de membresia
     * @throws IllegalArgumentException si el tipo de membresia no es valido
     */

    public static void validarTipoMembresia(TipoMembresia tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de membresia no puede estar vacio");
        }
    }

    /**
     * Este metodo valida la estructura del codigo de un socio, debe ser de esta forma:
     * S-001, S-002. Debe empezar con una S, seguido de un guión '-' y un consecutivo de numeros, por eso el \\d+ (digitos del 0-9)
     *
     * @param numSocio El numero del socio (entrada a validar)
     * @throws IllegalArgumentException en el caso de que el numero sea NULL
     * @throws IllegalArgumentException en el caso de que el num_socio no cumpla con el formato permitido
     */
    public static void validarNumSocio(String numSocio) {
        if (numSocio == null || numSocio.isBlank())
            throw new IllegalArgumentException("El número de socio no puede estar vacío");
        if (!numSocio.matches(REGEX_CODIGO_SOCIO))
            throw new IllegalArgumentException("Formato de número de socio inválido. Ejemplo: S-001");
    }

    /**
     * Metodo para validar el tipo de instalacion, que no sea null
     * El enum creado ya realiza las validaciones de los valores insertados
     *
     * @param tipo el tipo de instalacion
     * @throws IllegalArgumentException Si el tipo de instalacion ingresado es NULL
     */

    public static void validarTipoInstalacion(TipoInstalacion tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de instalacion no puede estar vacio");
        }
    }

    /**
     * Metodo que valida la capacidad de una instalacion, qie debe ser un numero estrictamente positivo
     * Una instalacion no puede tener un aforo 0 o negativo
     *
     * @param capacidad numero que indica la capacidad de la instalacion (a validar)
     * @throws IllegalArgumentException Cuando la capacidad es vacia o NULL
     * @throws IllegalArgumentException Cuando la instalacion es negativa o cero
     */
    public static void validarCapacidad(Integer capacidad) {
        if (capacidad == null)
            throw new IllegalArgumentException("La capacidad de la instalacion no puede estar vacia");
        if (capacidad <= 0)
            throw new IllegalArgumentException("La capacidad de una instalacion debe ser mayor que cero");
    }

    /**
     * Metodo que valida el estado de una reserva
     * Es un Enum, esto ya garantiza valores validos, entonces se comprueba si es NULL
     *
     * @param estado el objeto Enum a comprobar
     * @throws IllegalArgumentException si es objeto ingresado es null
     */
    public static void validarEstadoReserva(EstadoReserva estado) {
        if (estado == null)
            throw new IllegalArgumentException("El estado de la reserva no puede estar vacío");
    }

    /**
     * Metodo que valida que una fecha no sea nula
     *
     * @param fecha       la fecha ingresada a comprobar
     * @param nombreCampo El nombre del campo en cuestion
     * @throws IllegalArgumentException en el caso que la fecha esté vacia o sea nula
     */
    public static void validarFecha(LocalDate fecha, String nombreCampo) {
        if (fecha == null)
            throw new IllegalArgumentException("El campo '" + nombreCampo + "' no puede estar vacío");
    }

    /**
     * Metodo para validación basica de horas
     * La hora de fin debe ser posterior a la hora de inicio.
     * Ambas horas de entrada deben ser validadas antes de comprobar si cumplen con la primera condicion
     *
     * @param horaInicio la hora de inicio de la reserva
     * @param horaFin    la hora de finalizacion de la reserva
     */
    public static void validarRangoHoras(LocalTime horaInicio, LocalTime horaFin) {
        if (horaInicio == null)
            throw new IllegalArgumentException("La hora de inicio no puede estar vacía");
        if (horaFin == null)
            throw new IllegalArgumentException("La hora de fin no puede estar vacía");
        if (!horaFin.isAfter(horaInicio))
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio");
    }
}