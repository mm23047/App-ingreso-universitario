package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;

@Testcontainers
public abstract class AbstractBaseIT {

    //  'private' en vez de 'protected' para que las clases hijas lo hereden
    protected static EntityManagerFactory emf;

    // Singleton del contenedor PostgreSQL para todas las pruebas de integracion
    static class SharedPostgresContainer extends PostgreSQLContainer<SharedPostgresContainer> {

        private static final String IMAGE = "postgres:17.5-alpine";

        private static final SharedPostgresContainer INSTANCE = new SharedPostgresContainer();

        private SharedPostgresContainer() {
            super(IMAGE);
            withDatabaseName("ingresoTPI135");
            withInitScript("ingresoTPI135_init.sql");
            withUsername("postgres");
            withPassword("abc123");
        }

        static SharedPostgresContainer getInstance() {
            return INSTANCE;
        }
    }

    @Container
    protected static SharedPostgresContainer postgres = SharedPostgresContainer.getInstance();

    @BeforeAll
    static void inicializarConfiguracionDocker() {
        Integer puertoPostgresql = postgres.getMappedPort(5432);
        Map<String, Object> propiedades = new HashMap<>();
        propiedades.put("jakarta.persistence.jdbc.url", String.format("jdbc:postgresql://localhost:%d/ingresoTPI135", puertoPostgresql));
        propiedades.put("jakarta.persistence.jdbc.user", "postgres");
        propiedades.put("jakarta.persistence.jdbc.password", "abc123");
        emf = Persistence.createEntityManagerFactory("ingresoPUIT", propiedades);
    }
}