package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.bdd;

import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import static sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.RestHeaders.TOTAL_RECORDS;

/**
 * Esqueleto ST para el integrante 1.
 * Feature: flujo de inscripcion de aspirante.
 */
@Disabled("Esqueleto base para implementar pruebas del flujo de inscripcion")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FlujoInscripcionST extends BaseSistemaST {

    UUID idAspirantePrueba;
    UUID idInscripcionPrueba;

    @Test
    @Order(1)
    void listarAspirantesPaginado_debeRetornar200YTotalRecords() {
        // TODO integrante 1: GET /aspirantes_datos?first=0&max=50
        System.out.println("listarAspirantesPaginado_debeRetornar200");

        Response respuesta = targetDe("aspirantes_datos")
                .queryParam("first", 0)
                .queryParam("max", 50)
                .request(MediaType.APPLICATION_JSON)
                .get();

        Assertions.assertEquals(200, respuesta.getStatus());
        Assertions.assertTrue(respuesta.getHeaders().containsKey(TOTAL_RECORDS));

        //Leer la respuesta utilizando JSON
        List<Map<String, Object>> aspirantes = respuesta.readEntity(new GenericType< List<Map<String, Object >>>(){});
        Assertions.assertFalse(aspirantes.isEmpty(), "No debe de ser null poeque la BD prueba debe tener datos");

        //id del 1er aspirante para la siguiente prueba
        idAspirantePrueba = UUID.fromString(aspirantes.get(0).get("id").toString());

    }

    @Test
    @Order(2)
    void obtenerAspiranteExistentePorId_debeRetornar200() {
        // TODO integrante 1: GET /aspirantes_datos/{id}
        System.out.println("Orden 2\nobtenerAspiranteExistentePorId_debeRetornar200");
        System.out.println("ID semilla utilizada: "+idAspirantePrueba.toString());

        Assertions.assertNotNull(idAspirantePrueba,"El ID del Aspirante prueba NO debe ser null");

        Response respuesta = targetDe("Aspirante_datos/"+ idAspirantePrueba)
                .request(MediaType.APPLICATION_JSON).get();

        Assertions.assertEquals(200, respuesta.getStatus(), "Debe de encontrar el ID  del Aspirante prueba con un 200");

        Map<String, Object> aspirante = respuesta.readEntity(new  GenericType<Map<String, Object>>(){});

        //COmparamos IDs
        Assertions.assertEquals(idAspirantePrueba.toString(), aspirante.get("id").toString());

    }

    @Test
    @Order(3)*****
    void crearInscripcionValida_debeRetornar201YSerConsultable() {
        // TODO integrante 1: POST /inscripciones_prueba y luego GET por id
        System.out.println("Orden 3\ncrearInscripcionValida_debeRetornar201");
        Map<String, Object> nuevaInscripcion = new HashMap<>();


    }

    @Test
    @Order(4)
    void agregarCarrerasElegidas_debePersistirPrioridades() {
        // TODO integrante 1: POST /carreras_elegidas para una inscripcion creada
    }

    @Test
    @Order(5)
    void consultarProcesoAdmision_debeRetornarEstadoEsperado() {
        // TODO integrante 1: GET /proceso_admision_aspirante/{idInscripcion}
    }

    @Test
    @Order(6)
    void crearInscripcionConAspiranteInexistente_debeRetornarError() {
        // TODO integrante 1: escenario negativo por FK invalida
    }

    @Test
    @Order(7)
    void consultarInscripcionInexistente_debeRetornar404() {
        // TODO integrante 1: GET /inscripciones_prueba/{idInexistente}
    }
}
