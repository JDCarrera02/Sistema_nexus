package Util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Login {
    private static final String FICHERO_LOG = "log.txt";
    private static final String FICHERO_ERRORES  = "errores.txt";
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Login(){}

    /**
     * Registro de evento general en log.txt
     * Ejemplo: "Cliente 12345678A registrado correctamente"
     * @param mensaje el mensaje del log
     * */
    public static void log(String mensaje){
        escribir(FICHERO_LOG, mensaje);
    }

    /**
     * Registra un error en el fichero errores.txt
     * Ejemplo: "Error al insertar cliente: Duplicate entry";
     * @param mensaje el mensaje del error
     * */
    public static void error(String mensaje){
        escribir(FICHERO_ERRORES, mensaje);
    }

    /**
     * Metodo privado que genera la escritura correspondiente al fichero txt
     * @param fichero la ruta del fichero donde se va a escribir
     * @param mensaje el mensaje a escribir en el fichero
     * */
    private static void escribir(String fichero, String mensaje){
        try (BufferedWriter writer= new BufferedWriter(new FileWriter(fichero,true))){
            writer.write("["+ LocalDateTime.now().format(FORMATO)+"] "+mensaje);
            writer.newLine();
        } catch (IOException e){
            System.err.println("Error al escribir en fichero de log: "+ e.getMessage());
        }
    }
}
