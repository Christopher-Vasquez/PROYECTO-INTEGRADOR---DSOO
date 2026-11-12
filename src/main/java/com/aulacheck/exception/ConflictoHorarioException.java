package com.aulacheck.exception;

/** Se lanza cuando una reserva se cruza con otra ya activa en el mismo ambiente. */
public class ConflictoHorarioException extends AulaCheckException {
    public ConflictoHorarioException(String mensaje) {
        super(mensaje);
    }
}
