package com.aulacheck.persistence.jdbc;

import com.aulacheck.model.Ambiente;
import com.aulacheck.model.Aula;
import com.aulacheck.model.EstadoAmbiente;
import com.aulacheck.model.Laboratorio;
import com.aulacheck.model.SalaEstudio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class AmbienteJdbcRepository {

    public void guardarTodo(List<Ambiente> ambientes) throws SQLException {
        String sql = "INSERT INTO ambiente (id_ambiente, nombre, ubicacion, capacidad, estado, tipo, tiene_proyector, num_equipos, tipo_laboratorio, tiene_pizarra) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT (id_ambiente) DO UPDATE SET "
                + "nombre = EXCLUDED.nombre, "
                + "ubicacion = EXCLUDED.ubicacion, "
                + "capacidad = EXCLUDED.capacidad, "
                + "estado = EXCLUDED.estado, "
                + "tipo = EXCLUDED.tipo, "
                + "tiene_proyector = EXCLUDED.tiene_proyector, "
                + "num_equipos = EXCLUDED.num_equipos, "
                + "tipo_laboratorio = EXCLUDED.tipo_laboratorio, "
                + "tiene_pizarra = EXCLUDED.tiene_pizarra";

        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);

            try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
                for (Ambiente ambiente : ambientes) {
                    sentencia.setInt(1, ambiente.getIdAmbiente());
                    sentencia.setString(2, ambiente.getNombre());
                    sentencia.setString(3, ambiente.getUbicacion());
                    sentencia.setInt(4, ambiente.getCapacidad());
                    sentencia.setString(5, ambiente.getEstado().name());
                    sentencia.setString(6, ambiente.getTipo());

                    if (ambiente instanceof Aula aula) {
                        sentencia.setBoolean(7, aula.isTieneProyector());
                        sentencia.setNull(8, Types.INTEGER);
                        sentencia.setNull(9, Types.VARCHAR);
                        sentencia.setNull(10, Types.BOOLEAN);
                    } else if (ambiente instanceof Laboratorio laboratorio) {
                        sentencia.setNull(7, Types.BOOLEAN);
                        sentencia.setInt(8, laboratorio.getNumEquipos());
                        sentencia.setString(9, laboratorio.getTipoLaboratorio());
                        sentencia.setNull(10, Types.BOOLEAN);
                    } else if (ambiente instanceof SalaEstudio sala) {
                        sentencia.setNull(7, Types.BOOLEAN);
                        sentencia.setNull(8, Types.INTEGER);
                        sentencia.setNull(9, Types.VARCHAR);
                        sentencia.setBoolean(10, sala.isTienePizarra());
                    } else {
                        throw new SQLException("Tipo de ambiente no reconocido");
                    }

                    sentencia.addBatch();
                }

                sentencia.executeBatch();
                conexion.commit();
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        }
    }

    public List<Ambiente> cargarTodo() throws SQLException {
        List<Ambiente> ambientes = new ArrayList<>();
        String sql = "SELECT * FROM ambiente ORDER BY id_ambiente";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                int id = resultado.getInt("id_ambiente");
                String nombre = resultado.getString("nombre");
                String ubicacion = resultado.getString("ubicacion");
                int capacidad = resultado.getInt("capacidad");
                String tipo = resultado.getString("tipo");

                Ambiente ambiente = switch (tipo) {
                    case "Aula" -> new Aula(id, nombre, ubicacion, capacidad,
                            resultado.getBoolean("tiene_proyector"));
                    case "Laboratorio" -> new Laboratorio(id, nombre, ubicacion, capacidad,
                            resultado.getInt("num_equipos"),
                            resultado.getString("tipo_laboratorio"));
                    case "Sala de estudio" -> new SalaEstudio(id, nombre, ubicacion, capacidad,
                            resultado.getBoolean("tiene_pizarra"));
                    default -> throw new SQLException("Tipo de ambiente desconocido: " + tipo);
                };

                ambiente.setEstado(EstadoAmbiente.valueOf(resultado.getString("estado")));
                ambientes.add(ambiente);
            }
        }

        return ambientes;
    }
}
