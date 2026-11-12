package com.aulacheck.model;

import java.time.LocalDate;

public class Incidencia {

    private final int idIncidencia;
    private final String descripcion;
    private final LocalDate fechaReporte;
    private EstadoIncidencia estado;

    private final Usuario reportadoPor;
    private final Ambiente ambiente;

    Incidencia(int idIncidencia, String descripcion, Usuario reportadoPor, Ambiente ambiente) {
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción de la incidencia es obligatoria");
        }
        this.idIncidencia = idIncidencia;
        this.descripcion = descripcion;
        this.fechaReporte = LocalDate.now();
        this.estado = EstadoIncidencia.ABIERTA;
        this.reportadoPor = reportadoPor;
        this.ambiente = ambiente;
    }

    /** Cambia el estado y sincroniza el estado del ambiente afectado. */
    public void actualizarEstado(EstadoIncidencia nuevoEstado) {
        this.estado = nuevoEstado;
        ambiente.actualizarEstadoPorIncidencias();
    }

    public int getIdIncidencia() { return idIncidencia; }
    public String getDescripcion() { return descripcion; }
    public LocalDate getFechaReporte() { return fechaReporte; }
    public EstadoIncidencia getEstado() { return estado; }
    public Usuario getReportadoPor() { return reportadoPor; }
    public Ambiente getAmbiente() { return ambiente; }

    @Override
    public String toString() {
        return String.format("Incidencia #%d | %s | %s | %s | %s",
                idIncidencia, ambiente.getNombre(), descripcion, fechaReporte, estado);
    }
}
