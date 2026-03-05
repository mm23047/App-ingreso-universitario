package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class InscripcionesPruebaDAOIT {

    @Container
    PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.5-alpine")
            .withDatabaseName("ingresoTPI135")
            .withInitScript("ingresoTPI135_init.sql")
            .withUsername("postgres")
            .withPassword("abc123");

    public InscripcionesPruebaDAOIT() {
    }

    private EntityManager crearEntityManager() {
        Integer puerto = postgres.getMappedPort(5432);
        Map<String, Object> props = Map.of(
                "jakarta.persistence.jdbc.url", "jdbc:postgresql://localhost:" + puerto + "/ingresoTPI135",
                "jakarta.persistence.jdbc.user", "postgres",
                "jakarta.persistence.jdbc.password", "abc123"
        );
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("ingresoPUIT", props);
        return emf.createEntityManager();
    }

    @Test
    public void testCount() {
        assertTrue(postgres.isRunning());

        InscripcionesPruebaDAO cut = new InscripcionesPruebaDAO();
        cut.em = crearEntityManager();

        int resultado = cut.count();

        // El init.sql inserta 2 inscripciones
        assertTrue(resultado > 0);
        assertEquals(2, resultado);
    }
}
