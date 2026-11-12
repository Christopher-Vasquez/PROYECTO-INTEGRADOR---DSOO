package com.aulacheck.service;

import com.aulacheck.exception.AulaCheckException;
import com.aulacheck.exception.PermisoDenegadoException;
import com.aulacheck.model.Ambiente;
import com.aulacheck.model.EstadoAmbiente;
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
        buscarPorId(idAmbiente).setEstado(nuevoEstado);
    }

    public void eliminar(Usuario actor, int idAmbiente) {
        exigirPermiso(actor);
        buscarPorId(idAmbiente);
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
