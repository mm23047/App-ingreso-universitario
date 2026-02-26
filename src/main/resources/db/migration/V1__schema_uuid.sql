-- =============================================================
-- V1: Migración a UUIDs
-- Elimina las tablas con PKs integer/smallint y las recrea
-- con tipo uuid. Todos los IDs surrogados pasan a uuid.
-- CatalogoCarrera conserva su PK varchar(10) (código de negocio).
-- =============================================================

-- -------------------------------------------------------
-- 1. Eliminar tablas (orden: hijos → padres) con CASCADE
-- -------------------------------------------------------
DROP TABLE IF EXISTS respuestas_examen CASCADE;
DROP TABLE IF EXISTS examenes_realizados CASCADE;
DROP TABLE IF EXISTS asignaciones_aula_pupitre CASCADE;
DROP TABLE IF EXISTS preguntas_por_clave CASCADE;
DROP TABLE IF EXISTS cupos_carrera CASCADE;
DROP TABLE IF EXISTS carreras_elegidas CASCADE;
DROP TABLE IF EXISTS proceso_admision_aspirante CASCADE;
DROP TABLE IF EXISTS opciones_respuesta CASCADE;
DROP TABLE IF EXISTS aulas_examen CASCADE;
DROP TABLE IF EXISTS inscripciones_prueba CASCADE;
DROP TABLE IF EXISTS claves_examen CASCADE;
DROP TABLE IF EXISTS banco_preguntas CASCADE;
DROP TABLE IF EXISTS turnos_examen CASCADE;
DROP TABLE IF EXISTS aspirantes_datos CASCADE;
DROP TABLE IF EXISTS pruebas_admision CASCADE;
DROP TABLE IF EXISTS areas_conocimiento CASCADE;
DROP TABLE IF EXISTS etapas_admision CASCADE;
DROP TABLE IF EXISTS catalogo_carreras CASCADE;
DROP TABLE IF EXISTS usuarios_sistema CASCADE;

-- -------------------------------------------------------
-- 2. Eliminar secuencias legacy (ya no necesarias con UUID)
-- -------------------------------------------------------
DROP SEQUENCE IF EXISTS banco_preguntas_id_pregunta_seq;
DROP SEQUENCE IF EXISTS claves_examen_id_clave_seq;
DROP SEQUENCE IF EXISTS etapas_admision_id_etapa_seq;
DROP SEQUENCE IF EXISTS examenes_realizados_id_examen_seq;
DROP SEQUENCE IF EXISTS inscripciones_prueba_id_inscripcion_seq;
DROP SEQUENCE IF EXISTS opciones_respuesta_id_opcion_seq;
DROP SEQUENCE IF EXISTS pruebas_admision_id_prueba_seq;
DROP SEQUENCE IF EXISTS areas_conocimiento_id_area_seq;
DROP SEQUENCE IF EXISTS asignaciones_aula_pupitre_id_asignacion_seq;
DROP SEQUENCE IF EXISTS aspirantes_datos_id_aspirante_seq;
DROP SEQUENCE IF EXISTS aulas_examen_id_aula_seq;
DROP SEQUENCE IF EXISTS respuestas_examen_id_respuesta_seq;
DROP SEQUENCE IF EXISTS turnos_examen_id_turno_seq;
DROP SEQUENCE IF EXISTS usuarios_sistema_id_usuario_seq;

-- =============================================================
-- 3. Recrear tablas con UUID
-- =============================================================

-- -------------------------------------------------------
-- 3.1 Tablas sin FKs (raíces del grafo de dependencias)
-- -------------------------------------------------------

CREATE TABLE usuarios_sistema (
    id_usuario       uuid         NOT NULL,
    nombre_usuario   varchar(50)  NOT NULL,
    correo           varchar(100) NOT NULL,
    contrasena_hash  text         NOT NULL,
    rol              varchar(20)  NOT NULL,
    CONSTRAINT pk_usuarios_sistema PRIMARY KEY (id_usuario)
);

CREATE TABLE etapas_admision (
    id_etapa        uuid           NOT NULL,
    nombre          varchar(50)    NOT NULL,
    puntaje_minimo  numeric(5, 2),
    puntaje_maximo  numeric(5, 2),
    descripcion     text,
    CONSTRAINT pk_etapas_admision PRIMARY KEY (id_etapa)
);

CREATE TABLE areas_conocimiento (
    id_area     uuid         NOT NULL,
    nombre_area varchar(100) NOT NULL,
    CONSTRAINT pk_areas_conocimiento PRIMARY KEY (id_area)
);

CREATE TABLE pruebas_admision (
    id_prueba     uuid         NOT NULL,
    nombre_prueba varchar(100) NOT NULL,
    anio          integer      NOT NULL,
    activa        boolean      DEFAULT true,
    CONSTRAINT pk_pruebas_admision PRIMARY KEY (id_prueba)
);

-- Catálogo de carreras: PK es código de negocio (varchar) — no cambia
CREATE TABLE catalogo_carreras (
    id_carrera varchar(10)  NOT NULL,
    nombre     varchar(100) NOT NULL,
    CONSTRAINT pk_catalogo_carreras PRIMARY KEY (id_carrera)
);

-- -------------------------------------------------------
-- 3.2 Tablas con una FK
-- -------------------------------------------------------

CREATE TABLE aspirantes_datos (
    id_aspirante      uuid         NOT NULL,
    id_usuario        uuid         NOT NULL,
    nombres           varchar(100) NOT NULL,
    apellidos         varchar(100) NOT NULL,
    dui               varchar(12)  NOT NULL,
    usa_silla_ruedas  boolean      NOT NULL DEFAULT false,
    CONSTRAINT pk_aspirantes_datos PRIMARY KEY (id_aspirante),
    CONSTRAINT fk_aspirantes_datos_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuarios_sistema (id_usuario)
);

CREATE TABLE turnos_examen (
    id_turno      uuid        NOT NULL,
    id_prueba     uuid        NOT NULL,
    nombre_turno  varchar(50) NOT NULL,
    fecha         date        NOT NULL,
    hora_inicio   time        NOT NULL,
    hora_fin      time        NOT NULL,
    CONSTRAINT pk_turnos_examen PRIMARY KEY (id_turno),
    CONSTRAINT fk_turnos_examen_prueba
        FOREIGN KEY (id_prueba) REFERENCES pruebas_admision (id_prueba)
);

CREATE TABLE banco_preguntas (
    id_pregunta uuid NOT NULL,
    id_area     uuid NOT NULL,
    enunciado   text NOT NULL,
    CONSTRAINT pk_banco_preguntas PRIMARY KEY (id_pregunta),
    CONSTRAINT fk_banco_preguntas_area
        FOREIGN KEY (id_area) REFERENCES areas_conocimiento (id_area)
);

CREATE TABLE claves_examen (
    id_clave      uuid        NOT NULL,
    id_prueba     uuid        NOT NULL,
    nombre_clave  varchar(50) NOT NULL,
    CONSTRAINT pk_claves_examen PRIMARY KEY (id_clave),
    CONSTRAINT fk_claves_examen_prueba
        FOREIGN KEY (id_prueba) REFERENCES pruebas_admision (id_prueba)
);

-- -------------------------------------------------------
-- 3.3 Tablas con múltiples FKs
-- -------------------------------------------------------

CREATE TABLE inscripciones_prueba (
    id_inscripcion uuid        NOT NULL,
    id_aspirante   uuid        NOT NULL,
    id_prueba      uuid        NOT NULL,
    estado         varchar(20) DEFAULT 'INSCRITO',
    CONSTRAINT pk_inscripciones_prueba PRIMARY KEY (id_inscripcion),
    CONSTRAINT fk_inscripciones_prueba_aspirante
        FOREIGN KEY (id_aspirante) REFERENCES aspirantes_datos (id_aspirante),
    CONSTRAINT fk_inscripciones_prueba_prueba
        FOREIGN KEY (id_prueba) REFERENCES pruebas_admision (id_prueba)
);

CREATE TABLE aulas_examen (
    id_aula                 uuid        NOT NULL,
    id_turno                uuid        NOT NULL,
    id_aula_api             varchar(50) NOT NULL,
    capacidad               integer     NOT NULL,
    cupos_ocupados          integer     DEFAULT 0,
    accesible_silla_ruedas  boolean     NOT NULL DEFAULT false,
    CONSTRAINT pk_aulas_examen PRIMARY KEY (id_aula),
    CONSTRAINT fk_aulas_examen_turno
        FOREIGN KEY (id_turno) REFERENCES turnos_examen (id_turno)
);

CREATE TABLE opciones_respuesta (
    id_opcion    uuid    NOT NULL,
    id_pregunta  uuid    NOT NULL,
    texto_opcion text    NOT NULL,
    es_correcta  boolean NOT NULL DEFAULT false,
    CONSTRAINT pk_opciones_respuesta PRIMARY KEY (id_opcion),
    CONSTRAINT fk_opciones_respuesta_pregunta
        FOREIGN KEY (id_pregunta) REFERENCES banco_preguntas (id_pregunta)
);

CREATE TABLE proceso_admision_aspirante (
    id_inscripcion   uuid        NOT NULL,
    id_etapa_actual  uuid        NOT NULL,
    estado           varchar(30) NOT NULL,
    carrera_asignada varchar(10),
    CONSTRAINT pk_proceso_admision_aspirante PRIMARY KEY (id_inscripcion),
    CONSTRAINT fk_paa_inscripcion
        FOREIGN KEY (id_inscripcion) REFERENCES inscripciones_prueba (id_inscripcion),
    CONSTRAINT fk_paa_etapa
        FOREIGN KEY (id_etapa_actual) REFERENCES etapas_admision (id_etapa),
    CONSTRAINT fk_paa_carrera
        FOREIGN KEY (carrera_asignada) REFERENCES catalogo_carreras (id_carrera)
);

-- -------------------------------------------------------
-- 3.4 Tablas con PKs compuestas (uuid + varchar/uuid)
-- -------------------------------------------------------

CREATE TABLE carreras_elegidas (
    id_inscripcion uuid        NOT NULL,
    id_carrera     varchar(10) NOT NULL,
    prioridad      smallint    NOT NULL,
    CONSTRAINT pk_carreras_elegidas PRIMARY KEY (id_inscripcion, id_carrera),
    CONSTRAINT fk_carreras_elegidas_inscripcion
        FOREIGN KEY (id_inscripcion) REFERENCES inscripciones_prueba (id_inscripcion),
    CONSTRAINT fk_carreras_elegidas_carrera
        FOREIGN KEY (id_carrera) REFERENCES catalogo_carreras (id_carrera)
);

CREATE TABLE cupos_carrera (
    id_prueba  uuid        NOT NULL,
    id_carrera varchar(10) NOT NULL,
    id_etapa   uuid        NOT NULL,
    cupos      integer     NOT NULL,
    CONSTRAINT pk_cupos_carrera PRIMARY KEY (id_prueba, id_carrera, id_etapa),
    CONSTRAINT fk_cupos_carrera_prueba
        FOREIGN KEY (id_prueba) REFERENCES pruebas_admision (id_prueba),
    CONSTRAINT fk_cupos_carrera_carrera
        FOREIGN KEY (id_carrera) REFERENCES catalogo_carreras (id_carrera),
    CONSTRAINT fk_cupos_carrera_etapa
        FOREIGN KEY (id_etapa) REFERENCES etapas_admision (id_etapa)
);

CREATE TABLE preguntas_por_clave (
    id_clave    uuid NOT NULL,
    id_pregunta uuid NOT NULL,
    CONSTRAINT pk_preguntas_por_clave PRIMARY KEY (id_clave, id_pregunta),
    CONSTRAINT fk_ppk_clave
        FOREIGN KEY (id_clave) REFERENCES claves_examen (id_clave),
    CONSTRAINT fk_ppk_pregunta
        FOREIGN KEY (id_pregunta) REFERENCES banco_preguntas (id_pregunta)
);

-- -------------------------------------------------------
-- 3.5 Tablas intermedias/terminales
-- -------------------------------------------------------

CREATE TABLE asignaciones_aula_pupitre (
    id_asignacion  uuid        NOT NULL,
    id_inscripcion uuid        NOT NULL,
    id_aula        uuid        NOT NULL,
    pupitre        varchar(20) NOT NULL,
    CONSTRAINT pk_asignaciones_aula_pupitre PRIMARY KEY (id_asignacion),
    CONSTRAINT fk_aap_inscripcion
        FOREIGN KEY (id_inscripcion) REFERENCES inscripciones_prueba (id_inscripcion),
    CONSTRAINT fk_aap_aula
        FOREIGN KEY (id_aula) REFERENCES aulas_examen (id_aula)
);

CREATE TABLE examenes_realizados (
    id_examen           uuid           NOT NULL,
    id_asignacion       uuid           NOT NULL,
    id_clave            uuid           NOT NULL,
    puntaje_final       numeric(5, 2),
    fecha_realizacion   timestamptz    DEFAULT CURRENT_TIMESTAMP,
    id_etapa            uuid           NOT NULL,
    CONSTRAINT pk_examenes_realizados PRIMARY KEY (id_examen),
    CONSTRAINT fk_er_asignacion
        FOREIGN KEY (id_asignacion) REFERENCES asignaciones_aula_pupitre (id_asignacion),
    CONSTRAINT fk_er_clave
        FOREIGN KEY (id_clave) REFERENCES claves_examen (id_clave),
    CONSTRAINT fk_er_etapa
        FOREIGN KEY (id_etapa) REFERENCES etapas_admision (id_etapa)
);

CREATE TABLE respuestas_examen (
    id_respuesta           uuid NOT NULL,
    id_examen              uuid NOT NULL,
    id_pregunta            uuid NOT NULL,
    id_opcion_seleccionada uuid,
    CONSTRAINT pk_respuestas_examen PRIMARY KEY (id_respuesta),
    CONSTRAINT fk_re_examen
        FOREIGN KEY (id_examen) REFERENCES examenes_realizados (id_examen),
    CONSTRAINT fk_re_pregunta
        FOREIGN KEY (id_pregunta) REFERENCES banco_preguntas (id_pregunta),
    CONSTRAINT fk_re_opcion
        FOREIGN KEY (id_opcion_seleccionada) REFERENCES opciones_respuesta (id_opcion)
);
