package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.bdd;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Duration;

/**
 * Clase base para pruebas de sistema (ST) y BDD.
 *
 * Gestión de contenedores:
 *  - postgres  : PostgreSQLContainer en red interna (alias "db"), usado por Liberty.
 *  - liberty   : GenericContainer con Open Liberty que despliega el WAR.
 *  - Ryuk      : lo gestiona Testcontainers automáticamente al salir la JVM.
 *
 * Solo se crean 3 contenedores en total (postgres + liberty + Ryuk).
 * El flag inicializacionFallida evita que un fallo cree múltiples contenedores
 * Liberty en reintentos consecutivos.
 */
public abstract class BaseSistemaBDD {

    protected static Client  cliente;
    protected static String  baseUrl;

    /** true si init() completó correctamente. */
    private static boolean inicializado = false;

    /**
     * true si init() falló en el primer intento.
     * Impide reintentos que crearían contenedores adicionales y prolongarían la ejecución.
     */
    private static boolean inicializacionFallida = false;

    // -------------------------------------------------------------------------
    // Contenedores (static final = un solo objeto por JVM, creados al cargar la clase)
    // -------------------------------------------------------------------------

    protected static final Network red = Network.newNetwork();

    protected static final MountableFile war = MountableFile.forHostPath(
            Paths.get("target/IngresoUniversitarioTPI135-1.0-SNAPSHOT.war").toAbsolutePath());

    /**
     * PostgreSQL exclusivo para las pruebas de sistema.
     * Accesible desde Liberty como hostname "db" en la red interna Docker.
     */
    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.5-alpine")
            .withDatabaseName("ingresoTPI135")
            .withInitScript("ingresoTPI135_init.sql")
            .withUsername("postgres")
            .withPassword("abc123")
            .withNetwork(red)
            .withNetworkAliases("db")
            .withExposedPorts(5432);

    /**
     * Open Liberty con el WAR de la aplicación.
     *
     * Wait strategy: solo TCP (puerto 9080 accesible desde el host).
     * La disponibilidad del WAR se comprueba en init() mediante esperarDespliegueApp(),
     * lo que da control total sobre el timeout y evita cuelgues del contenedor.
     */
    protected static final GenericContainer<?> liberty = new GenericContainer<>("ingresouniversitariotpi135-base:26.0.0.2")
            .withNetwork(red)
            .withEnv("PGSERVER",   "db")
            .withEnv("PGPORT",     "5432")
            .withEnv("PGDBNAME",   "ingresoTPI135")
            .withEnv("PGUSER",     "postgres")
            .withEnv("PGPASSWORD", "abc123")
            .dependsOn(postgres)
            .withCopyFileToContainer(war, "/opt/wlp/usr/servers/tpi135_2026/dropins/ingreso.war")
            .withExposedPorts(9080)
            // Solo verifica que Liberty escuche en 9080 (rápido: ~10-30 s).
            // La comprobación del WAR la hace esperarDespliegueApp().
            .waitingFor(Wait.forListeningPort()
                    .withStartupTimeout(Duration.ofSeconds(90)));

    // -------------------------------------------------------------------------
    // Inicialización singleton
    // -------------------------------------------------------------------------

    /**
     * Inicia los contenedores y configura el cliente HTTP.
     * Llamado una sola vez desde AbstractResourceST.@BeforeAll.
     *
     * Si falla, lanza RuntimeException y marca inicializacionFallida=true
     * para que las 9 clases restantes fallen de inmediato (sin otro timeout).
     */
    public static synchronized void init() {
        if (inicializado) return;

        if (inicializacionFallida) {
            throw new IllegalStateException(
                    "El entorno de sistema no pudo iniciarse. " +
                    "Revisa el log del primer intento para ver la causa raíz.");
        }

        try {
            postgres.start();
            liberty.start();

            // Espera activa hasta que el WAR esté desplegado y respondiendo 200.
            esperarDespliegueApp();

            ClientConfig config = new ClientConfig();
            config.register(ProveedorJacksonTiempo.class);

            cliente = ClientBuilder.newClient(config);
            baseUrl = String.format(
                    "http://localhost:%d/ingreso/resources/v1",
                    liberty.getMappedPort(9080)
            );

            inicializado = true;
        } catch (Exception e) {
            // Marca el fallo para que los reintentos no creen más contenedores.
            inicializacionFallida = true;
            throw new RuntimeException("Error inicializando entorno de sistema", e);
        }
    }

    /**
     * Sondea GET /ingreso/resources/v1/areas cada 3 segundos hasta obtener HTTP 200.
     * Espera un máximo de 60 segundos para que Liberty despliegue el WAR.
     *
     * <p>Si el problema persiste, obtén los logs del contenedor Liberty con:
     * <pre>docker logs $(docker ps --filter "ancestor=ingresouniversitariotpi135-base:26.0.0.2" -q --latest)</pre>
     *
     * @throws RuntimeException si el WAR no responde 200 en el tiempo máximo,
     *                          o si Liberty responde 5xx (error de despliegue).
     */
    private static void esperarDespliegueApp() throws Exception {
        String urlApp = String.format(
                "http://localhost:%d/ingreso/resources/v1/areas",
                liberty.getMappedPort(9080)
        );

        int maxIntentos = 20;   // 20 × 3 s = 60 segundos máximo
        int pausaMs     = 3_000;

        for (int intento = 1; intento <= maxIntentos; intento++) {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(urlApp).openConnection();
                conn.setConnectTimeout(2_000);
                conn.setReadTimeout(2_000);
                conn.setRequestMethod("GET");
                int status = conn.getResponseCode();
                conn.disconnect();

                if (status == 200) {
                    return; // WAR completamente desplegado
                }

                // 5xx indica que el WAR se desplegó pero tiene errores internos
                // (ej. datasource mal configurado). No tiene sentido seguir esperando.
                if (status >= 500) {
                    throw new RuntimeException(
                            "Liberty responde HTTP " + status + " en " + urlApp +
                            ". Posible error de datasource o CDI en el WAR. " +
                            "Diagnóstico: docker logs <id_liberty>");
                }

                // 404 / 503 = WAR aún no desplegado, seguir esperando

            } catch (java.io.IOException ignored) {
                // Incluye ConnectException (puerto no disponible aún) y
                // SocketTimeoutException (Liberty iniciando pero lento). Reintentar.
            }

            Thread.sleep(pausaMs);
        }

        throw new RuntimeException(
                "El WAR no respondió HTTP 200 en " + urlApp +
                " después de " + (maxIntentos * pausaMs / 1000) + " segundos. " +
                "Diagnóstico: docker logs $(docker ps --filter " +
                "\"ancestor=ingresouniversitariotpi135-base:26.0.0.2\" -q --latest)");
    }

    // -------------------------------------------------------------------------
    // Accessors y helpers HTTP
    // -------------------------------------------------------------------------

    public static Client getClient()  { return cliente; }
    public static String getBaseUrl() { return baseUrl; }

    protected WebTarget targetDe(String recurso) {
        return cliente.target(baseUrl + "/" + recurso);
    }

    protected Response get(String recurso) {
        return targetDe(recurso)
                .request(MediaType.APPLICATION_JSON)
                .get();
    }

    protected Response post(String recurso, Object payload) {
        return targetDe(recurso)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(payload, MediaType.APPLICATION_JSON));
    }

    protected Response put(String recurso, Object payload) {
        return targetDe(recurso)
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.entity(payload, MediaType.APPLICATION_JSON));
    }

    protected Response delete(String recurso) {
        return targetDe(recurso)
                .request(MediaType.APPLICATION_JSON)
                .delete();
    }
}
