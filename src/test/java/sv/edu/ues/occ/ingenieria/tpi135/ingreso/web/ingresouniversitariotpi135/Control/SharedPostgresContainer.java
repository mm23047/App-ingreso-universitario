package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control;

import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Contenedor PostgreSQL compartido para todas las pruebas de integración.
 *
 * Se inicia una sola vez por JVM (suite completa) y se reutiliza en todos
 * los tests que lo necesiten.
 */
public class SharedPostgresContainer extends PostgreSQLContainer<SharedPostgresContainer> {

    // Imagen de Docker que se usará para levantar PostgreSQL de pruebas.
    private static final String IMAGE = "postgres:17.5-alpine";

    // Instancia única (Singleton) del contenedor, compartida por toda la suite de pruebas.
    private static final SharedPostgresContainer INSTANCE = new SharedPostgresContainer();

    private SharedPostgresContainer() {
        // Configurar el contenedor con la imagen y la base de datos a utilizar.
        super(IMAGE);
        withDatabaseName("ingresoTPI135");
        withInitScript("ingresoTPI135_init.sql");
        withUsername("postgres");
        withPassword("abc123");
    }

    static {
        // Iniciar el contenedor una sola vez por JVM/suite.
        INSTANCE.start();
    }

    // Punto de acceso global a la instancia única del contenedor.
    public static SharedPostgresContainer getInstance() {
        return INSTANCE;
    }
}
