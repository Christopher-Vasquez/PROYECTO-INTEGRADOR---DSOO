package com.aulacheck.exception;

/** Se lanza cuando el rol del usuario no permite realizar la operación. */
public class PermisoDenegadoException extends AulaCheckException {
    public PermisoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
