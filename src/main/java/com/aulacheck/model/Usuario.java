package com.aulacheck.model;

import com.aulacheck.exception.AmbienteNoDisponibleException;
import com.aulacheck.exception.ConflictoHorarioException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Actor del sistema (estudiante, docente, administrador o personal de mantenimiento).
 * Relaciones: 1 Usuario "realiza" 0..* Reservas y 1 Usuario "reporta" 0..* Incidencias.
 */
public class Usuario {

    private final int idUsuario;
    private String nombre;
    private String email;
    private final RolUsuario rol;

    private final List<Reserva> reservas = new ArrayList<>();
    private final List<Incidencia> incidencias = new ArrayList<>();

    public Usuario(int idUsuario, String nombre, String email, RolUsuario rol) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Email inválido");
        }
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
    }

    /** Caso de uso: Consultar disponibilidad de ambientes. */
    public boolean consultarDisponibilidad(Ambiente ambiente, LocalDate fecha,
                                           LocalTime horaInicio, LocalTime horaFin) {
        return ambiente.consultarDisponibilidad(fecha, horaInicio, horaFin);
    }

    /**
     * Caso de uso: Registrar reserva (incluye «Validar conflicto de horarios»).
     *
     * @throws AmbienteNoDisponibleException si el ambiente no está DISPONIBLE
     * @throws ConflictoHorarioException     si el horario se cruza con otra reserva activa
     */
    public Reserva crearReserva(int idReserva, Ambiente ambiente, LocalDate fecha,
                                LocalTime horaInicio, LocalTime horaFin) {
        if (ambiente.getEstado() != EstadoAmbiente.DISPONIBLE) {
            throw new AmbienteNoDisponibleException(
                    "El ambiente '" + ambiente.getNombre() + "' no está disponible (" + ambiente.getEstado() + ")");
        }
        Reserva reserva = new Reserva(idReserva, fecha, horaInicio, horaFin, this, ambiente);
        if (reserva.validarConflictoHorario()) {
            throw new ConflictoHorarioException(
                    "Conflicto de horario en '" + ambiente.getNombre() + "' el " + fecha
                            + " de " + horaInicio + " a " + horaFin);
        }
        ambiente.agregarReserva(reserva);
        reservas.add(reserva);
        return reserva;
    }

    /** Caso de uso: Registrar incidencia. */
    public Incidencia reportarIncidencia(int idIncidencia, Ambiente ambiente, String descripcion) {
        Incidencia incidencia = new Incidencia(idIncidencia, descripcion, this, ambiente);
        ambiente.agregarIncidencia(incidencia);
        incidencias.add(incidencia);
        return incidencia;
    }

    public int getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public RolUsuario getRol() { return rol; }
    public List<Reserva> getReservas() { return Collections.unmodifiableList(reservas); }
    public List<Incidencia> getIncidencias() { return Collections.unmodifiableList(incidencias); }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return nombre + " (" + rol + ")";
    }
}
