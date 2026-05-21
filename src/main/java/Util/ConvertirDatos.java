package Util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class ConvertirDatos {

    private ConvertirDatos(){}

    /**
     * Metodo privado para convertir un String a Integer.
     * Lanza IllegalArgumentException con mensaje claro si el formato es inválido.
     * @param texto       el texto a convertir
     * @param nombreCampo el nombre del campo para el mensaje de error
     * @return el Integer resultante
     */
    public static Integer parsearEntero(String texto, String nombreCampo) {
        if (texto == null || texto.isBlank())
            throw new IllegalArgumentException(
                    nombreCampo + " no puede estar vacío"
            );
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    nombreCampo + " debe ser un número entero. Ejemplo: 4"
            );
        }
    }

    /**
     * Metodo privado para convertir un String a BigDecimal.
     * Lanza IllegalArgumentException con mensaje claro si el formato es inválido.
     * @param texto       el texto a convertir
     * @param nombreCampo el nombre del campo para el mensaje de error
     * @return el BigDecimal resultante
     */
    public static BigDecimal parsearDecimal(String texto, String nombreCampo) {
        if (texto == null || texto.isBlank())
            throw new IllegalArgumentException(
                    nombreCampo + " no puede estar vacío"
            );
        try {
            return new BigDecimal(texto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    nombreCampo + " debe ser un número válido. Ejemplo: 29.99"
            );
        }
    }

    /**
     * Metodo para convertir un String a un LocalTime.
     * Lanza IllegalArgumentException con mensaje claro si el formato es invalido
     * @param txtHora el texto a convertir
     * @param nombreCampo el nombre del campo para el mensaje de error
     * @return el LocalTime resultante
     * */
    public static LocalTime parsearHora(String txtHora, String nombreCampo){
        if (txtHora == null  || txtHora.isBlank()){
            throw new IllegalArgumentException("'"+nombreCampo+"' no puede estar vacio");
        }

        try {
            return LocalTime.parse(txtHora);
        } catch (DateTimeParseException e){
            throw new IllegalArgumentException(nombreCampo+" debe ser un formato de hora valido. Ejemplo: 10:00 o 22:00");
        }
    }

    /**
     * Metodo para convertir un String en una fecha valida con el formato estandar
     * @param txtFecha el texto a convertir
     * @param nombreCampo el nombre del campo para el mensaje de error
     * @return el LocalTime resultante
     * */
    public static LocalDate parsearFecha(String txtFecha, String nombreCampo){
        if (txtFecha == null || txtFecha.isBlank()) {
            throw new IllegalArgumentException("'"+nombreCampo+"' no puede estar vacio");
        }

        try {
            return LocalDate.parse(txtFecha);
        } catch (DateTimeParseException e){
            throw new IllegalArgumentException("'"+nombreCampo+"' tiene formato incorrecto. Ejemplo: 2026-04-22");
        }
    }
}
