package com.aulacheck.model;

public class Laboratorio extends Ambiente {

    private int numEquipos;
    private String tipoLaboratorio;

    public Laboratorio(int idAmbiente, String nombre, String ubicacion, int capacidad,
                       int numEquipos, String tipoLaboratorio) {
        super(idAmbiente, nombre, ubicacion, capacidad);
        this.numEquipos = numEquipos;
        this.tipoLaboratorio = tipoLaboratorio;
    }

    public int getNumEquipos() { return numEquipos; }
    public void setNumEquipos(int numEquipos) { this.numEquipos = numEquipos; }
    public String getTipoLaboratorio() { return tipoLaboratorio; }
    public void setTipoLaboratorio(String tipoLaboratorio) { this.tipoLaboratorio = tipoLaboratorio; }

    @Override
    public String getTipo() { return "Laboratorio"; }
}
