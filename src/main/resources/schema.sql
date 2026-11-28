-- Tabla de ambientes
CREATE TABLE ambiente (
    id_ambiente INTEGER PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    ubicacion VARCHAR(150),
    capacidad INTEGER NOT NULL CHECK (capacidad > 0),
    estado VARCHAR(30) NOT NULL DEFAULT 'DISPONIBLE'
        CHECK (estado IN ('DISPONIBLE', 'EN_MANTENIMIENTO', 'INACTIVO')),
    tipo VARCHAR(30) NOT NULL
        CHECK (tipo IN ('Aula', 'Laboratorio', 'Sala de estudio')),
    tiene_proyector BOOLEAN,
    num_equipos INTEGER,
    tipo_laboratorio VARCHAR(100),
    tiene_pizarra BOOLEAN,
    CHECK (
        (tipo = 'Aula' AND tiene_proyector IS NOT NULL
            AND num_equipos IS NULL AND tipo_laboratorio IS NULL
            AND tiene_pizarra IS NULL)
        OR
        (tipo = 'Laboratorio' AND num_equipos IS NOT NULL
            AND tipo_laboratorio IS NOT NULL AND tiene_proyector IS NULL
            AND tiene_pizarra IS NULL)
        OR
        (tipo = 'Sala de estudio' AND tiene_pizarra IS NOT NULL
            AND tiene_proyector IS NULL AND num_equipos IS NULL
            AND tipo_laboratorio IS NULL)
    )
);
-- Tabla de usuarios
CREATE TABLE usuario (
    id_usuario INTEGER PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    rol VARCHAR(30) NOT NULL
        CHECK (rol IN ('ESTUDIANTE', 'DOCENTE', 'ADMINISTRADOR', 'MANTENIMIENTO'))
);
-- Tabla de reservas
CREATE TABLE reserva (
    id_reserva INTEGER PRIMARY KEY,
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA'
        CHECK (estado IN ('ACTIVA', 'CANCELADA')),
    id_usuario INTEGER NOT NULL,
    id_ambiente INTEGER NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    FOREIGN KEY (id_ambiente) REFERENCES ambiente(id_ambiente),
    CHECK (hora_inicio < hora_fin)
);

-- Tabla de incidencias
CREATE TABLE incidencia (
    id_incidencia INTEGER PRIMARY KEY,
    descripcion TEXT NOT NULL,
    fecha_reporte DATE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ABIERTA'
        CHECK (estado IN ('ABIERTA', 'EN_PROCESO', 'RESUELTA')),
    id_usuario INTEGER NOT NULL,
    id_ambiente INTEGER NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    FOREIGN KEY (id_ambiente) REFERENCES ambiente(id_ambiente)
);
