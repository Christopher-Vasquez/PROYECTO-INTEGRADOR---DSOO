package com.aulacheck;

import com.aulacheck.model.*;
import com.aulacheck.persistence.json.AmbienteJsonRepository;
import com.aulacheck.persistence.json.IncidenciaJsonRepository;
import com.aulacheck.persistence.json.ReservaJsonRepository;
import com.aulacheck.persistence.json.UsuarioJsonRepository;
import com.aulacheck.service.AmbienteService;
import com.aulacheck.service.IncidenciaService;
import com.aulacheck.service.ReservaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Verifica que lo que se carga desde JSON queda idéntico a lo que se guardó. */
class PersistenciaIntegridadTest {

    private Usuario docente;
    private Aula aula;

    @BeforeEach
    void preparar() {
        docente = new Usuario(2, "Luis Prado", "luis@uni.edu", RolUsuario.DOCENTE);
        aula = new Aula(1, "A-101", "Pabellón A", 40, true);
    }

    @Test
    void reconstruirIncidenciaResueltaNoAlteraElEstadoGuardadoDelAmbiente() {
        // El administrador dejó el ambiente EN_MANTENIMIENTO manualmente (con una incidencia ya resuelta).
        aula.setEstado(EstadoAmbiente.EN_MANTENIMIENTO);

        Incidencia.reconstruir(1, "Falla antigua", LocalDate.of(2026, 1, 10),
                EstadoIncidencia.RESUELTA, docente, aula);

        assertEquals(EstadoAmbiente.EN_MANTENIMIENTO, aula.getEstado());
    }

    @Test
    void reconstruirIncidenciaPendienteConservaElEstadoGuardado() {
        aula.setEstado(EstadoAmbiente.EN_MANTENIMIENTO);

        Incidencia inc = Incidencia.reconstruir(1, "Proyector dañado", LocalDate.of(2026, 11, 20),
                EstadoIncidencia.ABIERTA, docente, aula);

        assertEquals(EstadoAmbiente.EN_MANTENIMIENTO, aula.getEstado());
        assertEquals(LocalDate.of(2026, 11, 20), inc.getFechaReporte());
        assertEquals(1, docente.getIncidencias().size());
        assertEquals(1, aula.getIncidencias().size());
    }

    @Test
    void reconstruirReservaCanceladaConservaEstadoYRelaciones() {
        LocalDate dia = LocalDate.now().plusDays(2);

        Reserva r = Reserva.reconstruir(5, dia, LocalTime.of(8, 0), LocalTime.of(10, 0),
                EstadoReserva.CANCELADA, docente, aula);

        assertEquals(EstadoReserva.CANCELADA, r.getEstado());
        assertEquals(1, docente.getReservas().size());
        assertEquals(1, aula.getReservas().size());
        assertTrue(aula.consultarDisponibilidad(dia, LocalTime.of(8, 0), LocalTime.of(10, 0)));
    }

    @Test
    void despuesDeCargarLosIdsContinuanSinRepetirse() {
        AmbienteService ambientes = new AmbienteService();
        ReservaService reservas = new ReservaService(ambientes);
        IncidenciaService incidencias = new IncidenciaService(ambientes);
        LocalDate dia = LocalDate.now().plusDays(2);

        ambientes.cargarDesdePersistencia(List.of(aula));
        reservas.cargarDesdePersistencia(List.of(Reserva.reconstruir(5, dia, LocalTime.of(8, 0),
                LocalTime.of(10, 0), EstadoReserva.ACTIVA, docente, aula)));
        incidencias.cargarDesdePersistencia(List.of(Incidencia.reconstruir(3, "Falla antigua",
                LocalDate.of(2026, 1, 10), EstadoIncidencia.RESUELTA, docente, aula)));

        Reserva nueva = reservas.registrarReserva(docente, 1, dia, LocalTime.of(10, 0), LocalTime.of(12, 0));
        Incidencia nuevaInc = incidencias.reportar(docente, 1, "Mesa rota");

        assertEquals(6, nueva.getIdReserva());
        assertEquals(4, nuevaInc.getIdIncidencia());
    }

    @Test
    void jsonGuardaYCargaLosCuatroArchivosEntreSesiones(@TempDir Path carpeta) {
        LocalDate dia = LocalDate.now().plusDays(3);
        docente.crearReserva(1, aula, dia, LocalTime.of(14, 0), LocalTime.of(16, 0));
        docente.reportarIncidencia(1, aula, "Proyector dañado");

        UsuarioJsonRepository usuarioRepo = new UsuarioJsonRepository(carpeta.resolve("usuarios.json"));
        AmbienteJsonRepository ambienteRepo = new AmbienteJsonRepository(carpeta.resolve("ambientes.json"));
        ReservaJsonRepository reservaRepo = new ReservaJsonRepository(carpeta.resolve("reservas.json"));
        IncidenciaJsonRepository incidenciaRepo = new IncidenciaJsonRepository(carpeta.resolve("incidencias.json"));

        // "Cerrar" la aplicación: guardar todo
        usuarioRepo.guardarTodos(List.of(docente));
        ambienteRepo.guardarTodos(List.of(aula));
        reservaRepo.guardarTodas(aula.getReservas());
        incidenciaRepo.guardarTodas(aula.getIncidencias());

        // "Abrir" otra sesión: cargar todo
        List<Usuario> usuarios = usuarioRepo.cargarTodos();
        List<Ambiente> ambientes = ambienteRepo.cargarTodos();
        List<Reserva> reservas = reservaRepo.cargarTodas(usuarios, ambientes);
        List<Incidencia> incidencias = incidenciaRepo.cargarTodas(usuarios, ambientes);

        assertEquals(1, usuarios.size());
        assertEquals(1, ambientes.size());
        assertEquals(1, reservas.size());
        assertEquals(1, incidencias.size());
        assertEquals(dia, reservas.get(0).getFecha());
        assertEquals("Proyector dañado", incidencias.get(0).getDescripcion());
        assertEquals(EstadoAmbiente.EN_MANTENIMIENTO, ambientes.get(0).getEstado());
        assertEquals(1, usuarios.get(0).getReservas().size());
    }
}
