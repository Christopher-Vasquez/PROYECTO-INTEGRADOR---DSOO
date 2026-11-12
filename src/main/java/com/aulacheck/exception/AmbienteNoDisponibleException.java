package com.aulacheck.exception;

/** Se lanza cuando se intenta reservar un ambiente en mantenimiento o inactivo. */
public class AmbienteNoDisponibleException extends AulaCheckException {
    public AmbienteNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
