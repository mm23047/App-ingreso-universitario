package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.sistema;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import java.net.URI;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.RestHeaders.SERVER_EXCEPTION;
import static sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.RestHeaders.TOTAL_RECORDS;

/**
 * Esqueleto ST para el integrante 2.
 * Feature: flujo logistico de aplicacion de prueba.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FlujoLogisticaST extends BaseSistemaST {

    private static final UUID ID_PRUEBA_SEMILLA = UUID.fromString("d1000000-0000-0000-0000-000000000001");
    private static final UUID ID_INSCRIPCION_SEMILLA = UUID.fromString("09000000-0000-0000-0000-000000000001");

    @Test
    @Order(1)
    void listarTurnosPaginado_debeRetornar200YTotalRecords() {
        Response respuesta = targetDe("turnos_examen")
                .queryParam("first", 0)
                .queryParam("max", 50)
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertEquals(200, respuesta.getStatus());
        assertTrue(respuesta.getHeaders().containsKey(TOTAL_RECORDS));
        assertTrue(Integer.parseInt(respuesta.getHeaderString(TOTAL_RECORDS)) >= 2);

        String cuerpo = respuesta.readEntity(String.class);
        assertNotNull(cuerpo);
        assertFalse("[]".equals(cuerpo.trim()));
        assertTrue(cuerpo.contains("Turno Mañana") || cuerpo.contains("Turno Tarde"));
    }

    @Test
    @Order(2)
    void crearTurnoValido_debeRetornar201YSerConsultable() {
        UUID idTurnoCreado = null;
        String nombreTurno = "Turno ST " + UUID.randomUUID().toString().substring(0, 8);
        String payload = """
                {
                  \"idPrueba\": { \"id\": \"%s\" },
                  \"nombreTurno\": \"%s\",
                  \"fecha\": \"2026-05-20\",
                  \"horaInicio\": \"08:00:00\",
                  \"horaFin\": \"11:00:00\"
                }
                """.formatted(ID_PRUEBA_SEMILLA, nombreTurno);

        try {
            Response crear = postJson("turnos_examen", payload);

            assertEquals(201, crear.getStatus());
            assertNotNull(crear.getLocation());

            idTurnoCreado = extraerUuidDesdeLocation(crear.getLocation());

            Response consultar = get("turnos_examen/" + idTurnoCreado);
            assertEquals(200, consultar.getStatus());

            String cuerpo = consultar.readEntity(String.class);
            assertTrue(cuerpo.contains(idTurnoCreado.toString()));
            assertTrue(cuerpo.contains(nombreTurno));
            assertTrue(cuerpo.contains(ID_PRUEBA_SEMILLA.toString()));
        } finally {
            eliminarSiExiste("turnos_examen", idTurnoCreado);
        }
    }

    @Test
    @Order(3)
    void crearAulaValidaParaTurno_debeRetornar201YSerConsultable() {
        UUID idTurnoCreado = null;
        UUID idAulaCreada = null;
        String nombreTurno = "Turno Aula ST " + UUID.randomUUID().toString().substring(0, 8);
        String idAulaApi = "AULA-ST-" + UUID.randomUUID().toString().substring(0, 8);

        try {
            idTurnoCreado = crearTurno(nombreTurno, "2026-05-21", "09:00:00", "12:00:00");

            String payloadAula = """
                    {
                      \"idTurno\": { \"id\": \"%s\" },
                      \"idAulaApi\": \"%s\",
                      \"capacidad\": 30,
                      \"cuposOcupados\": 0,
                      \"accesibleSillaRuedas\": true
                    }
                    """.formatted(idTurnoCreado, idAulaApi);

            Response crearAula = postJson("aulas_examen", payloadAula);

            assertEquals(201, crearAula.getStatus());
            assertNotNull(crearAula.getLocation());

            idAulaCreada = extraerUuidDesdeLocation(crearAula.getLocation());

            Response consultar = get("aulas_examen/" + idAulaCreada);
            assertEquals(200, consultar.getStatus());

            String cuerpo = consultar.readEntity(String.class);
            assertTrue(cuerpo.contains(idAulaCreada.toString()));
            assertTrue(cuerpo.contains(idAulaApi));
            assertTrue(cuerpo.contains(idTurnoCreado.toString()));
        } finally {
            eliminarSiExiste("aulas_examen", idAulaCreada);
            eliminarSiExiste("turnos_examen", idTurnoCreado);
        }
    }

    @Test
    @Order(4)
    void asignarPupitreAInscripcionValida_debeRetornar201YSerConsultable() {
        UUID idTurnoCreado = null;
        UUID idAulaCreada = null;
        UUID idAsignacionCreada = null;
        String pupitre = "ST-" + UUID.randomUUID().toString().substring(0, 4);

        try {
            idTurnoCreado = crearTurno("Turno Asignacion ST " + UUID.randomUUID().toString().substring(0, 8),
                    "2026-05-22", "13:00:00", "16:00:00");
            idAulaCreada = crearAula(idTurnoCreado, "AULA-ASIG-" + UUID.randomUUID().toString().substring(0, 8), 25, false);

            String payloadAsignacion = """
                    {
                      \"idInscripcion\": { \"id\": \"%s\" },
                      \"idAula\": { \"id\": \"%s\" },
                      \"pupitre\": \"%s\"
                    }
                    """.formatted(ID_INSCRIPCION_SEMILLA, idAulaCreada, pupitre);

            Response crearAsignacion = postJson("asignaciones_aula_pupitre", payloadAsignacion);

            assertEquals(201, crearAsignacion.getStatus());
            assertNotNull(crearAsignacion.getLocation());

            idAsignacionCreada = extraerUuidDesdeLocation(crearAsignacion.getLocation());

            Response consultar = get("asignaciones_aula_pupitre/" + idAsignacionCreada);
            assertEquals(200, consultar.getStatus());

            String cuerpo = consultar.readEntity(String.class);
            assertTrue(cuerpo.contains(idAsignacionCreada.toString()));
            assertTrue(cuerpo.contains(ID_INSCRIPCION_SEMILLA.toString()));
            assertTrue(cuerpo.contains(idAulaCreada.toString()));
            assertTrue(cuerpo.contains(pupitre));
        } finally {
            eliminarSiExiste("asignaciones_aula_pupitre", idAsignacionCreada);
            eliminarSiExiste("aulas_examen", idAulaCreada);
            eliminarSiExiste("turnos_examen", idTurnoCreado);
        }
    }

    @Test
    @Order(5)
    void crearAulaConTurnoInexistente_debeRetornarError() {
        String payload = """
                {
                  \"idTurno\": { \"id\": \"%s\" },
                  \"idAulaApi\": \"AULA-FK-INVALIDA\",
                  \"capacidad\": 20,
                  \"cuposOcupados\": 0,
                  \"accesibleSillaRuedas\": false
                }
                """.formatted(UUID.randomUUID());

        Response respuesta = postJson("aulas_examen", payload);

        assertEquals(500, respuesta.getStatus());
        assertTrue(respuesta.getHeaders().containsKey(SERVER_EXCEPTION));
    }

    @Test
    @Order(6)
    void asignarPupitreConInscripcionInexistente_debeRetornarError() {
        UUID idTurnoCreado = null;
        UUID idAulaCreada = null;

        try {
            idTurnoCreado = crearTurno("Turno Negativo ST " + UUID.randomUUID().toString().substring(0, 8),
                    "2026-05-23", "07:00:00", "10:00:00");
            idAulaCreada = crearAula(idTurnoCreado, "AULA-NEG-" + UUID.randomUUID().toString().substring(0, 8), 20, true);

            String payload = """
                    {
                      \"idInscripcion\": { \"id\": \"%s\" },
                      \"idAula\": { \"id\": \"%s\" },
                      \"pupitre\": \"FK-01\"
                    }
                    """.formatted(UUID.randomUUID(), idAulaCreada);

            Response respuesta = postJson("asignaciones_aula_pupitre", payload);

            assertEquals(500, respuesta.getStatus());
            assertTrue(respuesta.getHeaders().containsKey(SERVER_EXCEPTION));
        } finally {
            eliminarSiExiste("aulas_examen", idAulaCreada);
            eliminarSiExiste("turnos_examen", idTurnoCreado);
        }
    }

    private UUID crearTurno(String nombreTurno, String fecha, String horaInicio, String horaFin) {
        String payload = """
                {
                  \"idPrueba\": { \"id\": \"%s\" },
                  \"nombreTurno\": \"%s\",
                  \"fecha\": \"%s\",
                  \"horaInicio\": \"%s\",
                  \"horaFin\": \"%s\"
                }
                """.formatted(ID_PRUEBA_SEMILLA, nombreTurno, fecha, horaInicio, horaFin);

        Response respuesta = postJson("turnos_examen", payload);
        assertEquals(201, respuesta.getStatus());
        assertNotNull(respuesta.getLocation());
        return extraerUuidDesdeLocation(respuesta.getLocation());
    }

    private UUID crearAula(UUID idTurno, String idAulaApi, int capacidad, boolean accesibleSillaRuedas) {
        String payload = """
                {
                  \"idTurno\": { \"id\": \"%s\" },
                  \"idAulaApi\": \"%s\",
                  \"capacidad\": %d,
                  \"cuposOcupados\": 0,
                  \"accesibleSillaRuedas\": %s
                }
                """.formatted(idTurno, idAulaApi, capacidad, accesibleSillaRuedas);

        Response respuesta = postJson("aulas_examen", payload);
        assertEquals(201, respuesta.getStatus());
        assertNotNull(respuesta.getLocation());
        return extraerUuidDesdeLocation(respuesta.getLocation());
    }

    private Response postJson(String recurso, String payloadJson) {
        return targetDe(recurso)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(payloadJson, MediaType.APPLICATION_JSON));
    }

    private UUID extraerUuidDesdeLocation(URI location) {
        String path = location.getPath();
        return UUID.fromString(path.substring(path.lastIndexOf('/') + 1));
    }

    private void eliminarSiExiste(String recurso, UUID id) {
        if (id == null) {
            return;
        }
        try {
            delete(recurso + "/" + id).close();
        } catch (Exception ignored) {
            // La limpieza es de mejor esfuerzo para no ocultar el error principal del test.
        }
    }
}
