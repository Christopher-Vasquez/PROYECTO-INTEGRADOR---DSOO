package com.aulacheck;

import com.aulacheck.model.*;
import com.aulacheck.service.AmbienteService;
import com.aulacheck.service.IncidenciaService;
import com.aulacheck.service.ReservaService;
import com.aulacheck.ui.AulaCheckFrame;

import javax.swing.*;
import java.util.List;

/** Punto de entrada de AulaCheck (interfaz gráfica Swing). */
public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        AmbienteService ambientes = new AmbienteService();
        ReservaService reservas = new ReservaService(ambientes);
        IncidenciaService incidencias = new IncidenciaService(ambientes);

        Usuario admin = new Usuario(1, "Carla Ríos", "carla@uni.edu", RolUsuario.ADMINISTRADOR);
        Usuario docente = new Usuario(2, "Luis Prado", "luis@uni.edu", RolUsuario.DOCENTE);
        Usuario estudiante = new Usuario(3, "Ana Torres", "ana@uni.edu", RolUsuario.ESTUDIANTE);
        Usuario tecnico = new Usuario(4, "Mario Silva", "mario@uni.edu", RolUsuario.MANTENIMIENTO);

        // Datos iniciales de demostración
        ambientes.registrar(admin, new Aula(1, "A-101", "Pabellón A, piso 1", 40, true));
        ambientes.registrar(admin, new Laboratorio(2, "LAB-REDES", "Pabellón B, piso 2", 25, 25, "Redes"));
        ambientes.registrar(admin, new SalaEstudio(3, "SE-01", "Biblioteca, piso 3", 8, true));

        List<Usuario> usuarios = List.of(admin, docente, estudiante, tecnico);

        SwingUtilities.invokeLater(() -> {
            AulaCheckFrame frame = new AulaCheckFrame(ambientes, reservas, incidencias, usuarios);
            frame.setVisible(true);
        });
    }
}
