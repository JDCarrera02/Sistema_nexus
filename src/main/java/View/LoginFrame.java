package View;

import Controller.SesionController;
import Util.Validator;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final SesionController sesionController;

    // Componentes
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnEntrar;
    private JButton btnRegistrarse;
    private JLabel lblTitulo;
    private JLabel lblUsuario;
    private JLabel lblPassword;
    private JLabel lblInfo;

    public LoginFrame() {
        sesionController = new SesionController(this);
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        setTitle("Sistema Nexus");
        setSize(380, 280);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centra la ventana
        setResizable(false);

        // Panel principal
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título
        lblTitulo = new JLabel("Iniciar sesion", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(lblTitulo, gbc);

        // Info de login
        lblInfo = new JLabel("DNI (usuario) y Email (contraseña)", SwingConstants.CENTER);
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 10));
        lblInfo.setForeground(Color.GRAY);
        gbc.gridy = 1;
        panel.add(lblInfo, gbc);

        // Campo usuario
        gbc.gridwidth = 1;
        lblUsuario = new JLabel("Usuario:");
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(lblUsuario, gbc);

        txtUsuario = new JTextField(15);
        limitarCaracteres(txtUsuario, 12);
        gbc.gridx = 1;
        panel.add(txtUsuario, gbc);

        // Campo contraseña
        lblPassword = new JLabel("Contraseña:");
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(lblPassword, gbc);

        txtPassword = new JPasswordField(15);
        limitarCaracteres(txtPassword, Validator.MAX_EMAIL);
        gbc.gridx = 1;
        panel.add(txtPassword, gbc);

        // Botón entrar
        btnEntrar = new JButton("Entrar");
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(btnEntrar, gbc);

        // Botón registrarse
        btnRegistrarse = new JButton("¿No tienes cuenta? Regístrate");
        btnRegistrarse.setFont(new Font("Arial", Font.PLAIN, 10));
        gbc.gridy = 5;
        panel.add(btnRegistrarse, gbc);

        add(panel);

        // Configuracion de eventos

        btnEntrar.addActionListener(e -> {
            String usuario  = txtUsuario.getText().trim();
            String password = new String(txtPassword.getPassword()).trim();
            sesionController.iniciarSesion(usuario, password);
        });

        // Permite hacer login con Enter desde el campo contraseña
        txtPassword.addActionListener(e -> btnEntrar.doClick());

        btnRegistrarse.addActionListener(e -> {
            new RegistroClienteFrame().setVisible(true);
            // No cerramos el login — el usuario puede volver
        });
    }

    // Metodo para limitar los caracteres en los campos de ingreso
    private void limitarCaracteres(JTextField campo, int maxCaracteres) {
        campo.setDocument(new javax.swing.text.PlainDocument() {
            @Override
            public void insertString(int offs, String str,
                                     javax.swing.text.AttributeSet a)
                    throws javax.swing.text.BadLocationException {
                if (str == null) return;
                if ((getLength() + str.length()) <= maxCaracteres)
                    super.insertString(offs, str, a);
            }
        });
    }

    // Seccion de navegacion.
    // Estos metodos son llamados por la clase SesionController

    public void navegarAAdmin() {
        new AdminFrame().setVisible(true);
        this.setVisible(false);
    }

    public void navegarASocio() {
        new SocioFrame().setVisible(true);
        this.setVisible(false);
    }

    public void navegarACliente() {
        new ClienteFrame().setVisible(true);
        this.setVisible(false);
    }

    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje,
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje,
                "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new LoginFrame().setVisible(true)
        );
    }
}
