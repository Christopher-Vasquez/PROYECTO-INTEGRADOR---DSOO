package com.aulacheck.ui;

import com.aulacheck.exception.AulaCheckException;
import com.aulacheck.model.Ambiente;
import com.aulacheck.model.Incidencia;
import com.aulacheck.model.Reserva;
import com.aulacheck.model.Usuario;
import com.aulacheck.persistence.json.AmbienteJsonRepository;
import com.aulacheck.persistence.json.IncidenciaJsonRepository;
import com.aulacheck.persistence.json.ReservaJsonRepository;
import com.aulacheck.persistence.json.UsuarioJsonRepository;
import com.aulacheck.service.AmbienteService;
import com.aulacheck.service.IncidenciaService;
import com.aulacheck.service.ReservaService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AulaCheckFrame extends JFrame {

    private final AmbienteService ambienteService;
    private final ReservaService reservaService;
    private final IncidenciaService incidenciaService;
    private final List<Usuario> usuarios;

    private Usuario usuarioActual;

    private final JComboBox<Usuario> comboUsuarios;
    private final JLabel lblInfoUsuario;
    private final PanelAmbientes panelAmbientes;
    private final PanelReservas panelReservas;
    private final PanelIncidencias panelIncidencias;

    /** true si los archivos JSON existían pero no se pudieron cargar. */
    private boolean cargaFallida = false;

    private final UsuarioJsonRepository usuarioRepo = new UsuarioJsonRepository(Path.of("data", "usuarios.json"));
    private final AmbienteJsonRepository ambienteRepo = new AmbienteJsonRepository(Path.of("data", "ambientes.json"));
    private final ReservaJsonRepository reservaRepo = new ReservaJsonRepository(Path.of("data", "reservas.json"));
    private final IncidenciaJsonRepository incidenciaRepo = new IncidenciaJsonRepository(Path.of("data", "incidencias.json"));

    public AulaCheckFrame(AmbienteService ambienteService,
                          ReservaService reservaService,
                          IncidenciaService incidenciaService,
                          List<Usuario> usuariosIniciales) {
        this.ambienteService = ambienteService;
        this.reservaService = reservaService;
        this.incidenciaService = incidenciaService;
        this.usuarios = new ArrayList<>(usuariosIniciales);

        // Cargar datos guardados antes de armar el combo y las tablas
        cargarDatosAlIniciar();

        this.usuarioActual = this.usuarios.get(0);

        setTitle("AulaCheck - Gestión de Ambientes Universitarios");
        setSize(1020, 660);
        setMinimumSize(new Dimension(900, 560));
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
        comboUsuarios = new JComboBox<>(this.usuarios.toArray(new Usuario[0]));
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

        // Guardar automáticamente en json al cerrar la ventana
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                guardarDatosAlCerrar();
                dispose();
                System.exit(0);
            }
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

    private void cargarDatosAlIniciar() {
        Path rutaUsuarios = Path.of("data", "usuarios.json");
        Path rutaAmbientes = Path.of("data", "ambientes.json");
        Path rutaReservas = Path.of("data", "reservas.json");
        Path rutaIncidencias = Path.of("data", "incidencias.json");

        if (!Files.exists(rutaUsuarios) || !Files.exists(rutaAmbientes)) {
            guardarDatosAlCerrar();
            return;
        }

        try {
            List<Usuario> usuariosCargados = usuarioRepo.cargarTodos();
            List<Ambiente> ambientesCargados = ambienteRepo.cargarTodos();

            List<Reserva> reservasCargadas = Files.exists(rutaReservas)
                    ? reservaRepo.cargarTodas(usuariosCargados, ambientesCargados)
                    : List.of();

            List<Incidencia> incidenciasCargadas = Files.exists(rutaIncidencias)
                    ? incidenciaRepo.cargarTodas(usuariosCargados, ambientesCargados)
                    : List.of();

            this.usuarios.clear();
            this.usuarios.addAll(usuariosCargados);
            ambienteService.cargarDesdePersistencia(ambientesCargados);
            reservaService.cargarDesdePersistencia(reservasCargadas);
            incidenciaService.cargarDesdePersistencia(incidenciasCargadas);
        } catch (AulaCheckException e) {
            cargaFallida = true;
            JOptionPane.showMessageDialog(this,
                    "No se pudieron cargar los datos guardados: " + e.getMessage()
                            + "\nAl cerrar se conservará una copia .bak de los archivos originales.",
                    "Aviso de persistencia",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    /** Si la carga falló, guarda copias .bak de los archivos originales antes de sobrescribirlos. */
    private void respaldarArchivosOriginales() {
        for (String nombre : List.of("usuarios", "ambientes", "reservas", "incidencias")) {
            Path origen = Path.of("data", nombre + ".json");
            if (Files.exists(origen)) {
                try {
                    Files.copy(origen, Path.of("data", nombre + ".json.bak"), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ignorada) {
                    // Si no se puede respaldar, igualmente se continúa.
                }
            }
        }
    }

    private void guardarDatosAlCerrar() {
        if (cargaFallida) {
            respaldarArchivosOriginales();
            cargaFallida = false;
        }
        try {
            List<Ambiente> ambientesActuales = ambienteService.listarTodos();

            List<Reserva> todasReservas = new ArrayList<>();
            for (Ambiente a : ambientesActuales) {
                todasReservas.addAll(a.getReservas());
            }
            todasReservas.sort(Comparator.comparingInt(Reserva::getIdReserva));

            usuarioRepo.guardarTodos(usuarios);
            ambienteRepo.guardarTodos(ambientesActuales);
            reservaRepo.guardarTodas(todasReservas);
            incidenciaRepo.guardarTodas(incidenciaService.listarTodas());
        } catch (AulaCheckException e) {
            JOptionPane.showMessageDialog(this,
                    "Error al guardar los datos en JSON: " + e.getMessage(),
                    "Error de persistencia",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}