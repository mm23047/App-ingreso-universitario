package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.it;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.ExamenRealizadoResource;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ST para ExamenRealizadoResource — flujo resultado.html.
 *
 * Cubre los endpoints del flujo "consultar estado del aspirante":
 *   GET  /examen_realizado/aspirante/{idAspirante}  — CASO A (búsqueda por UUID)
 *   GET  /examen_realizado/buscar?dui=xxx           — CASO B (búsqueda por DUI)
 *   GET  /examen_realizado/buscar?correo=xxx        — CASO B (búsqueda por correo)
 *   GET  /examen_realizado/{idExamen}               — consulta examen individual
 *   GET  /examen_realizado/{idExamen}/preguntas     — preguntas del examen
 *   POST /examen_realizado                          — iniciar examen
 *
 * Datos semilla en init.sql:
 *   Aspirante e1000000 (Prueba Usuario, DUI 99999999-9, correo prueba.test@example.com)
 *     → examen ffffeee1  puntaje 70.00  etapa aaaa  proceso EN_PROCESO
 *   Aspirante e2222222 (María Fernanda, DUI 02234567-8)
 *     → examen 0d000000  puntaje 65.00  etapa aaaa
 *   Aspirante e1111111 (Carlos Alberto, DUI 01234567-8)
 *     → inscripcion 09000000-0002  SIN examen  proceso ADMITIDO carrera ISI
 *   Inscripcion ffff1002 (José Miguel, e3333333)
 *     → SIN examen — disponible para prueba de POST
 */
public class ExamenRealizadoResourceST extends AbstractResourceST {

    // ── Aspirantes semilla ────────────────────────────────────────────────────
    private static final UUID   ID_ASPIRANTE_CON_EXAMEN = UUID.fromString("e1000000-0000-0000-0000-000000000001");
    private static final UUID   ID_ASPIRANTE_SIN_EXAMEN = UUID.fromString("e1111111-1111-1111-1111-111111111111");
    private static final String DUI_CON_EXAMEN          = "99999999-9";
    private static final String DUI_SIN_EXAMEN          = "01234567-8";
    private static final String CORREO_CON_EXAMEN       = "prueba.test@example.com";

    // ── Exámenes semilla ──────────────────────────────────────────────────────
    private static final UUID ID_EXAMEN_1 = UUID.fromString("ffffeee1-1111-1111-1111-111111111111");

    // ── IDs de apoyo ──────────────────────────────────────────────────────────
    private static final UUID ID_INEXISTENTE            = UUID.fromString("ffffffff-0000-0000-0000-000000000000");
    private static final UUID ID_INSCRIPCION_SIN_EXAMEN = UUID.fromString("ffff1002-1002-1002-1002-000000001002");
    private static final UUID ID_ETAPA_A                = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID ID_ETAPA_B                = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID ID_INSCRIPCION_CON_EXAMEN = UUID.fromString("09000000-0000-0000-0000-000000000001");

    // ─────────────────────────────────────────────────────────────────────────
    // GET /examen_realizado/aspirante/{idAspirante}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void getExamenesPorAspirante_ConExamenesRegistrados_DebeRetornar200ConLista() {
        Response response = get("examen_realizado/aspirante/" + ID_ASPIRANTE_CON_EXAMEN);

        assertEquals(200, response.getStatus());

        Map[] lista = response.readEntity(Map[].class);
        assertNotNull(lista);
        assertTrue(lista.length >= 1, "Prueba Usuario debe tener al menos 1 examen");
        assertNotNull(lista[0].get("idExamenRealizado"), "Debe incluir idExamenRealizado");
        assertNotNull(lista[0].get("puntajeFinal"),      "Debe incluir puntajeFinal");
    }

    @Test
    void getExamenesPorAspirante_ConProcesoAdmision_DebeIncluirEstadoAdmision() {
        Response response = get("examen_realizado/aspirante/" + ID_ASPIRANTE_CON_EXAMEN);

        assertEquals(200, response.getStatus());

        Map[] lista = response.readEntity(Map[].class);
        assertTrue(lista.length >= 1);

        // Prueba Usuario tiene ProcesoAdmisionAspirante con estado 'EN_PROCESO'
        Map dto = lista[0];
        assertNotNull(dto.get("estadoAdmision"), "Debe incluir estadoAdmision del proceso de admisión");
        assertEquals("EN_PROCESO", dto.get("estadoAdmision"));
    }

    @Test
    void getExamenesPorAspirante_ConHeaderTotalRecords_DebeIncluirConteo() {
        Response response = get("examen_realizado/aspirante/" + ID_ASPIRANTE_CON_EXAMEN);

        assertEquals(200, response.getStatus());

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader, "Debe incluir el header Total-records");
        assertTrue(Integer.parseInt(totalHeader) >= 1);
    }

    @Test
    void getExamenesPorAspirante_SinExamenesRegistrados_DebeRetornar200ConListaVacia() {
        Response response = get("examen_realizado/aspirante/" + ID_ASPIRANTE_SIN_EXAMEN);

        assertEquals(200, response.getStatus());

        Map[] lista = response.readEntity(Map[].class);
        assertNotNull(lista);
        assertEquals(0, lista.length, "Carlos Alberto no tiene exámenes realizados");
    }

    @Test
    void getExamenesPorAspirante_ConUUIDInexistente_DebeRetornar200ConListaVacia() {
        Response response = get("examen_realizado/aspirante/" + ID_INEXISTENTE);

        assertEquals(200, response.getStatus());

        Map[] lista = response.readEntity(Map[].class);
        assertNotNull(lista);
        assertEquals(0, lista.length, "UUID inexistente no debe tener exámenes");
    }

    @Test
    void getExamenesPorAspirante_ConFormatoUUIDInvalido_DebeRetornar400() {
        Response response = get("examen_realizado/aspirante/no-es-uuid");

        assertEquals(400, response.getStatus());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /examen_realizado/buscar?dui=xxx
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void buscarPorDui_ConExamenRegistrado_DebeRetornar200ConResultados() {
        Response response = get("examen_realizado/buscar?dui=" + DUI_CON_EXAMEN);

        assertEquals(200, response.getStatus());

        Map[] lista = response.readEntity(Map[].class);
        assertNotNull(lista);
        assertTrue(lista.length >= 1, "DUI 99999999-9 debe tener al menos 1 examen");
        assertNotNull(lista[0].get("idExamenRealizado"));
        assertNotNull(lista[0].get("puntajeFinal"));
    }

    @Test
    void buscarPorDui_ConExamenRegistrado_DebeIncluirEstadoAdmision() {
        Response response = get("examen_realizado/buscar?dui=" + DUI_CON_EXAMEN);

        assertEquals(200, response.getStatus());

        Map[] lista = response.readEntity(Map[].class);
        assertTrue(lista.length >= 1);

        // Prueba Usuario tiene proceso EN_PROCESO
        assertNotNull(lista[0].get("estadoAdmision"),
                "El DTO enriquecido debe incluir estadoAdmision");
        assertEquals("EN_PROCESO", lista[0].get("estadoAdmision"));
    }

    @Test
    void buscarPorDui_SinExamenesRegistrados_DebeRetornar200ConListaVacia() {
        Response response = get("examen_realizado/buscar?dui=" + DUI_SIN_EXAMEN);

        assertEquals(200, response.getStatus());

        Map[] lista = response.readEntity(Map[].class);
        assertNotNull(lista);
        assertEquals(0, lista.length, "Carlos Alberto (01234567-8) no tiene exámenes");
    }

    @Test
    void buscarPorDui_ConDuiInexistente_DebeRetornar200ConListaVacia() {
        Response response = get("examen_realizado/buscar?dui=00000000-9");

        assertEquals(200, response.getStatus());

        Map[] lista = response.readEntity(Map[].class);
        assertNotNull(lista);
        assertEquals(0, lista.length, "DUI inexistente no debe retornar resultados");
    }

    @Test
    void buscarPorDui_ConHeaderTotalRecords_DebeIncluirConteo() {
        Response response = get("examen_realizado/buscar?dui=" + DUI_CON_EXAMEN);

        assertEquals(200, response.getStatus());

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader, "Debe incluir el header Total-records");
        assertTrue(Integer.parseInt(totalHeader) >= 1);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /examen_realizado/buscar?correo=xxx
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void buscarPorCorreo_ConExamenRegistrado_DebeRetornar200ConResultados() {
        Response response = get("examen_realizado/buscar?correo=" + CORREO_CON_EXAMEN);

        assertEquals(200, response.getStatus());

        Map[] lista = response.readEntity(Map[].class);
        assertNotNull(lista);
        assertTrue(lista.length >= 1, "prueba.test@example.com debe tener al menos 1 examen");
        assertNotNull(lista[0].get("idExamenRealizado"));
    }

    @Test
    void buscarPorCorreo_ConCorreoInexistente_DebeRetornar200ConListaVacia() {
        Response response = get("examen_realizado/buscar?correo=nadie@nonexistent.sv");

        assertEquals(200, response.getStatus());

        Map[] lista = response.readEntity(Map[].class);
        assertNotNull(lista);
        assertEquals(0, lista.length, "Correo inexistente no debe retornar resultados");
    }

    @Test
    void buscarSinParametros_DebeRetornar400() {
        Response response = get("examen_realizado/buscar");

        assertEquals(400, response.getStatus());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /examen_realizado/{idExamen}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void getExamen_ConIdExistente_DebeRetornar200ConEntidad() {
        Response response = get("examen_realizado/" + ID_EXAMEN_1);

        assertEquals(200, response.getStatus());

        Map entidad = response.readEntity(Map.class);
        assertNotNull(entidad);
        assertNotNull(entidad.get("idExamenRealizado"), "Debe incluir idExamenRealizado");
    }

    @Test
    void getExamen_ConIdInexistente_DebeRetornar404() {
        Response response = get("examen_realizado/" + ID_INEXISTENTE);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"),
                "Debe incluir el header Not-found-id");
    }

    @Test
    void getExamen_ConFormatoUUIDInvalido_DebeRetornar400() {
        Response response = get("examen_realizado/no-es-uuid");

        assertEquals(400, response.getStatus());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /examen_realizado/{idExamen}/preguntas
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void getPreguntasDelExamen_ConIdExistente_DebeRetornar200() {
        Response response = get("examen_realizado/" + ID_EXAMEN_1 + "/preguntas");

        assertEquals(200, response.getStatus());

        // La semilla puede o no tener preguntas; solo verificamos que el endpoint responde
        Map[] preguntas = response.readEntity(Map[].class);
        assertNotNull(preguntas);
    }

    @Test
    void getPreguntasDelExamen_ConIdInexistente_DebeRetornar404() {
        Response response = get("examen_realizado/" + ID_INEXISTENTE + "/preguntas");

        assertEquals(404, response.getStatus());
    }

    @Test
    void getPreguntasDelExamen_ConFormatoUUIDInvalido_DebeRetornar400() {
        Response response = get("examen_realizado/no-es-uuid/preguntas");

        assertEquals(400, response.getStatus());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /examen_realizado — iniciar examen
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void iniciarExamen_ConBodyNulo_DebeRetornar400() {
        Response response = post("examen_realizado", null);

        assertEquals(400, response.getStatus());
    }

    @Test
    void iniciarExamen_SinIdInscripcion_DebeRetornar400() {
        ExamenRealizadoResource.ExamenInicioDTO dto = new ExamenRealizadoResource.ExamenInicioDTO();
        dto.setIdEtapa(ID_ETAPA_B);
        // idInscripcion es null

        Response response = post("examen_realizado", dto);

        assertEquals(400, response.getStatus());
    }

    @Test
    void iniciarExamen_SinIdEtapa_DebeRetornar400() {
        ExamenRealizadoResource.ExamenInicioDTO dto = new ExamenRealizadoResource.ExamenInicioDTO();
        dto.setIdInscripcion(ID_INSCRIPCION_SIN_EXAMEN);
        // idEtapa es null

        Response response = post("examen_realizado", dto);

        assertEquals(400, response.getStatus());
    }

    @Test
    void iniciarExamen_ConInscripcionYEtapaYaUsados_DebeRetornar409Conflicto() {
        // La inscripcion 09000000-0001 + etapa aaaa ya tiene el examen ffffeee1 en la semilla
        ExamenRealizadoResource.ExamenInicioDTO dto = new ExamenRealizadoResource.ExamenInicioDTO();
        dto.setIdInscripcion(ID_INSCRIPCION_CON_EXAMEN);
        dto.setIdEtapa(ID_ETAPA_A);

        Response response = post("examen_realizado", dto);

        assertEquals(409, response.getStatus(),
                "La combinación inscripcion+etapa ya existe en la semilla — debe retornar 409 CONFLICT");
    }

    @Test
    void iniciarExamen_ConInscripcionSinExamenPrevio_DebeRetornar201OConflicto() {
        // inscripcion ffff1002 (José Miguel) + etapa B no tiene examen en la semilla
        ExamenRealizadoResource.ExamenInicioDTO dto = new ExamenRealizadoResource.ExamenInicioDTO();
        dto.setIdInscripcion(ID_INSCRIPCION_SIN_EXAMEN);
        dto.setIdEtapa(ID_ETAPA_B);

        Response response = post("examen_realizado", dto);

        // 201 en primera ejecución del contenedor; 409 si el test ya corrió antes en la misma instancia
        assertTrue(response.getStatus() == 201 || response.getStatus() == 409,
                "Debe retornar 201 (primer arranque) o 409 (ejecución previa en el mismo contenedor)");

        if (response.getStatus() == 201) {
            String location = response.getHeaderString("Location");
            assertNotNull(location, "Debe retornar Location apuntando al nuevo examen");

            // Verificar que el examen recién creado es consultable
            String idNuevo = location.substring(location.lastIndexOf('/') + 1);
            Response getResp = get("examen_realizado/" + idNuevo);
            assertEquals(200, getResp.getStatus(),
                    "El examen recién creado debe ser consultable por su ID");
        }
    }
}
