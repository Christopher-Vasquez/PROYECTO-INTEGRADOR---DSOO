package com.aulacheck.model;

public enum EstadoIncidencia {
    ABIERTA, EN_PROCESO, RESUELTA;

    public boolean estaPendiente() {
        return this != RESUELTA;
    }
}
