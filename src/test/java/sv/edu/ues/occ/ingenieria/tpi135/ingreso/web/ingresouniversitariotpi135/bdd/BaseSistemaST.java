package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.bdd;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Paths;

/**
 * Clase base para pruebas de sistema.
 * Centraliza infraestructura de Testcontainers y helpers HTTP para reutilizar
 * en clases ST separadas por feature/caso de uso.
 */
@Testcontainers
public abstract class BaseSistemaST {

    protected static Client cliente;
    protected static String baseUrl;

    protected static final Network red = Network.newNetwork();

    protected static final MountableFile war = MountableFile.forHostPath(
            Paths.get("target/IngresoUniversitarioTPI135-1.0-SNAPSHOT.war").toAbsolutePath());

    @Container
    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.5-alpine")
            .withDatabaseName("ingresoTPI135")
            .withInitScript("ingresoTPI135_init.sql")
            .withUsername("postgres")
            .withPassword("abc123")
            .withNetwork(red)
            .withNetworkAliases("db")
            .withExposedPorts(5432);

    @Container
    protected static final GenericContainer<?> liberty = new GenericContainer<>("ingresouniversitariotpi135-base:26.0.0.2")
            .withNetwork(red)
            .withEnv("PGSERVER", "db")
            .withEnv("PGPORT", "5432")
            .withEnv("PGDBNAME", "ingresoTPI135")
            .withEnv("PGUSER", "postgres")
            .withEnv("PGPASSWORD", "abc123")
            .dependsOn(postgres)
            .withCopyFileToContainer(war, "/opt/wlp/usr/servers/tpi135_2026/dropins/ingreso.war")
            .withExposedPorts(9080);

    @BeforeAll
    static void initSistema() {
        Assertions.assertTrue(liberty.isRunning());
        cliente = ClientBuilder.newClient();
        baseUrl = String.format("http://localhost:%d/ingreso/resources/v1", liberty.getMappedPort(9080));
    }

    @AfterAll
    static void cerrarCliente() {
        if (cliente != null) {
            cliente.close();
        }
    }

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