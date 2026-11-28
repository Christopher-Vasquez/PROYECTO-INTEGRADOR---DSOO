package com.aulacheck.persistence.jdbc;

import com.aulacheck.model.Ambiente;
import com.aulacheck.model.EstadoReserva;
import com.aulacheck.model.Reserva;
import com.aulacheck.model.Usuario;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReservaJdbcRepository {

    public void guardarTodo(List<Reserva> reservas) throws SQLException {
        String sql = "INSERT INTO reserva (id_reserva, fecha, hora_inicio, hora_fin, estado, id_usuario, id_ambiente) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT (id_reserva) DO UPDATE SET "
                + "fecha = EXCLUDED.fecha, "
                + "hora_inicio = EXCLUDED.hora_inicio, "
                + "hora_fin = EXCLUDED.hora_fin, "
                + "estado = EXCLUDED.estado, "
                + "id_usuario = EXCLUDED.id_usuario, "
                + "id_ambiente = EXCLUDED.id_ambiente";

        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);

            try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
                for (Reserva reserva : reservas) {
                    sentencia.setInt(1, reserva.getIdReserva());
                    sentencia.setDate(2, Date.valueOf(reserva.getFecha()));
                    sentencia.setTime(3, Time.valueOf(reserva.getHoraInicio()));
                    sentencia.setTime(4, Time.valueOf(reserva.getHoraFin()));
                    sentencia.setString(5, reserva.getEstado().name());
                    sentencia.setInt(6, reserva.getUsuario().getIdUsuario());
                    sentencia.setInt(7, reserva.getAmbiente().getIdAmbiente());
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

    public List<Reserva> cargarTodo(List<Usuario> usuarios, List<Ambiente> ambientes)
            throws SQLException {

        List<Reserva> reservas = new ArrayList<>();
        Map<Integer, Usuario> usuariosPorId = new HashMap<>();
        Map<Integer, Ambiente> ambientesPorId = new HashMap<>();

        for (Usuario usuario : usuarios) {
            usuariosPorId.put(usuario.getIdUsuario(), usuario);
        }

        for (Ambiente ambiente : ambientes) {
            ambientesPorId.put(ambiente.getIdAmbiente(), ambiente);
        }

        String sql = "SELECT * FROM reserva ORDER BY id_reserva";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                int idUsuario = resultado.getInt("id_usuario");
                int idAmbiente = resultado.getInt("id_ambiente");

                Usuario usuario = usuariosPorId.get(idUsuario);
                Ambiente ambiente = ambientesPorId.get(idAmbiente);

                if (usuario == null || ambiente == null) {
                    throw new SQLException("No se encontro el usuario o ambiente de la reserva "
                            + resultado.getInt("id_reserva"));
                }

                Reserva reserva = Reserva.reconstruir(
                        resultado.getInt("id_reserva"),
                        resultado.getDate("fecha").toLocalDate(),
                        resultado.getTime("hora_inicio").toLocalTime(),
                        resultado.getTime("hora_fin").toLocalTime(),
                        EstadoReserva.valueOf(resultado.getString("estado")),
                        usuario,
                        ambiente
                );

                reservas.add(reserva);
            }
        }

        return reservas;
    }
}
