package com.aulacheck;

import com.aulacheck.exception.AmbienteNoDisponibleException;
import com.aulacheck.exception.ConflictoHorarioException;
import com.aulacheck.exception.PermisoDenegadoException;
import com.aulacheck.model.*;
import com.aulacheck.service.AmbienteService;
import com.aulacheck.service.IncidenciaService;
import com.aulacheck.service.ReservaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class ReservaServiceTest {

    private AmbienteService ambientes;
    private ReservaService reservas;
    private IncidenciaService incidencias;
    private Usuario admin, docente, estudiante, tecnico;
    private final LocalDate dia = LocalDate.now().plusDays(1);

    @BeforeEach
    void preparar() {
        ambientes = new AmbienteService();
        reservas = new ReservaService(ambientes);
        incidencias = new IncidenciaService(ambientes);
        admin = new Usuario(1, "Admin", "admin@uni.edu", RolUsuario.ADMINISTRADOR);
        docente = new Usuario(2, "Docente", "doc@uni.edu", RolUsuario.DOCENTE);
        estudiante = new Usuario(3, "Estudiante", "est@uni.edu", RolUsuario.ESTUDIANTE);
        tecnico = new Usuario(4, "Tecnico", "tec@uni.edu", RolUsuario.MANTENIMIENTO);
        ambientes.registrar(admin, new Aula(1, "A-101", "Pabellón A", 40, true));
    }

    @Test
    void registraReservaValida() {
        Reserva r = reservas.registrarReserva(docente, 1, dia, LocalTime.of(8, 0), LocalTime.of(10, 0));
        assertEquals(EstadoReserva.ACTIVA, r.getEstado());
        assertFalse(ambientes.buscarPorId(1).consultarDisponibilidad(dia, LocalTime.of(9, 0), LocalTime.of(9, 30)));
    }

    @Test
    void rechazaHorariosCruzados() {
        reservas.registrarReserva(docente, 1, dia, LocalTime.of(8, 0), LocalTime.of(10, 0));
        assertThrows(ConflictoHorarioException.class,
                () -> reservas.registrarReserva(estudiante, 1, dia, LocalTime.of(9, 0), LocalTime.of(11, 0)));
    }

    @Test
    void permiteReservasContiguas() {
        reservas.registrarReserva(docente, 1, dia, LocalTime.of(8, 0), LocalTime.of(10, 0));
        assertDoesNotThrow(
                () -> reservas.registrarReserva(estudiante, 1, dia, LocalTime.of(10, 0), LocalTime.of(12, 0)));
    }

    @Test
    void cancelarLiberaElHorario() {
        Reserva r = reservas.registrarReserva(docente, 1, dia, LocalTime.of(8, 0), LocalTime.of(10, 0));
        reservas.cancelarReserva(docente, r.getIdReserva());
        assertTrue(ambientes.buscarPorId(1).consultarDisponibilidad(dia, LocalTime.of(8, 0), LocalTime.of(10, 0)));
    }

    @Test
    void soloDuenoOAdminCancela() {
        Reserva r = reservas.registrarReserva(docente, 1, dia, LocalTime.of(8, 0), LocalTime.of(10, 0));
        assertThrows(PermisoDenegadoException.class, () -> reservas.cancelarReserva(estudiante, r.getIdReserva()));
        assertDoesNotThrow(() -> reservas.cancelarReserva(admin, r.getIdReserva()));
    }

    @Test
    void horaInicioDebeSerAnteriorAFin() {
        assertThrows(IllegalArgumentException.class,
                () -> reservas.registrarReserva(docente, 1, dia, LocalTime.of(10, 0), LocalTime.of(8, 0)));
    }

    @Test
    void incidenciaPonePendienteEnMantenimientoYResolverLoLibera() {
        Incidencia inc = incidencias.reportar(docente, 1, "Proyector dañado");
        assertEquals(EstadoAmbiente.EN_MANTENIMIENTO, ambientes.buscarPorId(1).getEstado());
        assertThrows(AmbienteNoDisponibleException.class,
                () -> reservas.registrarReserva(estudiante, 1, dia, LocalTime.of(8, 0), LocalTime.of(9, 0)));

        incidencias.actualizarEstado(tecnico, inc.getIdIncidencia(), EstadoIncidencia.RESUELTA);
        assertEquals(EstadoAmbiente.DISPONIBLE, ambientes.buscarPorId(1).getEstado());
    }

    @Test
    void soloPersonalAutorizadoGestionaIncidencias() {
        Incidencia inc = incidencias.reportar(estudiante, 1, "Silla rota");
        assertThrows(PermisoDenegadoException.class,
                () -> incidencias.actualizarEstado(estudiante, inc.getIdIncidencia(), EstadoIncidencia.RESUELTA));
    }

    @Test
    void soloAdminGestionaAmbientes() {
        assertThrows(PermisoDenegadoException.class,
                () -> ambientes.registrar(docente, new SalaEstudio(2, "SE-01", "Biblioteca", 6, false)));
    }

    @Test
    void historialSeOrdenaPorFechaYHora() {
        reservas.registrarReserva(docente, 1, dia, LocalTime.of(14, 0), LocalTime.of(15, 0));
        reservas.registrarReserva(docente, 1, dia, LocalTime.of(8, 0), LocalTime.of(9, 0));
        var historial = reservas.historialDeUsuario(docente);
        assertEquals(LocalTime.of(8, 0), historial.get(0).getHoraInicio());
        assertEquals(2, historial.size());
    }
    @Test
    void rechazaReservaEnFechaPasada() {
        LocalDate ayer = LocalDate.now().minusDays(1);
        assertThrows(IllegalArgumentException.class,
                () -> reservas.registrarReserva(docente, 1, ayer, LocalTime.of(8, 0), LocalTime.of(10, 0)));
    }

    @Test
    void multiplesIncidenciasMantienenEnMantenimientoHastaResolverTodas() {
        Incidencia inc1 = incidencias.reportar(docente, 1, "Proyector no enciende");
        Incidencia inc2 = incidencias.reportar(estudiante, 1, "Aire acondicionado malogrado");

        assertEquals(EstadoAmbiente.EN_MANTENIMIENTO, ambientes.buscarPorId(1).getEstado());

        // Al resolver solo una, debe seguir en mantenimiento
        incidencias.actualizarEstado(tecnico, inc1.getIdIncidencia(), EstadoIncidencia.RESUELTA);
        assertEquals(EstadoAmbiente.EN_MANTENIMIENTO, ambientes.buscarPorId(1).getEstado());

        // Recién al resolver la segunda vuelve a disponible
        incidencias.actualizarEstado(tecnico, inc2.getIdIncidencia(), EstadoIncidencia.RESUELTA);
        assertEquals(EstadoAmbiente.DISPONIBLE, ambientes.buscarPorId(1).getEstado());
    }

    @Test
    void impideCambiarADisponibleConIncidenciasPendientes() {
        incidencias.reportar(docente, 1, "Pizarra dañada");
        assertThrows(com.aulacheck.exception.AulaCheckException.class,
                () -> ambientes.cambiarEstado(admin, 1, EstadoAmbiente.DISPONIBLE));
    }

    @Test
    void reservasCanceladasNoCuentanEnUsoPorAmbiente() {
        Reserva r1 = reservas.registrarReserva(docente, 1, dia, LocalTime.of(8, 0), LocalTime.of(10, 0));
        reservas.registrarReserva(estudiante, 1, dia, LocalTime.of(10, 0), LocalTime.of(12, 0));

        reservas.cancelarReserva(docente, r1.getIdReserva());

        assertEquals(1L, reservas.usoPorAmbiente().get("A-101"));
    }

    @Test
    void noPermiteCancelarDosVecesLaMismaReserva() {
        Reserva r = reservas.registrarReserva(docente, 1, dia, LocalTime.of(8, 0), LocalTime.of(10, 0));
        reservas.cancelarReserva(docente, r.getIdReserva());

        assertThrows(com.aulacheck.exception.AulaCheckException.class,
                () -> reservas.cancelarReserva(docente, r.getIdReserva()));
    }

    @Test
    void validarRangoHorarioAlConsultarDisponibilidad() {
        assertThrows(IllegalArgumentException.class,
                () -> ambientes.listarDisponibles(dia, LocalTime.of(14, 0), LocalTime.of(10, 0)));
    }
}
