package com.aulacheck.persistence.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexionBD {
    private static final String ARCHIVO_CONFIG = "/config.properties";

    public static Connection obtenerConexion() throws SQLException {
        Properties propiedades = new Properties();

        try (InputStream entrada = ConexionBD.class.getResourceAsStream(ARCHIVO_CONFIG)) {
            if (entrada == null) {
                throw new IllegalStateException("No se encontro config.properties");
            }
            propiedades.load(entrada);
        } catch (IOException e) {
            throw new IllegalStateException("Error al leer la configuracion", e);
        }

        String url = propiedades.getProperty("db.url");
        String usuario = propiedades.getProperty("db.user");
        String contrasena = propiedades.getProperty("db.password");

        return DriverManager.getConnection(url, usuario, contrasena);
    }
}
