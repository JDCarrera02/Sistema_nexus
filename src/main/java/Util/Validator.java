package Util;

import java.math.BigDecimal;

/**
 * Clase Validator
 *
 * @author Juan Diego Carrera
 * @version 1.2
 * <p>
 * En esta clase se tendrán los métodos que realizarán validaciones como Regex o patrones de diseño
 * que deben cumplir algunos de los atributos antes de hacer inserciones, modificaciones
 */
public class Validator {

    private static final String REGEX_DNI = "[0-9]{8}[A-Z]";
    private static final String REGEX_EMAIL = "^[a-zA-Z0-9._\\-*+]+@[a-zA-Z0-9._\\-+*]+\\.[a-zA-Z]{2,}$";
    private static final String REGEX_NOMBRE_COMPLETO = "\\p{Lu}\\p{Ll}+(\\s\\p{Lu}\\p{Ll}+)*";
    private static final String REGEX_TELEFONO = "\\d{3}-?\\d{3}-?\\d{3}";
    private static final String REGEX_NOMBRE_INSTALACION = "(?=.*\\p{L})[\\p{L}\\p{N}\\s]+";

    // Constantes para definir longitudes maximas de los datos, y evitar desbordamiento cuando el usuario manipule registros
    public static final int MAX_NOMBRE = 100; // El maximo de caracteres, siguiendo el modelo relacional, para el nombre son 100 caracteres (VARCHAR(100))
    public static final int MAX_APELLIDOS = 150; // El maximo de caracteres definido en el modelo relacional, para apellidos son 150
    public static final int MAX_EMAIL = 150; // El maximo de caracteres para el email son 150
    public static final int MAX_TELEFONO = 15; // El maximo de caracteres para el telefono es de 15
    public static final int MAX_NOMBRE_INST = 100; // El maximo de caracteres para el nombre de una instalacion es de 100
    public static final int MAX_DESCRIPCION = 500; // Este atributo se encuentra en Membresias, al ser TEXT, soporta un maximo de 500 caracteres
    public static final int MAX_RESERVAS = 999; // se refiere al atributo max_reservas, presente en Membresias, se define un maximo de 999 (valor y longitud)
    public static final int MAX_CAPACIDAD = 9999; // Se refiere al atributo capacidad, presente en Instalaciones, se define un maximo de 9999 (valor y longitud)

    /*
     * Se refiere al precio_hora de Instalaciones y precio_mensual de Membresias.
     * En el modelo relacional son datos de tipo DECIMAL(8,2), internamente permiten un valor maximo a = 999999.99
     * */
    public static final BigDecimal MAX_PRECIO = new BigDecimal("999999.99");


    // Para validar el codigo del socio, tendrá una estructura a esta: S-001, S-002... tiene que comenzar con S-...
    private static final String REGEX_CODIGO_SOCIO = "S-\\d+";

    // Esta clase no debe instanciarse, por lo que se creará el constructor vacio, en el caso que se vaya a instanciar
    private Validator() {
    }

    /*
     * =================================================================================
     * Anotaciones nuevas: se añadieron a los metodos las validaciones de longitud maxima
     * para evitar desbordamiento.
     * =================================================================================
     * */

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

        // Limite según campo
        int max = nombreCampo.equalsIgnoreCase("Apellidos") ? MAX_APELLIDOS : MAX_NOMBRE;

        if (valor.length() > max) {
            throw new IllegalArgumentException("El campo: '" + nombreCampo + "' no puede superar " +
                    max + " caracteres. Longitud actual: " + valor.length());
        }

        if (!valor.matches(REGEX_NOMBRE_COMPLETO))
            throw new IllegalArgumentException("El campo: '" + nombreCampo + "' solo admite letras, con la primera letra de cada palabra en mayuscula. Ejemplo: Francisco Javier");
    }

    /**
     * Metodo para validar que un email tenga un formato basico valido
     *
     * @param email la entrada a comprobar
     * @throws IllegalArgumentException cuando el formato del email no es valido
     */
    public static void validarEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email no puede estar vacio");
        }

        // Validar maximo de longitud
        if (email.length() > MAX_EMAIL) {
            throw new IllegalArgumentException("El email no puede superar " + MAX_EMAIL +
                    " caracteres. Longitud actual: " + email.length());
        }

        if (!email.trim().matches(REGEX_EMAIL)) {
            throw new IllegalArgumentException("Formato de email invalido: " + email);
        }
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

        // Si no está vacio, comprobar longitud
        if (telefono.length() > MAX_TELEFONO) {
            throw new IllegalArgumentException("El telefono no puede superar " + MAX_TELEFONO +
                    " caracteres");
        }

        if (!telefono.trim().matches(REGEX_TELEFONO)) {
            throw new IllegalArgumentException("Formato de telefono invalido. Debe tener 9 digitos. Ejemplo: 612-345-678 o 612345678");
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

    /*
     * Metodos nuevos -- validaciones con limites
     * */

    /**
     * Metodo para validar el nombre de una instalacion.
     * Permite letras, numeros, espacios, y caracteres basicos.
     * Se añadio el regex que permite letras y numeros, haciendo que por lo menos exista una letra
     * y se controle evitar un nombre como "29" o "9"
     * Explicación del regex:
     * [\\p{L}\\p{N}\\s]+ -> permite letras Unicode, números y espacios
     * (?=.*\\p{L}) -> lookahead que exige al menos una letra
     *
     * @param nombre el nombre de la instalacion
     */
    public static void validarNombreInstalacion(String nombre) {

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la instalacion no puede estar vacio");
        }

        // Verificar longitud
        if (nombre.length() > MAX_NOMBRE_INST) {
            throw new IllegalArgumentException("El nombre no puede superar " + MAX_NOMBRE_INST +
                    " caracteres. Longitud actual: " + nombre.length());
        }

        // Validar formato
        if (!nombre.matches(REGEX_NOMBRE_INSTALACION)) {
            throw new IllegalArgumentException("El nombre de la instalacion solo admite letras, numeros y espacios " +
                    "y debe contener al menos una letra. Ejemplo: Pista Pádel 1");
        }
    }

    /**
     * Metodo para validar un precio -- mayor que cero y dentro del rango DECIMAL(8,2).
     *
     * @param precio      el precio de una instalacion o de una membresia
     * @param nombreCampo el nombre del campo que se esta validando
     */
    public static void validarPrecio(BigDecimal precio, String nombreCampo) {
        // Verificar si es null
        if (precio == null) {
            throw new IllegalArgumentException("El campo '" + nombreCampo + "' no puede estar vacio");
        }

        // Validar que sea mayor a cero
        if (precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El campo '" + nombreCampo + "' debe ser mayor que cero");
        }

        // Validar limite
        if (precio.compareTo(MAX_PRECIO) > 0) {
            throw new IllegalArgumentException("El campo '" + nombreCampo + "' no puede superar " + MAX_PRECIO + " €");
        }

        // Comprobar que no tenga mas de 2 decimales
        if (precio.scale() > 2) {
            throw new IllegalArgumentException("El campo '" + nombreCampo + "' solo admite hasta 2 decimales");
        }

    }

    /**
     * Metodo para validar la capacidad de una instalacion
     *
     * @param capacidad la capacidad de la instalacion
     */
    public static void validarCapacidad(Integer capacidad) {
        // Validar si es null
        if (capacidad == null) {
            throw new IllegalArgumentException("La capacidad no puede estar vacia ");
        }

        // Verificar si es menor o igual a cero
        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor que cero");
        }

        // Validar maxima longitud y valor del dato
        if (capacidad > MAX_CAPACIDAD) {
            throw new IllegalArgumentException("La capacidad no puede superar " + MAX_CAPACIDAD + " personas.");
        }
    }

    /**
     * Metodo para validar el maximo de reservas de una membresia.
     * 0 = sin limite, es un valor valido
     *
     * @param maxReservas el maximo de reservas que establece una membresia
     */
    public static void validarMaxReservas(Integer maxReservas) {
        // Validar si es null
        if (maxReservas == null) {
            throw new IllegalArgumentException("El maximo de reservas no puede estar vacio");
        }

        // Verificar si es un valor negativo
        if (maxReservas < 0) {
            throw new IllegalArgumentException("El maximo de reservas no puede ser un valor negativo.");
        }

        // Validar longitud y valor maximo
        if (maxReservas > MAX_RESERVAS) {
            throw new IllegalArgumentException("El maximo de reservas no puede superar " + MAX_RESERVAS);
        }
    }

    /**
     * Metodo para validar la descripcion de una membresia -- campo TEXT opcional con limite
     *
     * @param descripcion la descripcion de la membresia
     */
    public static void validarDescripcion(String descripcion) {
        // Verificar si es null o es vacio (para no validar nada mas)
        if (descripcion == null || descripcion.isBlank()) return;

        // Validar longitud maxima
        if (descripcion.length() > MAX_DESCRIPCION) {
            throw new IllegalArgumentException("La descripcion no puede superar " + MAX_DESCRIPCION +
                    " caracteres. Longitud actual: " + descripcion.length());
        }
    }


}