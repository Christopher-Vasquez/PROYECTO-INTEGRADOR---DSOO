package com.aulacheck.persistence.jdbc;

import com.aulacheck.model.RolUsuario;
import com.aulacheck.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioJdbcRepository {

    public void guardarTodo(List<Usuario> usuarios) throws SQLException {
        String sql = "INSERT INTO usuario (id_usuario, nombre, email, rol) "
                + "VALUES (?, ?, ?, ?) "
                + "ON CONFLICT (id_usuario) DO UPDATE SET "
                + "nombre = EXCLUDED.nombre, "
                + "email = EXCLUDED.email, "
                + "rol = EXCLUDED.rol";

        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);

            try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
                for (Usuario usuario : usuarios) {
                    sentencia.setInt(1, usuario.getIdUsuario());
                    sentencia.setString(2, usuario.getNombre());
                    sentencia.setString(3, usuario.getEmail());
                    sentencia.setString(4, usuario.getRol().name());
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

    public List<Usuario> cargarTodo() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuario ORDER BY id_usuario";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                Usuario usuario = new Usuario(
                        resultado.getInt("id_usuario"),
                        resultado.getString("nombre"),
                        resultado.getString("email"),
                        RolUsuario.valueOf(resultado.getString("rol"))
                );

                usuarios.add(usuario);
            }
        }

        return usuarios;
    }
}
