package com.aulacheck.service;

import com.aulacheck.exception.AulaCheckException;
import com.aulacheck.exception.PermisoDenegadoException;
import com.aulacheck.model.EstadoIncidencia;
import com.aulacheck.model.Incidencia;
import com.aulacheck.model.Usuario;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Caso de uso: Registrar / Gestionar incidencia. */
public class IncidenciaService {

    private final AmbienteService ambienteService;
    private final Map<Integer, Incidencia> incidencias = new LinkedHashMap<>();
    private int siguienteId = 1;

    public IncidenciaService(AmbienteService ambienteService) {
        this.ambienteService = ambienteService;
    }

    public Incidencia reportar(Usuario usuario, int idAmbiente, String descripcion) {
        Incidencia incidencia = usuario.reportarIncidencia(
                siguienteId, ambienteService.buscarPorId(idAmbiente), descripcion);
        incidencias.put(siguienteId, incidencia);
        siguienteId++;
        return incidencia;
    }

    public void actualizarEstado(Usuario actor, int idIncidencia, EstadoIncidencia nuevoEstado) {
        if (!actor.getRol().puedeGestionarIncidencias()) {
            throw new PermisoDenegadoException("Solo administración o mantenimiento pueden gestionar incidencias");
        }
        Incidencia incidencia = incidencias.get(idIncidencia);
        if (incidencia == null) {
            throw new AulaCheckException("No existe la incidencia con id " + idIncidencia);
        }
        incidencia.actualizarEstado(nuevoEstado);
    }

    public List<Incidencia> listarPendientes() {
        return incidencias.values().stream()
                .filter(i -> i.getEstado().estaPendiente())
                .toList();
    }

    public List<Incidencia> listarTodas() {
        return List.copyOf(incidencias.values());
    }

    public void cargarDesdePersistencia(List<Incidencia> lista) {
        incidencias.clear();
        int maxId = 0;
        for (Incidencia inc : lista) {
            incidencias.put(inc.getIdIncidencia(), inc);
            if (inc.getIdIncidencia() > maxId) {
                maxId = inc.getIdIncidencia();
            }
        }
        siguienteId = maxId + 1;
    }
}
