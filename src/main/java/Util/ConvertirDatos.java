package Util;

import java.math.BigDecimal;

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
}
