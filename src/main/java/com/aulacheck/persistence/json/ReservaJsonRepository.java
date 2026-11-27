package com.aulacheck.persistence.json;

import com.aulacheck.exception.AulaCheckException;
import com.aulacheck.model.Ambiente;
import com.aulacheck.model.EstadoReserva;
import com.aulacheck.model.Reserva;
import com.aulacheck.model.Usuario;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Repositorio json para persistir reservas y reconstruir sus relaciones. */
public class ReservaJsonRepository {

    private final JsonStore store;
    private final Path ruta;

    public ReservaJsonRepository(Path ruta) {
        this.store = new JsonStore();
        this.ruta = ruta;
    }

    public void guardarTodas(List<Reserva> reservas) throws AulaCheckException {
        List<ReservaDTO> dtos = new ArrayList<>();
        for (Reserva r : reservas) {
            ReservaDTO dto = new ReservaDTO();
            dto.idReserva = r.getIdReserva();
            dto.idUsuario = r.getUsuario().getIdUsuario();
            dto.idAmbiente = r.getAmbiente().getIdAmbiente();
            dto.fecha = r.getFecha().toString();
            dto.horaInicio = r.getHoraInicio().toString();
            dto.horaFin = r.getHoraFin().toString();
            dto.estado = r.getEstado().name();
            dtos.add(dto);
        }
        store.guardar(ruta, dtos);
    }

    public List<Reserva> cargarTodas(List<Usuario> usuarios, List<Ambiente> ambientes) throws AulaCheckException {
        Map<Integer, Usuario> mapaUsuarios = new HashMap<>();
        for (Usuario u : usuarios) {
            mapaUsuarios.put(u.getIdUsuario(), u);
        }

        Map<Integer, Ambiente> mapaAmbientes = new HashMap<>();
        for (Ambiente a : ambientes) {
            mapaAmbientes.put(a.getIdAmbiente(), a);
        }

        List<ReservaDTO> dtos = store.cargar(ruta, ReservaDTO.class);
        List<Reserva> reservas = new ArrayList<>();
        for (ReservaDTO dto : dtos) {
            Usuario u = mapaUsuarios.get(dto.idUsuario);
            Ambiente a = mapaAmbientes.get(dto.idAmbiente);
            if (u != null && a != null) {
                reservas.add(Reserva.reconstruir(
                        dto.idReserva,
                        LocalDate.parse(dto.fecha),
                        LocalTime.parse(dto.horaInicio),
                        LocalTime.parse(dto.horaFin),
                        EstadoReserva.valueOf(dto.estado),
                        u,
                        a
                ));
            }
        }
        return reservas;
    }

    private static class ReservaDTO {
        int idReserva;
        int idUsuario;
        int idAmbiente;
        String fecha;
        String horaInicio;
        String horaFin;
        String estado;
    }
}