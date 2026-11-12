package com.aulacheck.model;

/** Roles del sistema (actores del diagrama de casos de uso). */
public enum RolUsuario {
    ESTUDIANTE, DOCENTE, ADMINISTRADOR, MANTENIMIENTO;

    public boolean puedeGestionarAmbientes() {
        return this == ADMINISTRADOR;
    }

    public boolean puedeGestionarIncidencias() {
        return this == ADMINISTRADOR || this == MANTENIMIENTO;
    }
}
