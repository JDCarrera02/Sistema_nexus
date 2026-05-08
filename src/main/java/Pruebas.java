public class Pruebas {
    public static void main(String[] args) {
        System.out.println("Nombre válido: " + "Juan".matches("\\p{Lu}\\p{Ll}+(\\s\\p{Lu}\\p{Ll}+)*"));
        System.out.println("Apellido válido: " + "García López".matches("\\p{Lu}\\p{Ll}+(\\s\\p{Lu}\\p{Ll}+)*"));
    }
}
