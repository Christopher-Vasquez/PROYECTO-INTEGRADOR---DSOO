package com.aulacheck.model;

public class SalaEstudio extends Ambiente {

    private boolean tienePizarra;

    public SalaEstudio(int idAmbiente, String nombre, String ubicacion, int capacidad, boolean tienePizarra) {
        super(idAmbiente, nombre, ubicacion, capacidad);
        this.tienePizarra = tienePizarra;
    }

    public boolean isTienePizarra() { return tienePizarra; }
    public void setTienePizarra(boolean tienePizarra) { this.tienePizarra = tienePizarra; }

    @Override
    public String getTipo() { return "Sala de estudio"; }
}
