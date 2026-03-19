package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Contenedor PostgreSQL compartido para todas las pruebas de integración.
 *
 * Se inicia una sola vez por JVM (suite completa) y se reutiliza en todos
 * los tests que lo necesiten.
 */
public class SharedPostgresContainer extends PostgreSQLContainer<SharedPostgresContainer> {

    private static final String IMAGE = "postgres:17.5-alpine";

    private static final SharedPostgresContainer INSTANCE = new SharedPostgresContainer();

    private SharedPostgresContainer() {
        super(IMAGE);
        withDatabaseName("ingresoTPI135");
        withInitScript("ingresoTPI135_init.sql");
        withUsername("postgres");
        withPassword("abc123");
    }

    static {
        // Iniciar el contenedor una sola vez por JVM/suite
        INSTANCE.start();
    }

    public static SharedPostgresContainer getInstance() {
        return INSTANCE;
    }
}
