package View;

// Interfaz que será implementada por RegistroClienteFrame (vista), y ClienteController la recibe como parametro
public interface VistaCliente {
    void mostrarError(String mensaje);
    void mostrarMensaje(String mensaje);
}
