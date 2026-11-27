package com.aulacheck.model;

import com.aulacheck.exception.AulaCheckException;

import java.time.LocalDate;
import java.time.LocalTime;

public class Reserva {

    private final int idReserva;
    private final LocalDate fecha;
    private final LocalTime horaInicio;
    private final LocalTime horaFin;
    private EstadoReserva estado;

    private final Usuario usuario;
    private final Ambiente ambiente;

    Reserva(int idReserva, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
            Usuario usuario, Ambiente ambiente) {
        if (fecha == null || horaInicio == null || horaFin == null) {
            throw new IllegalArgumentException("Fecha y horas son obligatorias");
        }
        if (!horaInicio.isBefore(horaFin)) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin");
        }
        this.idReserva = idReserva;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.usuario = usuario;
        this.ambiente = ambiente;
        this.estado = EstadoReserva.ACTIVA;
    }

    /** true si esta reserva está activa y se cruza con la fecha y rango indicados. */
    public boolean solapaCon(LocalDate otraFecha, LocalTime otroInicio, LocalTime otroFin) {
        return estado == EstadoReserva.ACTIVA
                && fecha.equals(otraFecha)
                && horaInicio.isBefore(otroFin)
                && otroInicio.isBefore(horaFin);
    }

    /** true si EXISTE conflicto con otra reserva activa del mismo ambiente. */
    public boolean validarConflictoHorario() {
        return ambiente.getReservas().stream()
                .filter(otra -> otra != this)
                .anyMatch(otra -> otra.solapaCon(fecha, horaInicio, horaFin));
    }

    public void cancelarReserva() {
        if (this.estado == EstadoReserva.CANCELADA) {
            throw new AulaCheckException("La reserva #" + idReserva + " ya se encuentra cancelada");
        }
        this.estado = EstadoReserva.CANCELADA;
    }

    public int getIdReserva() { return idReserva; }
    public LocalDate getFecha() { return fecha; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public EstadoReserva getEstado() { return estado; }
    public Usuario getUsuario() { return usuario; }
    public Ambiente getAmbiente() { return ambiente; }

    @Override
    public String toString() {
        return String.format("Reserva #%d | %s | %s %s-%s | %s | %s",
                idReserva, ambiente.getNombre(), fecha, horaInicio, horaFin, usuario.getNombre(), estado);
    }
    public static Reserva reconstruir(int idReserva, LocalDate fecha, LocalTime horaInicio,
                                      LocalTime horaFin, EstadoReserva estado,
                                      Usuario usuario, Ambiente ambiente) {
        Reserva r = new Reserva(idReserva, fecha, horaInicio, horaFin, usuario, ambiente);
        r.estado = estado;
        ambiente.agregarReserva(r);
        usuario.agregarReserva(r);
        return r;
    }
}
