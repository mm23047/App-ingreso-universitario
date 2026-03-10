package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ClavesExaman;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntasPorClave;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntasPorClaveId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PreguntasPorClaveDAOIT {

    // UUIDs del init.sql
    private static final UUID ID_CLAVE_A    = UUID.fromString("08000000-0000-0000-0000-000000000001");
    private static final UUID ID_CLAVE_B    = UUID.fromString("08000000-0000-0000-0000-000000000002");
    private static final UUID ID_PREGUNTA_1 = UUID.fromString("f1000000-0000-0000-0000-000000000001");
    private static final UUID ID_PREGUNTA_3 = UUID.fromString("f1000000-0000-0000-0000-000000000003");

    // ID creado en testCrear — compartido entre tests
    private PreguntasPorClaveId idCreado;

    // EMF compartido — inicializado una sola vez en @BeforeAll
    private EntityManagerFactory emf;

    // static  un solo contenedor levantado una vez para toda la clase
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.5-alpine")
            .withDatabaseName("ingresoTPI135")
            .withInitScript("ingresoTPI135_init.sql")
            .withUsername("postgres")
            .withPassword("abc123");

    public PreguntasPorClaveDAOIT() {
    }

    @BeforeAll
    void inicializar() {
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
        assertTrue(postgres.isRunning());

        PreguntasPorClaveDAO cut = new PreguntasPorClaveDAO();
        cut.em = emf.createEntityManager();

        int resultado = cut.count();

        // BD recién iniciada con init.sql  4 registros: Clave A→{pregunta1, pregunta2}, Clave B→{pregunta3, pregunta4}
        assertTrue(resultado > 0);
        assertEquals(4, resultado);
    }

    @Test
    @Order(2)
    public void testFindRange() {
        assertTrue(postgres.isRunning());

        PreguntasPorClaveDAO cut = new PreguntasPorClaveDAO();
        cut.em = emf.createEntityManager();

        List<PreguntasPorClave> resultado = cut.findRange(0, 10);

        // Aún no se ha insertado nada  sigue habiendo 4
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        assertEquals(4, resultado.size());
    }

    @Test
    @Order(3)
    public void testCrear() {
        assertTrue(postgres.isRunning());

        EntityManager em = emf.createEntityManager();
        PreguntasPorClaveDAO cut = new PreguntasPorClaveDAO();
        cut.em = em;

        // Clave A solo tiene pregunta1 y pregunta2 en el init.sql
        //  (Clave_A, pregunta3) es una combinación nueva sin conflicto de clave primaria
        ClavesExaman clave    = em.find(ClavesExaman.class, ID_CLAVE_A);
        BancoPregunta pregunta = em.find(BancoPregunta.class, ID_PREGUNTA_3);

        PreguntasPorClaveId id = new PreguntasPorClaveId();
        id.setIdClave(ID_CLAVE_A);
        id.setIdPregunta(ID_PREGUNTA_3);

        PreguntasPorClave nuevo = new PreguntasPorClave();
        nuevo.setId(id);
        nuevo.setIdClave(clave);
        nuevo.setIdPregunta(pregunta);

        em.getTransaction().begin();
        cut.crear(nuevo);
        em.getTransaction().commit();

        // Guardar el ID para que testLeer, testActualizar y testEliminar lo usen
        idCreado = nuevo.getId();

        assertEquals(5, cut.count());
    }

    @Test
    @Order(4)
    public void testLeer() {
        assertTrue(postgres.isRunning());

        PreguntasPorClaveDAO cut = new PreguntasPorClaveDAO();
        cut.em = emf.createEntityManager();

        // Leer Clave A + Pregunta 1 — primer registro del init.sql
        PreguntasPorClaveId id = new PreguntasPorClaveId();
        id.setIdClave(ID_CLAVE_A);
        id.setIdPregunta(ID_PREGUNTA_1);

        PreguntasPorClave resultado = cut.leer(id);

        assertNotNull(resultado);
        assertEquals(ID_CLAVE_A,    resultado.getId().getIdClave());
        assertEquals(ID_PREGUNTA_1, resultado.getId().getIdPregunta());
    }

    @Test
    @Order(5)
    public void testActualizar() {
        assertTrue(postgres.isRunning());

        EntityManager em = emf.createEntityManager();
        PreguntasPorClaveDAO cut = new PreguntasPorClaveDAO();
        cut.em = em;

        // PreguntasPorClave es una tabla de unión pura: solo tiene los dos UUIDs de la clave
        // compuesta y no posee columnas mutables adicionales.
        // Se verifica que actualizar() no lanza excepción y devuelve el mismo registro intacto.
        PreguntasPorClaveId id = new PreguntasPorClaveId();
        id.setIdClave(ID_CLAVE_B);
        id.setIdPregunta(ID_PREGUNTA_3);

        PreguntasPorClave registro = cut.leer(id);
        assertNotNull(registro);

        em.getTransaction().begin();
        PreguntasPorClave resultado = cut.actualizar(registro);
        em.getTransaction().commit();

        // No hay campos que cambien; se verifica que la operación es idempotente
        assertNotNull(resultado);
        assertEquals(ID_CLAVE_B,    resultado.getId().getIdClave());
        assertEquals(ID_PREGUNTA_3, resultado.getId().getIdPregunta());
        // testCrear ya insertó uno nuevo → el conteo en este punto es 5
        assertEquals(5, cut.count());
    }

    @Test
    @Order(6)
    public void testEliminar() {
        assertTrue(postgres.isRunning());

        EntityManager em = emf.createEntityManager();
        PreguntasPorClaveDAO cut = new PreguntasPorClaveDAO();
        cut.em = em;

        // Eliminar el registro creado en testCrear
        PreguntasPorClave registro = cut.leer(idCreado);
        assertNotNull(registro);

        em.getTransaction().begin();
        cut.eliminar(registro);
        em.getTransaction().commit();

        // Vuelve a los 4 registros originales del init.sql
        assertEquals(4, cut.count());
        assertNull(cut.leer(idCreado));
    }
}
