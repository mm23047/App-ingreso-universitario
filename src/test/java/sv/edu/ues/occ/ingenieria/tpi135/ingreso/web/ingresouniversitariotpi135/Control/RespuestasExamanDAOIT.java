package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ExamenesRealizado;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.OpcionesRespuesta;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.RespuestasExaman;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    // UUID de la respuesta creada en testCrear — compartido entre tests
    private UUID idCreado;

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

        RespuestasExamanDAO cut = new RespuestasExamanDAO();
        cut.em = emf.createEntityManager();

        int resultado = cut.count();

        // BD recién iniciada con init.sql  4 respuestas: 2 para examen1 y 2 para examen2
        assertTrue(resultado > 0);
        assertEquals(4, resultado);
    }

    @Test
    @Order(2)
    public void testFindRange() {
        assertTrue(postgres.isRunning());

        RespuestasExamanDAO cut = new RespuestasExamanDAO();
        cut.em = emf.createEntityManager();

        List<RespuestasExaman> resultado = cut.findRange(0, 10);

        // Aún no se ha insertado nada sigue habiendo 4
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        assertEquals(4, resultado.size());
    }

    @Test
    @Order(3)
    public void testCrear() {
        assertTrue(postgres.isRunning());

        EntityManager em = emf.createEntityManager();
        RespuestasExamanDAO cut = new RespuestasExamanDAO();
        cut.em = em;

        // Examen1 tiene pregunta1 y pregunta2 en el init.sql  (examen1, pregunta3) es nueva
        ExamenesRealizado examen    = em.find(ExamenesRealizado.class, ID_EXAMEN_1);
        BancoPregunta pregunta      = em.find(BancoPregunta.class, ID_PREGUNTA_3);
        OpcionesRespuesta opcion    = em.find(OpcionesRespuesta.class, ID_OPCION_7);

        RespuestasExaman nueva = new RespuestasExaman();
        nueva.setIdExamen(examen);
        nueva.setIdPregunta(pregunta);
        nueva.setIdOpcionSeleccionada(opcion);

        em.getTransaction().begin();
        cut.crear(nueva);
        em.getTransaction().commit();

        // Guardar el UUID para que testLeer, testActualizar y testEliminar lo usen
        idCreado = nueva.getId();

        assertNotNull(idCreado);
        assertEquals(5, cut.count());
    }

    @Test
    @Order(4)
    public void testLeer() {
        assertTrue(postgres.isRunning());

        RespuestasExamanDAO cut = new RespuestasExamanDAO();
        cut.em = emf.createEntityManager();

        // Leer primer registro del init.sql: examen1 + pregunta1 + opcion2
        RespuestasExaman resultado = cut.leer(ID_RESPUESTA_1);

        assertNotNull(resultado);
        assertEquals(ID_EXAMEN_1,   resultado.getIdExamen().getId());
        assertEquals(ID_PREGUNTA_1, resultado.getIdPregunta().getId());
        assertEquals(ID_OPCION_2,   resultado.getIdOpcionSeleccionada().getId());
    }

    @Test
    @Order(5)
    public void testActualizar() {
        assertTrue(postgres.isRunning());

        EntityManager em = emf.createEntityManager();
        RespuestasExamanDAO cut = new RespuestasExamanDAO();
        cut.em = em;

        // Respuesta1 tiene opcion2  cambiar a opcion1
        RespuestasExaman respuesta = cut.leer(ID_RESPUESTA_1);
        assertNotNull(respuesta);

        OpcionesRespuesta opcionNueva = em.find(OpcionesRespuesta.class, ID_OPCION_1);
        respuesta.setIdOpcionSeleccionada(opcionNueva);

        em.getTransaction().begin();
        RespuestasExaman actualizada = cut.actualizar(respuesta);
        em.getTransaction().commit();

        assertNotNull(actualizada);
        assertEquals(ID_OPCION_1, actualizada.getIdOpcionSeleccionada().getId());
    }

    @Test
    @Order(6)
    public void testEliminar() {
        assertTrue(postgres.isRunning());

        EntityManager em = emf.createEntityManager();
        RespuestasExamanDAO cut = new RespuestasExamanDAO();
        cut.em = em;

        // Eliminar la respuesta creada en testCrear
        RespuestasExaman respuesta = cut.leer(idCreado);
        assertNotNull(respuesta);

        em.getTransaction().begin();
        cut.eliminar(respuesta);
        em.getTransaction().commit();

        // Vuelve a los 4 registros originales del init.sql
        assertEquals(4, cut.count());
        assertNull(cut.leer(idCreado));
    }
}
