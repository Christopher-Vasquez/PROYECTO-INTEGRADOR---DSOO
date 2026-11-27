package com.aulacheck.persistence.json;

import com.aulacheck.model.*;
import com.google.gson.*;

import java.lang.reflect.Type;

/** Serializa y deserializa subclases de ambiente conservando su tipo concreto. */
public class AmbienteAdapter implements JsonSerializer<Ambiente>, JsonDeserializer<Ambiente> {

    @Override
    public JsonElement serialize(Ambiente src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.addProperty("tipo", src.getClass().getSimpleName());
        obj.addProperty("idAmbiente", src.getIdAmbiente());
        obj.addProperty("nombre", src.getNombre());
        obj.addProperty("ubicacion", src.getUbicacion());
        obj.addProperty("capacidad", src.getCapacidad());
        obj.addProperty("estado", src.getEstado().name());

        if (src instanceof Aula aula) {
            obj.addProperty("tieneProyector", aula.isTieneProyector());
        } else if (src instanceof Laboratorio lab) {
            obj.addProperty("numEquipos", lab.getNumEquipos());
            obj.addProperty("tipoLaboratorio", lab.getTipoLaboratorio());
        } else if (src instanceof SalaEstudio sala) {
            obj.addProperty("tienePizarra", sala.isTienePizarra());
        }
        return obj;
    }

    @Override
    public Ambiente deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        if (!obj.has("tipo")) {
            throw new JsonParseException("Falta el campo 'tipo' en el registro de Ambiente.");
        }

        String tipo = obj.get("tipo").getAsString();
        int id = obj.get("idAmbiente").getAsInt();
        String nombre = obj.get("nombre").getAsString();
        String ubicacion = obj.get("ubicacion").getAsString();
        int capacidad = obj.get("capacidad").getAsInt();

        Ambiente ambiente = switch (tipo) {
            case "Aula" -> new Aula(id, nombre, ubicacion, capacidad,
                    obj.has("tieneProyector") && obj.get("tieneProyector").getAsBoolean());
            case "Laboratorio" -> new Laboratorio(id, nombre, ubicacion, capacidad,
                    obj.has("numEquipos") ? obj.get("numEquipos").getAsInt() : 0,
                    obj.has("tipoLaboratorio") ? obj.get("tipoLaboratorio").getAsString() : "General");
            case "SalaEstudio" -> new SalaEstudio(id, nombre, ubicacion, capacidad,
                    obj.has("tienePizarra") && obj.get("tienePizarra").getAsBoolean());
            default -> throw new JsonParseException("Tipo de ambiente desconocido: " + tipo);
        };

        if (obj.has("estado")) {
            EstadoAmbiente estado = EstadoAmbiente.valueOf(obj.get("estado").getAsString());
            if (ambiente.getEstado() != estado) {
                ambiente.setEstado(estado);
            }
        }
        return ambiente;
    }
}
