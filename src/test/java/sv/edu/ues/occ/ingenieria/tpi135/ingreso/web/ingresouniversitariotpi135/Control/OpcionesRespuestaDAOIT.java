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


import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OpcionesRespuestaDAOIT {

    private static EntityManagerFactory emf;
    // ID que vamos a utilizar durante la prueba CRUD
    private static UUID idOpcionRespuesta;

        //Contenedor de Docker (Se levanta una vez para toda la clase)
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.5-alpine")
            .withDatabaseName("ingresoTPI135")
            .withInitScript("ingresoTPI135_init.sql")
            .withUsername("postgres")
            .withPassword("abc123");

    // Configuración inicial
    @BeforeAll
    static void inicializar() {
        Integer puertoPostgresql = postgres.getMappedPort(5432);
        Map<String, Object> propiedades = new HashMap<>();
        propiedades.put("jakarta.persistence.jdbc.url", String.format("jdbc:postgresql://localhost:%d/ingresoTPI135", puertoPostgresql));
        propiedades.put("jakarta.persistence.jdbc.user", "postgres");
        propiedades.put("jakarta.persistence.jdbc.password", "abc123");
        emf = Persistence.createEntityManagerFactory("ingresoPUIT", propiedades);
    }

    public OpcionesRespuestaDAOIT() {
    }
    
    @Test
    @Order(1)
    public void testCount() {
        System.out.println("Inicializando TEST COUNT() del DAO OpcionesRespuesta");
        assertTrue(postgres.isRunning());

        EntityManager em = emf.createEntityManager();
        OpcionesRespuestaDAO cut = new OpcionesRespuestaDAO();
        cut.em = em;

        int resultado = cut.count();
        assertEquals(resultado, 10);
    }

    @Test
    @Order(2)
    public void testFindRange() {
        System.out.println("Inicializando TEST testFindRange() del DAO OpcionesRespuesta");
        OpcionesRespuestaDAO cut = new OpcionesRespuestaDAO();
        EntityManager em = emf.createEntityManager();
        cut.em = em;
        List<OpcionesRespuesta> resultado = cut.findRange(0, 10);
        assertNotNull(resultado);
        System.out.println("Opciones de respuesta encontradas: " + resultado.size());
        assertTrue(resultado.size() > 0);

    }

    @Test
    @Order(3)
    public void testCreate() {
        System.out.println("Inicializando TEST testCreate() del DAO OpcionesRespuesta");
        OpcionesRespuestaDAO cut = new OpcionesRespuestaDAO();
        EntityManager em = emf.createEntityManager();
        
        cut.em = em;

        BancoPregunta idBancoPreguntaParaGuardar = em.createQuery("SELECT bp FROM BancoPregunta bp", BancoPregunta.class)
                .setMaxResults(1)
                .getSingleResult();

        OpcionesRespuesta nuevaOpcion = new OpcionesRespuesta();

        nuevaOpcion.setIdPregunta(idBancoPreguntaParaGuardar);
        nuevaOpcion.setTextoOpcion("4");
        nuevaOpcion.setEsCorrecta(true);

        cut.em.getTransaction().begin();
        cut.crear(nuevaOpcion);
        cut.em.getTransaction().commit();

        assertEquals(11, cut.count());
        System.out.println("Cantidad de datos en la BD: "+cut.count());

    }
    



}