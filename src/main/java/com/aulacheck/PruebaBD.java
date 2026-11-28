package com.aulacheck;

import com.aulacheck.model.Aula;
import com.aulacheck.model.Ambiente;
import com.aulacheck.model.EstadoIncidencia;
import com.aulacheck.model.EstadoReserva;
import com.aulacheck.model.Incidencia;
import com.aulacheck.model.Reserva;
import com.aulacheck.model.RolUsuario;
import com.aulacheck.model.Usuario;
import com.aulacheck.persistence.jdbc.AmbienteJdbcRepository;
import com.aulacheck.persistence.jdbc.UsuarioJdbcRepository;
import com.aulacheck.persistence.jdbc.ReservaJdbcRepository;
import com.aulacheck.persistence.jdbc.IncidenciaJdbcRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class PruebaBD {

    public static void main(String[] args) throws SQLException {
        UsuarioJdbcRepository usuarioRepo = new UsuarioJdbcRepository();
        AmbienteJdbcRepository ambienteRepo = new AmbienteJdbcRepository();
        ReservaJdbcRepository reservaRepo = new ReservaJdbcRepository();
        IncidenciaJdbcRepository incidenciaRepo = new IncidenciaJdbcRepository();

        Usuario usuario = new Usuario(9001, "Usuario de prueba",
                "prueba@aulacheck.edu", RolUsuario.ESTUDIANTE);

        Ambiente ambiente = new Aula(9001, "Aula de prueba",
                "Pabellon A", 30, true);

        Reserva reserva = Reserva.reconstruir(
                9001,
                LocalDate.now().plusDays(1),
                LocalTime.of(8, 0),
                LocalTime.of(10, 0),
                EstadoReserva.ACTIVA,
                usuario,
                ambiente
        );

        Incidencia incidencia = Incidencia.reconstruir(
                9001,
                "Incidencia de prueba JDBC",
                LocalDate.now(),
                EstadoIncidencia.ABIERTA,
                usuario,
                ambiente
        );

        List<Usuario> usuarios = List.of(usuario);
        List<Ambiente> ambientes = List.of(ambiente);
        List<Reserva> reservas = List.of(reserva);
        List<Incidencia> incidencias = List.of(incidencia);

        usuarioRepo.guardarTodo(usuarios);
        ambienteRepo.guardarTodo(ambientes);
        reservaRepo.guardarTodo(reservas);
        incidenciaRepo.guardarTodo(incidencias);

        System.out.println("DATOS GUARDADOS EN POSTGRESQL");

        List<Usuario> usuariosCargados = usuarioRepo.cargarTodo();
        List<Ambiente> ambientesCargados = ambienteRepo.cargarTodo();
        List<Reserva> reservasCargadas =
                reservaRepo.cargarTodo(usuariosCargados, ambientesCargados);
        List<Incidencia> incidenciasCargadas =
                incidenciaRepo.cargarTodo(usuariosCargados, ambientesCargados);

        System.out.println("\nUSUARIOS:");
        usuariosCargados.forEach(System.out::println);

        System.out.println("\nAMBIENTES:");
        ambientesCargados.forEach(System.out::println);

        System.out.println("\nRESERVAS:");
        reservasCargadas.forEach(System.out::println);

        System.out.println("\nINCIDENCIAS:");
        incidenciasCargadas.forEach(System.out::println);

        System.out.println("\nPRUEBA JDBC FINALIZADA");
    }
}
