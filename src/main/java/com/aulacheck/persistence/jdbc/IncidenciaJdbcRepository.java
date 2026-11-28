
package com.aulacheck.persistence.jdbc;

import com.aulacheck.model.Ambiente;
import com.aulacheck.model.EstadoIncidencia;
import com.aulacheck.model.Incidencia;
import com.aulacheck.model.Usuario;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IncidenciaJdbcRepository {

    public void guardarTodo(List<Incidencia> incidencias) throws SQLException {
        String sql = "INSERT INTO incidencia (id_incidencia, descripcion, fecha_reporte, estado, id_usuario, id_ambiente) "
                + "VALUES (?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT (id_incidencia) DO UPDATE SET "
                + "descripcion = EXCLUDED.descripcion, "
                + "fecha_reporte = EXCLUDED.fecha_reporte, "
                + "estado = EXCLUDED.estado, "
                + "id_usuario = EXCLUDED.id_usuario, "
                + "id_ambiente = EXCLUDED.id_ambiente";

        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);

            try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
                for (Incidencia incidencia : incidencias) {
                    sentencia.setInt(1, incidencia.getIdIncidencia());
                    sentencia.setString(2, incidencia.getDescripcion());
                    sentencia.setDate(3, Date.valueOf(incidencia.getFechaReporte()));
                    sentencia.setString(4, incidencia.getEstado().name());
                    sentencia.setInt(5, incidencia.getReportadoPor().getIdUsuario());
                    sentencia.setInt(6, incidencia.getAmbiente().getIdAmbiente());
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

    public List<Incidencia> cargarTodo(List<Usuario> usuarios, List<Ambiente> ambientes)
            throws SQLException {

        List<Incidencia> incidencias = new ArrayList<>();
        Map<Integer, Usuario> usuariosPorId = new HashMap<>();
        Map<Integer, Ambiente> ambientesPorId = new HashMap<>();

        for (Usuario usuario : usuarios) {
            usuariosPorId.put(usuario.getIdUsuario(), usuario);
        }

        for (Ambiente ambiente : ambientes) {
            ambientesPorId.put(ambiente.getIdAmbiente(), ambiente);
        }

        String sql = "SELECT * FROM incidencia ORDER BY id_incidencia";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                int idUsuario = resultado.getInt("id_usuario");
                int idAmbiente = resultado.getInt("id_ambiente");

                Usuario usuario = usuariosPorId.get(idUsuario);
                Ambiente ambiente = ambientesPorId.get(idAmbiente);

                if (usuario == null || ambiente == null) {
                    throw new SQLException("No se encontro el usuario o ambiente de la incidencia "
                            + resultado.getInt("id_incidencia"));
                }

                Incidencia incidencia = Incidencia.reconstruir(
                        resultado.getInt("id_incidencia"),
                        resultado.getString("descripcion"),
                        resultado.getDate("fecha_reporte").toLocalDate(),
                        EstadoIncidencia.valueOf(resultado.getString("estado")),
                        usuario,
                        ambiente
                );

                incidencias.add(incidencia);
            }
        }

        return incidencias;
    }
}
