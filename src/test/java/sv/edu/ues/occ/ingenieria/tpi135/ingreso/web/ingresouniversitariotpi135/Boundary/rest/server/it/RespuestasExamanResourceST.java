package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.it;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ExamenesRealizado;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.OpcionesRespuesta;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.RespuestasExaman;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integracion REST para el recurso RespuestasExamanResource.
 *
 * Estas pruebas:
 * - Usan WAR real desplegada en Liberty (via BaseSistemaST / AbstractResourceIT).
 * - Consumen endpoints reales via HTTP.
 * - Validan el contrato REST (codigos HTTP, headers, cuerpo JSON) y la integracion
 *   con la base de datos inicializada por ingresoTPI135_init.sql.
 */
public class RespuestasExamanResourceST extends AbstractResourceIT {

    // UUIDs tomados del init.sql (mismos que en RespuestasExamanDAOIT)
    private static final UUID ID_EXAMEN_1    = UUID.fromString("0d000000-0000-0000-0000-000000000001");
    private static final UUID ID_PREGUNTA_1  = UUID.fromString("f1000000-0000-0000-0000-000000000001");
    private static final UUID ID_PREGUNTA_3  = UUID.fromString("f1000000-0000-0000-0000-000000000003");
    private static final UUID ID_OPCION_7    = UUID.fromString("0b000000-0000-0000-0000-000000000007");
    private static final UUID ID_RESPUESTA_1 = UUID.fromString("0e000000-0000-0000-0000-000000000001");

    /**
     * GET /respuestas_examen/{id} con un id existente debe devolver 200 y el cuerpo esperado.
     */
    @Test
    void findById_ConIdExistente_DebeRetornar200() {
        Response response = get("respuestas_examen/" + ID_RESPUESTA_1);

        assertEquals(200, response.getStatus());

        RespuestasExaman entidad = response.readEntity(RespuestasExaman.class);
        assertNotNull(entidad);
        assertEquals(ID_RESPUESTA_1, entidad.getId());
        assertNotNull(entidad.getIdExamen());
        assertEquals(ID_EXAMEN_1, entidad.getIdExamen().getId());
        assertNotNull(entidad.getIdPregunta());
        assertEquals(ID_PREGUNTA_1, entidad.getIdPregunta().getId());
    }

    /**
     * GET /respuestas_examen/{id} con un id inexistente debe devolver 404 y header Not-found-id.
     */
    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");

        Response response = get("respuestas_examen/" + idInexistente);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    /**
     * GET /respuestas_examen?examenId=... con un examenId valido debe devolver las respuestas del examen.
     */
    @Test
    void findRange_ConExamenIdValido_DebeRetornarLista() {
        Response response = get("respuestas_examen?examenId=" + ID_EXAMEN_1);

        assertEquals(200, response.getStatus());

        RespuestasExaman[] arreglo = response.readEntity(RespuestasExaman[].class);
        assertNotNull(arreglo);
        assertTrue(arreglo.length > 0);

        for (RespuestasExaman r : arreglo) {
            assertNotNull(r.getIdExamen());
            assertEquals(ID_EXAMEN_1, r.getIdExamen().getId());
        }
    }

    /**
     * GET /respuestas_examen?examenId=... con un examenId con formato invalido debe devolver 422 y Missing-parameter.
     */
    @Test
    void findRange_ConExamenIdInvalido_DebeRetornar422() {
        Response response = get("respuestas_examen?examenId=no-es-uuid");

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }

    /**
     * GET /respuestas_examen sin filtros debe usar el comportamiento paginado heredado
     * de AbstractResource, devolviendo al menos las respuestas iniciales y el header Total-records.
     */
    @Test
    void findRange_SinFiltros_DebeRetornarListaYPaginacion() {
        Response response = get("respuestas_examen?first=0&max=10");

        assertEquals(200, response.getStatus());

        RespuestasExaman[] arreglo = response.readEntity(RespuestasExaman[].class);
        assertNotNull(arreglo);
        assertTrue(arreglo.length > 0);

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader);
        int total = Integer.parseInt(totalHeader);
        assertTrue(total >= 4);
    }

    /**
     * POST /respuestas_examen con una entidad valida debe devolver 201 y permitir consultar luego el recurso creado.
     */
    @Test
    void create_ConEntidadValida_DebeRetornar201_YPermitirConsultar() {
        RespuestasExaman nueva = new RespuestasExaman();

        ExamenesRealizado examen = new ExamenesRealizado();
        examen.setId(ID_EXAMEN_1);
        nueva.setIdExamen(examen);

        BancoPregunta pregunta = new BancoPregunta();
        pregunta.setId(ID_PREGUNTA_3);
        nueva.setIdPregunta(pregunta);

        OpcionesRespuesta opcion = new OpcionesRespuesta();
        opcion.setId(ID_OPCION_7);
        nueva.setIdOpcionSeleccionada(opcion);

        Response responseCreacion = post("respuestas_examen", nueva);

        assertEquals(201, responseCreacion.getStatus());
        String location = responseCreacion.getHeaderString("Location");
        assertNotNull(location);

        String idString = location.substring(location.lastIndexOf('/') + 1);
        UUID idCreado = UUID.fromString(idString);

        Response responseConsulta = get("respuestas_examen/" + idCreado);
        assertEquals(200, responseConsulta.getStatus());

        RespuestasExaman creada = responseConsulta.readEntity(RespuestasExaman.class);
        assertNotNull(creada);
        assertEquals(idCreado, creada.getId());
        assertNotNull(creada.getIdExamen());
        assertEquals(ID_EXAMEN_1, creada.getIdExamen().getId());
        assertNotNull(creada.getIdPregunta());
        assertEquals(ID_PREGUNTA_3, creada.getIdPregunta().getId());
        assertNotNull(creada.getIdOpcionSeleccionada());
    }

    /**
     * POST /respuestas_examen con una entidad invalida (sin examen) debe devolver 422.
     */
    @Test
    void create_ConEntidadInvalida_SinExamen_DebeRetornar422() {
        RespuestasExaman nueva = new RespuestasExaman();

        BancoPregunta pregunta = new BancoPregunta();
        pregunta.setId(ID_PREGUNTA_3);
        nueva.setIdPregunta(pregunta);

        OpcionesRespuesta opcion = new OpcionesRespuesta();
        opcion.setId(ID_OPCION_7);
        nueva.setIdOpcionSeleccionada(opcion);

        Response response = post("respuestas_examen", nueva);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }

    /**
     * POST /respuestas_examen con una entidad invalida (sin pregunta) debe devolver 422.
     */
    @Test
    void create_ConEntidadInvalida_SinPregunta_DebeRetornar422() {
        RespuestasExaman nueva = new RespuestasExaman();

        ExamenesRealizado examen = new ExamenesRealizado();
        examen.setId(ID_EXAMEN_1);
        nueva.setIdExamen(examen);

        OpcionesRespuesta opcion = new OpcionesRespuesta();
        opcion.setId(ID_OPCION_7);
        nueva.setIdOpcionSeleccionada(opcion);

        Response response = post("respuestas_examen", nueva);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }

    /**
     * POST /respuestas_examen con una entidad invalida (sin opcion seleccionada) debe devolver 422.
     */
    @Test
    void create_ConEntidadInvalida_SinOpcion_DebeRetornar422() {
        RespuestasExaman nueva = new RespuestasExaman();

        ExamenesRealizado examen = new ExamenesRealizado();
        examen.setId(ID_EXAMEN_1);
        nueva.setIdExamen(examen);

        BancoPregunta pregunta = new BancoPregunta();
        pregunta.setId(ID_PREGUNTA_3);
        nueva.setIdPregunta(pregunta);

        Response response = post("respuestas_examen", nueva);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }
}
