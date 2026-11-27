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

    // Nuevo constructor con fecha y estado originales
    Incidencia(int idIncidencia, String descripcion, LocalDate fechaReporte,
               EstadoIncidencia estado, Usuario reportadoPor, Ambiente ambiente) {
        this.idIncidencia = idIncidencia;
        this.descripcion = descripcion;
        this.fechaReporte = fechaReporte;
        this.estado = estado;
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

    // NUEVO MÉTODO PARA RECONSTRUIR DESDE JSON 
    public static Incidencia reconstruir(int idIncidencia, String descripcion, LocalDate fechaReporte,
                                         EstadoIncidencia estado, Usuario reportadoPor, Ambiente ambiente) {
        Incidencia inc = new Incidencia(idIncidencia, descripcion, fechaReporte, estado, reportadoPor, ambiente);
        ambiente.agregarIncidencia(inc);
        reportadoPor.agregarIncidencia(inc);
        return inc;
    }
}