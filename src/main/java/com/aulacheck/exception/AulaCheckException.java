package com.aulacheck.exception;

/** Excepción base de las reglas de negocio de AulaCheck. */
public class AulaCheckException extends RuntimeException {
    public AulaCheckException(String mensaje) {
        super(mensaje);
    }
}
