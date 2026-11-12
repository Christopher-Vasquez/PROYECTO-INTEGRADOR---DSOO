package com.aulacheck.model;

public class Aula extends Ambiente {

    private boolean tieneProyector;

    public Aula(int idAmbiente, String nombre, String ubicacion, int capacidad, boolean tieneProyector) {
        super(idAmbiente, nombre, ubicacion, capacidad);
        this.tieneProyector = tieneProyector;
    }

    public boolean isTieneProyector() { return tieneProyector; }
    public void setTieneProyector(boolean tieneProyector) { this.tieneProyector = tieneProyector; }

    @Override
    public String getTipo() { return "Aula"; }
}
