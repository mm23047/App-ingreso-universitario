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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CuposCarrera;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CuposCarreraId;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CuposCarreraDAOIT {

    // UUIDs del init.sql
    private static final UUID ID_PRUEBA_2026  = UUID.fromString("d1000000-0000-0000-0000-000000000001");
    private static final UUID ID_ETAPA_FINAL  = UUID.fromString("c1000000-0000-0000-0000-000000000003");
    private static final UUID ID_ETAPA_1      = UUID.fromString("c1000000-0000-0000-0000-000000000001");

    // ID del cupo creado en testCrear — compartido entre tests
    private static CuposCarreraId idCreado;

    // EMF compartido — inicializado una sola vez en @BeforeAll
    private static EntityManagerFactory emf;

    // static → un solo contenedor levantado una vez para toda la clase
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.5-alpine")
            .withDatabaseName("ingresoTPI135")
            .withInitScript("ingresoTPI135_init.sql")
            .withUsername("postgres")
            .withPassword("abc123");

    public CuposCarreraDAOIT() {
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
        assertTrue(postgres.isRunning());

        CuposCarreraDAO cut = new CuposCarreraDAO();
        cut.em = emf.createEntityManager();

        int resultado = cut.count();

        // BD recién iniciada con init.sql  3 cupos: ICS=50, ISI=60, ICC=45
        assertTrue(resultado > 0);
        assertEquals(3, resultado);
    }

    @Test
    @Order(2)
    public void testFindRange() {
        assertTrue(postgres.isRunning());

        CuposCarreraDAO cut = new CuposCarreraDAO();
        cut.em = emf.createEntityManager();

        List<CuposCarrera> resultado = cut.findRange(0, 10);

        // Aún no se ha insertado nada  sigue habiendo 3
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        assertEquals(3, resultado.size());
    }

    @Test
    @Order(3)
    public void testCrear() {
        assertTrue(postgres.isRunning());

        EntityManager em = emf.createEntityManager();
        CuposCarreraDAO cut = new CuposCarreraDAO();
        cut.em = em;

        // MAT aún no tiene cupos asignados en el init.sql  combinación única
        PruebasAdmision prueba  = em.find(PruebasAdmision.class, ID_PRUEBA_2026);
        CatalogoCarrera carrera = em.find(CatalogoCarrera.class, "MAT");
        EtapasAdmision  etapa   = em.find(EtapasAdmision.class, ID_ETAPA_1);

        CuposCarreraId clave = new CuposCarreraId();
        clave.setIdPrueba(ID_PRUEBA_2026);
        clave.setIdCarrera("MAT");
        clave.setIdEtapa(ID_ETAPA_1);

        CuposCarrera nuevo = new CuposCarrera();
        nuevo.setId(clave);
        nuevo.setIdPrueba(prueba);
        nuevo.setIdCarrera(carrera);
        nuevo.setIdEtapa(etapa);
        nuevo.setCupos(30);

        em.getTransaction().begin();
        cut.crear(nuevo);
        em.getTransaction().commit();

        // Guardar el ID para que testLeer, testActualizar y testEliminar lo usen
        idCreado = nuevo.getId();

        assertEquals(4, cut.count());
    }

    @Test
    @Order(4)
    public void testLeer() {
        assertTrue(postgres.isRunning());

        CuposCarreraDAO cut = new CuposCarreraDAO();
        cut.em = emf.createEntityManager();

        // Leer el registro ICS del init.sql: prueba 2026, carrera ICS, etapa final, cupos=50
        CuposCarreraId clave = new CuposCarreraId();
        clave.setIdPrueba(ID_PRUEBA_2026);
        clave.setIdCarrera("ICS");
        clave.setIdEtapa(ID_ETAPA_FINAL);

        CuposCarrera resultado = cut.leer(clave);

        assertNotNull(resultado);
        assertEquals("ICS", resultado.getId().getIdCarrera());
        assertEquals(ID_PRUEBA_2026, resultado.getId().getIdPrueba());
        assertEquals(ID_ETAPA_FINAL, resultado.getId().getIdEtapa());
        assertEquals(50, resultado.getCupos());
    }

    @Test
    @Order(5)
    public void testActualizar() {
        assertTrue(postgres.isRunning());

        EntityManager em = emf.createEntityManager();
        CuposCarreraDAO cut = new CuposCarreraDAO();
        cut.em = em;

        // Leer ISI (cupos=60) y cambiar a 75
        CuposCarreraId clave = new CuposCarreraId();
        clave.setIdPrueba(ID_PRUEBA_2026);
        clave.setIdCarrera("ISI");
        clave.setIdEtapa(ID_ETAPA_FINAL);

        CuposCarrera cupo = cut.leer(clave);
        cupo.setCupos(75);

        em.getTransaction().begin();
        CuposCarrera resultado = cut.actualizar(cupo);
        em.getTransaction().commit();

        assertNotNull(resultado);
        assertEquals(75, resultado.getCupos());

        // Limpiar cache y verificar que el cambio persiste en BD
        em.clear();
        CuposCarrera verificacion = cut.leer(clave);
        assertEquals(75, verificacion.getCupos());
    }

    @Test
    @Order(6)
    public void testEliminar() {
        assertTrue(postgres.isRunning());

        EntityManager em = emf.createEntityManager();
        CuposCarreraDAO cut = new CuposCarreraDAO();
        cut.em = em;

        // Eliminar el cupo creado en testCrear
        CuposCarrera cupo = cut.leer(idCreado);
        assertNotNull(cupo);

        em.getTransaction().begin();
        cut.eliminar(cupo);
        em.getTransaction().commit();

        // Vuelve a los 3 registros originales del init.sql
        assertEquals(3, cut.count());
        assertNull(cut.leer(idCreado));
    }
}
