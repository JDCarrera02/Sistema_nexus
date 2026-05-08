package Controller;

import DAO.ClienteDAO;
import DAO.SocioDAO;
import Model.Cliente;
import Model.Socio;
import Util.Login;
import Util.Sesion;
import View.LoginFrame;

import java.sql.SQLException;

public class SesionController {

    // Credenciales del administrador predefinidas
    private static final String ADMIN_USUARIO  = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    private final LoginFrame vista;
    private final ClienteDAO clienteDAO;
    private final SocioDAO socioDAO;

    public SesionController(LoginFrame vista) {
        this.vista      = vista;
        this.clienteDAO = new ClienteDAO();
        this.socioDAO   = new SocioDAO();
    }

    /**
     * Intenta iniciar sesión con las credenciales proporcionadas.
     * Detecta automáticamente si es admin, socio o cliente.
     */
    public void iniciarSesion(String usuario, String password) {

        // Paso 1 — Comprobamos campos vacíos
        if (usuario == null || usuario.isBlank() ||
                password == null || password.isBlank()) {
            vista.mostrarError("Por favor, introduce usuario y contraseña.");
            return;
        }

        // Paso 2 — Comprobamos si es administrador
        if (usuario.equals(ADMIN_USUARIO) && password.equals(ADMIN_PASSWORD)) {
            Sesion.getInstancia().iniciarComoAdmin();
            Login.log("Inicio de sesión como administrador.");
            vista.navegarAAdmin();
            return;
        }

        // Paso 3 — Buscamos el cliente por DNI + email
        try {
            Cliente cliente = clienteDAO.buscarPorId(usuario);

            if (cliente == null || !cliente.getEmail().equals(password)) {
                vista.mostrarError("DNI o email incorrectos.");
                Login.error("Intento de login fallido — DNI: " + usuario);
                return;
            }

            // Paso 4 — Comprobamos si es socio
            Socio socio = socioDAO.buscarPorDni(usuario);

            if (socio != null && socio.isActivo()) {
                Sesion.getInstancia().iniciarComoSocio(usuario, socio);
                Login.log("Inicio de sesión como socio — DNI: " + usuario);
                vista.navegarASocio();
            } else {
                Sesion.getInstancia().iniciarComoCliente(usuario);
                Login.log("Inicio de sesión como cliente — DNI: " + usuario);
                vista.navegarACliente();
            }

        } catch (SQLException e) {
            vista.mostrarError("Error de conexión. Inténtalo de nuevo.");
            Login.error("Error en login: " + e.getMessage());
        }
    }

    /**
     * Cierra la sesión activa y vuelve al LoginFrame.
     */
    public void cerrarSesion() {
        Login.log("Cierre de sesión — " + Sesion.getInstancia().getDniUsuario());
        Sesion.getInstancia().cerrarSesion();
        vista.setVisible(true);
    }
}
