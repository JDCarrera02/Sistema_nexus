package View;

import Controller.ClienteController;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class RegistroClienteFrame extends JFrame implements VistaCliente{

    private ClienteController clienteController;

    // Componentes
    private JTextField txtDni;
    private JTextField txtNombre;
    private JTextField txtApellidos;
    private JTextField txtEmail;
    private JTextField txtTelefono;
    private JTextField txtFechaNacimiento;
    private JButton btnRegistrar;
    private JButton btnCancelar;

    public RegistroClienteFrame() {
        clienteController = new ClienteController(this);
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        setTitle("Registro de Cliente");
        setSize(420, 380);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Panel principal
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título
        JLabel lblTitulo = new JLabel("Registro de Cliente", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(lblTitulo, gbc);

        // Separador
        gbc.gridy = 1;
        panel.add(new JSeparator(), gbc);

        // Campos del formulario
        gbc.gridwidth = 1;

        agregarCampo(panel, gbc, "DNI:", 2);
        txtDni = new JTextField(15);
        agregarComponente(panel, gbc, txtDni, 2);

        agregarCampo(panel, gbc, "Nombre:", 3);
        txtNombre = new JTextField(15);
        agregarComponente(panel, gbc, txtNombre, 3);

        agregarCampo(panel, gbc, "Apellidos:", 4);
        txtApellidos = new JTextField(15);
        agregarComponente(panel, gbc, txtApellidos, 4);

        agregarCampo(panel, gbc, "Email:", 5);
        txtEmail = new JTextField(15);
        agregarComponente(panel, gbc, txtEmail, 5);

        agregarCampo(panel, gbc, "Teléfono (opcional):", 6);
        txtTelefono = new JTextField(15);
        agregarComponente(panel, gbc, txtTelefono, 6);

        agregarCampo(panel, gbc, "Fecha nacimiento (yyyy-MM-dd):", 7);
        txtFechaNacimiento = new JTextField(15);
        agregarComponente(panel, gbc, txtFechaNacimiento, 7);

        // Nota informativa
        JLabel lblNota = new JLabel(
                "<html><i>* Tu solicitud será revisada por el administrador</i></html>",
                SwingConstants.CENTER
        );
        lblNota.setForeground(Color.GRAY);
        lblNota.setFont(new Font("Arial", Font.PLAIN, 10));
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        panel.add(lblNota, gbc);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        btnRegistrar = new JButton("Registrarse");
        btnCancelar = new JButton("Cancelar");

        panelBotones.add(btnRegistrar);
        panelBotones.add(btnCancelar);

        gbc.gridy = 9;
        panel.add(panelBotones, gbc);

        add(panel);

        // Configuracion de eventos
        btnRegistrar.addActionListener(e -> registrar());

        btnCancelar.addActionListener(e -> dispose());
    }

    private void registrar() {
        // Recoger los datos del formulario
        String dni      = txtDni.getText().trim();
        String nombre   = txtNombre.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        String email    = txtEmail.getText().trim();
        String telefono = txtTelefono.getText().trim();

        // Teléfono vacío → null (es opcional)
        if (telefono.isEmpty()) telefono = null;

        // Conversion de fecha, si está vacía se deja null
        LocalDate fechaNacimiento = null;
        String fechaTexto = txtFechaNacimiento.getText().trim();

        if (!fechaTexto.isEmpty()) {
            try {
                fechaNacimiento = LocalDate.parse(fechaTexto);
            } catch (DateTimeParseException e) {
                mostrarError("Formato de fecha incorrecto. Usa: yyyy-MM-dd");
                return;
            }
        }

        // Llamar al controlador de cliente para insertar
        clienteController.insertarCliente(dni, nombre, apellidos,
                email, telefono, fechaNacimiento);
    }

    // Metodos auxiliares, para inicializar campos y añadirlos
    private void agregarCampo(JPanel panel, GridBagConstraints gbc,
                              String texto, int fila) {
        gbc.gridx = 0; gbc.gridy = fila;
        panel.add(new JLabel(texto), gbc);
    }

    private void agregarComponente(JPanel panel, GridBagConstraints gbc,
                                   JComponent componente, int fila) {
        gbc.gridx = 1; gbc.gridy = fila;
        panel.add(componente, gbc);
    }

    // Metodos de comunicacion, son llamados por el controlador

    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje,
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje,
                "Información", JOptionPane.INFORMATION_MESSAGE);
        limpiarFormulario();
        dispose(); // Cierra el frame tras registro exitoso
    }

    private void limpiarFormulario() {
        txtDni.setText("");
        txtNombre.setText("");
        txtApellidos.setText("");
        txtEmail.setText("");
        txtTelefono.setText("");
        txtFechaNacimiento.setText("");
    }
}
