package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.Test;
import java.util.Map;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class AreasConocimientoDAOIT {


    @Container
    GenericContainer postgres = new PostgreSQLContainer("postgres:17.5-alpine")
            .withDatabaseName("ingresoTPI135")
            .withInitScript("ingresoTPI135_init.sql")
            .withPassword("abc123")
            .withUsername("postgres")
            .withExposedPorts(5432);
public AreasConocimientoDAOIT() {
    }


   @Test
    public void testCount() {
        System.out.println("count");
       assertTrue(postgres.isRunning());
       Integer puertoPostgresql = postgres.getMappedPort(5432);
       Map<String, Object> props = Map.of(
           "jakarta.persistence.jdbc.url", "jdbc:postgresql://localhost:" + puertoPostgresql + "/ingresoTPI135",
           "jakarta.persistence.jdbc.user", "postgres",
           "jakarta.persistence.jdbc.password", "abc123"
       );
       EntityManagerFactory emf = Persistence.createEntityManagerFactory("ingresoPUIT", props);
       EntityManager em = emf.createEntityManager();
       AreasConocimientoDAO cut = new AreasConocimientoDAO();
       cut.em = em;
       int resultado = cut.count();
       assertTrue(resultado>0);

        fail("The test case is a prototype.");
     }

}