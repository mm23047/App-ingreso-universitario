package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.BeforeAll;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractBaseIT {

    // EntityManagerFactory compartido por todas las pruebas de integración
    protected static EntityManagerFactory emf;

    // Contenedor PostgreSQL compartido a nivel de suite/JVM
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