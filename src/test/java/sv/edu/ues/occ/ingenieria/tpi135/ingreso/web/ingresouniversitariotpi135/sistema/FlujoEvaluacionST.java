package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.sistema;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import java.net.URI;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.RestHeaders.NOT_FOUND_ID;
import static sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.RestHeaders.TOTAL_RECORDS;

/**
 * Esqueleto ST para el integrante 3.
 * Feature: flujo de evaluacion y resultado de admision.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FlujoEvaluacionST extends BaseSistemaST {

    private static final UUID ID_PRUEBA_SEMILLA = UUID.fromString("d1000000-0000-0000-0000-000000000001");
    private static final UUID ID_PREGUNTA_SEMILLA = UUID.fromString("f1000000-0000-0000-0000-000000000003");
    private static final UUID ID_EXAMEN_SEMILLA = UUID.fromString("0d000000-0000-0000-0000-000000000001");
    private static final UUID ID_INSCRIPCION_SEMILLA = UUID.fromString("09000000-0000-0000-0000-000000000001");
    private static final UUID ID_ETAPA_ORIGINAL = UUID.fromString("c1000000-0000-0000-0000-000000000001");
    private static final UUID ID_ETAPA_ACTUALIZADA = UUID.fromString("c1000000-0000-0000-0000-000000000002");

    private UUID idClaveCreada;
    private boolean preguntaPorClaveCreada;

    @Test
    @Order(1)
    void listarClavesExamenPaginado_debeRetornar200YTotalRecords() {
    Response respuesta = targetDe("claves_examen")
        .queryParam("first", 0)
        .queryParam("max", 50)
        .request(MediaType.APPLICATION_JSON)
        .get();

    assertNotNull(respuesta);
    assertEquals(200, respuesta.getStatus());
    assertTrue(respuesta.getHeaders().containsKey(TOTAL_RECORDS));
    assertTrue(Integer.parseInt(respuesta.getHeaderString(TOTAL_RECORDS)) >= 2);
    }

    @Test
    @Order(2)
    void crearClaveExamenValida_debeRetornar201YSerConsultable() {
    String nombreClave = "I3_CLAVE_" + System.currentTimeMillis();
    String payload = """
        {
          "idPrueba": {"id": "%s"},
          "nombreClave": "%s"
        }
        """.formatted(ID_PRUEBA_SEMILLA, nombreClave);

    Response creacion = targetDe("claves_examen")
        .request(MediaType.APPLICATION_JSON)
        .post(Entity.entity(payload, MediaType.APPLICATION_JSON));

    assertEquals(201, creacion.getStatus());
    URI location = creacion.getLocation();
    assertNotNull(location);

    idClaveCreada = extraerUuidFinal(location);
    assertNotNull(idClaveCreada);

    Response consulta = targetDe("claves_examen/" + idClaveCreada)
        .request(MediaType.APPLICATION_JSON)
        .get();

    assertEquals(200, consulta.getStatus());
    String body = consulta.readEntity(String.class);
    assertTrue(body.contains(nombreClave));
    }

    @Test
    @Order(3)
    void asociarPreguntasPorClave_debeRetornar201YSerConsultable() {
    assertNotNull(idClaveCreada, "Debe existir una clave creada en el test anterior");

    String payload = """
        {
          "id": {
            "idClave": "%s",
            "idPregunta": "%s"
          },
          "idClave": {"id": "%s"},
          "idPregunta": {"id": "%s"}
        }
        """.formatted(idClaveCreada, ID_PREGUNTA_SEMILLA, idClaveCreada, ID_PREGUNTA_SEMILLA);

    Response creacion = targetDe("preguntas_por_clave")
        .request(MediaType.APPLICATION_JSON)
        .post(Entity.entity(payload, MediaType.APPLICATION_JSON));

    assertEquals(201, creacion.getStatus());
    preguntaPorClaveCreada = true;

    Response consulta = targetDe("preguntas_por_clave/" + idClaveCreada + "/" + ID_PREGUNTA_SEMILLA)
        .request(MediaType.APPLICATION_JSON)
        .get();

    assertEquals(200, consulta.getStatus());
    String body = consulta.readEntity(String.class);
    assertTrue(body.contains(idClaveCreada.toString()));
    assertTrue(body.contains(ID_PREGUNTA_SEMILLA.toString()));
    }

    @Test
    @Order(4)
    void consultarExamenRealizadoSemilla_debeRetornar200() {
    Response consulta = targetDe("examenes_realizados/" + ID_EXAMEN_SEMILLA)
        .request(MediaType.APPLICATION_JSON)
        .get();

    assertEquals(200, consulta.getStatus());
    String body = consulta.readEntity(String.class);
    assertTrue(body.contains(ID_EXAMEN_SEMILLA.toString()));
    }

    @Test
    @Order(5)
    void actualizarProcesoAdmisionSegunResultado_debeRetornar200() {
    String rutaProceso = "proceso_admision_aspirante/" + ID_INSCRIPCION_SEMILLA;
    String estadoTemporal = "EVALUADO_ST";

    Response actualizacion = null;
    try {
        String payloadUpdate = """
            {
              "inscripcionesPrueba": {"id": "%s"},
              "idEtapaActual": {"id": "%s"},
              "estado": "%s",
              "carreraAsignada": {"idCarrera": "ICS"}
            }
            """.formatted(ID_INSCRIPCION_SEMILLA, ID_ETAPA_ACTUALIZADA, estadoTemporal);

        actualizacion = targetDe(rutaProceso)
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.entity(payloadUpdate, MediaType.APPLICATION_JSON));

        assertEquals(200, actualizacion.getStatus());

        Response consulta = targetDe(rutaProceso)
            .request(MediaType.APPLICATION_JSON)
            .get();

        assertEquals(200, consulta.getStatus());
        String body = consulta.readEntity(String.class);
        assertTrue(body.contains(estadoTemporal));
        assertTrue(body.contains(ID_ETAPA_ACTUALIZADA.toString()));
        assertTrue(body.contains("ICS"));
    } finally {
        String payloadRestore = """
            {
              "inscripcionesPrueba": {"id": "%s"},
              "idEtapaActual": {"id": "%s"},
              "estado": "EN_PROCESO",
              "carreraAsignada": null
            }
            """.formatted(ID_INSCRIPCION_SEMILLA, ID_ETAPA_ORIGINAL);

        Response restauracion = targetDe(rutaProceso)
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.entity(payloadRestore, MediaType.APPLICATION_JSON));

        assertEquals(200, restauracion.getStatus());
    }
    }

    @Test
    @Order(6)
    void registrarExamenNoPermitido_debeRetornar405() {
    String payload = """
        {
          "idAsignacion": {"id": "0c000000-0000-0000-0000-000000009999"},
          "idClave": {"id": "08000000-0000-0000-0000-000000000001"},
          "idEtapa": {"id": "c1000000-0000-0000-0000-000000000001"},
          "puntajeFinal": 7.5
        }
        """;

    Response intento = targetDe("examenes_realizados")
        .request(MediaType.APPLICATION_JSON)
        .post(Entity.entity(payload, MediaType.APPLICATION_JSON));

    assertEquals(405, intento.getStatus());
    }

    @Test
    @Order(7)
    void consultarPreguntaPorClaveInexistente_debeRetornar404() {
    UUID idClaveInexistente = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
    UUID idPreguntaInexistente = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");

    Response consulta = targetDe("preguntas_por_clave/" + idClaveInexistente + "/" + idPreguntaInexistente)
        .request(MediaType.APPLICATION_JSON)
        .get();

    assertEquals(404, consulta.getStatus());
    assertTrue(consulta.getHeaders().containsKey(NOT_FOUND_ID));
    }

    @AfterAll
    void limpiarDatosCreados() {
    if (preguntaPorClaveCreada && idClaveCreada != null) {
        Response deletePpk = targetDe("preguntas_por_clave/" + idClaveCreada + "/" + ID_PREGUNTA_SEMILLA)
            .request(MediaType.APPLICATION_JSON)
            .delete();
        assertTrue(deletePpk.getStatus() == 204 || deletePpk.getStatus() == 404);
    }

    if (idClaveCreada != null) {
        Response deleteClave = targetDe("claves_examen/" + idClaveCreada)
            .request(MediaType.APPLICATION_JSON)
            .delete();
        assertTrue(deleteClave.getStatus() == 204 || deleteClave.getStatus() == 404);
    }
    }

    private UUID extraerUuidFinal(URI location) {
    String[] partes = location.getPath().split("/");
    return UUID.fromString(partes[partes.length - 1]);
    }
}