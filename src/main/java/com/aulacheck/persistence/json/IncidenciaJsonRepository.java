package com.aulacheck.persistence.json;

import com.aulacheck.exception.AulaCheckException;
import com.aulacheck.model.Ambiente;
import com.aulacheck.model.EstadoIncidencia;
import com.aulacheck.model.Incidencia;
import com.aulacheck.model.Usuario;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Repositorio json para persistir incidencias y reconstruir sus relaciones. */
public class IncidenciaJsonRepository {

    private final JsonStore store;
    private final Path ruta;

    public IncidenciaJsonRepository(Path ruta) {
        this.store = new JsonStore();
        this.ruta = ruta;
    }

    public void guardarTodas(List<Incidencia> incidencias) throws AulaCheckException {
        List<IncidenciaDTO> dtos = new ArrayList<>();
        for (Incidencia inc : incidencias) {
            IncidenciaDTO dto = new IncidenciaDTO();
            dto.idIncidencia = inc.getIdIncidencia();
            dto.idUsuario = inc.getReportadoPor().getIdUsuario();
            dto.idAmbiente = inc.getAmbiente().getIdAmbiente();
            dto.descripcion = inc.getDescripcion();
            dto.fechaReporte = inc.getFechaReporte().toString();
            dto.estado = inc.getEstado().name();
            dtos.add(dto);
        }
        store.guardar(ruta, dtos);
    }

    public List<Incidencia> cargarTodas(List<Usuario> usuarios, List<Ambiente> ambientes) throws AulaCheckException {
        Map<Integer, Usuario> mapaUsuarios = new HashMap<>();
        for (Usuario u : usuarios) {
            mapaUsuarios.put(u.getIdUsuario(), u);
        }

        Map<Integer, Ambiente> mapaAmbientes = new HashMap<>();
        for (Ambiente a : ambientes) {
            mapaAmbientes.put(a.getIdAmbiente(), a);
        }

        List<IncidenciaDTO> dtos = store.cargar(ruta, IncidenciaDTO.class);
        List<Incidencia> incidencias = new ArrayList<>();
        for (IncidenciaDTO dto : dtos) {
            Usuario u = mapaUsuarios.get(dto.idUsuario);
            Ambiente a = mapaAmbientes.get(dto.idAmbiente);
            if (u != null && a != null) {
                incidencias.add(Incidencia.reconstruir(
                        dto.idIncidencia,
                        dto.descripcion,
                        LocalDate.parse(dto.fechaReporte),
                        EstadoIncidencia.valueOf(dto.estado),
                        u,
                        a
                ));
            }
        }
        return incidencias;
    }

    private static class IncidenciaDTO {
        int idIncidencia;
        int idUsuario;
        int idAmbiente;
        String descripcion;
        String fechaReporte;
        String estado;
    }
}