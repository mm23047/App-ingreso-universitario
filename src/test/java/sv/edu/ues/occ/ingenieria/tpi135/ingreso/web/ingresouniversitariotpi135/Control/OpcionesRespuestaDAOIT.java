package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.OpcionesRespuesta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OpcionesRespuestaDAOIT {

    // UUIDs del init.sql
    // f1...003 = "¿Cuántos planetas tiene el sistema solar?" — se usará para la nueva opción
    private static final UUID ID_PREGUNTA_3 = UUID.fromString("f1000000-0000-0000-0000-000000000003");

    // UUID de la opción creada en testCrear — compartido entre tests
    private static UUID idCreado;

    // EMF compartido — inicializado una sola vez en @BeforeAll
    private static EntityManagerFactory emf;

    // static → un solo contenedor levantado una vez para toda la clase
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.5-alpine")
            .withDatabaseName("ingresoTPI135")
            .withInitScript("ingresoTPI135_init.sql")
            .withUsername("postgres")
            .withPassword("abc123");

    public OpcionesRespuestaDAOIT() {
    }

    @BeforeAll
    static void inicializar() {
        Integer puertoPostgresql = postgres.getMappedPort(5432);
        Map<String, Object> propiedades = new HashMap<>();
        propiedades.put("jakarta.persistence.jdbc.url", String.format("jdbc:postgresql://localhost:%d/ingresoTPI135", puertoPostgresql));
        propiedades.put("jakarta.persistence.jdbc.user", "postgres");
        propiedades.put("jakarta.persistence.jdbc.password", "abc123");
        emf = Persistence.createEntityManagerFactory("ingresoPUIT", propiedades);
    }

    @Test
    @Order(1)
    public void testCount() {
        System.out.println("count");
        assertTrue(postgres.isRunning());

        OpcionesRespuestaDAO cut = new OpcionesRespuestaDAO();
        cut.em = emf.createEntityManager();

        int resultado = cut.count();

        // BD recién iniciada con init.sql → 10 opciones de respuesta en total
        assertTrue(resultado > 0);
        assertEquals(10, resultado);
    }

    @Test
    @Order(2)
    public void testFindRange() {
        System.out.println("findRange");
        assertTrue(postgres.isRunning());

        OpcionesRespuestaDAO cut = new OpcionesRespuestaDAO();
        cut.em = emf.createEntityManager();

        List<OpcionesRespuesta> resultado = cut.findRange(0, 15);

        // Aún no se ha insertado nada → sigue habiendo 10
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        assertEquals(10, resultado.size());
    }

    @Test
    @Order(3)
    public void testCrear() {
        System.out.println("crear");
        assertTrue(postgres.isRunning());

        EntityManager em = emf.createEntityManager();
        OpcionesRespuestaDAO cut = new OpcionesRespuestaDAO();
        cut.em = em;

        // Agregar una nueva opción a la pregunta f1...003 ("¿Cuántos planetas...?")
        BancoPregunta pregunta = em.find(BancoPregunta.class, ID_PREGUNTA_3);
        assertNotNull(pregunta);

        OpcionesRespuesta nueva = new OpcionesRespuesta();
        nueva.setIdPregunta(pregunta);
        nueva.setTextoOpcion("9");
        nueva.setEsCorrecta(false);

        em.getTransaction().begin();
        cut.crear(nueva);
        em.getTransaction().commit();

        // Guardar el UUID para que testLeer, testActualizar y testEliminar lo usen
        idCreado = nueva.getId();

        assertNotNull(idCreado);
        assertEquals(11, cut.count());
    }

    @Test
    @Order(4)
    public void testLeer() {
        System.out.println("leer");
        assertTrue(postgres.isRunning());

        OpcionesRespuestaDAO cut = new OpcionesRespuestaDAO();
        cut.em = emf.createEntityManager();

        // Lee el registro insertado en testCrear usando el UUID almacenado
        OpcionesRespuesta resultado = cut.leer(idCreado);

        assertNotNull(resultado);
        assertEquals("9", resultado.getTextoOpcion());
        assertFalse(resultado.getEsCorrecta());
    }

    @Test
    @Order(5)
    public void testActualizar() {
        System.out.println("actualizar");
        assertTrue(postgres.isRunning());

        EntityManager em = emf.createEntityManager();
        OpcionesRespuestaDAO cut = new OpcionesRespuestaDAO();
        cut.em = em;

        // Modifica el registro creado en testCrear
        OpcionesRespuesta opcion = cut.leer(idCreado);
        assertNotNull(opcion);
        opcion.setTextoOpcion("texto actualizado");

        em.getTransaction().begin();
        OpcionesRespuesta resultado = cut.actualizar(opcion);
        em.getTransaction().commit();

        assertNotNull(resultado);
        assertEquals("texto actualizado", resultado.getTextoOpcion());
        // El conteo no cambia al actualizar → sigue en 11
        assertEquals(11, cut.count());
    }

    @Test
    @Order(6)
    public void testEliminar() {
        System.out.println("eliminar");
        assertTrue(postgres.isRunning());

        EntityManager em = emf.createEntityManager();
        OpcionesRespuestaDAO cut = new OpcionesRespuestaDAO();
        cut.em = em;

        // Elimina el registro creado en testCrear
        OpcionesRespuesta opcion = cut.leer(idCreado);
        assertNotNull(opcion);

        em.getTransaction().begin();
        cut.eliminar(opcion);
        em.getTransaction().commit();

        // Vuelve a los 10 registros originales del init.sql
        assertEquals(10, cut.count());
        assertNull(cut.leer(idCreado));
    }
}
