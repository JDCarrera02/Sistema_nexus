package View;

import Util.Sesion;

import javax.swing.*;
import java.awt.*;

public class ClienteFrame extends JFrame implements VistaCliente{
    public ClienteFrame() {
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        setTitle("Area Cliente");
        setSize(500, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Panel superior
        JPanel panelSuperior = new JPanel(new BorderLayout());
        JLabel lblBienvenida = new JLabel(
                "Bienvenido, " + Sesion.getInstancia().getDniUsuario(),
                SwingConstants.LEFT
        );
        lblBienvenida.setFont(new Font("Arial", Font.BOLD, 13));
        JButton btnCerrarSesion = new JButton("Cerrar sesión");
        btnCerrarSesion.addActionListener(e -> cerrarSesion());
        panelSuperior.add(lblBienvenida, BorderLayout.WEST);
        panelSuperior.add(btnCerrarSesion, BorderLayout.EAST);

        // Mensaje informativo
        JPanel panelInfo = new JPanel(new GridLayout(4, 1, 5, 5));

        JLabel lblTitulo = new JLabel(
                "Área de cliente", SwingConstants.CENTER
        );
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));

        JLabel lblInfo1 = new JLabel("Como cliente puedes consultar información del club.", SwingConstants.CENTER);

        JLabel lblInfo2 = new JLabel("Para acceder a reservas y más beneficios,", SwingConstants.CENTER);

        JLabel lblInfo3 = new JLabel("contacta con el administrador para hacerte socio.", SwingConstants.CENTER);

        lblInfo3.setForeground(new Color(0, 100, 0));
        lblInfo3.setFont(new Font("Arial", Font.BOLD, 11));

        panelInfo.add(lblTitulo);
        panelInfo.add(lblInfo1);
        panelInfo.add(lblInfo2);
        panelInfo.add(lblInfo3);

        panel.add(panelSuperior, BorderLayout.NORTH);
        panel.add(panelInfo, BorderLayout.CENTER);

        add(panel);
    }

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
    }
}
