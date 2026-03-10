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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AreasConocimiento;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AreasConocimientoDAOIT {

    // UUID del área creada en testCrear — compartido entre tests mediante campo de instancia
    private UUID idCreado;

    // EMF compartido — inicializado una sola vez en @BeforeAll
    private EntityManagerFactory emf;

    // static requerido por Testcontainers: debe ser static para arrancar en BeforeAllCallback
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.5-alpine")
            .withDatabaseName("ingresoTPI135")
            .withInitScript("ingresoTPI135_init.sql")
            .withUsername("postgres")
            .withPassword("abc123");

    public AreasConocimientoDAOIT() {
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
        System.out.println("count");
        assertTrue(postgres.isRunning());

        AreasConocimientoDAO cut = new AreasConocimientoDAO();
        cut.em = emf.createEntityManager();

        int resultado = cut.count();

        // BD recién iniciada con init.sql  3 áreas de conocimiento
        assertTrue(resultado > 0);
        assertEquals(3, resultado);
    }

    @Test
    @Order(2)
    public void testFindRange() {
        System.out.println("findRange");
        assertTrue(postgres.isRunning());

        AreasConocimientoDAO cut = new AreasConocimientoDAO();
        cut.em = emf.createEntityManager();

        List<AreasConocimiento> resultado = cut.findRange(0, 10);

        // Aún no se ha insertado nada  sigue habiendo 3
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        assertEquals(3, resultado.size());
    }

    @Test
    @Order(3)
    public void testCrear() {
        System.out.println("crear");
        assertTrue(postgres.isRunning());
        EntityManager em = emf.createEntityManager();

        AreasConocimientoDAO cut = new AreasConocimientoDAO();
        cut.em = em;

        AreasConocimiento nuevo = new AreasConocimiento();
        nuevo.setNombreArea("registro prueba 1");

        cut.em.getTransaction().begin();
        cut.crear(nuevo);
        cut.em.getTransaction().commit();

        // Guardar el UUID para que testLeer, testActualizar y testEliminar lo usen
        idCreado = nuevo.getId();

        int resultado = cut.count();
        assertEquals(4, resultado);
        System.out.println("resultado: " + resultado);
    }

    @Test
    @Order(4)
    public void testLeer() {
        System.out.println("leer");
        assertTrue(postgres.isRunning());

        AreasConocimientoDAO cut = new AreasConocimientoDAO();
        cut.em = emf.createEntityManager();

        // Lee el registro insertado en testCrear usando el UUID almacenado
        AreasConocimiento resultado = cut.leer(idCreado);

        assertNotNull(resultado);
        assertEquals("registro prueba 1", resultado.getNombreArea());
    }

    @Test
    @Order(5)
    public void testActualizar() {
        System.out.println("actualizar");
        assertTrue(postgres.isRunning());

        EntityManager em = emf.createEntityManager();
        AreasConocimientoDAO cut = new AreasConocimientoDAO();
        cut.em = em;

        // Modifica el registro creado en testCrear
        AreasConocimiento area = cut.leer(idCreado);
        assertNotNull(area);
        area.setNombreArea("registro actualizado");

        em.getTransaction().begin();
        AreasConocimiento resultado = cut.actualizar(area);
        em.getTransaction().commit();

        assertNotNull(resultado);
        assertEquals("registro actualizado", resultado.getNombreArea());
        // El conteo no cambia al actualizar  sigue en 4
        assertEquals(4, cut.count());
    }

    @Test
    @Order(6)
    public void testEliminar() {
        System.out.println("eliminar");
        assertTrue(postgres.isRunning());

        EntityManager em = emf.createEntityManager();
        AreasConocimientoDAO cut = new AreasConocimientoDAO();
        cut.em = em;

        // Elimina el registro creado en testCrear
        AreasConocimiento area = cut.leer(idCreado);
        assertNotNull(area);

        em.getTransaction().begin();
        cut.eliminar(area);
        em.getTransaction().commit();

        // Vuelve a los 3 registros originales del init.sql
        assertEquals(3, cut.count());
        assertNull(cut.leer(idCreado));
    }
}