package View;

import Controller.ClienteController;
import Controller.InstalacionController;
import Controller.ReservaController;
import Model.*;
import Util.Sesion;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

public class SocioFrame extends JFrame implements VistaCliente{
    private ReservaController reservaController;
    private InstalacionController instalacionController;
    private ClienteController clienteController;
    private List<ReservaDetalle>reservasCargadas;

    // Pestañas
    private JTabbedPane tabbedPane;

    // =========================================================
    // Pestaña, reservas del socio
    // =========================================================
    private JTable tablaReservas;
    private DefaultTableModel modeloReservas;
    private JButton btnCancelarReserva;
    private JButton btnRefrescarReservas;

    // =========================================================
    // Pestaña para reservar
    // =========================================================
    private JComboBox<TipoInstalacion> cmbTipoInstalacion;
    private JComboBox<String> cmbInstalacion;
    private List<Instalacion> instalacionesCargadas;
    private JTextField txtFecha;
    private JTextField txtHoraInicio;
    private JTextField txtHoraFin;
    private JLabel lblPrecioEstimado;
    private JButton btnReservar;

    public SocioFrame() {
        reservaController     = new ReservaController(this);
        instalacionController = new InstalacionController(this);
        clienteController = new ClienteController(this);
        inicializarComponentes();
        cargarDatosIniciales();
    }

    private void inicializarComponentes() {
        setTitle("DashBoard — Socio");
        setSize(750, 520);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Panel superior
        JPanel panelSuperior = new JPanel(new BorderLayout());

        // Para obtener el nombre del socio
        Socio socio = Sesion.getInstancia().getSocio(); // Obtener el socio

        Cliente cliente = clienteController.buscarPorDni(socio.getDni()); // Buscar en clientes por el dni para obtener su nombre

        // Guardar nombre en variable para mostrar
        String nombreSocio = cliente.getNombre();

        JLabel lblBienvenida = new JLabel(
                "  Bienvenido/a, " +nombreSocio,
                SwingConstants.LEFT
        );
        lblBienvenida.setFont(new Font("Arial", Font.BOLD, 13));

        JButton btnCerrarSesion = new JButton("Cerrar sesión");
        btnCerrarSesion.addActionListener(e -> cerrarSesion());

        panelSuperior.add(lblBienvenida, BorderLayout.WEST);
        panelSuperior.add(btnCerrarSesion, BorderLayout.EAST);
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // TabbedPane
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Mis reservas",   crearPestanaMisReservas());
        tabbedPane.addTab("Nueva reserva",  crearPestanaNuevaReserva());
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 0)
                cargarMisReservas();
        });

        add(panelSuperior, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    // =========================================================
    // Pestaña, reservas del socio
    // =========================================================

    private JPanel crearPestanaMisReservas() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columnas = {"Instalación", "Tipo", "Fecha",
                "Inicio", "Fin", "Precio", "Estado"};
        modeloReservas = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaReservas = new JTable(modeloReservas);
        tablaReservas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Panel de busqueda por nombre de instalacion
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField txtBuscarInstalacion = new JTextField(12);
        JButton btnBuscar    = new JButton("Buscar");
        JButton btnVerTodas  = new JButton("Ver todas");
        panelBusqueda.add(new JLabel("Buscar por instalación (nombre):"));
        panelBusqueda.add(txtBuscarInstalacion);
        panelBusqueda.add(btnBuscar);
        panelBusqueda.add(btnVerTodas);

        // Botones de accion
        // Botones de acción
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnCancelarReserva = new JButton("Cancelar reserva");
        panelBotones.add(btnCancelarReserva);

        panel.add(panelBusqueda, BorderLayout.NORTH);
        panel.add(new JScrollPane(tablaReservas), BorderLayout.CENTER);
        panel.add(panelBotones, BorderLayout.SOUTH);

        // Eventos

        // Boton buscar
        btnBuscar.addActionListener(e -> {
            String termino = txtBuscarInstalacion.getText().trim();
            String dni     = Sesion.getInstancia().getDniUsuario();
            cargarReservasConTermino(dni, termino);
        });

        btnVerTodas.addActionListener(e -> cargarMisReservas());

        // Boton cancelar
        btnCancelarReserva.addActionListener(e -> {
            int fila = tablaReservas.getSelectedRow();
            if (fila == -1) {
                mostrarError("Selecciona una reserva de la tabla.");
                return;
            }
            // Solo se pueden cancelar reservas CONFIRMADAS
            String estado = (String) modeloReservas.getValueAt(fila, 6);
            if (!estado.equals(EstadoReserva.CONFIRMADA.getNombreVisible())) {
                mostrarError("Solo puedes cancelar reservas confirmadas.");
                return;
            }
            int indice = tablaReservas.getSelectedRow();

            if (reservasCargadas == null || indice >= reservasCargadas.size()) return;

            Integer idReserva = reservasCargadas.get(indice).getIdReserva();

            int confirmacion = JOptionPane.showConfirmDialog(this,
                    "¿Cancelar esta reserva?",
                    "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirmacion == JOptionPane.YES_OPTION) {
                reservaController.actualizarEstado(idReserva, EstadoReserva.CANCELADA);
                cargarMisReservas();
            }
        });

        return panel;
    }

    private void cargarReservasConTermino(String dni, String termino){
        reservasCargadas = reservaController.buscarDetallesPorInstalacion(dni, termino);
        modeloReservas.setRowCount(0);
        if (reservasCargadas != null){
            reservasCargadas.forEach(r -> modeloReservas.addRow(new Object[]{
                    r.getNombreInstalacion(), r.getTipoInstalacion(),
                    r.getFechaReserva(), r.getHoraInicio(), r.getHoraFin(),
                    r.getPrecio() + " €", r.getEstado()
            }));
        }
    }



    // =========================================================
    // Pestaña, crear una nueva reserva
    // =========================================================

    private JPanel crearPestanaNuevaReserva() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título
        JLabel lblTitulo = new JLabel("Nueva reserva", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblTitulo, gbc);

        // Tipo de instalación
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Tipo de instalación:"), gbc);
        cmbTipoInstalacion = new JComboBox<>(TipoInstalacion.values());
        gbc.gridx = 1;
        panel.add(cmbTipoInstalacion, gbc);

        // Instalación concreta
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Instalación:"), gbc);
        cmbInstalacion = new JComboBox<>();
        gbc.gridx = 1;
        panel.add(cmbInstalacion, gbc);

        // Fecha
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Fecha (yyyy-MM-dd):"), gbc);
        txtFecha = new JTextField(LocalDate.now().toString(), 15);
        gbc.gridx = 1;
        panel.add(txtFecha, gbc);

        // Hora inicio
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Hora inicio (HH:mm):"), gbc);
        txtHoraInicio = new JTextField("10:00", 15);
        gbc.gridx = 1;
        panel.add(txtHoraInicio, gbc);

        // Hora fin
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Hora fin (HH:mm):"), gbc);
        txtHoraFin = new JTextField("11:00", 15);
        gbc.gridx = 1;
        panel.add(txtHoraFin, gbc);

        // Precio estimado
        gbc.gridx = 0; gbc.gridy = 6;
        panel.add(new JLabel("Precio estimado:"), gbc);
        lblPrecioEstimado = new JLabel("—");
        lblPrecioEstimado.setFont(new Font("Arial", Font.BOLD, 13));
        gbc.gridx = 1;
        panel.add(lblPrecioEstimado, gbc);

        // Botón calcular precio
        JButton btnCalcular = new JButton("Calcular precio");
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        panel.add(btnCalcular, gbc);

        // Botón reservar
        btnReservar = new JButton("Confirmar reserva");
        gbc.gridy = 8;
        panel.add(btnReservar, gbc);

        // =====================================================
        // EVENTOS
        // =====================================================

        // Al cambiar el tipo, recarga las instalaciones disponibles
        cmbTipoInstalacion.addActionListener(e -> {
            TipoInstalacion tipo =
                    (TipoInstalacion) cmbTipoInstalacion.getSelectedItem();
            cargarInstalacionesPorTipo(tipo);
        });

        btnCalcular.addActionListener(e -> calcularPrecioEstimado());

        btnReservar.addActionListener(e -> realizarReserva());

        return panel;
    }

    // =========================================================
    // Carga de datos
    // =========================================================

    private void cargarDatosIniciales() {
        cargarMisReservas();
        // Cargamos instalaciones del tipo seleccionado por defecto
        cargarInstalacionesPorTipo(
                (TipoInstalacion) cmbTipoInstalacion.getSelectedItem()
        );
    }

    private void cargarMisReservas() {
        String dni = Sesion.getInstancia().getDniUsuario();
        reservasCargadas = reservaController.listarDetallesPorCliente(dni);
        modeloReservas.setRowCount(0);

        if (reservasCargadas != null){
            reservasCargadas.forEach(r -> modeloReservas.addRow(new Object[]{
                    r.getNombreInstalacion(), r.getTipoInstalacion(), r.getFechaReserva(),
                    r.getHoraInicio(), r.getHoraFin(), r.getPrecio()+" €", r.getEstado()
            }));
        }
    }

    // Solamente se cargan las instalaciones que se encuentran activas
    private void cargarInstalacionesPorTipo(TipoInstalacion tipo) {
        cmbInstalacion.removeAllItems();
        instalacionesCargadas = instalacionController.listarActivasPorTipo(tipo);
        if (instalacionesCargadas != null)
            instalacionesCargadas.forEach(i ->
                    cmbInstalacion.addItem(i.getNombreInstalacion())
            );
    }

    // =========================================================
    // LÓGICA DE RESERVA
    // =========================================================

    private void calcularPrecioEstimado() {
        try {
            int indice = cmbInstalacion.getSelectedIndex();
            if (indice == -1 || instalacionesCargadas == null) {
                mostrarError("Selecciona una instalación.");
                return;
            }
            Instalacion instalacion = instalacionesCargadas.get(indice);
            LocalTime inicio = LocalTime.parse(txtHoraInicio.getText().trim());
            LocalTime fin    = LocalTime.parse(txtHoraFin.getText().trim());

            if (!fin.isAfter(inicio)) {
                mostrarError("La hora de fin debe ser posterior a la hora de inicio.");
                return;
            }

            long minutos = java.time.Duration.between(inicio, fin).toMinutes();
            java.math.BigDecimal duracion = java.math.BigDecimal.valueOf(minutos)
                    .divide(java.math.BigDecimal.valueOf(60), 2,
                            java.math.RoundingMode.HALF_UP);
            java.math.BigDecimal precio =
                    instalacion.getPrecioHora().multiply(duracion);

            lblPrecioEstimado.setText(precio + " €");

        } catch (DateTimeParseException e) {
            mostrarError("Formato de hora incorrecto. Usa: HH:mm");
        }
    }

    private void realizarReserva() {
        try {
            int indice = cmbInstalacion.getSelectedIndex();
            if (indice == -1 || instalacionesCargadas == null) {
                mostrarError("Selecciona una instalación.");
                return;
            }

            Instalacion instalacion = instalacionesCargadas.get(indice);
            String fecha  = txtFecha.getText().trim();
            String  inicio = txtHoraInicio.getText().trim();
            String fin = txtHoraFin.getText().trim();
            String dni = Sesion.getInstancia().getDniUsuario();

            reservaController.insertar(
                    dni, instalacion.getIdInstalacion(), fecha, inicio, fin
            );

        } catch (DateTimeParseException e) {
            mostrarError("Formato de fecha u hora incorrecto.");
        }
    }

    // =========================================================
    // AUXILIARES
    // =========================================================

    private void cerrarSesion() {
        Sesion.getInstancia().cerrarSesion();
        new LoginFrame().setVisible(true);
        this.dispose();
    }

    @Override
    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje,
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje,
                "Información", JOptionPane.INFORMATION_MESSAGE);
        if (tabbedPane.getSelectedIndex() == 1)
            cargarMisReservas();
    }

}
