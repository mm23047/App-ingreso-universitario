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
public class RespuestasExamanDAOIT extends AbstractBaseIT {

    // UUIDs del init.sql
    private static final UUID ID_EXAMEN_1    = UUID.fromString("0d000000-0000-0000-0000-000000000001");
    private static final UUID ID_PREGUNTA_1  = UUID.fromString("f1000000-0000-0000-0000-000000000001");
    private static final UUID ID_PREGUNTA_3  = UUID.fromString("f1000000-0000-0000-0000-000000000003");
    private static final UUID ID_OPCION_1    = UUID.fromString("0b000000-0000-0000-0000-000000000001");
    private static final UUID ID_OPCION_2    = UUID.fromString("0b000000-0000-0000-0000-000000000002");
    private static final UUID ID_OPCION_7    = UUID.fromString("0b000000-0000-0000-0000-000000000007");
    private static final UUID ID_RESPUESTA_1 = UUID.fromString("0e000000-0000-0000-0000-000000000001");

    public RespuestasExamanDAOIT() {
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
            nueva.setIdExamen(examen);
            nueva.setIdPreguntaOpcion(opcion);

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
            assertEquals(ID_EXAMEN_1,   resultado.getIdExamen().getIdExamenRealizado());
            assertEquals(ID_OPCION_2,   resultado.getIdPreguntaOpcion().getIdPreguntaOpcion());

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
            respuesta.setIdPreguntaOpcion(opcionNueva);

            RespuestaExamen actualizada = cut.actualizar(respuesta);

            assertNotNull(actualizada);
            assertEquals(ID_OPCION_1, actualizada.getIdPreguntaOpcion().getIdPreguntaOpcion());

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
            nueva.setIdExamen(examen);
            nueva.setIdPreguntaOpcion(opcion);

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
                    .allMatch(r -> r.getIdExamen() != null && ID_EXAMEN_1.equals(r.getIdExamen().getIdExamenRealizado())));

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
}
