package com.aulacheck.ui;

import com.aulacheck.model.Usuario;
import com.aulacheck.service.AmbienteService;
import com.aulacheck.service.IncidenciaService;
import com.aulacheck.service.ReservaService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AulaCheckFrame extends JFrame {

    private Usuario usuarioActual;

    private final JComboBox<Usuario> comboUsuarios;
    private final JLabel lblInfoUsuario;
    private final PanelAmbientes panelAmbientes;
    private final PanelReservas panelReservas;
    private final PanelIncidencias panelIncidencias;

    public AulaCheckFrame(AmbienteService ambienteService,
                          ReservaService reservaService,
                          IncidenciaService incidenciaService,
                          List<Usuario> usuarios) {
        this.usuarioActual = usuarios.get(0);

        setTitle("AulaCheck - Gestión de Ambientes Universitarios");
        setSize(1020, 660);
        setMinimumSize(new Dimension(900, 560));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        // Barra superior de sesión / cambio de rol
        JPanel barraSuperior = new JPanel(new BorderLayout(10, 10));
        barraSuperior.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        JPanel izquierdaSesion = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        izquierdaSesion.add(new JLabel("Usuario activo:"));
        comboUsuarios = new JComboBox<>(usuarios.toArray(new Usuario[0]));
        izquierdaSesion.add(comboUsuarios);

        lblInfoUsuario = new JLabel();
        lblInfoUsuario.setFont(new Font("SansSerif", Font.BOLD, 12));
        izquierdaSesion.add(lblInfoUsuario);

        JButton btnRefrescar = new JButton("Actualizar datos");
        btnRefrescar.addActionListener(e -> refrescarTodo());

        barraSuperior.add(izquierdaSesion, BorderLayout.WEST);
        barraSuperior.add(btnRefrescar, BorderLayout.EAST);
        add(barraSuperior, BorderLayout.NORTH);

        // Paneles funcionales
        panelAmbientes = new PanelAmbientes(ambienteService, this::getUsuarioActual, this::refrescarTodo);
        panelReservas = new PanelReservas(ambienteService, reservaService, this::getUsuarioActual, this::refrescarTodo);
        panelIncidencias = new PanelIncidencias(ambienteService, incidenciaService, this::getUsuarioActual, this::refrescarTodo);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Ambientes y Disponibilidad", panelAmbientes);
        pestanas.addTab("Reservas e Historial", panelReservas);
        pestanas.addTab("Incidencias y Mantenimiento", panelIncidencias);
        add(pestanas, BorderLayout.CENTER);

        comboUsuarios.addActionListener(e -> {
            usuarioActual = (Usuario) comboUsuarios.getSelectedItem();
            actualizarSesion();
        });

        actualizarSesion();
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    private void actualizarSesion() {
        if (usuarioActual != null) {
            lblInfoUsuario.setText("  |  Rol: " + usuarioActual.getRol() + "  (" + usuarioActual.getEmail() + ")");
            refrescarTodo();
        }
    }

    public void refrescarTodo() {
        panelAmbientes.actualizarVista();
        panelReservas.actualizarVista();
        panelIncidencias.actualizarVista();
    }
}