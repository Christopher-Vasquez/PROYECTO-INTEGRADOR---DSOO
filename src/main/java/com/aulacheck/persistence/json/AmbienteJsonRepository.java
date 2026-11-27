package com.aulacheck.persistence.json;

import com.aulacheck.exception.AulaCheckException;
import com.aulacheck.model.Ambiente;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.file.Path;
import java.util.List;

/** Repositorio json para persistir ambientes polimórficos como Aula, Laboratorio o SalaEstudio. */
public class AmbienteJsonRepository {

    private final JsonStore store;
    private final Path ruta;

    public AmbienteJsonRepository(Path ruta) {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeHierarchyAdapter(Ambiente.class, new AmbienteAdapter())
                .create();
        this.store = new JsonStore(gson);
        this.ruta = ruta;
    }

    public void guardarTodos(List<Ambiente> ambientes) throws AulaCheckException {
        store.guardar(ruta, ambientes);
    }

    public List<Ambiente> cargarTodos() throws AulaCheckException {
        return store.cargar(ruta, Ambiente.class);
    }
}