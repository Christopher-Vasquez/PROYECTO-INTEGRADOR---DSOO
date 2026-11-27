package com.aulacheck.persistence.json;

import com.aulacheck.exception.AulaCheckException;
import com.aulacheck.model.RolUsuario;
import com.aulacheck.model.Usuario;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Repositorio json para persistir usuarios del sistema. */
public class UsuarioJsonRepository {

    private final JsonStore store;
    private final Path ruta;

    public UsuarioJsonRepository(Path ruta) {
        this.store = new JsonStore();
        this.ruta = ruta;
    }

    public void guardarTodos(List<Usuario> usuarios) throws AulaCheckException {
        List<UsuarioDTO> dtos = new ArrayList<>();
        for (Usuario u : usuarios) {
            UsuarioDTO dto = new UsuarioDTO();
            dto.idUsuario = u.getIdUsuario();
            dto.nombre = u.getNombre();
            dto.email = u.getEmail();
            dto.rol = u.getRol().name();
            dtos.add(dto);
        }
        store.guardar(ruta, dtos);
    }

    public List<Usuario> cargarTodos() throws AulaCheckException {
        List<UsuarioDTO> dtos = store.cargar(ruta, UsuarioDTO.class);
        List<Usuario> usuarios = new ArrayList<>();
        for (UsuarioDTO dto : dtos) {
            usuarios.add(new Usuario(
                    dto.idUsuario,
                    dto.nombre,
                    dto.email,
                    RolUsuario.valueOf(dto.rol)
            ));
        }
        return usuarios;
    }

    private static class UsuarioDTO {
        int idUsuario;
        String nombre;
        String email;
        String rol;
    }
}