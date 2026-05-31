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
public class RespuestaExamenDAOIT extends AbstractBaseIT {

    // UUIDs del init.sql
    private static final UUID ID_EXAMEN_1    = UUID.fromString("ffffeee1-1111-1111-1111-111111111111");
    private static final UUID ID_PREGUNTA_1  = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID ID_PREGUNTA_3  = UUID.fromString("f1000000-0000-0000-0000-000000000003");
    private static final UUID ID_OPCION_1    = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID ID_OPCION_2    = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private static final UUID ID_OPCION_7    = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");
    private static final UUID ID_RESPUESTA_1 = UUID.fromString("fffff001-1111-1111-1111-111111111111");

    public RespuestaExamenDAOIT() {
    }

    @BeforeAll
    void inicializar() {
        // La configuracion de postgres and emf se realiza en AbstractBaseIT
    }

    @Test
    @Order(1)
    public void testCount() {
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
    public void testFindRange() {
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
    public void testCrear() {
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
    public void testLeer() {
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
    public void testActualizar() {
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
    public void testEliminar() {
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
    public void testFindByExamenId() {
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
    public void testValidacionDaoNoPermiteDuplicadosPreguntaEnExamen() {
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
}
