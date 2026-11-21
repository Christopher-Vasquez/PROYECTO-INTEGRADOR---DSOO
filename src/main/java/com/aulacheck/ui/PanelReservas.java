package com.aulacheck.ui;

import com.aulacheck.exception.AulaCheckException;
import com.aulacheck.model.*;
import com.aulacheck.service.AmbienteService;
import com.aulacheck.service.ReservaService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class PanelReservas extends JPanel {

    private final AmbienteService ambienteService;
    private final ReservaService reservaService;
    private final Supplier<Usuario> usuarioSupplier;
    private final Runnable onCambioGlobal;

    private final JComboBox<Ambiente> comboAmbientes = new JComboBox<>();
    private final JTextField txtFecha = new JTextField(LocalDate.now().plusDays(1).toString(), 9);
    private final JTextField txtInicio = new JTextField("10:00", 5);
    private final JTextField txtFin = new JTextField("12:00", 5);

    private final DefaultTableModel modeloReservas;
    private final JTable tablaReservas;
    private final JLabel lblEstadisticas = new JLabel("Uso por ambiente: sin reservas activas");

    public PanelReservas(AmbienteService ambienteService, ReservaService reservaService,
                         Supplier<Usuario> usuarioSupplier, Runnable onCambioGlobal) {
        this.ambienteService = ambienteService;
        this.reservaService = reservaService;
        this.usuarioSupplier = usuarioSupplier;
        this.onCambioGlobal = onCambioGlobal;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Formulario superior
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        form.setBorder(BorderFactory.createTitledBorder("Nueva Reserva"));
        JButton btnReservar = new JButton("Registrar Reserva");

        form.add(new JLabel("Ambiente:"));
        form.add(comboAmbientes);
        form.add(new JLabel("Fecha:"));
        form.add(txtFecha);
        form.add(new JLabel("Inicio:"));
        form.add(txtInicio);
        form.add(new JLabel("Fin:"));
        form.add(txtFin);
        form.add(btnReservar);
        add(form, BorderLayout.NORTH);

        // Tabla de historial
        modeloReservas = new DefaultTableModel(
                new String[]{"ID", "Ambiente", "Fecha", "Inicio", "Fin", "Usuario", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaReservas = new JTable(modeloReservas);
        tablaReservas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tablaReservas), BorderLayout.CENTER);

        // Barra inferior: estadísticas y cancelación
        JPanel pie = new JPanel(new BorderLayout(10, 10));
        pie.setBorder(BorderFactory.createEmptyBorder(4, 2, 0, 2));
        JButton btnCancelar = new JButton("Cancelar Reserva Seleccionada");

        pie.add(lblEstadisticas, BorderLayout.WEST);
        pie.add(btnCancelar, BorderLayout.EAST);
        add(pie, BorderLayout.SOUTH);

        btnReservar.addActionListener(e -> crearReserva());
        btnCancelar.addActionListener(e -> cancelarSeleccionada());
    }

    private void crearReserva() {
        Ambiente seleccionado = (Ambiente) comboAmbientes.getSelectedItem();
        if (seleccionado == null) {
            JOptionPane.showMessageDialog(this, "No hay ambientes registrados para reservar.");
            return;
        }

        try {
            LocalDate fecha = LocalDate.parse(txtFecha.getText().trim());
            LocalTime inicio = LocalTime.parse(txtInicio.getText().trim());
            LocalTime fin = LocalTime.parse(txtFin.getText().trim());

            Reserva r = reservaService.registrarReserva(
                    usuarioSupplier.get(), seleccionado.getIdAmbiente(), fecha, inicio, fin);
            JOptionPane.showMessageDialog(this, "Reserva #" + r.getIdReserva() + " registrada en " + seleccionado.getNombre() + ".");
            onCambioGlobal.run();
        } catch (AulaCheckException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo reservar", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Usa el formato AAAA-MM-DD para fecha y HH:MM para hora.", "Formato inválido", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void cancelarSeleccionada() {
        int fila = tablaReservas.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una reserva de la tabla primero.");
            return;
        }
        int idReserva = (int) modeloReservas.getValueAt(fila, 0);
        try {
            reservaService.cancelarReserva(usuarioSupplier.get(), idReserva);
            JOptionPane.showMessageDialog(this, "Reserva #" + idReserva + " cancelada.");
            onCambioGlobal.run();
        } catch (AulaCheckException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al cancelar", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void actualizarVista() {
        comboAmbientes.removeAllItems();
        ambienteService.listarTodos().forEach(comboAmbientes::addItem);

        Usuario actual = usuarioSupplier.get();
        if (actual == null) return;

        List<Reserva> historial = (actual.getRol() == RolUsuario.ADMINISTRADOR)
                ? reservaService.historialCompleto(actual)
                : reservaService.historialDeUsuario(actual);

        modeloReservas.setRowCount(0);
        for (Reserva r : historial) {
            modeloReservas.addRow(new Object[]{
                    r.getIdReserva(),
                    r.getAmbiente().getNombre(),
                    r.getFecha(),
                    r.getHoraInicio(),
                    r.getHoraFin(),
                    r.getUsuario().getNombre(),
                    r.getEstado()
            });
        }

        Map<String, Long> uso = reservaService.usoPorAmbiente();
        lblEstadisticas.setText(uso.isEmpty()
                ? "Reservas activas por ambiente: ninguna"
                : "Reservas activas por ambiente: " + uso);
    }
}
