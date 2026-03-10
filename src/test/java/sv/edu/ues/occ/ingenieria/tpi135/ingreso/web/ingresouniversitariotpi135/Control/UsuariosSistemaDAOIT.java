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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.UsuariosSistema;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UsuariosSistemaDAOIT {

    // UUID del usuario creado en testCrear — compartido entre tests
    private UUID idCreado;

    // EMF compartido — inicializado una sola vez en @BeforeAll
    private EntityManagerFactory emf;

    // static → un solo contenedor levantado una vez para toda la clase
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.5-alpine")
            .withDatabaseName("ingresoTPI135")
            .withInitScript("ingresoTPI135_init.sql")
            .withUsername("postgres")
            .withPassword("abc123");

    public UsuariosSistemaDAOIT() {
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

        UsuariosSistemaDAO cut = new UsuariosSistemaDAO();
        cut.em = emf.createEntityManager();

        int resultado = cut.count();

        // BD recién iniciada con init.sql → 3 usuarios del sistema
        assertTrue(resultado > 0);
        assertEquals(3, resultado);
    }

    @Test
    @Order(2)
    public void testFindRange() {
        System.out.println("findRange");
        assertTrue(postgres.isRunning());

        UsuariosSistemaDAO cut = new UsuariosSistemaDAO();
        cut.em = emf.createEntityManager();

        List<UsuariosSistema> resultado = cut.findRange(0, 10);

        // Aún no se ha insertado nada → sigue habiendo 3
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
        UsuariosSistemaDAO cut = new UsuariosSistemaDAO();
        cut.em = em;

        UsuariosSistema nuevo = new UsuariosSistema();
        nuevo.setNombreUsuario("testuser");
        nuevo.setCorreo("testuser@ues.edu.sv");
        nuevo.setContrasenaHash("$2a$10$hashtest");
        nuevo.setRol("ASPIRANTE");

        em.getTransaction().begin();
        cut.crear(nuevo);
        em.getTransaction().commit();

        // Guardar el UUID para que testLeer, testActualizar y testEliminar lo usen
        idCreado = nuevo.getId();

        assertNotNull(idCreado);
        assertEquals(4, cut.count());
    }

    @Test
    @Order(4)
    public void testLeer() {
        System.out.println("leer");
        assertTrue(postgres.isRunning());

        UsuariosSistemaDAO cut = new UsuariosSistemaDAO();
        cut.em = emf.createEntityManager();

        // Lee el registro insertado en testCrear usando el UUID almacenado
        UsuariosSistema resultado = cut.leer(idCreado);

        assertNotNull(resultado);
        assertEquals("testuser", resultado.getNombreUsuario());
        assertEquals("testuser@ues.edu.sv", resultado.getCorreo());
    }

    @Test
    @Order(5)
    public void testActualizar() {
        System.out.println("actualizar");
        assertTrue(postgres.isRunning());

        EntityManager em = emf.createEntityManager();
        UsuariosSistemaDAO cut = new UsuariosSistemaDAO();
        cut.em = em;

        // Modifica el registro creado en testCrear
        UsuariosSistema usuario = cut.leer(idCreado);
        assertNotNull(usuario);
        usuario.setNombreUsuario("testuser_actualizado");

        em.getTransaction().begin();
        UsuariosSistema resultado = cut.actualizar(usuario);
        em.getTransaction().commit();

        assertNotNull(resultado);
        assertEquals("testuser_actualizado", resultado.getNombreUsuario());
        // El conteo no cambia al actualizar → sigue en 4
        assertEquals(4, cut.count());
    }

    @Test
    @Order(6)
    public void testEliminar() {
        System.out.println("eliminar");
        assertTrue(postgres.isRunning());

        EntityManager em = emf.createEntityManager();
        UsuariosSistemaDAO cut = new UsuariosSistemaDAO();
        cut.em = em;

        // Elimina el registro creado en testCrear
        UsuariosSistema usuario = cut.leer(idCreado);
        assertNotNull(usuario);

        em.getTransaction().begin();
        cut.eliminar(usuario);
        em.getTransaction().commit();

        // Vuelve a los 3 registros originales del init.sql
        assertEquals(3, cut.count());
        assertNull(cut.leer(idCreado));
    }
}
