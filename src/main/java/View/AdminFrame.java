package View;

import Controller.*;
import Model.*;
import Util.Sesion;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

public class AdminFrame extends JFrame implements VistaCliente {
    // Controllers
    private ClienteController clienteController;
    private SocioController socioController;
    private MembresiaController membresiaController;
    private InstalacionController instalacionController;
    private ReservaController reservaController;

    // Pestañas
    private JTabbedPane tabbedPane;

    // =========================================================
    // PESTAÑA CLIENTES
    // =========================================================
    private JTable tablaClientes;
    private DefaultTableModel modeloClientes;
    private JButton btnBuscarCliente;
    private JButton btnEliminarCliente;
    private JButton btnModificarCliente;
    private JButton btnRefrescarClientes;
    private JButton btnCrearCliente;
    private JTextField txtBuscarDni;

    // =========================================================
    // PESTAÑA SOCIOS
    // =========================================================
    private JTable tablaSocios;
    private DefaultTableModel modeloSocios;
    private JButton btnDarDeAltaSocio;
    private JButton btnDesactivarSocio;
    private JButton btnRefrescarSocios;
    private JButton btnModificarSocio;

    // =========================================================
    // PESTAÑA MEMBRESIAS
    // =========================================================
    private JTable tablaMembresias;
    private DefaultTableModel modeloMembresias;
    private JButton btnEditarMembresia;
    private JButton btnRefrescarMembresias;

    // =========================================================
    // PESTAÑA INSTALACIONES
    // =========================================================
    private JTable tablaInstalaciones;
    private DefaultTableModel modeloInstalaciones;
    private JButton btnNuevaInstalacion;
    private JButton btnEditarInstalacion;
    private JButton btnEliminarInstalacion;
    private JButton btnRefrescarInstalaciones;

    // =========================================================
    // PESTAÑA RESERVAS
    // =========================================================
    private JTable tablaReservas;
    private DefaultTableModel modeloReservas;
    private JButton btnCancelarReserva;
    private JButton btnCompletarReserva;
    private JButton btnRefrescarReservas;

    public AdminFrame() {
        clienteController = new ClienteController(this);
        socioController = new SocioController(this);
        membresiaController = new MembresiaController(this);
        instalacionController = new InstalacionController(this);
        reservaController = new ReservaController(this);
        inicializarComponentes();
        cargarDatosIniciales();
    }

    private void inicializarComponentes() {
        setTitle("Sistema Nexus — Administrador");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Panel superior con bienvenida y botón cerrar sesión
        JPanel panelSuperior = getJPanel();

        // TabbedPane con las cinco secciones
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Clientes", crearPestanaClientes());
        tabbedPane.addTab("Socios", crearPestanaSocios());
        tabbedPane.addTab("Membresias", crearPestanaMembresias());
        tabbedPane.addTab("Instalaciones", crearPestanaInstalaciones());
        tabbedPane.addTab("Reservas", crearPestanaReservas());

        // Recarga los datos al cambiar de pestaña
        tabbedPane.addChangeListener(e -> cargarDatosSegunPestana());

        add(panelSuperior, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel getJPanel() {
        JPanel panelSuperior = new JPanel(new BorderLayout());
        JLabel lblBienvenida = new JLabel(
                "  Bienvenido, Administrador", SwingConstants.LEFT
        );
        lblBienvenida.setFont(new Font("Arial", Font.BOLD, 13));

        JButton btnCerrarSesion = new JButton("Cerrar sesion");
        btnCerrarSesion.addActionListener(e -> cerrarSesion());

        panelSuperior.add(lblBienvenida, BorderLayout.WEST);
        panelSuperior.add(btnCerrarSesion, BorderLayout.EAST);
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return panelSuperior;
    }

    // =========================================================
    // Pestaña Clientes
    // =========================================================

    private JPanel crearPestanaClientes() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Tabla
        String[] columnas = {"DNI", "Nombre", "Apellidos", "Email", "Telefono"};
        modeloClientes = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tablaClientes = new JTable(modeloClientes);
        tablaClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Panel de búsqueda
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBusqueda.add(new JLabel("Buscar:"));
        txtBuscarDni = new JTextField(12);
        btnBuscarCliente = new JButton("Buscar");
        btnRefrescarClientes = new JButton("Ver todos");
        panelBusqueda.add(txtBuscarDni);
        panelBusqueda.add(btnBuscarCliente);
        panelBusqueda.add(btnRefrescarClientes);


        // Botones de acción — eliminar, dar de alta como socio a un cliente y modificar, y crear un cliente
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnCrearCliente = new JButton("Registrar cliente");
        btnEliminarCliente = new JButton("Eliminar cliente");
        btnModificarCliente = new JButton("Modificar cliente");
        btnDarDeAltaSocio = new JButton("Dar de alta como socio");
        panelBotones.add(btnCrearCliente);
        panelBotones.add(btnEliminarCliente);
        panelBotones.add(btnDarDeAltaSocio);
        panelBotones.add(btnModificarCliente);

        panel.add(panelBusqueda, BorderLayout.NORTH);
        panel.add(new JScrollPane(tablaClientes), BorderLayout.CENTER);
        panel.add(panelBotones, BorderLayout.SOUTH);

        // Botones de eventos

        // Boton para crear o insertar un cliente
        btnCrearCliente.addActionListener(e -> mostrarDialogoNuevoCliente());

        // Boton para buscar clientes
        btnBuscarCliente.addActionListener(e -> {
            String termino = txtBuscarDni.getText().trim();
            List<Cliente> clientes = clienteController.buscarPorTermino(termino);
            modeloClientes.setRowCount(0);
            if (clientes != null)
                clientes.forEach(c -> modeloClientes.addRow(new Object[]{
                        c.getDni(), c.getNombre(), c.getApellidos(),
                        c.getEmail(), c.getTelefono()
                }));
        });

        btnRefrescarClientes.addActionListener(e -> cargarClientes());

        // Boton para eliminar clientes
        btnEliminarCliente.addActionListener(e -> {
            int fila = tablaClientes.getSelectedRow();
            if (fila == -1) {
                mostrarError("Selecciona un cliente de la tabla.");
                return;
            }
            String dni = (String) modeloClientes.getValueAt(fila, 0);
            int confirmacion = JOptionPane.showConfirmDialog(this,
                    "¿Eliminar al cliente con DNI: " + dni + "?",
                    "Confirmar eliminacion", JOptionPane.YES_NO_OPTION);
            if (confirmacion == JOptionPane.YES_OPTION) {
                clienteController.eliminar(dni);
                cargarClientes();
            }
        });

        // Boton para dar de alta como socio a un cliente
        btnDarDeAltaSocio.addActionListener(e -> {
            int fila = tablaClientes.getSelectedRow();
            if (fila == -1) {
                mostrarError("Selecciona un cliente de la tabla.");
                return;
            }
            String dni = (String) modeloClientes.getValueAt(fila, 0);
            Cliente cliente = clienteController.buscarPorDni(dni);
            if (cliente != null)
                mostrarDialogoAltaSocio(cliente);
        });

        // Boton modificar cliente
        btnModificarCliente.addActionListener(e -> {
            int fila = tablaClientes.getSelectedRow();
            if (fila == -1) {
                mostrarError("Selecciona un cliente de la tabla.");
                return;
            }
            String dni = (String) modeloClientes.getValueAt(fila, 0);
            Cliente cliente = clienteController.buscarPorDni(dni);
            if (cliente != null)
                mostrarDialogoModificarCliente(cliente);
        });

        return panel;
    }

    // Metodo para crear un nuevo cliente en la pestaña cliente
    private void mostrarDialogoNuevoCliente() {
        JDialog dialogo = new JDialog(this, "Nuevo cliente", true);
        dialogo.setSize(400, 340);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel lblTitulo = new JLabel("Registrar nuevo cliente", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 13));
        dialogo.add(lblTitulo, gbc);

        gbc.gridwidth = 1;

        // DNI
        gbc.gridx = 0;
        gbc.gridy = 1;
        dialogo.add(new JLabel("DNI:"), gbc);
        JTextField txtDni = new JTextField(15);
        gbc.gridx = 1;
        dialogo.add(txtDni, gbc);

        // Nombre
        gbc.gridx = 0;
        gbc.gridy = 2;
        dialogo.add(new JLabel("Nombre:"), gbc);
        JTextField txtNombre = new JTextField(15);
        gbc.gridx = 1;
        dialogo.add(txtNombre, gbc);

        // Apellidos
        gbc.gridx = 0;
        gbc.gridy = 3;
        dialogo.add(new JLabel("Apellidos:"), gbc);
        JTextField txtApellidos = new JTextField(15);
        gbc.gridx = 1;
        dialogo.add(txtApellidos, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy = 4;
        dialogo.add(new JLabel("Email:"), gbc);
        JTextField txtEmail = new JTextField(15);
        gbc.gridx = 1;
        dialogo.add(txtEmail, gbc);

        // Teléfono
        gbc.gridx = 0;
        gbc.gridy = 5;
        dialogo.add(new JLabel("Teléfono (opcional):"), gbc);
        JTextField txtTelefono = new JTextField(15);
        gbc.gridx = 1;
        dialogo.add(txtTelefono, gbc);

        // Fecha nacimiento
        gbc.gridx = 0;
        gbc.gridy = 6;
        dialogo.add(new JLabel("Fecha nacimiento (yyyy-MM-dd):"), gbc);
        JTextField txtFecha = new JTextField(15);
        gbc.gridx = 1;
        dialogo.add(txtFecha, gbc);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnGuardar = new JButton("Registrar");
        JButton btnCancelar = new JButton("Cancelar");
        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        dialogo.add(panelBotones, gbc);

        // Eventos
        btnGuardar.addActionListener(e -> {
            // Teléfono — opcional
            String telefono = txtTelefono.getText().trim();
            if (telefono.isEmpty()) telefono = null;

            // Fecha — opcional
            LocalDate fechaNac = null;
            String fechaTexto = txtFecha.getText().trim();
            if (!fechaTexto.isEmpty()) {
                try {
                    fechaNac = LocalDate.parse(fechaTexto);
                } catch (DateTimeParseException ex) {
                    mostrarError("Formato de fecha incorrecto. Usa: yyyy-MM-dd");
                    return;
                }
            }

            boolean exito = clienteController.insertarCliente(
                    txtDni.getText().trim(),
                    txtNombre.getText().trim(),
                    txtApellidos.getText().trim(),
                    txtEmail.getText().trim(),
                    telefono, fechaNac);
            if (exito) {
                dialogo.dispose();
                cargarClientes();
            }
        });

        btnCancelar.addActionListener(e -> dialogo.dispose());
        dialogo.setVisible(true);
    }

    // Metodo para modificar cliente en la pestaña cliente
    private void mostrarDialogoModificarCliente(Cliente cliente) {
        JDialog dialogo = new JDialog(this, "Modificar cliente", true);
        dialogo.setSize(400, 320);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // DNI — solo lectura, informativo
        gbc.gridx = 0;
        gbc.gridy = 0;
        dialogo.add(new JLabel("DNI:"), gbc);
        JLabel lblDni = new JLabel(cliente.getDni());
        lblDni.setFont(new Font("Arial", Font.BOLD, 11));
        gbc.gridx = 1;
        dialogo.add(lblDni, gbc);

        // Nombre
        gbc.gridx = 0;
        gbc.gridy = 1;
        dialogo.add(new JLabel("Nombre:"), gbc);
        JTextField txtNombre = new JTextField(cliente.getNombre(), 15);
        gbc.gridx = 1;
        dialogo.add(txtNombre, gbc);

        // Apellidos
        gbc.gridx = 0;
        gbc.gridy = 2;
        dialogo.add(new JLabel("Apellidos:"), gbc);
        JTextField txtApellidos = new JTextField(cliente.getApellidos(), 15);
        gbc.gridx = 1;
        dialogo.add(txtApellidos, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy = 3;
        dialogo.add(new JLabel("Email:"), gbc);
        JTextField txtEmail = new JTextField(cliente.getEmail(), 15);
        gbc.gridx = 1;
        dialogo.add(txtEmail, gbc);

        // Teléfono
        gbc.gridx = 0;
        gbc.gridy = 4;
        dialogo.add(new JLabel("Teléfono:"), gbc);
        JTextField txtTelefono = new JTextField(
                cliente.getTelefono() != null ? cliente.getTelefono() : "", 15
        );
        gbc.gridx = 1;
        dialogo.add(txtTelefono, gbc);

        // Fecha nacimiento
        gbc.gridx = 0;
        gbc.gridy = 5;
        dialogo.add(new JLabel("Fecha nacimiento (yyyy-MM-dd):"), gbc);
        JTextField txtFecha = new JTextField(
                cliente.getFechaNacimiento() != null
                        ? cliente.getFechaNacimiento().toString() : "", 15
        );
        gbc.gridx = 1;
        dialogo.add(txtFecha, gbc);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnGuardar = new JButton("Guardar cambios");
        JButton btnCancelar = new JButton("Cancelar");
        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        dialogo.add(panelBotones, gbc);

        // Eventos
        btnGuardar.addActionListener(e -> {
            // Fecha — opcional
            LocalDate fechaNac = null;
            String fechaTexto = txtFecha.getText().trim();
            if (!fechaTexto.isEmpty()) {
                try {
                    fechaNac = LocalDate.parse(fechaTexto);
                } catch (DateTimeParseException ex) {
                    mostrarError("Formato de fecha incorrecto. Usa: yyyy-MM-dd");
                    return;
                }
            }

            // Teléfono — opcional
            String telefono = txtTelefono.getText().trim();
            if (telefono.isEmpty()) telefono = null;

            boolean exito = clienteController.actualizarCliente(
                    cliente.getDni(),
                    txtNombre.getText().trim(),
                    txtApellidos.getText().trim(),
                    txtEmail.getText().trim(),
                    telefono,
                    fechaNac
            );
            if (exito) {
                dialogo.dispose();
                cargarClientes();
            }
        });

        btnCancelar.addActionListener(e -> dialogo.dispose());
        dialogo.setVisible(true);
    }

    // =========================================================
    // Pestaña -- Socios
    // =========================================================

    private JPanel crearPestanaSocios() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Tabla
        String[] columnas = {"Nº Socio", "DNI", "Nombre", "Apellidos", "Membresia", "Fecha de Alta", "Activo"};
        modeloSocios = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tablaSocios = new JTable(modeloSocios);
        tablaSocios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Panel de búsqueda
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField txtBuscarSocio = new JTextField(12);
        JButton btnBuscarSocio = new JButton("Buscar");
        btnRefrescarSocios = new JButton("Ver todos");
        panelBusqueda.add(new JLabel("Buscar:"));
        panelBusqueda.add(txtBuscarSocio);
        panelBusqueda.add(btnBuscarSocio);
        panelBusqueda.add(btnRefrescarSocios);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnDetalleSocio = new JButton("Ver detalle");
        JButton btnActivarSocio = new JButton("Activar");
        btnDesactivarSocio = new JButton("Desactivar");
        btnModificarSocio = new JButton("Modificar socio");
        panelBotones.add(btnDetalleSocio);
        panelBotones.add(btnActivarSocio);
        panelBotones.add(btnDesactivarSocio);
        panelBotones.add(btnModificarSocio);

        panel.add(panelBusqueda, BorderLayout.NORTH);
        panel.add(new JScrollPane(tablaSocios), BorderLayout.CENTER);
        panel.add(panelBotones, BorderLayout.SOUTH);

        // Botones de accion

        // Buscar socio
        btnBuscarSocio.addActionListener(e -> {
            String termino = txtBuscarSocio.getText().trim();
            cargarSociosConTermino(termino);
        });

        btnRefrescarSocios.addActionListener(e -> cargarSocios());

        btnDetalleSocio.addActionListener(e -> {
            int fila = tablaSocios.getSelectedRow();
            if (fila == -1) {
                mostrarError("Selecciona un socio de la tabla.");
                return;
            }
            mostrarDetalleSocio(fila);
        });

        // Desactivar socio
        btnDesactivarSocio.addActionListener(e -> {
            int fila = tablaSocios.getSelectedRow();
            if (fila == -1) {
                mostrarError("Selecciona un socio de la tabla.");
                return;
            }
            String numSocio = (String) modeloSocios.getValueAt(fila, 0);
            int confirmacion = JOptionPane.showConfirmDialog(this,
                    "¿Desactivar al socio " + numSocio + "?",
                    "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirmacion == JOptionPane.YES_OPTION) {
                socioController.desactivar(numSocio);
                cargarSocios();
            }
        });

        // Activar socio
        btnActivarSocio.addActionListener(e -> {
            int fila = tablaSocios.getSelectedRow();
            if (fila == -1) {
                mostrarError("Selecciona un socio de la tabla.");
                return;
            }
            String numSocio = (String) modeloSocios.getValueAt(fila, 0);
            int confirmacion = JOptionPane.showConfirmDialog(this,
                    "¿Activar al socio " + numSocio + "?",
                    "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirmacion == JOptionPane.YES_OPTION) {
                socioController.activar(numSocio);
                cargarSocios();
            }
        });

        // Modificar socio
        btnModificarSocio.addActionListener(e -> {
            int fila = tablaSocios.getSelectedRow();
            if (fila == -1) {
                mostrarError("Selecciona un socio de la tabla.");
                return;
            }
            // Recuperar el SocioDetalle directamente de la tabla
            String numSocio = (String) modeloSocios.getValueAt(fila, 0);
            String dni = (String) modeloSocios.getValueAt(fila, 1);
            String nombre = (String) modeloSocios.getValueAt(fila, 2);
            String apellidos = (String) modeloSocios.getValueAt(fila, 3);
            String membresia = (String) modeloSocios.getValueAt(fila, 4);
            String fechaAlta = modeloSocios.getValueAt(fila, 5).toString();
            String activo = (String) modeloSocios.getValueAt(fila, 6);

            // Completar email y teléfono consultando al cliente
            Cliente cliente = clienteController.buscarPorDni(dni);
            if (cliente != null) {
                SocioDetalle detalleCompleto = new SocioDetalle(
                        numSocio, dni, nombre, apellidos,
                        cliente.getEmail(), cliente.getTelefono(),
                        LocalDate.parse(fechaAlta),
                        activo.equals("Sí"),
                        membresia, null
                );
                mostrarDialogoModificarSocio(detalleCompleto);
            }
        });

        return panel;
    }

    // =========================================================
    // Panel de dialogo para modificar un socio
    // =========================================================
    private void mostrarDialogoModificarSocio(SocioDetalle detalle) {

        JDialog dialogo = new JDialog(this, "Modificar socio", true);
        dialogo.setSize(420, 400);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Encabezado informativo — datos no modificables
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel lblInfo = new JLabel(
                "<html><b>Socio:</b> " + detalle.getNumSocio() +
                        " &nbsp;&nbsp; <b>DNI:</b> " + detalle.getDni() + "</html>"
        );
        lblInfo.setFont(new Font("Arial", Font.PLAIN, 11));
        dialogo.add(lblInfo, gbc);

        // Separador
        gbc.gridy = 1;
        dialogo.add(new JSeparator(), gbc);

        // =====================================================
        // DATOS DE CLIENTE — modifican tabla CLIENTES
        // =====================================================
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel lblSeccionCliente = new JLabel("— Datos personales —");
        lblSeccionCliente.setFont(new Font("Arial", Font.BOLD, 11));
        lblSeccionCliente.setForeground(new Color(70, 70, 150));
        dialogo.add(lblSeccionCliente, gbc);

        gbc.gridwidth = 1;

        // Nombre
        gbc.gridx = 0;
        gbc.gridy = 3;
        dialogo.add(new JLabel("Nombre:"), gbc);
        JTextField txtNombre = new JTextField(detalle.getNombre(), 15);
        gbc.gridx = 1;
        dialogo.add(txtNombre, gbc);

        // Apellidos
        gbc.gridx = 0;
        gbc.gridy = 4;
        dialogo.add(new JLabel("Apellidos:"), gbc);
        JTextField txtApellidos = new JTextField(detalle.getApellidos(), 15);
        gbc.gridx = 1;
        dialogo.add(txtApellidos, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy = 5;
        dialogo.add(new JLabel("Email:"), gbc);
        JTextField txtEmail = new JTextField(detalle.getEmail(), 15);
        gbc.gridx = 1;
        dialogo.add(txtEmail, gbc);

        // Teléfono
        gbc.gridx = 0;
        gbc.gridy = 6;
        dialogo.add(new JLabel("Teléfono:"), gbc);
        JTextField txtTelefono = new JTextField(
                detalle.getTelefono() != null ? detalle.getTelefono() : "", 15
        );
        gbc.gridx = 1;
        dialogo.add(txtTelefono, gbc);

        // =====================================================
        // Datos del socio - modificacion a la tabla SOCIOS
        // =====================================================

        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 7;
        JLabel lblSeccionSocio = new JLabel("— Datos de membresia —");
        lblSeccionSocio.setFont(new Font("Arial", Font.BOLD, 11));
        lblSeccionSocio.setForeground(new Color(70, 70, 150));
        dialogo.add(lblSeccionSocio, gbc);

        gbc.gridwidth = 1;

        // Membresia
        gbc.gridx = 0;
        gbc.gridy = 8;
        dialogo.add(new JLabel("Membresia:"), gbc);
        JComboBox<TipoMembresia> cmbMembresia =
                new JComboBox<>(TipoMembresia.values());
        // Seleccion de la membresía actual del socio
        for (TipoMembresia tipo : TipoMembresia.values()) {
            if (tipo.getNombreMembresia().equals(detalle.getTipoMembresia())) {
                cmbMembresia.setSelectedItem(tipo);
                break;
            }
        }
        gbc.gridx = 1;
        dialogo.add(cmbMembresia, gbc);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnGuardar = new JButton("Guardar cambios");
        JButton btnCancelar = new JButton("Cancelar");
        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);

        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        dialogo.add(panelBotones, gbc);

        // Eventos

        // Boton guardar
        btnGuardar.addActionListener(e -> {
            // Teléfono — opcional
            String telefono = txtTelefono.getText().trim();
            if (telefono.isEmpty()) telefono = null;

            // Actualizar los datos del cliente (en la tabla cliente)
            boolean exitoCliente = clienteController.actualizarCliente(
                    detalle.getDni(),
                    txtNombre.getText().trim(),
                    txtApellidos.getText().trim(),
                    txtEmail.getText().trim(),
                    telefono,
                    null  // No se modifica la fecha de nacimiento desde aquí
            );
            if (!exitoCliente) return; // Si falla esta parte, no se continua

            // Actualizacion de la membresia del socio
            TipoMembresia tipoSeleccionado = (TipoMembresia) cmbMembresia.getSelectedItem();

            Membresia membresia = membresiaController.buscarPorTipo(tipoSeleccionado);
            if (membresia == null) return; // No se puede continuar si falla la tabla Membresia

            boolean exitoSocio = socioController.actualizarMembresia(
                    detalle.getNumSocio(),
                    membresia.getIdMembresia());
            if (exitoSocio) {
                dialogo.dispose();
                cargarSocios();
            }
        });

        btnCancelar.addActionListener(e -> dialogo.dispose());
        dialogo.setVisible(true);
    }

    // =========================================================
    // Panel de dialogo, dar de alta a socio
    // =========================================================

    private void mostrarDialogoAltaSocio(Cliente cliente) {
        JDialog dialogo = new JDialog(this, "Dar de alta como socio", true);
        dialogo.setSize(380, 280);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Info del cliente seleccionado
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        dialogo.add(new JLabel(
                "<html><b>Cliente:</b> " + cliente.getNombre() +
                        " " + cliente.getApellidos() +
                        " — DNI: " + cliente.getDni() + "</html>"
        ), gbc);

        // Membresia
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        dialogo.add(new JLabel("Membresia:"), gbc);

        JComboBox<TipoMembresia> cmbMembresia =
                new JComboBox<>(TipoMembresia.values());
        gbc.gridx = 1;
        dialogo.add(cmbMembresia, gbc);

        // Referido por
        gbc.gridx = 0;
        gbc.gridy = 2;
        dialogo.add(new JLabel("Referido por (opcional):"), gbc);

        JTextField txtReferidoPor = new JTextField(10);
        gbc.gridx = 1;
        dialogo.add(txtReferidoPor, gbc);

        // Fecha alta
        gbc.gridx = 0;
        gbc.gridy = 3;
        dialogo.add(new JLabel("Fecha alta (yyyy-MM-dd):"), gbc);

        JTextField txtFechaAlta = new JTextField(
                LocalDate.now().toString(), 10
        );
        gbc.gridx = 1;
        dialogo.add(txtFechaAlta, gbc);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnConfirmar = new JButton("Confirmar alta");
        JButton btnCancelar = new JButton("Cancelar");
        panelBotones.add(btnConfirmar);
        panelBotones.add(btnCancelar);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        dialogo.add(panelBotones, gbc);

        // Eventos del dialogo

        // Boton confirmar
        btnConfirmar.addActionListener(e -> {
            LocalDate fechaAlta;
            try {
                fechaAlta = LocalDate.parse(txtFechaAlta.getText().trim());
            } catch (DateTimeParseException ex) {
                mostrarError("Formato de fecha incorrecto. Usa: yyyy-MM-dd");
                return;
            }

            TipoMembresia tipo = ((TipoMembresia) cmbMembresia.getSelectedItem());
            String referidoPor = txtReferidoPor.getText().trim();
            if (referidoPor.isEmpty()) referidoPor = null;

            // Obtencion del id de la membresía seleccionada (De manera interna)
            Membresia membresia = membresiaController.buscarPorTipo(tipo);
            if (membresia == null) return;

            boolean exito = socioController.insertar(
                    cliente.getDni(), fechaAlta,
                    membresia.getIdMembresia(), referidoPor
            );

            if (exito){
                dialogo.dispose();
                cargarSocios();
            }
        });

        btnCancelar.addActionListener(e -> dialogo.dispose());
        dialogo.setVisible(true);
    }

    private void mostrarDetalleSocio(int fila) {

        String numSocio = (String) modeloSocios.getValueAt(fila, 0);
        String dni = (String) modeloSocios.getValueAt(fila, 1);
        String nombre = (String) modeloSocios.getValueAt(fila, 2);
        String apellidos = (String) modeloSocios.getValueAt(fila, 3);
        String membresia = (String) modeloSocios.getValueAt(fila, 4);
        String fechaAlta = modeloSocios.getValueAt(fila, 5).toString();
        String activo = (String) modeloSocios.getValueAt(fila, 6);

        JDialog dialogo = new JDialog(this, "Detalle del socio", true);
        dialogo.setSize(350, 280);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Añadir cada campo como par etiqueta — valor (Los detalles visualmente funcionales)
        int fila2 = 0;
        agregarFilaDetalle(dialogo, gbc, fila2++, "Nº Socio:", numSocio);
        agregarFilaDetalle(dialogo, gbc, fila2++, "DNI:", dni);
        agregarFilaDetalle(dialogo, gbc, fila2++, "Nombre:", nombre);
        agregarFilaDetalle(dialogo, gbc, fila2++, "Apellidos:", apellidos);
        agregarFilaDetalle(dialogo, gbc, fila2++, "Membresía:", membresia);
        agregarFilaDetalle(dialogo, gbc, fila2++, "Fecha alta:", fechaAlta);
        agregarFilaDetalle(dialogo, gbc, fila2++, "Activo:", activo);

        JButton btnCerrar = new JButton("Cerrar");
        gbc.gridx = 0;
        gbc.gridy = fila2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        dialogo.add(btnCerrar, gbc);

        btnCerrar.addActionListener(e -> dialogo.dispose());
        dialogo.setVisible(true);
    }

    private void agregarFilaDetalle(JDialog dialogo, GridBagConstraints gbc, int fila, String etiqueta, String valor) {
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = fila;
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(new Font("Arial", Font.BOLD, 11));
        dialogo.add(lbl, gbc);

        gbc.gridx = 1;
        dialogo.add(new JLabel(valor != null ? valor : "—"), gbc);
    }


    // =========================================================
    // Pestaña de membresias
    // =========================================================

    private JPanel crearPestanaMembresias() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columnas = {"ID", "Tipo", "Precio mensual", "Max. reservas", "Descripcion"};
        modeloMembresias = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tablaMembresias = new JTable(modeloMembresias);
        tablaMembresias.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnEditarMembresia = new JButton("Editar membresia");
        btnRefrescarMembresias = new JButton("Refrescar");
        panelBotones.add(btnRefrescarMembresias);
        panelBotones.add(btnEditarMembresia);

        panel.add(new JScrollPane(tablaMembresias), BorderLayout.CENTER);
        panel.add(panelBotones, BorderLayout.SOUTH);

        btnEditarMembresia.addActionListener(e -> {
            int fila = tablaMembresias.getSelectedRow();
            if (fila == -1) {
                mostrarError("Selecciona una membresia de la tabla.");
                return;
            }
            Integer id = (Integer) modeloMembresias.getValueAt(fila, 0);
            Membresia membresia = membresiaController.buscarPorId(id);
            if (membresia != null)
                mostrarDialogoEditarMembresia(membresia);
        });

        btnRefrescarMembresias.addActionListener(e -> cargarMembresias());

        return panel;
    }

    // =========================================================
    // Ventana de dialogo, de edicion de membresia
    // =========================================================

    private void mostrarDialogoEditarMembresia(Membresia membresia) {
        JDialog dialogo = new JDialog(this, "Editar membresia", true);
        dialogo.setSize(360, 250);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Precio
        gbc.gridx = 0;
        gbc.gridy = 0;
        dialogo.add(new JLabel("Precio mensual:"), gbc);
        JTextField txtPrecio = new JTextField(
                membresia.getPrecioMensual().toString(), 10
        );
        gbc.gridx = 1;
        dialogo.add(txtPrecio, gbc);

        // Máx. reservas
        gbc.gridx = 0;
        gbc.gridy = 1;
        dialogo.add(new JLabel("Max. reservas:"), gbc);
        JTextField txtMaxReservas = new JTextField(
                membresia.getMaxReservas().toString(), 10
        );
        gbc.gridx = 1;
        dialogo.add(txtMaxReservas, gbc);

        // Descripcion
        gbc.gridx = 0;
        gbc.gridy = 2;
        dialogo.add(new JLabel("Descripcion:"), gbc);
        JTextField txtDescripcion = new JTextField(
                membresia.getDescripcion() != null ? membresia.getDescripcion() : "", 10
        );
        gbc.gridx = 1;
        dialogo.add(txtDescripcion, gbc);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");
        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        dialogo.add(panelBotones, gbc);

        // Eventos

        // Boton Guardar
        btnGuardar.addActionListener(e -> {
            boolean exito = membresiaController.actualizar(
                    membresia.getIdMembresia(),
                    membresia.getNombre(),
                    txtPrecio.getText().trim(),
                    txtMaxReservas.getText().trim(),
                    txtDescripcion.getText().trim()
            );

            if (exito){
                dialogo.dispose();
                cargarMembresias();
            }
        });

        btnCancelar.addActionListener(e -> dialogo.dispose());
        dialogo.setVisible(true);
    }

    // =========================================================
    // Pestaña de las instalaciones
    // =========================================================

    private JPanel crearPestanaInstalaciones() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columnas = {"ID", "Nombre", "Tipo", "Capacidad", "Precio/hora", "Activa"};
        modeloInstalaciones = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tablaInstalaciones = new JTable(modeloInstalaciones);
        tablaInstalaciones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnNuevaInstalacion = new JButton("Nueva instalación");
        btnEditarInstalacion = new JButton("Editar");
        btnEliminarInstalacion = new JButton("Eliminar");
        btnRefrescarInstalaciones = new JButton("Refrescar");
        panelBotones.add(btnRefrescarInstalaciones);
        panelBotones.add(btnNuevaInstalacion);
        panelBotones.add(btnEditarInstalacion);
        panelBotones.add(btnEliminarInstalacion);

        panel.add(new JScrollPane(tablaInstalaciones), BorderLayout.CENTER);
        panel.add(panelBotones, BorderLayout.SOUTH);

        btnNuevaInstalacion.addActionListener(e ->
                mostrarDialogoInstalacion(null)
        );

        btnEditarInstalacion.addActionListener(e -> {
            int fila = tablaInstalaciones.getSelectedRow();
            if (fila == -1) {
                mostrarError("Selecciona una instalacion de la tabla.");
                return;
            }
            Integer id = (Integer) modeloInstalaciones.getValueAt(fila, 0);
            Instalacion instalacion = instalacionController.buscarPorId(id);
            if (instalacion != null)
                mostrarDialogoInstalacion(instalacion);
        });

        btnEliminarInstalacion.addActionListener(e -> {
            int fila = tablaInstalaciones.getSelectedRow();
            if (fila == -1) {
                mostrarError("Selecciona una instalacion de la tabla.");
                return;
            }
            Integer id = (Integer) modeloInstalaciones.getValueAt(fila, 0);
            int confirmacion = JOptionPane.showConfirmDialog(this,
                    "¿Eliminar esta instalacion?",
                    "Confirmar eliminacion", JOptionPane.YES_NO_OPTION);
            if (confirmacion == JOptionPane.YES_OPTION) {
                instalacionController.eliminar(id);
                cargarInstalaciones();
            }
        });

        btnRefrescarInstalaciones.addActionListener(e -> cargarInstalaciones());

        return panel;
    }

    // =========================================================
    // DIÁLOGO — NUEVA / EDITAR INSTALACIÓN
    // =========================================================

    private void mostrarDialogoInstalacion(Instalacion instalacion) {
        boolean esNueva = instalacion == null;
        JDialog dialogo = new JDialog(this,
                esNueva ? "Nueva instalacion" : "Editar instalacion", true);
        dialogo.setSize(360, 280);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Nombre
        gbc.gridx = 0;
        gbc.gridy = 0;
        dialogo.add(new JLabel("Nombre:"), gbc);
        JTextField txtNombre = new JTextField(
                esNueva ? "" : instalacion.getNombreInstalacion(), 15
        );
        gbc.gridx = 1;
        dialogo.add(txtNombre, gbc);

        // Tipo
        gbc.gridx = 0;
        gbc.gridy = 1;
        dialogo.add(new JLabel("Tipo:"), gbc);
        JComboBox<TipoInstalacion> cmbTipo =
                new JComboBox<>(TipoInstalacion.values());
        if (!esNueva) cmbTipo.setSelectedItem(instalacion.getTipoInstalacion());
        gbc.gridx = 1;
        dialogo.add(cmbTipo, gbc);

        // Capacidad
        gbc.gridx = 0;
        gbc.gridy = 2;
        dialogo.add(new JLabel("Capacidad:"), gbc);
        JTextField txtCapacidad = new JTextField(
                esNueva ? "" : instalacion.getCapacidad().toString(), 15
        );
        gbc.gridx = 1;
        dialogo.add(txtCapacidad, gbc);

        // Precio hora
        gbc.gridx = 0;
        gbc.gridy = 3;
        dialogo.add(new JLabel("Precio/hora:"), gbc);
        JTextField txtPrecio = new JTextField(
                esNueva ? "" : instalacion.getPrecioHora().toString(), 15
        );
        gbc.gridx = 1;
        dialogo.add(txtPrecio, gbc);

        // Activa — solo visible al editar
        JCheckBox chkActiva = new JCheckBox(
                "Activa", esNueva || instalacion.isActiva()
        );
        if (!esNueva) {
            gbc.gridx = 0;
            gbc.gridy = 4;
            gbc.gridwidth = 2;
            dialogo.add(chkActiva, gbc);
        }

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");
        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        dialogo.add(panelBotones, gbc);

        btnGuardar.addActionListener(e -> {
            TipoInstalacion tipo = (TipoInstalacion) cmbTipo.getSelectedItem();
            boolean exito;

            if (esNueva) {
                exito = instalacionController.insertar(
                        txtNombre.getText().trim(), tipo,
                        parsearEntero(txtCapacidad.getText().trim()),
                        parsearDecimal(txtPrecio.getText().trim())
                );
            } else {
                exito = instalacionController.actualizar(
                        instalacion.getIdInstalacion(),
                        txtNombre.getText().trim(), tipo,
                        parsearEntero(txtCapacidad.getText().trim()),
                        parsearDecimal(txtPrecio.getText().trim()),
                        chkActiva.isSelected()
                );
            }

            if (exito) {
                dialogo.dispose();
                cargarInstalaciones();
            }
        });

        btnCancelar.addActionListener(e -> dialogo.dispose());
        dialogo.setVisible(true);
    }

    // =========================================================
    // Pestaña de las reservas
    // =========================================================

    private JPanel crearPestanaReservas() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columnas = {"ID", "DNI Cliente", "Instalación",
                "Fecha", "Inicio", "Fin", "Precio", "Estado"};
        modeloReservas = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tablaReservas = new JTable(modeloReservas);
        tablaReservas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnCancelarReserva = new JButton("Cancelar reserva");
        btnCompletarReserva = new JButton("Marcar completada");
        btnRefrescarReservas = new JButton("Refrescar");
        panelBotones.add(btnRefrescarReservas);
        panelBotones.add(btnCancelarReserva);
        panelBotones.add(btnCompletarReserva);

        panel.add(new JScrollPane(tablaReservas), BorderLayout.CENTER);
        panel.add(panelBotones, BorderLayout.SOUTH);

        btnCancelarReserva.addActionListener(e -> {
            int fila = tablaReservas.getSelectedRow();
            if (fila == -1) {
                mostrarError("Selecciona una reserva de la tabla.");
                return;
            }
            Integer id = (Integer) modeloReservas.getValueAt(fila, 0);
            reservaController.actualizarEstado(id, EstadoReserva.CANCELADA);
            cargarReservas();
        });

        btnCompletarReserva.addActionListener(e -> {
            int fila = tablaReservas.getSelectedRow();
            if (fila == -1) {
                mostrarError("Selecciona una reserva de la tabla.");
                return;
            }
            Integer id = (Integer) modeloReservas.getValueAt(fila, 0);
            reservaController.actualizarEstado(id, EstadoReserva.COMPLETADA);
            cargarReservas();
        });

        btnRefrescarReservas.addActionListener(e -> cargarReservas());

        return panel;
    }

    // =========================================================
    // Carga de datos a las tablas, para mantener datos actualizados despues de cada operacion realizada
    // =========================================================

    private void cargarDatosIniciales() {
        cargarClientes();
        cargarSocios();
        cargarMembresias();
        cargarInstalaciones();
        cargarReservas();
    }

    private void cargarDatosSegunPestana() {
        switch (tabbedPane.getSelectedIndex()) {
            case 0:
                cargarClientes();
                break;
            case 1:
                cargarSocios();
                break;
            case 2:
                cargarMembresias();
                break;
            case 3:
                cargarInstalaciones();
                break;
            case 4:
                cargarReservas();
                break;
        }
    }

    private void cargarClientes() {
        modeloClientes.setRowCount(0);
        List<Cliente> clientes = clienteController.listarTodos();
        if (clientes != null)
            clientes.forEach(c -> modeloClientes.addRow(new Object[]{
                    c.getDni(), c.getNombre(), c.getApellidos(),
                    c.getEmail(), c.getTelefono()
            }));
    }

    private void cargarSocios() {
        modeloSocios.setRowCount(0);
        List<SocioDetalle> detalles = socioController.listarDetalles();
        if (detalles != null)
            detalles.forEach(s -> modeloSocios.addRow(new Object[]{
                    s.getNumSocio(), s.getDni(), s.getNombre(), s.getApellidos(), s.getTipoMembresia(), s.getFechaAlta(),
                    s.isActivo() ? "Sí" : "No"
            }));
    }

    private void cargarSociosConTermino(String termino) {
        modeloSocios.setRowCount(0);
        List<SocioDetalle> detalles = socioController.buscarDetalles(termino);
        if (detalles != null)
            detalles.forEach(s -> modeloSocios.addRow(new Object[]{
                    s.getNumSocio(), s.getDni(), s.getNombre(), s.getApellidos(),
                    s.getTipoMembresia(), s.getFechaAlta(),
                    s.isActivo() ? "Sí" : "No"
            }));
    }

    private void cargarMembresias() {
        modeloMembresias.setRowCount(0);
        List<Membresia> membresias = membresiaController.listarTodos();
        if (membresias != null)
            membresias.forEach(m -> modeloMembresias.addRow(new Object[]{
                    m.getIdMembresia(), m.getNombre().getNombreMembresia(),
                    m.getPrecioMensual(), m.getMaxReservas(), m.getDescripcion()
            }));
    }

    private void cargarInstalaciones() {
        modeloInstalaciones.setRowCount(0);
        List<Instalacion> instalaciones = instalacionController.listarTodos();
        if (instalaciones != null)
            instalaciones.forEach(i -> modeloInstalaciones.addRow(new Object[]{
                    i.getIdInstalacion(), i.getNombreInstalacion(),
                    i.getTipoInstalacion().getNombreInstalacion(), i.getCapacidad(),
                    i.getPrecioHora(), i.isActiva() ? "Sí" : "No"
            }));
    }

    private void cargarReservas() {
        modeloReservas.setRowCount(0);
        List<Reserva> reservas = reservaController.listarTodos();
        if (reservas != null)
            reservas.forEach(r -> modeloReservas.addRow(new Object[]{
                    r.getIdReserva(), r.getDniCliente(), r.getIdInstalacion(),
                    r.getFechaReserva(), r.getHoraInicio(), r.getHoraFin(),
                    r.getPrecio(), r.getEstado().getNombreVisible()
            }));
    }

    // =========================================================
    // MÉTODOS AUXILIARES
    // =========================================================

    private Integer parsearEntero(String texto) {
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private java.math.BigDecimal parsearDecimal(String texto) {
        try {
            return new java.math.BigDecimal(texto);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void cerrarSesion() {
        Sesion.getInstancia().cerrarSesion();
        new LoginFrame().setVisible(true);
        this.dispose();
    }

    // =========================================================
    // MÉTODOS DE COMUNICACIÓN — VistaCliente
    // =========================================================

    @Override
    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje,
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje,
                "Información", JOptionPane.INFORMATION_MESSAGE);
    }
}