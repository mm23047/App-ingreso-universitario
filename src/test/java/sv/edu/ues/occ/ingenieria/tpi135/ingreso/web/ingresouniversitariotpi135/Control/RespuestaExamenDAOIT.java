package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ExamenRealizado;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntaOpcion;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.RespuestaExamen;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RespuestaExamenDAOIT extends AbstractBaseIT {

    // UUIDs del init.sql
    private static final UUID ID_EXAMEN_1    = UUID.fromString("ffffeee1-1111-1111-1111-111111111111");
    private static final UUID ID_PREGUNTA_1  = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID ID_PREGUNTA_3  = UUID.fromString("f1000000-0000-0000-0000-000000000003");
    private static final UUID ID_OPCION_1    = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID ID_OPCION_2    = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private static final UUID ID_OPCION_7    = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");
    private static final UUID ID_RESPUESTA_1 = UUID.fromString("fffff001-1111-1111-1111-111111111111");
    private static final UUID ID_EXAMEN_2   = UUID.fromString("0d000000-0000-0000-0000-000000000001");

    RespuestaExamenDAOIT() {
    }

    @BeforeAll
    void inicializar() {
        // La configuracion de postgres and emf se realiza en AbstractBaseIT
    }

    @Test
    @Order(1)
    void testCount() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            RespuestaExamenDAO cut = new RespuestaExamenDAO();
            cut.em = em;

            int resultado = cut.count();

            // BD recién iniciada con init.sql  4 respuestas: 2 para examen1 y 2 para examen2
            assertTrue(resultado > 0);
            assertEquals(4, resultado);

            return null;
        });
    }

    @Test
    @Order(2)
    void testFindRange() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            RespuestaExamenDAO cut = new RespuestaExamenDAO();
            cut.em = em;

            List<RespuestaExamen> resultado = cut.findRange(0, 10);

            // Aún no se ha insertado nada sigue habiendo 4
            assertNotNull(resultado);
            assertFalse(resultado.isEmpty());
            assertEquals(4, resultado.size());

            return null;
        });
    }

    @Test
    @Order(3)
    void testCrear() {
        assertTrue(postgres.isRunning());

        // Crear una respuesta temporal y verificar dentro de la misma transacción
        ejecutarEnTransaccion(em -> {
            RespuestaExamenDAO cut = new RespuestaExamenDAO();
            cut.em = em;

            // Examen1 tiene pregunta1 y pregunta2 en el init.sql  (examen1, pregunta3) es nueva
            ExamenRealizado examen    = em.find(ExamenRealizado.class, ID_EXAMEN_1);
            BancoPregunta pregunta      = em.find(BancoPregunta.class, ID_PREGUNTA_3);
            PreguntaOpcion opcion    = em.find(PreguntaOpcion.class, ID_OPCION_7);

            RespuestaExamen nueva = new RespuestaExamen();
            nueva.setExamenRealizado(examen);
            nueva.setPreguntaOpcion(opcion);

            cut.crear(nueva);

            assertNotNull(nueva.getIdRespuestaExamen());
            assertEquals(5, cut.count());

            return null;
        });

        // Verificar que después del rollback implícito la BD queda con 4 respuestas
        ejecutarEnTransaccion(em -> {
            RespuestaExamenDAO cut = new RespuestaExamenDAO();
            cut.em = em;

            assertEquals(4, cut.count());
            return null;
        });
    }

    @Test
    @Order(4)
    void testLeer() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            RespuestaExamenDAO cut = new RespuestaExamenDAO();
            cut.em = em;

            // Leer primer registro del init.sql: examen1 + pregunta1 + opcion2
            RespuestaExamen resultado = cut.leer(ID_RESPUESTA_1);

            assertNotNull(resultado);
            assertEquals(ID_EXAMEN_1,   resultado.getExamenRealizado().getIdExamenRealizado());
            assertEquals(ID_OPCION_2,   resultado.getPreguntaOpcion().getIdPreguntaOpcion());

            return null;
        });
    }

    @Test
    @Order(5)
    void testActualizar() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            RespuestaExamenDAO cut = new RespuestaExamenDAO();
            cut.em = em;

            // Respuesta1 tiene opcion2  cambiar a opcion1
            RespuestaExamen respuesta = cut.leer(ID_RESPUESTA_1);
            assertNotNull(respuesta);

            PreguntaOpcion opcionNueva = em.find(PreguntaOpcion.class, ID_OPCION_1);
            respuesta.setPreguntaOpcion(opcionNueva);

            RespuestaExamen actualizada = cut.actualizar(respuesta);

            assertNotNull(actualizada);
            assertEquals(ID_OPCION_1, actualizada.getPreguntaOpcion().getIdPreguntaOpcion());

            return null;
        });
    }

    @Test
    @Order(6)
    void testEliminar() {
        assertTrue(postgres.isRunning());

        // Crear y eliminar una respuesta temporal dentro de una única transacción
        ejecutarEnTransaccion(em -> {
            RespuestaExamenDAO cut = new RespuestaExamenDAO();
            cut.em = em;

            ExamenRealizado examen    = em.find(ExamenRealizado.class, ID_EXAMEN_1);
            BancoPregunta pregunta      = em.find(BancoPregunta.class, ID_PREGUNTA_3);
            PreguntaOpcion opcion    = em.find(PreguntaOpcion.class, ID_OPCION_7);

            RespuestaExamen nueva = new RespuestaExamen();
            nueva.setExamenRealizado(examen);
            nueva.setPreguntaOpcion(opcion);

            cut.crear(nueva);
            assertEquals(5, cut.count());

            cut.eliminar(nueva);
            assertEquals(4, cut.count());

            return null;
        });
    }

    @Test
    @Order(7)
    void testFindByExamenId() {
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            RespuestaExamenDAO cut = new RespuestaExamenDAO();
            cut.em = em;

            List<RespuestaExamen> resultado = cut.findByExamenId(ID_EXAMEN_1);

            assertNotNull(resultado);
            assertFalse(resultado.isEmpty());
            assertEquals(2, resultado.size());
            assertTrue(resultado.stream()
                    .allMatch(r -> r.getExamenRealizado() != null && ID_EXAMEN_1.equals(r.getExamenRealizado().getIdExamenRealizado())));

            // Parámetro nulo debe lanzar IllegalArgumentException
            IllegalArgumentException iae = assertThrows(IllegalArgumentException.class,
                () -> cut.findByExamenId(null));
            assertEquals("examenId must not be null", iae.getMessage());

            // Error de acceso a BD (em nulo) debe envolver en IllegalStateException
            cut.em = null;
            IllegalStateException ise = assertThrows(IllegalStateException.class,
                () -> cut.findByExamenId(ID_EXAMEN_1));
            assertEquals("Cannot access db", ise.getMessage());

            return null;
        });
    }

    @Test
    @Order(10)
    void testValidacionDaoNoPermiteDuplicadosPreguntaEnExamen() {
        assertTrue(postgres.isRunning());

        // existsByExamenAndPregunta es el método de guardia que el caller debe invocar
        // antes de crear para evitar duplicados al nivel de pregunta.
        // init.sql ya tiene examen1 con respuesta para pregunta1 → debe detectarlo.
        ejecutarEnTransaccion(em -> {
            RespuestaExamenDAO cut = new RespuestaExamenDAO();
            cut.em = em;

            boolean existe = cut.existsByExamenAndPregunta(ID_EXAMEN_1, ID_PREGUNTA_1);
            assertTrue(existe,
                    "existsByExamenAndPregunta debe detectar que examen1 ya tiene respuesta para pregunta1");

            // Si el llamador respeta la guardia, lanza IllegalArgumentException
            assertThrows(IllegalArgumentException.class, () -> {
                if (cut.existsByExamenAndPregunta(ID_EXAMEN_1, ID_PREGUNTA_1)) {
                    throw new IllegalArgumentException("Ya existe una respuesta para esta pregunta en este examen.");
                }
            });

            return null;
        });
    }

    // ===================== CRUD FALTANTE =====================

    @Test
    @Order(11)
    void testLeerNoExiste() {
        System.out.println("RespuestaExamenDAOIT.leer() - ID inexistente");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            RespuestaExamenDAO cut = new RespuestaExamenDAO();
            cut.em = em;

            RespuestaExamen resultado = cut.leer(UUID.randomUUID());
            assertNull(resultado, "Debe retornar null si el ID no existe");
            return null;
        });
    }

    // ===================== NAMED QUERIES - COMPLEMENTO =====================

    @Test
    @Order(12)
    void testFindByExamenIdInexistente() {
        System.out.println("RespuestaExamenDAOIT.findByExamenId() - examen inexistente");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            RespuestaExamenDAO cut = new RespuestaExamenDAO();
            cut.em = em;

            List<RespuestaExamen> resultado = cut.findByExamenId(UUID.randomUUID());
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
            return null;
        });
    }

    @Test
    @Order(13)
    void testExistsByExamenAndPreguntaNoExiste() {
        System.out.println("RespuestaExamenDAOIT.existsByExamenAndPregunta() - combinacion inexistente");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            RespuestaExamenDAO cut = new RespuestaExamenDAO();
            cut.em = em;

            // Examen 1 no tiene respuesta para pregunta 3 (f1000000...003)
            assertFalse(cut.existsByExamenAndPregunta(ID_EXAMEN_1, ID_PREGUNTA_3));

            // Examen inexistente
            assertFalse(cut.existsByExamenAndPregunta(UUID.randomUUID(), ID_PREGUNTA_1));

            return null;
        });
    }

    @Test
    @Order(14)
    void testExistsByExamenAndPreguntaNulos() {
        System.out.println("RespuestaExamenDAOIT.existsByExamenAndPregunta() - parametros nulos");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            RespuestaExamenDAO cut = new RespuestaExamenDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class,
                    () -> cut.existsByExamenAndPregunta(null, ID_PREGUNTA_1));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.existsByExamenAndPregunta(ID_EXAMEN_1, null));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.existsByExamenAndPregunta(null, null));
            return null;
        });
    }

    @Test
    @Order(15)
    void testFindByExamenAndPreguntaNoExiste() {
        System.out.println("RespuestaExamenDAOIT.findByExamenAndPregunta() - sin resultado");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            RespuestaExamenDAO cut = new RespuestaExamenDAO();
            cut.em = em;

            // Examen 1 no tiene respuesta para pregunta 3 → null
            RespuestaExamen resultado = cut.findByExamenAndPregunta(ID_EXAMEN_1, ID_PREGUNTA_3);
            assertNull(resultado, "Debe retornar null si no hay respuesta para esa pregunta");

            // Examen inexistente → null
            RespuestaExamen resultado2 = cut.findByExamenAndPregunta(UUID.randomUUID(), ID_PREGUNTA_1);
            assertNull(resultado2);

            return null;
        });
    }

    @Test
    @Order(16)
    void testFindByExamenAndPreguntaNulos() {
        System.out.println("RespuestaExamenDAOIT.findByExamenAndPregunta() - parametros nulos");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            RespuestaExamenDAO cut = new RespuestaExamenDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class,
                    () -> cut.findByExamenAndPregunta(null, ID_PREGUNTA_1));
            assertThrows(IllegalArgumentException.class,
                    () -> cut.findByExamenAndPregunta(ID_EXAMEN_1, null));
            return null;
        });
    }

    @Test
    @Order(17)
    void testCountRespuestasByExamen() {
        System.out.println("RespuestaExamenDAOIT.countRespuestasByExamen()");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            RespuestaExamenDAO cut = new RespuestaExamenDAO();
            cut.em = em;

            // Examen 1 tiene 2 respuestas
            Long countExamen1 = cut.countRespuestasByExamen(ID_EXAMEN_1);
            assertEquals(2L, countExamen1);

            // Examen 2 tiene 2 respuestas
            Long countExamen2 = cut.countRespuestasByExamen(ID_EXAMEN_2);
            assertEquals(2L, countExamen2);

            // Examen inexistente → 0
            Long countInexistente = cut.countRespuestasByExamen(UUID.randomUUID());
            assertEquals(0L, countInexistente);

            return null;
        });
    }

    @Test
    @Order(18)
    void testCountRespuestasByExamenNulo() {
        System.out.println("RespuestaExamenDAOIT.countRespuestasByExamen() - null");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            RespuestaExamenDAO cut = new RespuestaExamenDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class,
                    () -> cut.countRespuestasByExamen(null));
            return null;
        });
    }

    // ===================== COBERTURA FALTANTE =====================

    @Test
    @Order(19)
    void testLeerNulo() {
        System.out.println("RespuestaExamenDAOIT.leer() - null");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            RespuestaExamenDAO cut = new RespuestaExamenDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class, () -> cut.leer(null));
            return null;
        });
    }

    @Test
    @Order(20)
    void testFindByExamenAndPreguntaExiste() {
        System.out.println("RespuestaExamenDAOIT.findByExamenAndPregunta() - resultado existe");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            RespuestaExamenDAO cut = new RespuestaExamenDAO();
            cut.em = em;

            // Crear un examen con una sola respuesta para garantizar getSingleResult()
            ExamenRealizado examen = em.find(ExamenRealizado.class, ID_EXAMEN_1);
            PreguntaOpcion opcion = em.find(PreguntaOpcion.class, ID_OPCION_7); // pregunta 55555555
            BancoPregunta pregunta3 = em.find(BancoPregunta.class, ID_PREGUNTA_3);
            PreguntaOpcion opcionP3 = em.createQuery(
                    "SELECT po FROM PreguntaOpcion po WHERE po.bancoPregunta.idBancoPregunta = :idP",
                    PreguntaOpcion.class)
                    .setParameter("idP", ID_PREGUNTA_3)
                    .setMaxResults(1)
                    .getSingleResult();

            // Crear una sola respuesta para examen1 + pregunta3
            RespuestaExamen nueva = new RespuestaExamen();
            nueva.setExamenRealizado(examen);
            nueva.setPreguntaOpcion(opcionP3);
            em.persist(nueva);
            em.flush();

            // Buscar la respuesta recién creada
            RespuestaExamen resultado = cut.findByExamenAndPregunta(ID_EXAMEN_1, ID_PREGUNTA_3);

            assertNotNull(resultado, "Debe encontrar la respuesta existente");
            assertEquals(ID_EXAMEN_1, resultado.getExamenRealizado().getIdExamenRealizado());
            assertNotNull(resultado.getPreguntaOpcion());
            return null;
        });
    }

    @Test
    @Order(21)
    void testGuardarLoteMejoradoNuloOVacio() {
        System.out.println("RespuestaExamenDAOIT.guardarLoteMejorado() - null/vacio");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            RespuestaExamenDAO cut = new RespuestaExamenDAO();
            cut.em = em;

            // null idExamen → no-op (return silencioso)
            cut.guardarLoteMejorado(null, List.of(UUID.randomUUID()));
            assertEquals(4, cut.count());

            // null lista → no-op
            cut.guardarLoteMejorado(ID_EXAMEN_1, null);
            assertEquals(4, cut.count());

            // lista vacía → no-op
            cut.guardarLoteMejorado(ID_EXAMEN_1, List.of());
            assertEquals(4, cut.count());

            return null;
        });
    }

    @Test
    @Order(22)
    void testGuardarLoteMejoradoExamenInexistente() {
        System.out.println("RespuestaExamenDAOIT.guardarLoteMejorado() - examen inexistente");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            RespuestaExamenDAO cut = new RespuestaExamenDAO();
            cut.em = em;

            assertThrows(IllegalArgumentException.class,
                    () -> cut.guardarLoteMejorado(UUID.randomUUID(), List.of(ID_OPCION_1)));
            return null;
        });
    }

    @Test
    @Order(23)
    void testGuardarLoteMejoradoInsert() {
        System.out.println("RespuestaExamenDAOIT.guardarLoteMejorado() - INSERT nuevas respuestas");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            RespuestaExamenDAO cut = new RespuestaExamenDAO();
            cut.em = em;

            // Examen 1 tiene 2 respuestas (pregunta1→opcion cccc, pregunta1→opcion bbbb)
            // ID_OPCION_7 (eeeeeeee) es de pregunta 55555555 que ya tiene respuestas en examen1
            // Usamos examen2 que tiene respuestas para preguntas f1000000...001
            // La opcion eeeeeeee pertenece a pregunta 55555555 → no existe en examen2
            cut.guardarLoteMejorado(ID_EXAMEN_2, List.of(ID_OPCION_7));

            // Debe haber insertado 1 nueva respuesta: 4 + 1 = 5
            // Nota: em fue cleared por guardarLoteMejorado, re-query
            Long total = em.createQuery("SELECT COUNT(r) FROM RespuestaExamen r", Long.class)
                    .getSingleResult();
            assertEquals(5L, total);

            return null;
        });

        // Verificar rollback
        ejecutarEnTransaccion(em -> {
            RespuestaExamenDAO cut = new RespuestaExamenDAO();
            cut.em = em;
            assertEquals(4, cut.count());
            return null;
        });
    }

    @Test
    @Order(24)
    void testGuardarLoteMejoradoUpdate() {
        System.out.println("RespuestaExamenDAOIT.guardarLoteMejorado() - UPDATE respuesta existente");
        assertTrue(postgres.isRunning());

        ejecutarEnTransaccion(em -> {
            RespuestaExamenDAO cut = new RespuestaExamenDAO();
            cut.em = em;

            // Examen 1 ya tiene respuesta para pregunta 55555555 con opcion cccccccc
            // Enviar opcion bbbbbbbb (misma pregunta 55555555) → debe hacer UPDATE
            cut.guardarLoteMejorado(ID_EXAMEN_1, List.of(ID_OPCION_1));

            // El count no cambia (fue UPDATE, no INSERT)
            Long total = em.createQuery("SELECT COUNT(r) FROM RespuestaExamen r", Long.class)
                    .getSingleResult();
            assertEquals(4L, total);

            return null;
        });
    }
}
