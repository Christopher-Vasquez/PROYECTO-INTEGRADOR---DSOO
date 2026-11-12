package com.aulacheck;

import com.aulacheck.exception.AulaCheckException;
import com.aulacheck.model.*;
import com.aulacheck.service.AmbienteService;
import com.aulacheck.service.IncidenciaService;
import com.aulacheck.service.ReservaService;

import java.time.LocalDate;
import java.time.LocalTime;

/** Demostración de los casos de uso de AulaCheck por consola. */
public class Main {

    public static void main(String[] args) {
        AmbienteService ambientes = new AmbienteService();
        ReservaService reservas = new ReservaService(ambientes);
        IncidenciaService incidencias = new IncidenciaService(ambientes);

        Usuario admin = new Usuario(1, "Carla Ríos", "carla@uni.edu", RolUsuario.ADMINISTRADOR);
        Usuario docente = new Usuario(2, "Luis Prado", "luis@uni.edu", RolUsuario.DOCENTE);
        Usuario estudiante = new Usuario(3, "Ana Torres", "ana@uni.edu", RolUsuario.ESTUDIANTE);
        Usuario tecnico = new Usuario(4, "Mario Silva", "mario@uni.edu", RolUsuario.MANTENIMIENTO);

        titulo("1. Gestionar ambientes (administrador)");
        ambientes.registrar(admin, new Aula(1, "A-101", "Pabellón A, piso 1", 40, true));
        ambientes.registrar(admin, new Laboratorio(2, "LAB-REDES", "Pabellón B, piso 2", 25, 25, "Redes"));
        ambientes.registrar(admin, new SalaEstudio(3, "SE-01", "Biblioteca, piso 3", 8, true));
        ambientes.listarTodos().forEach(System.out::println);

        LocalDate manana = LocalDate.now().plusDays(1);

        titulo("2. Consultar disponibilidad (" + manana + " 10:00-12:00)");
        ambientes.listarDisponibles(manana, LocalTime.of(10, 0), LocalTime.of(12, 0))
                .forEach(System.out::println);

        titulo("3. Registrar reserva");
        System.out.println(reservas.registrarReserva(docente, 1, manana, LocalTime.of(10, 0), LocalTime.of(12, 0)));

        titulo("4. Validar conflicto de horarios (debe fallar)");
        try {
            reservas.registrarReserva(estudiante, 1, manana, LocalTime.of(11, 0), LocalTime.of(13, 0));
        } catch (AulaCheckException e) {
            System.out.println("Rechazada -> " + e.getMessage());
        }
        System.out.println(reservas.registrarReserva(estudiante, 1, manana, LocalTime.of(12, 0), LocalTime.of(14, 0)));

        titulo("5. Registrar y gestionar incidencia");
        Incidencia inc = incidencias.reportar(docente, 1, "El proyector no enciende");
        System.out.println(inc);
        System.out.println("Estado del ambiente: " + ambientes.buscarPorId(1).getEstado());
        try {
            reservas.registrarReserva(estudiante, 1, manana, LocalTime.of(15, 0), LocalTime.of(16, 0));
        } catch (AulaCheckException e) {
            System.out.println("Rechazada -> " + e.getMessage());
        }
        incidencias.actualizarEstado(tecnico, inc.getIdIncidencia(), EstadoIncidencia.RESUELTA);
        System.out.println("Tras resolver: " + ambientes.buscarPorId(1).getEstado());

        titulo("6. Consultar historial de reservas");
        reservas.historialDeUsuario(docente).forEach(System.out::println);
        System.out.println("--- Historial completo (administrador) ---");
        reservas.historialCompleto(admin).forEach(System.out::println);
        System.out.println("--- Uso por ambiente ---");
        reservas.usoPorAmbiente().forEach((nombre, n) -> System.out.println(nombre + ": " + n + " reserva(s)"));
    }

    private static void titulo(String texto) {
        System.out.println("\n=== " + texto + " ===");
    }
}
