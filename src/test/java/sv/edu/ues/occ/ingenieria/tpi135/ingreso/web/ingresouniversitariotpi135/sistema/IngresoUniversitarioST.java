package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.sistema;

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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AreasConocimiento;

import java.util.List;

import static sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.RestHeaders.TOTAL_RECORDS;

/**
 * Pruebas de Sistema (ST) — levantan dos contenedores en una red Docker compartida:
 *   1. PostgreSQL 17-alpine con los datos iniciales (ingresoTPI135_init.sql)
 *   2. Open Liberty con el WAR inyectado en tiempo de test (sin embeber en la imagen)
 *
 * Pre-requisito: construir la imagen base UNA sola vez antes de ejecutar este perfil:
 *   docker build -t ingresouniversitariotpi135-base:26.0.0.2 .
 *
 * Ejecutar las pruebas de sistema:
 *   mvn -Psistema verify
 *
 * URL base resultante: http://localhost:<puerto>/ingreso/resources/v1/
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IngresoUniversitarioST extends BaseSistemaST {

    static WebTarget target;

    @BeforeAll
    public void inicializar() {
        Assertions.assertTrue(liberty.isRunning());
        target = targetDe("areas_conocimiento");
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
