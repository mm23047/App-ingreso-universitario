-- 1. Tablas Maestras (Catálogos)
CREATE TABLE IF NOT EXISTS area_conocimiento (
    id_area UUID PRIMARY KEY,
    nombre_area VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS catalogo_carrera (
    id_carrera VARCHAR(10) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS etapa_admision (
    id_etapa UUID PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    puntaje_minimo NUMERIC(5,2),
    puntaje_maximo NUMERIC(5,2),
    descripcion TEXT
);

CREATE TABLE IF NOT EXISTS prueba_admision (
    id_prueba UUID PRIMARY KEY,
    nombre_prueba VARCHAR(100) NOT NULL,
    anio INTEGER NOT NULL,
    activa BOOLEAN DEFAULT true,
    CONSTRAINT uk_nombre_anio UNIQUE (nombre_prueba, anio)
);

-- 2. Datos del Aspirante
CREATE TABLE IF NOT EXISTS aspirante_datos (
    id_aspirante UUID PRIMARY KEY,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    dui VARCHAR(12) NOT NULL UNIQUE,
    correo VARCHAR(100) NOT NULL UNIQUE,
    fecha_creacion_perfil DATE DEFAULT CURRENT_DATE,
    usa_silla_ruedas BOOLEAN NOT NULL DEFAULT false
);

-- Estructura Académica Refactorizada
CREATE TABLE IF NOT EXISTS tema (
    id_tema UUID PRIMARY KEY,
    id_area UUID NOT NULL REFERENCES area_conocimiento(id_area),
    nombre_tema VARCHAR(100) NOT NULL UNIQUE,
    -- LA CLAVE: Relación sobre sí misma (Recursividad)
    id_tema_padre UUID REFERENCES tema(id_tema)
);

CREATE TABLE IF NOT EXISTS banco_pregunta (
    id_pregunta UUID PRIMARY KEY,
    id_tema UUID NOT NULL REFERENCES tema(id_tema),
    enunciado TEXT NOT NULL UNIQUE -- Así nadie podrá escribir el mismo texto 2 veces en toda la BD
);

CREATE TABLE IF NOT EXISTS banco_respuesta (
    id_respuesta_global UUID PRIMARY KEY,
    texto_respuesta TEXT NOT NULL UNIQUE,
    -- LA CLAVE: Clasificar la respuesta por área
    id_area UUID NOT NULL REFERENCES area_conocimiento(id_area)
);

CREATE TABLE IF NOT EXISTS pregunta_opcion (
    id_pregunta_opcion UUID PRIMARY KEY,
    id_pregunta UUID NOT NULL REFERENCES banco_pregunta(id_pregunta),
    id_respuesta_global UUID NOT NULL REFERENCES banco_respuesta(id_respuesta_global),
    es_correcta BOOLEAN NOT NULL,
    CONSTRAINT uk_pregunta_respuesta UNIQUE (id_pregunta, id_respuesta_global)
);

-- 4. Logística y Turnos
CREATE TABLE IF NOT EXISTS turno_examen (
    id_turno UUID PRIMARY KEY,
    id_prueba UUID NOT NULL REFERENCES prueba_admision(id_prueba),
    nombre_turno VARCHAR(50) NOT NULL,
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    -- Validación de horas
    CONSTRAINT check_horas_distintas CHECK (hora_inicio < hora_fin)
);

CREATE TABLE IF NOT EXISTS aula (
    id_aula UUID PRIMARY KEY,
    codigo_aula_api VARCHAR(50) NOT NULL UNIQUE,
    capacidad_fisica INTEGER NOT NULL, -- La capacidad real del cuarto
    accesible_silla_ruedas BOOLEAN DEFAULT false
);

-- Esta tabla controla cuánta gente cabe en un aula en un horario específico
CREATE TABLE IF NOT EXISTS disponibilidad_aula_turno (
    id_aula UUID REFERENCES aula(id_aula),
    id_turno UUID REFERENCES turno_examen(id_turno),
    PRIMARY KEY (id_aula, id_turno)
);

-- 5. Inscripción y Proceso
CREATE TABLE IF NOT EXISTS inscripcion_prueba (
    id_inscripcion UUID PRIMARY KEY,
    id_aspirante UUID NOT NULL REFERENCES aspirante_datos(id_aspirante),
    id_prueba UUID NOT NULL REFERENCES prueba_admision(id_prueba),
    estado VARCHAR(20) DEFAULT 'INSCRITO',
    CONSTRAINT uk_aspirante_prueba UNIQUE (id_aspirante, id_prueba)
);

CREATE TABLE IF NOT EXISTS carrera_elegida (
    id_inscripcion UUID NOT NULL REFERENCES inscripcion_prueba(id_inscripcion),
    id_carrera VARCHAR(10) NOT NULL REFERENCES catalogo_carrera(id_carrera),
    prioridad SMALLINT NOT NULL,
    PRIMARY KEY (id_inscripcion, id_carrera),
    CONSTRAINT uk_inscripcion_prioridad UNIQUE (id_inscripcion, prioridad)
);

-- 6. Configuración del Examen (Claves)
CREATE TABLE IF NOT EXISTS clave_examen (
    id_clave UUID PRIMARY KEY,
    id_prueba UUID NOT NULL REFERENCES prueba_admision(id_prueba),
    nombre_clave VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS preguntas_por_clave (
    id_clave UUID NOT NULL REFERENCES clave_examen(id_clave),
    id_pregunta UUID NOT NULL REFERENCES banco_pregunta(id_pregunta),
    PRIMARY KEY (id_clave, id_pregunta)
);

-- 7. Resultados y Seguimiento
CREATE TABLE IF NOT EXISTS cupos_carrera (
    id_prueba UUID NOT NULL REFERENCES prueba_admision(id_prueba),
    id_carrera VARCHAR(10) NOT NULL REFERENCES catalogo_carrera(id_carrera),
    id_etapa UUID NOT NULL REFERENCES etapa_admision(id_etapa),
    cupos INTEGER NOT NULL,
    PRIMARY KEY (id_prueba, id_carrera, id_etapa)
);

CREATE TABLE IF NOT EXISTS proceso_admision_aspirante (
    id_inscripcion UUID PRIMARY KEY REFERENCES inscripcion_prueba(id_inscripcion),
    id_etapa_actual UUID NOT NULL REFERENCES etapa_admision(id_etapa),
    estado VARCHAR(20) NOT NULL,
    carrera_asignada VARCHAR(10) REFERENCES catalogo_carrera(id_carrera)
);

-- 8. Ejecución del Examen
CREATE TABLE IF NOT EXISTS examen_realizado (
    id_examen UUID PRIMARY KEY,
    id_inscripcion UUID NOT NULL REFERENCES inscripcion_prueba(id_inscripcion),
    id_clave UUID NOT NULL REFERENCES clave_examen(id_clave),
    id_etapa UUID NOT NULL REFERENCES etapa_admision(id_etapa),
    puntaje_final NUMERIC(5,2),
    fecha_realizacion TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_inscripcion_etapa UNIQUE (id_inscripcion, id_etapa)
);

CREATE TABLE IF NOT EXISTS respuesta_examen (
    id_respuesta_aspirante UUID PRIMARY KEY,
    id_examen UUID NOT NULL REFERENCES examen_realizado(id_examen),
    id_pregunta_opcion UUID NOT NULL REFERENCES pregunta_opcion(id_pregunta_opcion)
);


CREATE TABLE IF NOT EXISTS asignacion_aula_aspirante (
    id_asignacion UUID PRIMARY KEY,
    id_inscripcion UUID NOT NULL,
    id_aula UUID NOT NULL,
    id_turno UUID NOT NULL,

    -- Nos conectamos a la inscripción del aspirante
    CONSTRAINT fk_asignacion_inscripcion FOREIGN KEY (id_inscripcion)
        REFERENCES inscripcion_prueba(id_inscripcion),

    -- Nos conectamos directamente a la disponibilidad de ese turno y aula específicos
    CONSTRAINT fk_asignacion_disponibilidad FOREIGN KEY (id_aula, id_turno)
        REFERENCES disponibilidad_aula_turno(id_aula, id_turno),

    -- REGLA DE ORO: Un aspirante no puede estar en dos aulas en el mismo turno
    CONSTRAINT uk_inscripcion_turno UNIQUE (id_inscripcion, id_turno)
);


-- =========================================================
-- DATOS MAESTROS
-- =========================================================

INSERT INTO area_conocimiento VALUES
('11111111-1111-1111-1111-111111111111', 'Matemática'),
('22222222-2222-2222-2222-222222222222', 'Lenguaje'),
('33333333-3333-3333-3333-333333333333', 'Ciencias Naturales');

INSERT INTO catalogo_carrera VALUES
('ISI', 'Ingeniería en Sistemas Informáticos'),
('MED', 'Doctorado en Medicina'),
('ARQ', 'Arquitectura'),
('ADM', 'Administración de Empresas');

INSERT INTO etapa_admision VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
 'Primera Etapa',
 0,
 59.99,
 'Evaluación inicial'),

('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
 'Segunda Etapa',
 60,
 79.99,
 'Evaluación intermedia'),

('cccccccc-cccc-cccc-cccc-cccccccccccc',
 'Etapa Final',
 80,
 100,
 'Asignación definitiva');

-- =========================================================
-- PRUEBA
-- =========================================================

INSERT INTO prueba_admision VALUES
('dddddddd-dddd-dddd-dddd-dddddddddddd',
 'Prueba Nacional UES',
 2026,
 true);

-- =========================================================
-- ASPIRANTES
-- =========================================================

INSERT INTO aspirante_datos VALUES
(
 'e1111111-1111-1111-1111-111111111111',
 'Carlos Alberto',
 'Ramírez López',
 '2007-05-10',
 '01234567-8',
 'carlos.ramirez@gmail.com',
 CURRENT_DATE,
 false
),

(
 'e2222222-2222-2222-2222-222222222222',
 'María Fernanda',
 'Castillo Pérez',
 '2006-09-22',
 '02234567-8',
 'maria.castillo@gmail.com',
 CURRENT_DATE,
 true
),

(
 'e3333333-3333-3333-3333-333333333333',
 'José Miguel',
 'Hernández Flores',
 '2007-01-15',
 '03234567-8',
 'jose.hernandez@gmail.com',
 CURRENT_DATE,
 false
);

-- =========================================================
-- TEMAS
-- =========================================================

INSERT INTO tema VALUES
(
 't1111111-1111-1111-1111-111111111111',
 '11111111-1111-1111-1111-111111111111',
 'Álgebra',
 NULL
),

(
 't2222222-2222-2222-2222-222222222222',
 '11111111-1111-1111-1111-111111111111',
 'Ecuaciones Lineales',
 't1111111-1111-1111-1111-111111111111'
),

(
 't3333333-3333-3333-3333-333333333333',
 '22222222-2222-2222-2222-222222222222',
 'Comprensión Lectora',
 NULL
);

-- =========================================================
-- PREGUNTAS
-- =========================================================

INSERT INTO banco_pregunta VALUES
(
 'p1111111-1111-1111-1111-111111111111',
 't2222222-2222-2222-2222-222222222222',
 '¿Cuál es el valor de X en 2X + 4 = 10?'
),

(
 'p2222222-2222-2222-2222-222222222222',
 't3333333-3333-3333-3333-333333333333',
 '¿Cuál es la idea principal de un texto?'
);

-- =========================================================
-- RESPUESTAS GLOBALES
-- =========================================================

INSERT INTO banco_respuesta VALUES
(
 'r1111111-1111-1111-1111-111111111111',
 'X = 3',
 '11111111-1111-1111-1111-111111111111'
),

(
 'r2222222-2222-2222-2222-222222222222',
 'X = 5',
 '11111111-1111-1111-1111-111111111111'
),

(
 'r3333333-3333-3333-3333-333333333333',
 'El tema central del texto',
 '22222222-2222-2222-2222-222222222222'
),

(
 'r4444444-4444-4444-4444-444444444444',
 'El color favorito del autor',
 '22222222-2222-2222-2222-222222222222'
);

-- =========================================================
-- OPCIONES DE PREGUNTAS
-- =========================================================

INSERT INTO pregunta_opcion VALUES
(
 'o1111111-1111-1111-1111-111111111111',
 'p1111111-1111-1111-1111-111111111111',
 'r1111111-1111-1111-1111-111111111111',
 false
),

(
 'o2222222-2222-2222-2222-222222222222',
 'p1111111-1111-1111-1111-111111111111',
 'r2222222-2222-2222-2222-222222222222',
 true
),

(
 'o3333333-3333-3333-3333-333333333333',
 'p2222222-2222-2222-2222-222222222222',
 'r3333333-3333-3333-3333-333333333333',
 true
),

(
 'o4444444-4444-4444-4444-444444444444',
 'p2222222-2222-2222-2222-222222222222',
 'r4444444-4444-4444-4444-444444444444',
 false
);

-- =========================================================
-- TURNOS
-- =========================================================

INSERT INTO turno_examen VALUES
(
 'tr111111-1111-1111-1111-111111111111',
 'dddddddd-dddd-dddd-dddd-dddddddddddd',
 'Turno Mañana',
 '2026-07-15',
 '08:00',
 '11:00'
),

(
 'tr222222-2222-2222-2222-222222222222',
 'dddddddd-dddd-dddd-dddd-dddddddddddd',
 'Turno Tarde',
 '2026-07-15',
 '13:00',
 '16:00'
);

-- =========================================================
-- AULAS
-- =========================================================

INSERT INTO aula VALUES
(
 'au111111-1111-1111-1111-111111111111',
 'AULA-A101',
 40,
 true
),

(
 'au222222-2222-2222-2222-222222222222',
 'AULA-B202',
 35,
 false
);

-- =========================================================
-- DISPONIBILIDAD
-- =========================================================

INSERT INTO disponibilidad_aula_turno VALUES
(
 'au111111-1111-1111-1111-111111111111',
 'tr111111-1111-1111-1111-111111111111'
),

(
 'au222222-2222-2222-2222-222222222222',
 'tr222222-2222-2222-2222-222222222222'
);

-- =========================================================
-- INSCRIPCIONES
-- =========================================================

INSERT INTO inscripcion_prueba VALUES
(
 'in111111-1111-1111-1111-111111111111',
 'e1111111-1111-1111-1111-111111111111',
 'dddddddd-dddd-dddd-dddd-dddddddddddd',
 'INSCRITO'
),

(
 'in222222-2222-2222-2222-222222222222',
 'e2222222-2222-2222-2222-222222222222',
 'dddddddd-dddd-dddd-dddd-dddddddddddd',
 'INSCRITO'
);

-- =========================================================
-- CARRERAS ELEGIDAS
-- =========================================================

INSERT INTO carrera_elegida VALUES
(
 'in111111-1111-1111-1111-111111111111',
 'ISI',
 1
),

(
 'in111111-1111-1111-1111-111111111111',
 'ADM',
 2
),

(
 'in222222-2222-2222-2222-222222222222',
 'MED',
 1
);

-- =========================================================
-- CLAVES DE EXAMEN
-- =========================================================

INSERT INTO clave_examen VALUES
(
 'cl111111-1111-1111-1111-111111111111',
 'dddddddd-dddd-dddd-dddd-dddddddddddd',
 'Clave A'
);

INSERT INTO preguntas_por_clave VALUES
(
 'cl111111-1111-1111-1111-111111111111',
 'p1111111-1111-1111-1111-111111111111'
),

(
 'cl111111-1111-1111-1111-111111111111',
 'p2222222-2222-2222-2222-222222222222'
);

-- =========================================================
-- CUPOS
-- =========================================================

INSERT INTO cupos_carrera VALUES
(
 'dddddddd-dddd-dddd-dddd-dddddddddddd',
 'ISI',
 'cccccccc-cccc-cccc-cccc-cccccccccccc',
 100
),

(
 'dddddddd-dddd-dddd-dddd-dddddddddddd',
 'MED',
 'cccccccc-cccc-cccc-cccc-cccccccccccc',
 40
);

-- =========================================================
-- PROCESO ADMISION
-- =========================================================

INSERT INTO proceso_admision_aspirante VALUES
(
 'in111111-1111-1111-1111-111111111111',
 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
 'EN_PROCESO',
 NULL
),

(
 'in222222-2222-2222-2222-222222222222',
 'cccccccc-cccc-cccc-cccc-cccccccccccc',
 'ADMITIDO',
 'MED'
);

-- =========================================================
-- EXÁMENES REALIZADOS
-- =========================================================

INSERT INTO examen_realizado VALUES
(
 'ex111111-1111-1111-1111-111111111111',
 'in111111-1111-1111-1111-111111111111',
 'cl111111-1111-1111-1111-111111111111',
 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
 75.50,
 CURRENT_TIMESTAMP
);

-- =========================================================
-- RESPUESTAS DEL ASPIRANTE
-- =========================================================

INSERT INTO respuesta_examen VALUES
(
 're111111-1111-1111-1111-111111111111',
 'ex111111-1111-1111-1111-111111111111',
 'o2222222-2222-2222-2222-222222222222'
),

(
 're222222-2222-2222-2222-222222222222',
 'ex111111-1111-1111-1111-111111111111',
 'o3333333-3333-3333-3333-333333333333'
);

-- =========================================================
-- ASIGNACIONES DE AULA
-- =========================================================

INSERT INTO asignacion_aula_aspirante VALUES
(
 'as111111-1111-1111-1111-111111111111',
 'in111111-1111-1111-1111-111111111111',
 'au111111-1111-1111-1111-111111111111',
 'tr111111-1111-1111-1111-111111111111'
),

(
 'as222222-2222-2222-2222-222222222222',
 'in222222-2222-2222-2222-222222222222',
 'au111111-1111-1111-1111-111111111111',
 'tr111111-1111-1111-1111-111111111111'
);