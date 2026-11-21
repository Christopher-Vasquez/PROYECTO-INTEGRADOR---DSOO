package com.aulacheck.ui;

import com.aulacheck.exception.AulaCheckException;
import com.aulacheck.model.*;
import com.aulacheck.service.AmbienteService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.function.Supplier;

public class PanelAmbientes extends JPanel {

    private final AmbienteService ambienteService;
    private final Supplier<Usuario> usuarioSupplier;
    private final Runnable onCambioGlobal;

    private final DefaultTableModel modeloTabla;
    private final JTable tablaAmbientes;
    private final JTextField txtFecha = new JTextField(LocalDate.now().plusDays(1).toString(), 9);
    private final JTextField txtInicio = new JTextField("10:00", 5);
    private final JTextField txtFin = new JTextField("12:00", 5);

    // Controles exclusivos para Administrador
    private final JPanel panelAdmin;
    private final JComboBox<String> comboTipo = new JComboBox<>(new String[]{"Aula", "Laboratorio", "Sala de Estudio"});
    private final JTextField txtId = new JTextField(4);
    private final JTextField txtNombre = new JTextField(8);
    private final JTextField txtUbicacion = new JTextField(12);
    private final JTextField txtCapacidad = new JTextField(4);
    private final JCheckBox chkEquipamiento = new JCheckBox("Tiene proyector", true);
    private final JTextField txtTipoLab = new JTextField("Cómputo", 8);
    private final JLabel lblTipoLab = new JLabel("Especialidad:");

    public PanelAmbientes(AmbienteService ambienteService, Supplier<Usuario> usuarioSupplier, Runnable onCambioGlobal) {
        this.ambienteService = ambienteService;
        this.usuarioSupplier = usuarioSupplier;
        this.onCambioGlobal = onCambioGlobal;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Barra superior de consulta de disponibilidad
        JPanel panelFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        panelFiltro.setBorder(BorderFactory.createTitledBorder("Consultar Disponibilidad"));
        JButton btnBuscarDisponibles = new JButton("Filtrar Disponibles");
        JButton btnVerTodos = new JButton("Mostrar Todos");

        panelFiltro.add(new JLabel("Fecha (AAAA-MM-DD):"));
        panelFiltro.add(txtFecha);
        panelFiltro.add(new JLabel("Inicio (HH:MM):"));
        panelFiltro.add(txtInicio);
        panelFiltro.add(new JLabel("Fin (HH:MM):"));
        panelFiltro.add(txtFin);
        panelFiltro.add(btnBuscarDisponibles);
        panelFiltro.add(btnVerTodos);
        add(panelFiltro, BorderLayout.NORTH);

        // Tabla de ambientes
        modeloTabla = new DefaultTableModel(
                new String[]{"ID", "Nombre", "Tipo", "Equipamiento", "Ubicación", "Capacidad", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaAmbientes = new JTable(modeloTabla);
        tablaAmbientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tablaAmbientes), BorderLayout.CENTER);

        // Panel inferior de gestión (solo Administrador)
        panelAdmin = new JPanel(new GridLayout(2, 1, 4, 4));
        panelAdmin.setBorder(BorderFactory.createTitledBorder("Gestión de Ambientes (Solo Administrador)"));

        JPanel filaRegistro = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        JButton btnRegistrar = new JButton("Registrar Ambiente");

        filaRegistro.add(new JLabel("Tipo:"));
        filaRegistro.add(comboTipo);
        filaRegistro.add(new JLabel("ID:"));
        filaRegistro.add(txtId);
        filaRegistro.add(new JLabel("Nombre:"));
        filaRegistro.add(txtNombre);
        filaRegistro.add(new JLabel("Ubicación:"));
        filaRegistro.add(txtUbicacion);
        filaRegistro.add(new JLabel("Cap:"));
        filaRegistro.add(txtCapacidad);
        filaRegistro.add(chkEquipamiento);
        filaRegistro.add(lblTipoLab);
        filaRegistro.add(txtTipoLab);
        filaRegistro.add(btnRegistrar);

        JPanel filaAcciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        JComboBox<EstadoAmbiente> comboEstado = new JComboBox<>(EstadoAmbiente.values());
        JButton btnCambiarEstado = new JButton("Cambiar Estado Seleccionado");
        JButton btnEliminar = new JButton("Eliminar Seleccionado");

        filaAcciones.add(new JLabel("Acción sobre fila seleccionada -> Nuevo estado:"));
        filaAcciones.add(comboEstado);
        filaAcciones.add(btnCambiarEstado);
        filaAcciones.add(btnEliminar);

        panelAdmin.add(filaRegistro);
        panelAdmin.add(filaAcciones);
        add(panelAdmin, BorderLayout.SOUTH);

        // Eventos
        comboTipo.addActionListener(e -> actualizarCamposTipo());
        btnVerTodos.addActionListener(e -> cargarTabla(ambienteService.listarTodos()));
        btnBuscarDisponibles.addActionListener(e -> filtrarDisponibles());
        btnRegistrar.addActionListener(e -> registrarAmbiente());
        btnCambiarEstado.addActionListener(e -> cambiarEstadoSeleccionado((EstadoAmbiente) comboEstado.getSelectedItem()));
        btnEliminar.addActionListener(e -> eliminarSeleccionado());

        actualizarCamposTipo();
    }

    private void actualizarCamposTipo() {
        String tipo = (String) comboTipo.getSelectedItem();
        boolean esLab = "Laboratorio".equals(tipo);
        chkEquipamiento.setVisible(!esLab);
        lblTipoLab.setVisible(esLab);
        txtTipoLab.setVisible(esLab);
        if ("Aula".equals(tipo)) {
            chkEquipamiento.setText("Tiene proyector");
        } else if ("Sala de Estudio".equals(tipo)) {
            chkEquipamiento.setText("Tiene pizarra");
        }
    }

    private void filtrarDisponibles() {
        try {
            LocalDate fecha = LocalDate.parse(txtFecha.getText().trim());
            LocalTime inicio = LocalTime.parse(txtInicio.getText().trim());
            LocalTime fin = LocalTime.parse(txtFin.getText().trim());
            cargarTabla(ambienteService.listarDisponibles(fecha, inicio, fin));
        } catch (AulaCheckException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación de búsqueda", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Usa el formato AAAA-MM-DD para fecha y HH:MM para hora.", "Formato inválido", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void registrarAmbiente() {
        try {
            int id = Integer.parseInt(txtId.getText().trim());
            String nombre = txtNombre.getText().trim();
            String ubicacion = txtUbicacion.getText().trim();
            int cap = Integer.parseInt(txtCapacidad.getText().trim());
            String tipo = (String) comboTipo.getSelectedItem();

            Ambiente nuevo = switch (tipo) {
                case "Aula" -> new Aula(id, nombre, ubicacion, cap, chkEquipamiento.isSelected());
                case "Laboratorio" -> new Laboratorio(id, nombre, ubicacion, cap, cap, txtTipoLab.getText().trim());
                default -> new SalaEstudio(id, nombre, ubicacion, cap, chkEquipamiento.isSelected());
            };

            ambienteService.registrar(usuarioSupplier.get(), nuevo);
            limpiarFormulario();
            JOptionPane.showMessageDialog(this, "Ambiente '" + nombre + "' registrado.");
            onCambioGlobal.run();
        } catch (AulaCheckException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo registrar", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Verifica que ID y Capacidad sean números enteros.", "Dato inválido", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void cambiarEstadoSeleccionado(EstadoAmbiente nuevoEstado) {
        int fila = tablaAmbientes.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un ambiente en la tabla primero.");
            return;
        }
        int idAmbiente = (int) modeloTabla.getValueAt(fila, 0);
        try {
            ambienteService.cambiarEstado(usuarioSupplier.get(), idAmbiente, nuevoEstado);
            onCambioGlobal.run();
        } catch (AulaCheckException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Operación bloqueada", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarSeleccionado() {
        int fila = tablaAmbientes.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un ambiente en la tabla primero.");
            return;
        }
        int idAmbiente = (int) modeloTabla.getValueAt(fila, 0);
        try {
            ambienteService.eliminar(usuarioSupplier.get(), idAmbiente);
            onCambioGlobal.run();
        } catch (AulaCheckException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se puede eliminar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarFormulario() {
        txtId.setText("");
        txtNombre.setText("");
        txtUbicacion.setText("");
        txtCapacidad.setText("");
    }

    private String describirEquipamiento(Ambiente a) {
        if (a instanceof Aula aula) {
            return aula.isTieneProyector() ? "Con proyector" : "Sin proyector";
        }
        if (a instanceof Laboratorio lab) {
            return lab.getNumEquipos() + " equipos (" + lab.getTipoLaboratorio() + ")";
        }
        if (a instanceof SalaEstudio sala) {
            return sala.isTienePizarra() ? "Con pizarra" : "Sin pizarra";
        }
        return "-";
    }

    private void cargarTabla(List<Ambiente> lista) {
        modeloTabla.setRowCount(0);
        for (Ambiente a : lista) {
            modeloTabla.addRow(new Object[]{
                    a.getIdAmbiente(),
                    a.getNombre(),
                    a.getTipo(),
                    describirEquipamiento(a),
                    a.getUbicacion(),
                    a.getCapacidad(),
                    a.getEstado()
            });
        }
    }

    public void actualizarVista() {
        Usuario u = usuarioSupplier.get();
        panelAdmin.setVisible(u != null && u.getRol().puedeGestionarAmbientes());
        cargarTabla(ambienteService.listarTodos());
    }
}
