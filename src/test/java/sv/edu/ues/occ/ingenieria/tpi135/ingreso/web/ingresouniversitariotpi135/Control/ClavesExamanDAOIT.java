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

import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ClavesExaman;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClavesExamanDAOIT {

        //ID que utilizaremos durante la prueba CRUD
    private static UUID idClaveExamen;
    private static EntityManagerFactory emf;

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

    public ClavesExamanDAOIT() {}

    @Test
    @Order(1)
    public void testCount() {
        System.out.println("Inicializando TEST COUNT() del DAO ClavesExaman");
        assertTrue(postgres.isRunning());

        ClavesExamanDAO cut = new ClavesExamanDAO();
        cut.em=emf.createEntityManager();

        int resultado = cut.count();
        assertEquals(2, resultado);
        System.out.println("Cantidad de datos en la BD: "+resultado);

    }

    @Test
    @Order(2)
    public void testFindRange() {
        System.out.println("Inicializando TEST CREATE() del DAO ClavesExaman");
        ClavesExamanDAO cut = new ClavesExamanDAO();
        EntityManager em = emf.createEntityManager();
        cut.em = em;

        List<ClavesExaman> resultado = cut.findRange(0, 10);
        assertNotNull(resultado);
        System.out.println("Claves de examen encontradas: " + resultado.size());
        assertTrue(resultado.size() > 0);

    }
    @Test
    @Order(3)
    public void testCrear() {
        System.out.println("Inicializando TEST CREATE() del DAO ClavesExaman");
        ClavesExamanDAO cut = new ClavesExamanDAO();
        EntityManager em = emf.createEntityManager();
        cut.em = em;

        PruebasAdmision IdPruebaAdmision = em.createQuery("SELECT p FROM PruebasAdmision p", PruebasAdmision.class).setMaxResults(1).getSingleResult();

        ClavesExaman nuevaClave = new ClavesExaman();
        nuevaClave.setIdPrueba(IdPruebaAdmision);
        nuevaClave.setNombreClave("CLabe C");

        cut.em.getTransaction().begin();
        cut.crear(nuevaClave);
        cut.em.getTransaction().commit();

        idClaveExamen = nuevaClave.getId();
        assertNotNull(idClaveExamen);
        assertEquals(3, cut.count());
        System.out.println("Cantidad de datos en la BD: "+cut.count());

    }

    @Test
    @Order(4)
    public void testLeer() {
        System.out.println("Inicializando TEST LEER() del DAO ClavesExaman");
        ClavesExamanDAO cut = new ClavesExamanDAO();
        cut.em=emf.createEntityManager();

        ClavesExaman resultado = cut.leer(idClaveExamen);
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals("CLabe C", resultado.getNombreClave());
    }

    @Test
    @Order(5)
    public void testActualizar() {
        System.out.println("Inicializando TEST ACTUALIZAR() del DAO ClavesExaman");
        ClavesExamanDAO cut = new ClavesExamanDAO();
        cut.em=emf.createEntityManager();

        ClavesExaman claveExistente = cut.leer(idClaveExamen);
        assertNotNull(claveExistente, "La clave de examen a actualizar no debe ser nula");

        claveExistente.setNombreClave("Clave C Actualizada");

        cut.em.getTransaction().begin();
        cut.actualizar(claveExistente);
        cut.em.getTransaction().commit();

        ClavesExaman resultado = cut.leer(idClaveExamen);
        assertNotNull(resultado, "El resultado no debe ser nulo");

        assertEquals("Clave C Actualizada", resultado.getNombreClave());
        System.out.println("Nombre de clase actualizada: " + resultado.getNombreClave());
    }

     @Test
     @Order(6)
     public void testEliminar() {
         System.out.println("Inicializando TEST ELIMINAR() del DAO ClavesExaman");
         ClavesExamanDAO cut = new ClavesExamanDAO();
         cut.em=emf.createEntityManager();

         ClavesExaman claveExistente = cut.leer(idClaveExamen);
         assertNotNull(claveExistente, "La clave de examen a eliminar no debe ser nula");

         cut.em.getTransaction().begin();
         cut.eliminar(claveExistente);
         cut.em.getTransaction().commit();

         ClavesExaman resultado = cut.leer(idClaveExamen);
         assertNull(resultado, "El resultado debe ser nulo porque el registro fue eliminado");

         assertEquals(2, cut.count());
         System.out.println("Registros en la BD: "+cut.count());
    }



}
