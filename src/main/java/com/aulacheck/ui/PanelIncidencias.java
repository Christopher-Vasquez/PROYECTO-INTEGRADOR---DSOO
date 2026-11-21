package com.aulacheck.ui;

import com.aulacheck.exception.AulaCheckException;
import com.aulacheck.model.*;
import com.aulacheck.service.AmbienteService;
import com.aulacheck.service.IncidenciaService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.function.Supplier;

public class PanelIncidencias extends JPanel {

    private final AmbienteService ambienteService;
    private final IncidenciaService incidenciaService;
    private final Supplier<Usuario> usuarioSupplier;
    private final Runnable onCambioGlobal;

    private final JComboBox<Ambiente> comboAmbientes = new JComboBox<>();
    private final JTextField txtDescripcion = new JTextField(30);
    private final DefaultTableModel modeloIncidencias;
    private final JTable tablaIncidencias;
    private final JPanel panelTecnico = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));

    public PanelIncidencias(AmbienteService ambienteService, IncidenciaService incidenciaService,
                            Supplier<Usuario> usuarioSupplier, Runnable onCambioGlobal) {
        this.ambienteService = ambienteService;
        this.incidenciaService = incidenciaService;
        this.usuarioSupplier = usuarioSupplier;
        this.onCambioGlobal = onCambioGlobal;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Reportar incidencia
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        form.setBorder(BorderFactory.createTitledBorder("Reportar Incidencia"));
        JButton btnReportar = new JButton("Reportar Falla");

        form.add(new JLabel("Ambiente:"));
        form.add(comboAmbientes);
        form.add(new JLabel("Descripción:"));
        form.add(txtDescripcion);
        form.add(btnReportar);
        add(form, BorderLayout.NORTH);

        // Tabla de incidencias
        modeloIncidencias = new DefaultTableModel(
                new String[]{"ID", "Ambiente", "Descripción", "Reportado por", "Fecha", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaIncidencias = new JTable(modeloIncidencias);
        tablaIncidencias.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tablaIncidencias), BorderLayout.CENTER);

        // Controles técnicos (solo Mantenimiento o Administrador)
        JButton btnEnProceso = new JButton("Marcar EN PROCESO");
        JButton btnResolver = new JButton("Marcar RESUELTA");
        panelTecnico.add(new JLabel("Gestión técnica:"));
        panelTecnico.add(btnEnProceso);
        panelTecnico.add(btnResolver);
        add(panelTecnico, BorderLayout.SOUTH);

        btnReportar.addActionListener(e -> reportar());
        btnEnProceso.addActionListener(e -> cambiarEstado(EstadoIncidencia.EN_PROCESO));
        btnResolver.addActionListener(e -> cambiarEstado(EstadoIncidencia.RESUELTA));
    }

    private void reportar() {
        Ambiente amb = (Ambiente) comboAmbientes.getSelectedItem();
        String desc = txtDescripcion.getText().trim();
        if (amb == null || desc.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona un ambiente y describe el problema.");
            return;
        }
        try {
            incidenciaService.reportar(usuarioSupplier.get(), amb.getIdAmbiente(), desc);
            txtDescripcion.setText("");
            JOptionPane.showMessageDialog(this, "Incidencia registrada. '" + amb.getNombre() + "' pasó a EN_MANTENIMIENTO.");
            onCambioGlobal.run();
        } catch (AulaCheckException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al reportar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cambiarEstado(EstadoIncidencia nuevoEstado) {
        int fila = tablaIncidencias.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una incidencia de la tabla primero.");
            return;
        }
        int idIncidencia = (int) modeloIncidencias.getValueAt(fila, 0);
        try {
            incidenciaService.actualizarEstado(usuarioSupplier.get(), idIncidencia, nuevoEstado);
            onCambioGlobal.run();
        } catch (AulaCheckException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Acceso denegado", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void actualizarVista() {
        comboAmbientes.removeAllItems();
        ambienteService.listarTodos().forEach(comboAmbientes::addItem);

        Usuario actual = usuarioSupplier.get();
        panelTecnico.setVisible(actual != null && actual.getRol().puedeGestionarIncidencias());

        modeloIncidencias.setRowCount(0);
        for (Incidencia inc : incidenciaService.listarTodas()) {
            modeloIncidencias.addRow(new Object[]{
                    inc.getIdIncidencia(),
                    inc.getAmbiente().getNombre(),
                    inc.getDescripcion(),
                    inc.getReportadoPor().getNombre(),
                    inc.getFechaReporte(),
                    inc.getEstado()
            });
        }
    }
}