package com.aulacheck.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Clase abstracta que generaliza los espacios reservables de la universidad.
 * Relaciones (diagrama de clases): 1 Ambiente "tiene" 0..* Reservas y
 * 1 Ambiente "presenta" 0..* Incidencias.
 */
public abstract class Ambiente {

    private final int idAmbiente;
    private String nombre;
    private String ubicacion;
    private int capacidad;
    private EstadoAmbiente estado;

    private final List<Reserva> reservas = new ArrayList<>();
    private final List<Incidencia> incidencias = new ArrayList<>();

    protected Ambiente(int idAmbiente, String nombre, String ubicacion, int capacidad) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del ambiente es obligatorio");
        }
        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor que cero");
        }
        this.idAmbiente = idAmbiente;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.capacidad = capacidad;
        this.estado = EstadoAmbiente.DISPONIBLE;
    }

    /** Tipo legible del ambiente (polimorfismo). */
    public abstract String getTipo();

    /**
     * Indica si el ambiente puede reservarse en la fecha y rango horario dados:
     * debe estar DISPONIBLE y no cruzarse con ninguna reserva activa.
     */
    public boolean consultarDisponibilidad(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        if (estado != EstadoAmbiente.DISPONIBLE) {
            return false;
        }
        return reservas.stream().noneMatch(r -> r.solapaCon(fecha, horaInicio, horaFin));
    }

    // Package-private: solo las clases del modelo enlazan las asociaciones.
    void agregarReserva(Reserva reserva) {
        reservas.add(reserva);
    }

    void agregarIncidencia(Incidencia incidencia) {
        incidencias.add(incidencia);
        actualizarEstadoPorIncidencias();
    }

    /** Si hay incidencias pendientes pasa a mantenimiento; si todas se resolvieron, vuelve a disponible. */
    void actualizarEstadoPorIncidencias() {
        boolean hayPendientes = incidencias.stream().anyMatch(i -> i.getEstado().estaPendiente());
        if (hayPendientes && estado == EstadoAmbiente.DISPONIBLE) {
            estado = EstadoAmbiente.EN_MANTENIMIENTO;
        } else if (!hayPendientes && estado == EstadoAmbiente.EN_MANTENIMIENTO) {
            estado = EstadoAmbiente.DISPONIBLE;
        }
    }

    public int getIdAmbiente() { return idAmbiente; }
    public String getNombre() { return nombre; }
    public String getUbicacion() { return ubicacion; }
    public int getCapacidad() { return capacidad; }
    public EstadoAmbiente getEstado() { return estado; }
    public List<Reserva> getReservas() { return Collections.unmodifiableList(reservas); }
    public List<Incidencia> getIncidencias() { return Collections.unmodifiableList(incidencias); }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public void setCapacidad(int capacidad) {
        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor que cero");
        }
        this.capacidad = capacidad;
    }
    public void setEstado(EstadoAmbiente estado) { this.estado = estado; }

    @Override
    public String toString() {
        return String.format("[%d] %s %s - %s (cap. %d) - %s",
                idAmbiente, getTipo(), nombre, ubicacion, capacidad, estado);
    }
}
