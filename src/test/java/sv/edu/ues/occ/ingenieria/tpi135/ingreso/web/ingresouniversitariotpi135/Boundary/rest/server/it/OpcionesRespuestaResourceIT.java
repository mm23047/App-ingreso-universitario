package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.it;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.OpcionesRespuesta;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración REST para el recurso OpcionesRespuestaResource.
 * 
 * Valida el contrato HTTP de los endpoints de opciones de respuesta,
 * incluyendo filtros por pregunta, validaciones de FK, y persistencia.
 */
public class OpcionesRespuestaResourceIT extends AbstractResourceIT {

    // UUIDs de opciones del init.sql
    private static final UUID ID_OPCION_1 = UUID.fromString("0b000000-0000-0000-0000-000000000001");
    private static final UUID ID_OPCION_2 = UUID.fromString("0b000000-0000-0000-0000-000000000002");
    private static final UUID ID_OPCION_3 = UUID.fromString("0b000000-0000-0000-0000-000000000003");

    // UUIDs de preguntas del init.sql
    private static final UUID ID_PREGUNTA_1 = UUID.fromString("f1000000-0000-0000-0000-000000000001");
    private static final UUID ID_PREGUNTA_2 = UUID.fromString("f1000000-0000-0000-0000-000000000002");
    private static final UUID ID_PREGUNTA_3 = UUID.fromString("f1000000-0000-0000-0000-000000000003");

    /**
     * GET /resources/v1/opciones_respuesta debe retornar al menos 10 opciones iniciales.
     */
    @Test
    void findRange_ConDatosIniciales_DebeRetornarLista() {
        Response response = get("opciones_respuesta");

        assertEquals(200, response.getStatus());

        OpcionesRespuesta[] arreglo = response.readEntity(OpcionesRespuesta[].class);
        assertNotNull(arreglo);
        assertTrue(arreglo.length >= 10, "Debe haber al menos 10 opciones iniciales");

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader);
        int total = Integer.parseInt(totalHeader);
        assertTrue(total >= 10);

        // Verificar que está Opción 1 con datos válidos
        boolean encontroOpcion1 = false;
        for (OpcionesRespuesta opcion : arreglo) {
            if (ID_OPCION_1.equals(opcion.getId())) {
                encontroOpcion1 = true;
                assertEquals("3", opcion.getTextoOpcion());
                assertFalse(opcion.getEsCorrecta());
                assertNotNull(opcion.getIdPregunta());
                assertEquals(ID_PREGUNTA_1, opcion.getIdPregunta().getId());
                break;
            }
        }
        assertTrue(encontroOpcion1, "Debe encontrar opción 1");
    }

    /**
     * GET /resources/v1/opciones_respuesta?first=0&max=3 debe retornar máximo 3 registros.
     */
    @Test
    void findRange_ConPaginacion_DebeRetornarDatosLimitados() {
        Response response = get("opciones_respuesta?first=0&max=3");

        assertEquals(200, response.getStatus());

        OpcionesRespuesta[] arreglo = response.readEntity(OpcionesRespuesta[].class);
        assertNotNull(arreglo);
        assertEquals(3, arreglo.length, "Debe retornar exactamente 3 registros");

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader);
        int total = Integer.parseInt(totalHeader);
        assertTrue(total >= 10);
    }

    /**
     * GET /resources/v1/opciones_respuesta/{id} con un id existente debe retornar 200.
     */
    @Test
    void findById_ConIdExistente_DebeRetornar200() {
        Response response = get("opciones_respuesta/" + ID_OPCION_1);

        assertEquals(200, response.getStatus());

        OpcionesRespuesta entidad = response.readEntity(OpcionesRespuesta.class);
        assertNotNull(entidad);
        assertEquals(ID_OPCION_1, entidad.getId());
        assertEquals("3", entidad.getTextoOpcion());
        assertFalse(entidad.getEsCorrecta());
        assertNotNull(entidad.getIdPregunta());
    }

    /**
     * GET /resources/v1/opciones_respuesta/{id} con un id inexistente debe retornar 404.
     */
    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");

        Response response = get("opciones_respuesta/" + idInexistente);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    /**
     * GET /resources/v1/opciones_respuesta/no-es-uuid debe retornar 404.
     */
    @Test
    void findById_ConFormatoIdInvalido_DebeRetornar404() {
        Response response = get("opciones_respuesta/no-es-uuid");

        assertEquals(404, response.getStatus());
    }

    /**
     * GET /resources/v1/opciones_respuesta?idPregunta={id} debe retornar solo opciones de esa pregunta.
     */
    @Test
    void findRange_ConFiltroPregunta_DebeRetornarDelaPregunta() {
        Response response = get("opciones_respuesta?idPregunta=" + ID_PREGUNTA_1);

        assertEquals(200, response.getStatus());

        OpcionesRespuesta[] arreglo = response.readEntity(OpcionesRespuesta[].class);
        assertNotNull(arreglo);
        assertTrue(arreglo.length >= 3, "Pregunta 1 debe tener al menos 3 opciones");

        // Verificar que al menos una opción pertenece a Pregunta 1
        boolean encontroDelaPregunta = false;
        for (OpcionesRespuesta opcion : arreglo) {
            assertNotNull(opcion.getIdPregunta());
            if (ID_PREGUNTA_1.equals(opcion.getIdPregunta().getId())) {
                encontroDelaPregunta = true;
                break;
            }
        }
        assertTrue(encontroDelaPregunta);
    }

    /**
     * POST /resources/v1/opciones_respuesta con una entidad válida debe retornar 201.
     */
    @Test
    void create_ConEntidadValida_DebeRetornar201_YPermitirConsultar() {
        OpcionesRespuesta nueva = crearOpcion(ID_PREGUNTA_2, "nueva opción", true);

        Response responseCreacion = post("opciones_respuesta", nueva);

        assertEquals(201, responseCreacion.getStatus());
        String location = responseCreacion.getHeaderString("Location");
        assertNotNull(location);

        String idString = location.substring(location.lastIndexOf('/') + 1);
        UUID idCreado = UUID.fromString(idString);

        Response responseConsulta = get("opciones_respuesta/" + idCreado);
        assertEquals(200, responseConsulta.getStatus());

        OpcionesRespuesta creado = responseConsulta.readEntity(OpcionesRespuesta.class);
        assertNotNull(creado);
        assertEquals(idCreado, creado.getId());
        assertEquals("nueva opción", creado.getTextoOpcion());
        assertTrue(creado.getEsCorrecta());
        assertEquals(ID_PREGUNTA_2, creado.getIdPregunta().getId());
    }

    /**
     * POST /resources/v1/opciones_respuesta con una entidad inválida (sin pregunta) debe retornar 422.
     */
    @Test
    void create_ConEntidadInvalida_SinPregunta_DebeRetornar422() {
        OpcionesRespuesta nueva = new OpcionesRespuesta();
        nueva.setTextoOpcion("Opción sin pregunta");
        nueva.setEsCorrecta(false);
        // Falta: idPregunta

        Response response = post("opciones_respuesta", nueva);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }

    /**
     * POST /resources/v1/opciones_respuesta con una entidad inválida (sin texto) debe retornar 422.
     */
    @Test
    void create_ConEntidadInvalida_SinTexto_DebeRetornar422() {
        OpcionesRespuesta nueva = new OpcionesRespuesta();
        BancoPregunta pregunta = new BancoPregunta();
        pregunta.setId(ID_PREGUNTA_1);
        nueva.setIdPregunta(pregunta);
        nueva.setEsCorrecta(true);
        // Falta: textoOpcion

        Response response = post("opciones_respuesta", nueva);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }

    /**
     * PUT /resources/v1/opciones_respuesta/{id} con datos válidos debe retornar 200.
     */
    @Test
    void update_ConEntidadValida_DebeRetornar200() {
        UUID idCreado = crearOpcionReal(ID_PREGUNTA_3, "Opción original", false);

        OpcionesRespuesta actualizada = crearOpcion(ID_PREGUNTA_3, "Opción actualizada", true);

        Response responsePut = put("opciones_respuesta/" + idCreado, actualizada);

        assertEquals(200, responsePut.getStatus());

        OpcionesRespuesta actualizado = responsePut.readEntity(OpcionesRespuesta.class);
        assertNotNull(actualizado);
        assertEquals(idCreado, actualizado.getId());
        assertEquals("Opción actualizada", actualizado.getTextoOpcion());
        assertTrue(actualizado.getEsCorrecta());

        // Verificar persistencia
        Response responseConsulta = get("opciones_respuesta/" + idCreado);
        assertEquals(200, responseConsulta.getStatus());

        OpcionesRespuesta consultado = responseConsulta.readEntity(OpcionesRespuesta.class);
        assertEquals("Opción actualizada", consultado.getTextoOpcion());
        assertTrue(consultado.getEsCorrecta());
    }

    /**
     * PUT /resources/v1/opciones_respuesta/{id} con un id inexistente debe retornar 404.
     */
    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");
        OpcionesRespuesta actualizada = crearOpcion(ID_PREGUNTA_1, "No importa", false);

        Response response = put("opciones_respuesta/" + idInexistente, actualizada);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    /**
     * DELETE /resources/v1/opciones_respuesta/{id} debe retornar 204 y posteriores GETs deben retornar 404.
     */
    @Test
    void delete_ConIdExistente_DebeRetornar204_YNoEncontrarDespues() {
        UUID idCreado = crearOpcionReal(ID_PREGUNTA_1, "Opción a eliminar", false);

        Response responseAntesEliminar = get("opciones_respuesta/" + idCreado);
        assertEquals(200, responseAntesEliminar.getStatus());

        Response responseDelete = delete("opciones_respuesta/" + idCreado);
        assertEquals(204, responseDelete.getStatus());

        Response responseDespuesEliminar = get("opciones_respuesta/" + idCreado);
        assertEquals(404, responseDespuesEliminar.getStatus());
        assertNotNull(responseDespuesEliminar.getHeaderString("Not-found-id"));
    }

    /**
     * DELETE /resources/v1/opciones_respuesta/{id} con un id inexistente debe retornar 404.
     */
    @Test
    void delete_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");

        Response response = delete("opciones_respuesta/" + idInexistente);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    // ========== HELPERS ==========

    /**
     * Construye una entidad OpcionesRespuesta válida con los parámetros dados.
     * No ejecuta el POST, solo prepara el payload.
     */
    private OpcionesRespuesta crearOpcion(UUID idPregunta, String textoOpcion, Boolean esCorrecta) {
        OpcionesRespuesta opcion = new OpcionesRespuesta();
        BancoPregunta pregunta = new BancoPregunta();
        pregunta.setId(idPregunta);
        opcion.setIdPregunta(pregunta);
        opcion.setTextoOpcion(textoOpcion);
        opcion.setEsCorrecta(esCorrecta);
        return opcion;
    }

    /**
     * Construye una entidad OpcionesRespuesta válida y ejecuta el POST al recurso.
     * Devuelve el UUID del recurso creado extraído del header Location.
     * Falla si el POST no retorna 201.
     */
    private UUID crearOpcionReal(UUID idPregunta, String textoOpcion, Boolean esCorrecta) {
        OpcionesRespuesta opcion = crearOpcion(idPregunta, textoOpcion, esCorrecta);

        Response responseCreacion = post("opciones_respuesta", opcion);
        assertEquals(201, responseCreacion.getStatus(),
                "crearOpcionReal: POST debe retornar 201");

        String location = responseCreacion.getHeaderString("Location");
        assertNotNull(location);

        String idString = location.substring(location.lastIndexOf('/') + 1);
        return UUID.fromString(idString);
    }
}
