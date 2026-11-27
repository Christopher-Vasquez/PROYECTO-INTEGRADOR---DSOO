package com.aulacheck;

import com.aulacheck.exception.AulaCheckException;
import com.aulacheck.model.Ambiente;
import com.aulacheck.model.Aula;
import com.aulacheck.model.Laboratorio;
import com.aulacheck.model.SalaEstudio;
import com.aulacheck.persistence.json.AmbienteJsonRepository;
import com.aulacheck.persistence.json.JsonStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void guardarYCargarListaDeElementos() {
        JsonStore store = new JsonStore();
        Path archivo = tempDir.resolve("items.json");
        List<String> original = List.of("Aula A-101", "Lab Redes", "Sala SE-01");

        store.guardar(archivo, original);
        List<String> cargada = store.cargar(archivo, String.class);

        assertEquals(original, cargada);
    }

    @Test
    void guardarYCargarListaVacia() {
        JsonStore store = new JsonStore();
        Path archivo = tempDir.resolve("vacio_lista.json");

        store.guardar(archivo, List.of());
        List<String> cargada = store.cargar(archivo, String.class);

        assertTrue(cargada.isEmpty());
    }

    @Test
    void lanzaExcepcionCuandoArchivoNoExiste() {
        JsonStore store = new JsonStore();
        Path inexistente = tempDir.resolve("no_existe.json");

        assertThrows(AulaCheckException.class, () -> store.cargar(inexistente, String.class));
    }

    @Test
    void lanzaExcepcionCuandoJsonEstaCorrupto() throws IOException {
        JsonStore store = new JsonStore();
        Path corrupto = tempDir.resolve("corrupto.json");
        Files.writeString(corrupto, "[ { \"nombre\": \"sin cerrar\" ", StandardCharsets.UTF_8);

        assertThrows(AulaCheckException.class, () -> store.cargar(corrupto, String.class));
    }

    @Test
    void lanzaExcepcionCuandoArchivoEstaVacio() throws IOException {
        JsonStore store = new JsonStore();
        Path archivoVacio = tempDir.resolve("blanco.json");
        Files.writeString(archivoVacio, "", StandardCharsets.UTF_8);

        assertThrows(AulaCheckException.class, () -> store.cargar(archivoVacio, String.class));
    }

    @Test
    void guardarYCargarAmbientesConSuClaseConcreta() {
        Path archivoAmbientes = tempDir.resolve("ambientes.json");
        AmbienteJsonRepository repo = new AmbienteJsonRepository(archivoAmbientes);

        List<Ambiente> ambientes = List.of(
                new Aula(1, "A-101", "Pabellón A", 40, true),
                new Laboratorio(2, "LAB-01", "Pabellón B", 25, 25, "Cómputo"),
                new SalaEstudio(3, "SE-01", "Biblioteca", 8, true)
        );

        repo.guardarTodos(ambientes);
        List<Ambiente> recuperados = repo.cargarTodos();

        assertEquals(3, recuperados.size());
        assertInstanceOf(Aula.class, recuperados.get(0));
        assertInstanceOf(Laboratorio.class, recuperados.get(1));
        assertInstanceOf(SalaEstudio.class, recuperados.get(2));
    }
}