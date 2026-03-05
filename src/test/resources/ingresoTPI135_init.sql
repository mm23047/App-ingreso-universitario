-- =============================================================
-- Script de inicialización para pruebas de integración
-- Base de datos: ingresoTPI135
-- Generado: 2026-03-03
-- =============================================================

-- -------------------------------------------------------------
-- DDL - Creación de tablas en orden de dependencias FK
-- -------------------------------------------------------------

-- Nivel 0: Tablas sin dependencias externas

CREATE TABLE IF NOT EXISTS areas_conocimiento (
    id_area     UUID        NOT NULL,
    nombre_area VARCHAR(100) NOT NULL,
    CONSTRAINT pk_areas_conocimiento PRIMARY KEY (id_area)
);

CREATE TABLE IF NOT EXISTS usuarios_sistema (
    id_usuario      UUID        NOT NULL,
    nombre_usuario  VARCHAR(50)  NOT NULL,
    correo          VARCHAR(100) NOT NULL,
    contrasena_hash TEXT         NOT NULL,
    rol             VARCHAR(20)  NOT NULL,
    CONSTRAINT pk_usuarios_sistema PRIMARY KEY (id_usuario)
);

CREATE TABLE IF NOT EXISTS catalogo_carreras (
    id_carrera VARCHAR(10)  NOT NULL,
    nombre     VARCHAR(100) NOT NULL,
    CONSTRAINT pk_catalogo_carreras PRIMARY KEY (id_carrera)
);

CREATE TABLE IF NOT EXISTS etapas_admision (
    id_etapa        UUID          NOT NULL,
    nombre          VARCHAR(50)   NOT NULL,
    puntaje_minimo  NUMERIC(5,2),
    puntaje_maximo  NUMERIC(5,2),
    descripcion     TEXT,
    CONSTRAINT pk_etapas_admision PRIMARY KEY (id_etapa)
);

CREATE TABLE IF NOT EXISTS pruebas_admision (
    id_prueba     UUID         NOT NULL,
    nombre_prueba VARCHAR(100) NOT NULL,
    anio          INTEGER      NOT NULL,
    activa        BOOLEAN      DEFAULT TRUE,
    CONSTRAINT pk_pruebas_admision PRIMARY KEY (id_prueba)
);

-- Nivel 1: Tablas con una dependencia

CREATE TABLE IF NOT EXISTS aspirantes_datos (
    id_aspirante     UUID         NOT NULL,
    id_usuario       UUID         NOT NULL,
    nombres          VARCHAR(100) NOT NULL,
    apellidos        VARCHAR(100) NOT NULL,
    dui              VARCHAR(12)  NOT NULL,
    usa_silla_ruedas BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_aspirantes_datos       PRIMARY KEY (id_aspirante),
    CONSTRAINT fk_aspirantes_datos_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuarios_sistema(id_usuario)
);

CREATE TABLE IF NOT EXISTS banco_preguntas (
    id_pregunta UUID NOT NULL,
    id_area     UUID NOT NULL,
    enunciado   TEXT NOT NULL,
    CONSTRAINT pk_banco_preguntas       PRIMARY KEY (id_pregunta),
    CONSTRAINT fk_banco_preguntas_area  FOREIGN KEY (id_area)
        REFERENCES areas_conocimiento(id_area)
);

CREATE TABLE IF NOT EXISTS turnos_examen (
    id_turno     UUID        NOT NULL,
    id_prueba    UUID        NOT NULL,
    nombre_turno VARCHAR(50) NOT NULL,
    fecha        DATE        NOT NULL,
    hora_inicio  TIME        NOT NULL,
    hora_fin     TIME        NOT NULL,
    CONSTRAINT pk_turnos_examen       PRIMARY KEY (id_turno),
    CONSTRAINT fk_turnos_examen_prueba FOREIGN KEY (id_prueba)
        REFERENCES pruebas_admision(id_prueba)
);

CREATE TABLE IF NOT EXISTS claves_examen (
    id_clave     UUID        NOT NULL,
    id_prueba    UUID        NOT NULL,
    nombre_clave VARCHAR(50) NOT NULL,
    CONSTRAINT pk_claves_examen       PRIMARY KEY (id_clave),
    CONSTRAINT fk_claves_examen_prueba FOREIGN KEY (id_prueba)
        REFERENCES pruebas_admision(id_prueba)
);

-- Nivel 2: Tablas con dos dependencias

CREATE TABLE IF NOT EXISTS inscripciones_prueba (
    id_inscripcion UUID        NOT NULL,
    id_aspirante   UUID        NOT NULL,
    id_prueba      UUID        NOT NULL,
    estado         VARCHAR(20) DEFAULT 'INSCRITO',
    CONSTRAINT pk_inscripciones_prueba           PRIMARY KEY (id_inscripcion),
    CONSTRAINT fk_inscripciones_prueba_aspirante FOREIGN KEY (id_aspirante)
        REFERENCES aspirantes_datos(id_aspirante),
    CONSTRAINT fk_inscripciones_prueba_prueba    FOREIGN KEY (id_prueba)
        REFERENCES pruebas_admision(id_prueba)
);

CREATE TABLE IF NOT EXISTS aulas_examen (
    id_aula                UUID        NOT NULL,
    id_turno               UUID        NOT NULL,
    id_aula_api            VARCHAR(50) NOT NULL,
    capacidad              INTEGER     NOT NULL,
    cupos_ocupados         INTEGER     DEFAULT 0,
    accesible_silla_ruedas BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_aulas_examen       PRIMARY KEY (id_aula),
    CONSTRAINT fk_aulas_examen_turno FOREIGN KEY (id_turno)
        REFERENCES turnos_examen(id_turno)
);

CREATE TABLE IF NOT EXISTS opciones_respuesta (
    id_opcion   UUID    NOT NULL,
    id_pregunta UUID    NOT NULL,
    texto_opcion TEXT   NOT NULL,
    es_correcta BOOLEAN NOT NULL,
    CONSTRAINT pk_opciones_respuesta        PRIMARY KEY (id_opcion),
    CONSTRAINT fk_opciones_respuesta_pregunta FOREIGN KEY (id_pregunta)
        REFERENCES banco_preguntas(id_pregunta)
);

CREATE TABLE IF NOT EXISTS preguntas_por_clave (
    id_clave    UUID NOT NULL,
    id_pregunta UUID NOT NULL,
    CONSTRAINT pk_preguntas_por_clave  PRIMARY KEY (id_clave, id_pregunta),
    CONSTRAINT fk_ppk_clave            FOREIGN KEY (id_clave)
        REFERENCES claves_examen(id_clave),
    CONSTRAINT fk_ppk_pregunta         FOREIGN KEY (id_pregunta)
        REFERENCES banco_preguntas(id_pregunta)
);

-- Nivel 3

CREATE TABLE IF NOT EXISTS asignaciones_aula_pupitre (
    id_asignacion  UUID        NOT NULL,
    id_inscripcion UUID        NOT NULL,
    id_aula        UUID        NOT NULL,
    pupitre        VARCHAR(20) NOT NULL,
    CONSTRAINT pk_asignaciones_aula_pupitre PRIMARY KEY (id_asignacion),
    CONSTRAINT fk_aap_inscripcion           FOREIGN KEY (id_inscripcion)
        REFERENCES inscripciones_prueba(id_inscripcion),
    CONSTRAINT fk_aap_aula                  FOREIGN KEY (id_aula)
        REFERENCES aulas_examen(id_aula)
);

CREATE TABLE IF NOT EXISTS carreras_elegidas (
    id_inscripcion UUID        NOT NULL,
    id_carrera     VARCHAR(10) NOT NULL,
    prioridad      SMALLINT    NOT NULL,
    CONSTRAINT pk_carreras_elegidas          PRIMARY KEY (id_inscripcion, id_carrera),
    CONSTRAINT fk_carreras_elegidas_inscripcion FOREIGN KEY (id_inscripcion)
        REFERENCES inscripciones_prueba(id_inscripcion),
    CONSTRAINT fk_carreras_elegidas_carrera  FOREIGN KEY (id_carrera)
        REFERENCES catalogo_carreras(id_carrera)
);

CREATE TABLE IF NOT EXISTS cupos_carrera (
    id_prueba  UUID        NOT NULL,
    id_carrera VARCHAR(10) NOT NULL,
    id_etapa   UUID        NOT NULL,
    cupos      INTEGER     NOT NULL,
    CONSTRAINT pk_cupos_carrera       PRIMARY KEY (id_prueba, id_carrera, id_etapa),
    CONSTRAINT fk_cupos_carrera_prueba  FOREIGN KEY (id_prueba)
        REFERENCES pruebas_admision(id_prueba),
    CONSTRAINT fk_cupos_carrera_carrera FOREIGN KEY (id_carrera)
        REFERENCES catalogo_carreras(id_carrera),
    CONSTRAINT fk_cupos_carrera_etapa   FOREIGN KEY (id_etapa)
        REFERENCES etapas_admision(id_etapa)
);

CREATE TABLE IF NOT EXISTS proceso_admision_aspirante (
    id_inscripcion   UUID        NOT NULL,
    id_etapa_actual  UUID        NOT NULL,
    estado           VARCHAR(20) NOT NULL,
    carrera_asignada VARCHAR(10),
    CONSTRAINT pk_proceso_admision_aspirante PRIMARY KEY (id_inscripcion),
    CONSTRAINT fk_paa_inscripcion            FOREIGN KEY (id_inscripcion)
        REFERENCES inscripciones_prueba(id_inscripcion),
    CONSTRAINT fk_paa_etapa                  FOREIGN KEY (id_etapa_actual)
        REFERENCES etapas_admision(id_etapa),
    CONSTRAINT fk_paa_carrera                FOREIGN KEY (carrera_asignada)
        REFERENCES catalogo_carreras(id_carrera)
);

-- Nivel 4

CREATE TABLE IF NOT EXISTS examenes_realizados (
    id_examen        UUID                     NOT NULL,
    id_asignacion    UUID                     NOT NULL,
    id_clave         UUID                     NOT NULL,
    puntaje_final    NUMERIC(5,2),
    fecha_realizacion TIMESTAMPTZ,
    id_etapa         UUID                     NOT NULL,
    CONSTRAINT pk_examenes_realizados PRIMARY KEY (id_examen),
    CONSTRAINT fk_er_asignacion       FOREIGN KEY (id_asignacion)
        REFERENCES asignaciones_aula_pupitre(id_asignacion),
    CONSTRAINT fk_er_clave            FOREIGN KEY (id_clave)
        REFERENCES claves_examen(id_clave),
    CONSTRAINT fk_er_etapa            FOREIGN KEY (id_etapa)
        REFERENCES etapas_admision(id_etapa)
);

CREATE TABLE IF NOT EXISTS respuestas_examen (
    id_respuesta          UUID NOT NULL,
    id_examen             UUID NOT NULL,
    id_pregunta           UUID NOT NULL,
    id_opcion_seleccionada UUID NOT NULL,
    CONSTRAINT pk_respuestas_examen   PRIMARY KEY (id_respuesta),
    CONSTRAINT fk_re_examen           FOREIGN KEY (id_examen)
        REFERENCES examenes_realizados(id_examen),
    CONSTRAINT fk_re_pregunta         FOREIGN KEY (id_pregunta)
        REFERENCES banco_preguntas(id_pregunta),
    CONSTRAINT fk_re_opcion           FOREIGN KEY (id_opcion_seleccionada)
        REFERENCES opciones_respuesta(id_opcion)
);

-- =============================================================
-- DML - Datos de prueba
-- =============================================================

-- areas_conocimiento
INSERT INTO areas_conocimiento (id_area, nombre_area) VALUES
    ('a1000000-0000-0000-0000-000000000001', 'Matemáticas'),
    ('a1000000-0000-0000-0000-000000000002', 'Ciencias Naturales'),
    ('a1000000-0000-0000-0000-000000000003', 'Lenguaje y Literatura');

-- usuarios_sistema
INSERT INTO usuarios_sistema (id_usuario, nombre_usuario, correo, contrasena_hash, rol) VALUES
    ('b1000000-0000-0000-0000-000000000001', 'admin',         'admin@ues.edu.sv',           '$2a$10$hash1', 'ADMIN'),
    ('b1000000-0000-0000-0000-000000000002', 'jperez',        'jperez@correo.com',          '$2a$10$hash2', 'ASPIRANTE'),
    ('b1000000-0000-0000-0000-000000000003', 'mmartinez',     'mmartinez@correo.com',       '$2a$10$hash3', 'ASPIRANTE');

-- catalogo_carreras
INSERT INTO catalogo_carreras (id_carrera, nombre) VALUES
    ('ICS',  'Ingeniería en Ciencias de la Computación'),
    ('ISI',  'Ingeniería de Sistemas Informáticos'),
    ('ICC',  'Ingeniería en Computación'),
    ('MAT',  'Licenciatura en Matemáticas');

-- etapas_admision
INSERT INTO etapas_admision (id_etapa, nombre, puntaje_minimo, puntaje_maximo, descripcion) VALUES
    ('c1000000-0000-0000-0000-000000000001', 'Etapa 1 - Matemáticas',  0.00, 10.00, 'Evaluación de conocimientos matemáticos'),
    ('c1000000-0000-0000-0000-000000000002', 'Etapa 2 - Ciencias',     0.00, 10.00, 'Evaluación de ciencias naturales'),
    ('c1000000-0000-0000-0000-000000000003', 'Etapa Final',            5.00, 10.00, 'Etapa final de selección');

-- pruebas_admision
INSERT INTO pruebas_admision (id_prueba, nombre_prueba, anio, activa) VALUES
    ('d1000000-0000-0000-0000-000000000001', 'Prueba de Admisión 2026 - Ciclo 01', 2026, TRUE),
    ('d1000000-0000-0000-0000-000000000002', 'Prueba de Admisión 2025 - Ciclo 01', 2025, FALSE);

-- aspirantes_datos
INSERT INTO aspirantes_datos (id_aspirante, id_usuario, nombres, apellidos, dui, usa_silla_ruedas) VALUES
    ('e1000000-0000-0000-0000-000000000001', 'b1000000-0000-0000-0000-000000000002', 'Juan Carlos',  'Pérez López',    '01234567-8', FALSE),
    ('e1000000-0000-0000-0000-000000000002', 'b1000000-0000-0000-0000-000000000003', 'María José',   'Martínez Rivas', '09876543-2', FALSE);

-- banco_preguntas
INSERT INTO banco_preguntas (id_pregunta, id_area, enunciado) VALUES
    ('f1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000001', '¿Cuánto es 2 + 2?'),
    ('f1000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000001', '¿Cuál es la raíz cuadrada de 144?'),
    ('f1000000-0000-0000-0000-000000000003', 'a1000000-0000-0000-0000-000000000002', '¿Cuántos planetas tiene el sistema solar?'),
    ('f1000000-0000-0000-0000-000000000004', 'a1000000-0000-0000-0000-000000000003', '¿Cuántas letras tiene el abecedario español?');

-- turnos_examen
INSERT INTO turnos_examen (id_turno, id_prueba, nombre_turno, fecha, hora_inicio, hora_fin) VALUES
    ('07000000-0000-0000-0000-000000000001', 'd1000000-0000-0000-0000-000000000001', 'Turno Mañana',  '2026-04-15', '07:00', '10:00'),
    ('07000000-0000-0000-0000-000000000002', 'd1000000-0000-0000-0000-000000000001', 'Turno Tarde',   '2026-04-15', '13:00', '16:00');

-- claves_examen
INSERT INTO claves_examen (id_clave, id_prueba, nombre_clave) VALUES
    ('08000000-0000-0000-0000-000000000001', 'd1000000-0000-0000-0000-000000000001', 'Clave A'),
    ('08000000-0000-0000-0000-000000000002', 'd1000000-0000-0000-0000-000000000001', 'Clave B');

-- inscripciones_prueba
INSERT INTO inscripciones_prueba (id_inscripcion, id_aspirante, id_prueba, estado) VALUES
    ('09000000-0000-0000-0000-000000000001', 'e1000000-0000-0000-0000-000000000001', 'd1000000-0000-0000-0000-000000000001', 'INSCRITO'),
    ('09000000-0000-0000-0000-000000000002', 'e1000000-0000-0000-0000-000000000002', 'd1000000-0000-0000-0000-000000000001', 'INSCRITO');

-- aulas_examen
INSERT INTO aulas_examen (id_aula, id_turno, id_aula_api, capacidad, cupos_ocupados, accesible_silla_ruedas) VALUES
    ('0a000000-0000-0000-0000-000000000001', '07000000-0000-0000-0000-000000000001', 'AULA-101', 40, 2, FALSE),
    ('0a000000-0000-0000-0000-000000000002', '07000000-0000-0000-0000-000000000002', 'AULA-201', 35, 0, TRUE);

-- opciones_respuesta
INSERT INTO opciones_respuesta (id_opcion, id_pregunta, texto_opcion, es_correcta) VALUES
    ('0b000000-0000-0000-0000-000000000001', 'f1000000-0000-0000-0000-000000000001', '3',  FALSE),
    ('0b000000-0000-0000-0000-000000000002', 'f1000000-0000-0000-0000-000000000001', '4',  TRUE),
    ('0b000000-0000-0000-0000-000000000003', 'f1000000-0000-0000-0000-000000000001', '5',  FALSE),
    ('0b000000-0000-0000-0000-000000000004', 'f1000000-0000-0000-0000-000000000002', '10', FALSE),
    ('0b000000-0000-0000-0000-000000000005', 'f1000000-0000-0000-0000-000000000002', '12', TRUE),
    ('0b000000-0000-0000-0000-000000000006', 'f1000000-0000-0000-0000-000000000002', '14', FALSE),
    ('0b000000-0000-0000-0000-000000000007', 'f1000000-0000-0000-0000-000000000003', '7',  FALSE),
    ('0b000000-0000-0000-0000-000000000008', 'f1000000-0000-0000-0000-000000000003', '8',  TRUE),
    ('0b000000-0000-0000-0000-000000000009', 'f1000000-0000-0000-0000-000000000004', '27', FALSE),
    ('0b000000-0000-0000-0000-000000000010', 'f1000000-0000-0000-0000-000000000004', '29', TRUE);

-- preguntas_por_clave
INSERT INTO preguntas_por_clave (id_clave, id_pregunta) VALUES
    ('08000000-0000-0000-0000-000000000001', 'f1000000-0000-0000-0000-000000000001'),
    ('08000000-0000-0000-0000-000000000001', 'f1000000-0000-0000-0000-000000000002'),
    ('08000000-0000-0000-0000-000000000002', 'f1000000-0000-0000-0000-000000000003'),
    ('08000000-0000-0000-0000-000000000002', 'f1000000-0000-0000-0000-000000000004');

-- asignaciones_aula_pupitre
INSERT INTO asignaciones_aula_pupitre (id_asignacion, id_inscripcion, id_aula, pupitre) VALUES
    ('0c000000-0000-0000-0000-000000000001', '09000000-0000-0000-0000-000000000001', '0a000000-0000-0000-0000-000000000001', 'A-01'),
    ('0c000000-0000-0000-0000-000000000002', '09000000-0000-0000-0000-000000000002', '0a000000-0000-0000-0000-000000000001', 'A-02');

-- carreras_elegidas
INSERT INTO carreras_elegidas (id_inscripcion, id_carrera, prioridad) VALUES
    ('09000000-0000-0000-0000-000000000001', 'ICS', 1),
    ('09000000-0000-0000-0000-000000000001', 'ISI', 2),
    ('09000000-0000-0000-0000-000000000002', 'ISI', 1),
    ('09000000-0000-0000-0000-000000000002', 'ICC', 2);

-- cupos_carrera
INSERT INTO cupos_carrera (id_prueba, id_carrera, id_etapa, cupos) VALUES
    ('d1000000-0000-0000-0000-000000000001', 'ICS', 'c1000000-0000-0000-0000-000000000003', 50),
    ('d1000000-0000-0000-0000-000000000001', 'ISI', 'c1000000-0000-0000-0000-000000000003', 60),
    ('d1000000-0000-0000-0000-000000000001', 'ICC', 'c1000000-0000-0000-0000-000000000003', 45);

-- proceso_admision_aspirante
INSERT INTO proceso_admision_aspirante (id_inscripcion, id_etapa_actual, estado, carrera_asignada) VALUES
    ('09000000-0000-0000-0000-000000000001', 'c1000000-0000-0000-0000-000000000001', 'EN_PROCESO', NULL),
    ('09000000-0000-0000-0000-000000000002', 'c1000000-0000-0000-0000-000000000001', 'EN_PROCESO', NULL);

-- examenes_realizados
INSERT INTO examenes_realizados (id_examen, id_asignacion, id_clave, puntaje_final, fecha_realizacion, id_etapa) VALUES
    ('0d000000-0000-0000-0000-000000000001', '0c000000-0000-0000-0000-000000000001', '08000000-0000-0000-0000-000000000001', 8.50, '2026-04-15 09:00:00+00', 'c1000000-0000-0000-0000-000000000001'),
    ('0d000000-0000-0000-0000-000000000002', '0c000000-0000-0000-0000-000000000002', '08000000-0000-0000-0000-000000000001', 7.00, '2026-04-15 09:15:00+00', 'c1000000-0000-0000-0000-000000000001');

-- respuestas_examen
INSERT INTO respuestas_examen (id_respuesta, id_examen, id_pregunta, id_opcion_seleccionada) VALUES
    ('0e000000-0000-0000-0000-000000000001', '0d000000-0000-0000-0000-000000000001', 'f1000000-0000-0000-0000-000000000001', '0b000000-0000-0000-0000-000000000002'),
    ('0e000000-0000-0000-0000-000000000002', '0d000000-0000-0000-0000-000000000001', 'f1000000-0000-0000-0000-000000000002', '0b000000-0000-0000-0000-000000000005'),
    ('0e000000-0000-0000-0000-000000000003', '0d000000-0000-0000-0000-000000000002', 'f1000000-0000-0000-0000-000000000001', '0b000000-0000-0000-0000-000000000002'),
    ('0e000000-0000-0000-0000-000000000004', '0d000000-0000-0000-0000-000000000002', 'f1000000-0000-0000-0000-000000000002', '0b000000-0000-0000-0000-000000000004');
