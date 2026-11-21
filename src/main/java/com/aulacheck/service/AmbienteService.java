package com.aulacheck.service;

import com.aulacheck.exception.AulaCheckException;
import com.aulacheck.exception.PermisoDenegadoException;
import com.aulacheck.model.Ambiente;
import com.aulacheck.model.EstadoAmbiente;
import com.aulacheck.model.EstadoReserva;
import com.aulacheck.model.Usuario;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Casos de uso: Consultar disponibilidad de ambientes y Gestionar ambientes. */
public class AmbienteService {

    private final Map<Integer, Ambiente> ambientes = new LinkedHashMap<>();

    public void registrar(Usuario actor, Ambiente ambiente) {
        exigirPermiso(actor);
        if (ambientes.containsKey(ambiente.getIdAmbiente())) {
            throw new AulaCheckException("Ya existe un ambiente con id " + ambiente.getIdAmbiente());
        }
        ambientes.put(ambiente.getIdAmbiente(), ambiente);
    }

    public void cambiarEstado(Usuario actor, int idAmbiente, EstadoAmbiente nuevoEstado) {
        exigirPermiso(actor);
        Ambiente ambiente = buscarPorId(idAmbiente);
        if (nuevoEstado == EstadoAmbiente.DISPONIBLE && ambiente.tieneIncidenciasPendientes()) {
            throw new AulaCheckException(
                    "No se puede cambiar '" + ambiente.getNombre() + "' a DISPONIBLE mientras tenga incidencias pendientes");
        }
        ambiente.setEstado(nuevoEstado);
    }

    public void eliminar(Usuario actor, int idAmbiente) {
        exigirPermiso(actor);
        Ambiente ambiente = buscarPorId(idAmbiente);
        boolean tieneReservasActivas = ambiente.getReservas().stream()
                .anyMatch(r -> r.getEstado() == EstadoReserva.ACTIVA);
        if (tieneReservasActivas || ambiente.tieneIncidenciasPendientes()) {
            throw new AulaCheckException(
                    "No se puede eliminar '" + ambiente.getNombre() + "' porque tiene reservas activas o incidencias pendientes");
        }
        ambientes.remove(idAmbiente);
    }

    public Ambiente buscarPorId(int idAmbiente) {
        Ambiente ambiente = ambientes.get(idAmbiente);
        if (ambiente == null) {
            throw new AulaCheckException("No existe el ambiente con id " + idAmbiente);
        }
        return ambiente;
    }

    public List<Ambiente> listarTodos() {
        return new ArrayList<>(ambientes.values());
    }

    public List<Ambiente> listarDisponibles(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        if (fecha == null || horaInicio == null || horaFin == null) {
            throw new IllegalArgumentException("Fecha y horas son obligatorias");
        }
        if (!horaInicio.isBefore(horaFin)) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin");
        }
        return ambientes.values().stream()
                .filter(a -> a.consultarDisponibilidad(fecha, horaInicio, horaFin))
                .toList();
    }

    private void exigirPermiso(Usuario actor) {
        if (!actor.getRol().puedeGestionarAmbientes()) {
            throw new PermisoDenegadoException("Solo el administrador puede gestionar ambientes");
        }
    }
}
