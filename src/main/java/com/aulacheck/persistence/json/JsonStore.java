package com.aulacheck.persistence.json;

import com.aulacheck.exception.AulaCheckException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Utilidad genérica para guardar y cargar listas en formato JSON. */
public class JsonStore {

    private final Gson gson;

    public JsonStore() {
        this(new GsonBuilder().setPrettyPrinting().create());
    }

    public JsonStore(Gson gson) {
        this.gson = gson;
    }

    public <T> void guardar(Path ruta, List<T> lista) throws AulaCheckException {
        try {
            if (ruta.getParent() != null) {
                Files.createDirectories(ruta.getParent());
            }
            try (Writer writer = Files.newBufferedWriter(ruta, StandardCharsets.UTF_8)) {
                gson.toJson(lista, writer);
            }
        } catch (IOException e) {
            throw new AulaCheckException("No se pudo guardar el archivo JSON en " + ruta + ": " + e.getMessage());
        }
    }

    public <T> List<T> cargar(Path ruta, Class<T> claseElemento) throws AulaCheckException {
        Type tipoLista = TypeToken.getParameterized(List.class, claseElemento).getType();
        return cargar(ruta, tipoLista);
    }

    public <T> List<T> cargar(Path ruta, Type tipoLista) throws AulaCheckException {
        if (ruta == null || !Files.exists(ruta)) {
            throw new AulaCheckException("El archivo JSON no existe: " + ruta);
        }

        try (Reader reader = Files.newBufferedReader(ruta, StandardCharsets.UTF_8)) {
            List<T> resultado = gson.fromJson(reader, tipoLista);
            if (resultado == null) {
                throw new AulaCheckException("El archivo JSON está vacío o corrupto: " + ruta);
            }
            return new ArrayList<>(resultado);
        } catch (JsonParseException e) {
            throw new AulaCheckException("El archivo JSON está corrupto: " + ruta + " (" + e.getMessage() + ")");
        } catch (IOException e) {
            throw new AulaCheckException("Error al leer el archivo JSON: " + ruta + " (" + e.getMessage() + ")");
        }
    }
}