package com.aulacheck.service;

import com.aulacheck.exception.AulaCheckException;
import com.aulacheck.exception.PermisoDenegadoException;
import com.aulacheck.model.Ambiente;
import com.aulacheck.model.EstadoReserva;
import com.aulacheck.model.Reserva;
import com.aulacheck.model.RolUsuario;
import com.aulacheck.model.Usuario;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Casos de uso: Registrar reserva, Validar conflicto de horarios y Consultar historial de reservas. */
public class ReservaService {

    private final AmbienteService ambienteService;
    private final Map<Integer, Reserva> reservas = new LinkedHashMap<>();
    private int siguienteId = 1;

    public ReservaService(AmbienteService ambienteService) {
        this.ambienteService = ambienteService;
    }

    public Reserva registrarReserva(Usuario usuario, int idAmbiente, LocalDate fecha,
                                    LocalTime horaInicio, LocalTime horaFin) {
        Ambiente ambiente = ambienteService.buscarPorId(idAmbiente);
        Reserva reserva = usuario.crearReserva(siguienteId, ambiente, fecha, horaInicio, horaFin);
        reservas.put(siguienteId, reserva);
        siguienteId++;
        return reserva;
    }

    /** Solo puede cancelar quien la hizo o un administrador. */
    public void cancelarReserva(Usuario actor, int idReserva) {
        Reserva reserva = buscarPorId(idReserva);
        boolean esDueno = reserva.getUsuario().getIdUsuario() == actor.getIdUsuario();
        if (!esDueno && actor.getRol() != RolUsuario.ADMINISTRADOR) {
            throw new PermisoDenegadoException("No puede cancelar una reserva ajena");
        }
        reserva.cancelarReserva();
    }

    public Reserva buscarPorId(int idReserva) {
        Reserva reserva = reservas.get(idReserva);
        if (reserva == null) {
            throw new AulaCheckException("No existe la reserva con id " + idReserva);
        }
        return reserva;
    }

    public List<Reserva> historialDeUsuario(Usuario usuario) {
        return ordenar(usuario.getReservas());
    }

    public List<Reserva> historialDeAmbiente(int idAmbiente) {
        return ordenar(ambienteService.buscarPorId(idAmbiente).getReservas());
    }

    public List<Reserva> historialCompleto(Usuario actor) {
        if (!actor.getRol().puedeGestionarAmbientes()) {
            throw new PermisoDenegadoException("Solo el administrador puede ver el historial completo");
        }
        return ordenar(reservas.values());
    }

    /** Cantidad de reservas activas por ambiente, para optimizar su utilización. */
    public Map<String, Long> usoPorAmbiente() {
        return reservas.values().stream()
                .filter(r -> r.getEstado() == EstadoReserva.ACTIVA)
                .collect(Collectors.groupingBy(r -> r.getAmbiente().getNombre(),
                        LinkedHashMap::new, Collectors.counting()));
    }

    private List<Reserva> ordenar(Collection<Reserva> origen) {
        return origen.stream()
                .sorted(Comparator.comparing(Reserva::getFecha).thenComparing(Reserva::getHoraInicio))
                .toList();
    }
}
