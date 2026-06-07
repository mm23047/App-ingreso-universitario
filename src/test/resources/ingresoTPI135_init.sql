-- =============================================================
-- DDL (tablas)
-- =============================================================
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
    nombre VARCHAR(50) NOT NULL UNIQUE,
    puntaje_minimo NUMERIC(5,2),
    puntaje_maximo NUMERIC(5,2),
    descripcion TEXT,
    cantidad_preguntas_requeridas INTEGER NOT NULL DEFAULT 0
);
CREATE TABLE IF NOT EXISTS prueba_admision (
    id_prueba UUID PRIMARY KEY,
    nombre_prueba VARCHAR(100) NOT NULL,
    anio INTEGER NOT NULL,
    activa BOOLEAN DEFAULT true,
    CONSTRAINT uk_nombre_anio UNIQUE (nombre_prueba, anio)
);
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
CREATE TABLE IF NOT EXISTS tema (
    id_tema UUID PRIMARY KEY,
    id_area UUID NOT NULL REFERENCES area_conocimiento(id_area),
    nombre_tema VARCHAR(100) NOT NULL UNIQUE,
    id_tema_padre UUID REFERENCES tema(id_tema)
);
CREATE TABLE IF NOT EXISTS banco_pregunta (
    id_pregunta UUID PRIMARY KEY,
    id_tema UUID NOT NULL REFERENCES tema(id_tema),
    enunciado TEXT NOT NULL UNIQUE
);
CREATE TABLE IF NOT EXISTS banco_respuesta (
    id_respuesta_global UUID PRIMARY KEY,
    texto_respuesta TEXT NOT NULL,
    id_area UUID REFERENCES area_conocimiento(id_area)
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_respuesta_global_unica
    ON banco_respuesta (UPPER(TRIM(texto_respuesta))) WHERE id_area IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_respuesta_area_unica
    ON banco_respuesta (UPPER(TRIM(texto_respuesta)), id_area) WHERE id_area IS NOT NULL;
CREATE TABLE IF NOT EXISTS pregunta_opcion (
    id_pregunta_opcion UUID PRIMARY KEY,
    id_pregunta UUID NOT NULL REFERENCES banco_pregunta(id_pregunta),
    id_respuesta_global UUID NOT NULL REFERENCES banco_respuesta(id_respuesta_global),
    es_correcta BOOLEAN NOT NULL,
    CONSTRAINT uk_pregunta_respuesta UNIQUE (id_pregunta, id_respuesta_global)
);
CREATE TABLE IF NOT EXISTS turno_examen (
    id_turno UUID PRIMARY KEY,
    id_prueba UUID NOT NULL REFERENCES prueba_admision(id_prueba),
    nombre_turno VARCHAR(50) NOT NULL,
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    CONSTRAINT check_horas_distintas CHECK (hora_inicio < hora_fin)
);
CREATE TABLE IF NOT EXISTS aula (
    id_aula UUID PRIMARY KEY,
    codigo_aula_api VARCHAR(50) NOT NULL UNIQUE,
    capacidad_fisica INTEGER NOT NULL,
    accesible_silla_ruedas BOOLEAN DEFAULT false,
    nombre_sede VARCHAR(100) NOT NULL,
    departamento VARCHAR(50) NOT NULL,
    municipio VARCHAR(50)
);
CREATE TABLE IF NOT EXISTS disponibilidad_aula_turno (
    id_aula UUID REFERENCES aula(id_aula),
    id_turno UUID REFERENCES turno_examen(id_turno),
    PRIMARY KEY (id_aula, id_turno)
);
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
CREATE TABLE IF NOT EXISTS clave_examen (
    id_clave UUID PRIMARY KEY,
    id_prueba UUID NOT NULL REFERENCES prueba_admision(id_prueba),
    id_etapa UUID NOT NULL REFERENCES etapa_admision(id_etapa),
    nombre_clave VARCHAR(50) NOT NULL,
    CONSTRAINT uk_clave_examen_prueba_nombre UNIQUE (id_prueba, id_etapa, nombre_clave)
);
CREATE TABLE IF NOT EXISTS preguntas_por_clave (
    id_clave UUID NOT NULL REFERENCES clave_examen(id_clave),
    id_pregunta UUID NOT NULL REFERENCES banco_pregunta(id_pregunta),
    PRIMARY KEY (id_clave, id_pregunta)
);
CREATE TABLE IF NOT EXISTS cupos_carrera (
    id_prueba UUID NOT NULL REFERENCES prueba_admision(id_prueba),
    id_carrera VARCHAR(10) NOT NULL REFERENCES catalogo_carrera(id_carrera),
    id_etapa UUID NOT NULL REFERENCES etapa_admision(id_etapa),
    cupos INTEGER NOT NULL,
    PRIMARY KEY (id_prueba, id_carrera, id_etapa)
);
ALTER TABLE cupos_carrera ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
CREATE TABLE IF NOT EXISTS proceso_admision_aspirante (
    id_inscripcion UUID PRIMARY KEY REFERENCES inscripcion_prueba(id_inscripcion),
    id_etapa_actual UUID NOT NULL REFERENCES etapa_admision(id_etapa),
    estado VARCHAR(20) NOT NULL,
    carrera_asignada VARCHAR(10) REFERENCES catalogo_carrera(id_carrera)
);
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
    CONSTRAINT fk_asignacion_inscripcion FOREIGN KEY (id_inscripcion) REFERENCES inscripcion_prueba(id_inscripcion),
    CONSTRAINT fk_asignacion_disponibilidad FOREIGN KEY (id_aula, id_turno) REFERENCES disponibilidad_aula_turno(id_aula, id_turno),
    CONSTRAINT uk_inscripcion_turno UNIQUE (id_inscripcion, id_turno)
);

-- =============================================================
-- DATOS MAESTROS
-- =============================================================

INSERT INTO area_conocimiento VALUES
('11111111-1111-1111-1111-111111111111', 'Matemática'),
('22222222-2222-2222-2222-222222222222', 'Lenguaje'),
('33333333-3333-3333-3333-333333333333', 'Ciencias Naturales');

INSERT INTO catalogo_carrera VALUES
('ISI', 'Ingeniería en Sistemas Informáticos'),
('MED', 'Doctorado en Medicina'),
('ARQ', 'Arquitectura'),
('ADM', 'Administración de Empresas'),
('ICS', 'Ingeniería en Ciencias de la Computación'),
('ICC', 'Ciencias de la Computación Avanzada'),
('MAT', 'Matemáticas Aplicadas');

-- 5 etapas: 3 reales + 2 para tests IT
INSERT INTO etapa_admision VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Primera Etapa',   0,   59.99, 'Evaluación inicial',      20),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Segunda Etapa',  60,   79.99, 'Evaluación intermedia',   25),
('cccccccc-cccc-cccc-cccc-cccccccccccc', 'Etapa Final',    80,   100,   'Asignación definitiva',   30),
('c1000000-0000-0000-0000-000000000001', 'Etapa Inscripcion', 0, 100,   'Para tests IT',            0),
('c1000000-0000-0000-0000-000000000003', 'Etapa Asignacion',  0, 100,   'Para tests IT cupos',      0);

INSERT INTO prueba_admision VALUES
('dddddddd-dddd-dddd-dddd-dddddddddddd', 'Prueba Nacional UES', 2026, true),
('d1000000-0000-0000-0000-000000000001', 'Prueba Test A',        2024, true),
('d1000000-0000-0000-0000-000000000002', 'Prueba Test B',        2023, true);

-- 4 aspirantes
INSERT INTO aspirante_datos VALUES
('e1111111-1111-1111-1111-111111111111', 'Carlos Alberto',  'Ramírez López',    '2007-05-10', '01234567-8', 'carlos.ramirez@gmail.com',  CURRENT_DATE, false),
('e2222222-2222-2222-2222-222222222222', 'María Fernanda',  'Castillo Pérez',   '2006-09-22', '02234567-8', 'maria.castillo@gmail.com',  CURRENT_DATE, true),
('e3333333-3333-3333-3333-333333333333', 'José Miguel',     'Hernández Flores', '2007-01-15', '03234567-8', 'jose.hernandez@gmail.com',  CURRENT_DATE, false),
('e1000000-0000-0000-0000-000000000001', 'Prueba Usuario',  'Test Apellido',    '2005-01-01', '99999999-9', 'prueba.test@example.com',   CURRENT_DATE, false);

-- =============================================================
-- BANCO DE PREGUNTAS (count=4 para BancoPreguntaDAOIT)
-- =============================================================

INSERT INTO tema VALUES
('f0000001-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', 'Álgebra',             NULL),
('f0000002-0000-0000-0000-000000000002', '11111111-1111-1111-1111-111111111111', 'Ecuaciones Lineales', 'f0000001-0000-0000-0000-000000000001'),
('f0000003-0000-0000-0000-000000000003', '22222222-2222-2222-2222-222222222222', 'Comprensión Lectora', NULL);

INSERT INTO banco_respuesta VALUES
-- Para opciones bbbbbbbb/cccccccc/eeeeeeee (pregunta 55555555)
('ba000001-0000-0000-0000-000000000001', 'X = 1',                     '11111111-1111-1111-1111-111111111111'),
('ba000001-0000-0000-0000-000000000002', 'X = 2',                     '11111111-1111-1111-1111-111111111111'),
('ba000001-0000-0000-0000-000000000003', 'Ninguna de las anteriores', '22222222-2222-2222-2222-222222222222'),
-- Para opciones 0b000000 (pregunta f1000000...001 y f1000000...003)
('0c000000-0000-0000-0000-000000000001', 'Y = 1',    '11111111-1111-1111-1111-111111111111'),
('0c000000-0000-0000-0000-000000000002', 'Y = 2',    '11111111-1111-1111-1111-111111111111'),
('0c000000-0000-0000-0000-000000000007', 'Solo A',   '22222222-2222-2222-2222-222222222222');

-- 4 banco_pregunta para BancoPreguntaDAOIT
INSERT INTO banco_pregunta VALUES
('55555555-5555-5555-5555-555555555555', 'f0000002-0000-0000-0000-000000000002', '¿Cuál es el resultado de 2X + 4 = 10, hallar X?'),
('f1000000-0000-0000-0000-000000000001', 'f0000001-0000-0000-0000-000000000001', '¿Cuánto es la raíz cuadrada de 16?'),
('f1000000-0000-0000-0000-000000000003', 'f0000003-0000-0000-0000-000000000003', '¿Cuál es la idea principal de un texto?'),
('f1000000-0000-0000-0000-000000000004', 'f0000003-0000-0000-0000-000000000003', '¿Qué es un sinónimo?');

-- Opciones de preguntas
INSERT INTO pregunta_opcion VALUES
-- Para pregunta 55555555 (RespuestaExamenDAOIT)
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '55555555-5555-5555-5555-555555555555', 'ba000001-0000-0000-0000-000000000001', false),
('cccccccc-cccc-cccc-cccc-cccccccccccc', '55555555-5555-5555-5555-555555555555', 'ba000001-0000-0000-0000-000000000002', true),
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '55555555-5555-5555-5555-555555555555', 'ba000001-0000-0000-0000-000000000003', false),
-- Para pregunta f1000000...001 (RespuestasExamanDAOIT)
('0b000000-0000-0000-0000-000000000001', 'f1000000-0000-0000-0000-000000000001', '0c000000-0000-0000-0000-000000000001', false),
('0b000000-0000-0000-0000-000000000002', 'f1000000-0000-0000-0000-000000000001', '0c000000-0000-0000-0000-000000000002', true),
-- Para pregunta f1000000...003 (RespuestasExamanDAOIT testCrear)
('0b000000-0000-0000-0000-000000000007', 'f1000000-0000-0000-0000-000000000003', '0c000000-0000-0000-0000-000000000007', false);

-- =============================================================
-- TURNOS Y AULAS (count=2 cada uno para TurnosExamenDAOIT)
-- =============================================================

INSERT INTO turno_examen VALUES
('ffff0001-0001-0001-0001-000000000001', 'dddddddd-dddd-dddd-dddd-dddddddddddd', 'Turno Mañana', '2026-07-15', '08:00', '11:00'),
('ffff0002-0002-0002-0002-000000000002', 'dddddddd-dddd-dddd-dddd-dddddddddddd', 'Turno Tarde',  '2026-07-15', '13:00', '16:00');

INSERT INTO aula VALUES
('ffffff11-1111-1111-1111-111111111111', 'AULA-A101', 40, true,  'Sede Central',   'San Salvador', 'San Salvador'),
('ffffff22-2222-2222-2222-222222222222', 'AULA-B202', 35, false, 'Sede Santa Ana', 'Santa Ana',    'Santa Ana');

-- 3 disponibilidades (count=3 para DisponibilidadAulaTurnoDAOIT)
-- testCrear crea (ffffff11, ffff0002) como la 4a
-- testViolacion duplica (ffffff11, ffff0001)
INSERT INTO disponibilidad_aula_turno VALUES
('ffffff11-1111-1111-1111-111111111111', 'ffff0001-0001-0001-0001-000000000001'),
('ffffff22-2222-2222-2222-222222222222', 'ffff0001-0001-0001-0001-000000000001'),
('ffffff22-2222-2222-2222-222222222222', 'ffff0002-0002-0002-0002-000000000002');

-- =============================================================
-- INSCRIPCIONES (count=4 para InscripcionesPruebaDAOIT)
-- =============================================================

INSERT INTO inscripcion_prueba VALUES
-- Para InscripcionesPrueba/ProcesoAdmision/CarrerasElegida/RespuestaExamen
('09000000-0000-0000-0000-000000000001', 'e1000000-0000-0000-0000-000000000001', 'd1000000-0000-0000-0000-000000000001', 'INSCRITO'),
('09000000-0000-0000-0000-000000000002', 'e1111111-1111-1111-1111-111111111111', 'd1000000-0000-0000-0000-000000000001', 'INSCRITO'),
-- Para ExamenRealizado/AsignacionAula
('ffff1001-1001-1001-1001-000000001001', 'e2222222-2222-2222-2222-222222222222', 'dddddddd-dddd-dddd-dddd-dddddddddddd', 'INSCRITO'),
('ffff1002-1002-1002-1002-000000001002', 'e3333333-3333-3333-3333-333333333333', 'dddddddd-dddd-dddd-dddd-dddddddddddd', 'INSCRITO');

-- =============================================================
-- CARRERAS ELEGIDAS (count=4 para CarrerasElegidaDAOIT)
-- testLeer: (09000000...001, ICS, prio=1)
-- testActualizar: (09000000...001, ISI, prio=2)
-- =============================================================

INSERT INTO carrera_elegida VALUES
('09000000-0000-0000-0000-000000000001', 'ICS', 1),
('09000000-0000-0000-0000-000000000001', 'ISI', 2),
('09000000-0000-0000-0000-000000000002', 'MED', 1),
('09000000-0000-0000-0000-000000000002', 'ADM', 2);

-- =============================================================
-- CLAVES DE EXAMEN (count=2 para ClavesExamenDAOIT)
-- ID_CLAVE_A=08000000...001 (PreguntasPorClaveDAOIT)
-- ID_CLAVE_B=aaaabbbb... (PreguntasPorClave y ExamenRealizadoDAOIT)
-- =============================================================

INSERT INTO clave_examen VALUES
('08000000-0000-0000-0000-000000000001', 'd1000000-0000-0000-0000-000000000001', 'c1000000-0000-0000-0000-000000000001', 'Clave A'),
('aaaabbbb-cccc-dddd-eeee-ffffffffffff', 'd1000000-0000-0000-0000-000000000001', 'c1000000-0000-0000-0000-000000000001', 'Clave B');

-- =============================================================
-- PREGUNTAS POR CLAVE (count=4 para PreguntasPorClaveDAOIT)
-- testLeer: (08000000...001, f1000000...001)
-- testActualizar: (aaaabbbb..., f1000000...003)
-- testCrear agrega (08000000...001, f1000000...003) como 5a
-- =============================================================

INSERT INTO preguntas_por_clave VALUES
('08000000-0000-0000-0000-000000000001', 'f1000000-0000-0000-0000-000000000001'),
('08000000-0000-0000-0000-000000000001', '55555555-5555-5555-5555-555555555555'),
('aaaabbbb-cccc-dddd-eeee-ffffffffffff', 'f1000000-0000-0000-0000-000000000003'),
('aaaabbbb-cccc-dddd-eeee-ffffffffffff', 'f1000000-0000-0000-0000-000000000004');

-- =============================================================
-- CUPOS DE CARRERA (count=3 para CuposCarreraDAOIT)
-- =============================================================

INSERT INTO cupos_carrera (id_prueba, id_carrera, id_etapa, cupos, version) VALUES
('d1000000-0000-0000-0000-000000000001', 'ISI', 'c1000000-0000-0000-0000-000000000003', 60, 0),
('d1000000-0000-0000-0000-000000000001', 'ICS', 'c1000000-0000-0000-0000-000000000003', 50, 0),
('d1000000-0000-0000-0000-000000000001', 'ICC', 'c1000000-0000-0000-0000-000000000003', 45, 0);

-- =============================================================
-- PROCESO ADMISION (count=2 para ProcesoAdmisionAspiranteDAOIT)
-- =============================================================

INSERT INTO proceso_admision_aspirante VALUES
('09000000-0000-0000-0000-000000000001', 'c1000000-0000-0000-0000-000000000001', 'EN_PROCESO', NULL),
('09000000-0000-0000-0000-000000000002', 'c1000000-0000-0000-0000-000000000001', 'ADMITIDO',   'ISI');

-- =============================================================
-- EXAMENES REALIZADOS (count=2 para ExamenRealizadoDAOIT)
-- ffffeee1: para RespuestaExamenDAOIT
-- 0d000000...001: para RespuestasExamanDAOIT y ExamenRealizadoDAOIT
-- =============================================================

INSERT INTO examen_realizado VALUES
('ffffeee1-1111-1111-1111-111111111111', '09000000-0000-0000-0000-000000000001', '08000000-0000-0000-0000-000000000001', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 70.00, CURRENT_TIMESTAMP),
('0d000000-0000-0000-0000-000000000001', 'ffff1001-1001-1001-1001-000000001001', '08000000-0000-0000-0000-000000000001', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 65.00, CURRENT_TIMESTAMP);

-- =============================================================
-- ASIGNACIONES DE AULA (count=2 para AsignacionAulaAspiranteDAOIT)
-- testCrear agrega (ffff1002, ffffff22, ffff0002) como 3a
-- testViolacion: crea (ffff1001, ffffff11, ffff0001) OK, luego (ffff1001, ffffff22, ffff0001) → falla
-- =============================================================

INSERT INTO asignacion_aula_aspirante VALUES
('fa000001-0000-0000-0000-000000000001', 'ffff1001-1001-1001-1001-000000001001', 'ffffff22-2222-2222-2222-222222222222', 'ffff0002-0002-0002-0002-000000000002'),
('fa000001-0000-0000-0000-000000000002', 'ffff1002-1002-1002-1002-000000001002', 'ffffff11-1111-1111-1111-111111111111', 'ffff0001-0001-0001-0001-000000000001');

-- =============================================================
-- RESPUESTAS DEL ASPIRANTE (count=4 total)
-- 2 para ffffeee1 (RespuestaExamenDAOIT)
-- 2 para 0d000000...001 (RespuestasExamanDAOIT)
-- =============================================================

INSERT INTO respuesta_examen VALUES
-- RespuestaExamenDAOIT: ID_RESPUESTA_1=fffff001, examen=ffffeee1, opcion=cccccccc
('fffff001-1111-1111-1111-111111111111', 'ffffeee1-1111-1111-1111-111111111111', 'cccccccc-cccc-cccc-cccc-cccccccccccc'),
('fffff002-2222-2222-2222-222222222222', 'ffffeee1-1111-1111-1111-111111111111', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'),
-- RespuestasExamanDAOIT: ID_RESPUESTA_1=0e000000...001, examen=0d000000, opcion=0b000000...002
('0e000000-0000-0000-0000-000000000001', '0d000000-0000-0000-0000-000000000001', '0b000000-0000-0000-0000-000000000002'),
('0e000000-0000-0000-0000-000000000002', '0d000000-0000-0000-0000-000000000001', '0b000000-0000-0000-0000-000000000001');
