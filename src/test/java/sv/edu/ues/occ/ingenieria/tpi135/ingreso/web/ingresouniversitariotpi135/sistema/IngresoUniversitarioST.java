package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.sistema;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AreasConocimiento;

import java.nio.file.Paths;
import java.util.List;

import static sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.RestHeaders.TOTAL_RECORDS;

/**
 * Pruebas de Sistema (ST) — levantan dos contenedores en una red Docker compartida:
 *   1. PostgreSQL 17-alpine con los datos iniciales (ingresoTPI135_init.sql)
 *   2. Open Liberty con el WAR inyectado en tiempo de test (sin embeber en la imagen)
 *
 * Pre-requisito: construir la imagen base UNA sola vez antes de ejecutar este perfil:
 *   docker build -t ingresouniversitariotpi135-base:25.0.0.8 .
 *
 * Ejecutar las pruebas de sistema:
 *   mvn -Psistema verify
 *
 * URL base resultante: http://localhost:<puerto>/ingreso/resources/v1/
 */
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IngresoUniversitarioST {

    static Client cliente;
    static WebTarget target;

    // Red Docker compartida entre ambos contenedores
    static Network red = Network.newNetwork();

    // WAR construido por Maven en target/ — se inyecta en Liberty antes de arrancar
    static MountableFile war = MountableFile.forHostPath(
            Paths.get("target/IngresoUniversitarioTPI135-1.0-SNAPSHOT.war").toAbsolutePath());

    // static → Testcontainers los arranca en BeforeAllCallback (antes del @BeforeAll)
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.5-alpine")
            .withDatabaseName("ingresoTPI135")
            .withInitScript("ingresoTPI135_init.sql")
            .withUsername("postgres")
            .withPassword("abc123")
            .withNetwork(red)
            .withNetworkAliases("db")
            .withExposedPorts(5432);

    @Container
    static GenericContainer<?> liberty = new GenericContainer<>("ingresouniversitariotpi135-base:25.0.0.8")
            .withNetwork(red)
            .withEnv("PGSERVER",          "db")
            .withEnv("PGPORT",            "5432")
            .withEnv("PGDBNAME",          "ingresoTPI135")
            .withEnv("PGUSER",            "postgres")
            .withEnv("PGPASSWORD",        "abc123")
            .dependsOn(postgres)
            .withNetwork(red)
            .withCopyFileToContainer(war, "/opt/wlp/usr/servers/tpi135_2026/dropins/ingreso.war")
            .withExposedPorts(9080);

    @BeforeAll
    public void inicializar() {
        Assertions.assertTrue(liberty.isRunning());
        cliente = ClientBuilder.newClient();
        target = cliente.target(String.format("http://localhost:%d/ingreso/resources/v1/areas_conocimiento",
                liberty.getMappedPort(9080)));
    }

    @Test
    @Order(1)
    public void findRangeTest() {
        int first = 0;
        int max = 50;
        int esperado = 200;
        int totalEsperado = 3;

        Response respuesta = target.queryParam("first", first)
                .queryParam("max", max)
                .request(MediaType.APPLICATION_JSON)
                .get();
        Assertions.assertNotNull(respuesta);
        Assertions.assertEquals(esperado, respuesta.getStatus());
        Assertions.assertTrue(respuesta.getHeaders().containsKey(TOTAL_RECORDS));
        Assertions.assertEquals(totalEsperado, Integer.parseInt(respuesta.getHeaderString(TOTAL_RECORDS)));
        List<AreasConocimiento> registros = respuesta.readEntity(new GenericType<List<AreasConocimiento>>() {});

        for (AreasConocimiento registro : registros) {
            System.out.println(registro.getNombreArea());
        }
    }

}
