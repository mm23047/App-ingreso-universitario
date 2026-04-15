package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.it;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AreasConocimiento;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración REST para el recurso BancoPreguntaResource.
 * 
 * Estas pruebas validan el contrato HTTP de los endpoints de banco de preguntas,
 * incluyendo códigos de estado, headers y cuerpos JSON, con integración real
 * contra la base de datos inicializada por ingresoTPI135_init.sql.
 */
public class BancoPreguntaResourceIT extends AbstractResourceIT {

    // UUIDs tomados del init.sql - Preguntas
    private static final UUID ID_PREGUNTA_1 = UUID.fromString("f1000000-0000-0000-0000-000000000001");
    private static final UUID ID_PREGUNTA_2 = UUID.fromString("f1000000-0000-0000-0000-000000000002");
    private static final UUID ID_PREGUNTA_3 = UUID.fromString("f1000000-0000-0000-0000-000000000003");

    // UUIDs tomados del init.sql - Áreas de Conocimiento
    private static final UUID ID_AREA_MATEMATICAS = UUID.fromString("a1000000-0000-0000-0000-000000000001");
    private static final UUID ID_AREA_CIENCIAS = UUID.fromString("a1000000-0000-0000-0000-000000000002");

    /**
     * GET /recursos/v1/banco_preguntas debe devolver al menos las 4 preguntas iniciales
     * con un header Total-records válido.
     */
    @Test
    void findRange_ConDatosIniciales_DebeRetornarListaPreguntas() {
        Response response = get("banco_preguntas");

        assertEquals(200, response.getStatus());

        BancoPregunta[] arreglo = response.readEntity(BancoPregunta[].class);
        assertNotNull(arreglo);
        assertTrue(arreglo.length >= 4, "Debe haber al menos 4 preguntas iniciales");

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader);
        int total = Integer.parseInt(totalHeader);
        assertTrue(total >= 4);

        // Verificar que están las preguntas conocidas
        boolean encontroPregunta1 = false, encontroPregunta2 = false, encontroPregunta3 = false;
        for (BancoPregunta pregunta : arreglo) {
            if (ID_PREGUNTA_1.equals(pregunta.getId())) {
                encontroPregunta1 = true;
                assertEquals("¿Cuánto es 2 + 2?", pregunta.getEnunciado());
                assertNotNull(pregunta.getIdArea());
                assertEquals(ID_AREA_MATEMATICAS, pregunta.getIdArea().getId());
            }
            if (ID_PREGUNTA_2.equals(pregunta.getId())) {
                encontroPregunta2 = true;
                assertEquals("¿Cuál es la raíz cuadrada de 144?", pregunta.getEnunciado());
            }
            if (ID_PREGUNTA_3.equals(pregunta.getId())) {
                encontroPregunta3 = true;
                assertEquals("¿Cuántos planetas tiene el sistema solar?", pregunta.getEnunciado());
            }
        }
        assertTrue(encontroPregunta1, "Debe encontrar pregunta 1");
        assertTrue(encontroPregunta2, "Debe encontrar pregunta 2");
        assertTrue(encontroPregunta3, "Debe encontrar pregunta 3");
    }

    /**
     * GET /banco_preguntas?first=0&max=2 debe devolver máximo 2 registros.
     */
    @Test
    void findRange_ConPaginacion_DebeRetornarDatosLimitados() {
        Response response = get("banco_preguntas?first=0&max=2");

        assertEquals(200, response.getStatus());

        BancoPregunta[] arreglo = response.readEntity(BancoPregunta[].class);
        assertNotNull(arreglo);
        assertEquals(2, arreglo.length, "Debe retornar exactamente 2 registros");

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader);
        int total = Integer.parseInt(totalHeader);
        assertTrue(total >= 4, "Total debe ser al menos 4");
    }

    /**
     * GET /banco_preguntas/{id} con un id existente debe devolver 200 y los datos correctos.
     */
    @Test
    void findById_ConIdExistente_DebeRetornar200() {
        Response response = get("banco_preguntas/" + ID_PREGUNTA_1);

        assertEquals(200, response.getStatus());

        BancoPregunta entidad = response.readEntity(BancoPregunta.class);
        assertNotNull(entidad);
        assertEquals(ID_PREGUNTA_1, entidad.getId());
        assertEquals("¿Cuánto es 2 + 2?", entidad.getEnunciado());
        assertNotNull(entidad.getIdArea());
        assertEquals(ID_AREA_MATEMATICAS, entidad.getIdArea().getId());
    }

    /**
     * GET /banco_preguntas/{id} con un id inexistente debe devolver 404.
     */
    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");

        Response response = get("banco_preguntas/" + idInexistente);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"),
                "Debe tener header Not-found-id");
    }

    /**
     * GET /banco_preguntas/no-es-uuid debe devolver 404.
     */
    @Test
    void findById_ConFormatoIdInvalido_DebeRetornar404() {
        Response response = get("banco_preguntas/no-es-uuid");

        assertEquals(404, response.getStatus());
    }

    /**
     * GET /banco_preguntas?idArea={area} debe retornar preguntas del área solicitada.
     */
    @Test
    void findRange_ConFiltroArea_DebeRetornarDelArea() {
        // Solicitar preguntas del área de Ciencias
        Response response = get("banco_preguntas?idArea=" + ID_AREA_CIENCIAS);

        assertEquals(200, response.getStatus());

        BancoPregunta[] arreglo = response.readEntity(BancoPregunta[].class);
        assertNotNull(arreglo);
        assertTrue(arreglo.length >= 1, "Debe retornar al menos 1 pregunta del área de ciencias");

        // Verificar que al menos una pregunta pertenece al área de ciencias
        boolean encontroDelArea = false;
        for (BancoPregunta pregunta : arreglo) {
            assertNotNull(pregunta.getIdArea());
            if (ID_AREA_CIENCIAS.equals(pregunta.getIdArea().getId())) {
                encontroDelArea = true;
                break;
            }
        }
        assertTrue(encontroDelArea, "Debe encontrar al menos una pregunta del área de ciencias");
    }

    /**
     * POST /banco_preguntas con una entidad válida debe devolver 201.
     */
    @Test
    void create_ConEntidadValida_DebeRetornar201_YPermitirConsultar() {
        BancoPregunta nueva = crearPregunta("¿Cuál es la capital de Francia?", ID_AREA_CIENCIAS);

        Response responseCreacion = post("banco_preguntas", nueva);

        assertEquals(201, responseCreacion.getStatus(),
                "POST debe retornar 201 Created");
        String location = responseCreacion.getHeaderString("Location");
        assertNotNull(location, "Debe tener header Location");

        String idString = location.substring(location.lastIndexOf('/') + 1);
        UUID idCreado = UUID.fromString(idString);

        // Verificar que se puede consultar el recurso creado
        Response responseConsulta = get("banco_preguntas/" + idCreado);
        assertEquals(200, responseConsulta.getStatus());

        BancoPregunta creado = responseConsulta.readEntity(BancoPregunta.class);
        assertNotNull(creado);
        assertEquals(idCreado, creado.getId());
        assertEquals("¿Cuál es la capital de Francia?", creado.getEnunciado());
        assertNotNull(creado.getIdArea());
        assertEquals(ID_AREA_CIENCIAS, creado.getIdArea().getId());
    }

    /**
     * POST /banco_preguntas con una entidad inválida (sin área) debe devolver 422.
     */
    @Test
    void create_ConEntidadInvalida_SinArea_DebeRetornar422() {
        BancoPregunta nueva = new BancoPregunta();
        nueva.setEnunciado("Pregunta sin área");
        // Falta: idArea

        Response response = post("banco_preguntas", nueva);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }



    /**
     * PUT /banco_preguntas/{id} con datos válidos debe devolver 200.
     */
    @Test
    void update_ConEntidadValida_DebeRetornar200() {
        // Crear una pregunta temporal
        UUID idCreado = crearPreguntaReal("Pregunta para actualizar", ID_AREA_CIENCIAS);

        // Construir payload actualizado
        BancoPregunta actualizada = crearPregunta(
                "Pregunta actualizada - ¿Cuál es la raíz cuadrada de 16?",
                ID_AREA_MATEMATICAS);

        Response responsePut = put("banco_preguntas/" + idCreado, actualizada);

        assertEquals(200, responsePut.getStatus(),
                "PUT debe retornar 200 OK");

        BancoPregunta actualizado = responsePut.readEntity(BancoPregunta.class);
        assertNotNull(actualizado);
        assertEquals(idCreado, actualizado.getId());
        assertEquals("Pregunta actualizada - ¿Cuál es la raíz cuadrada de 16?", actualizado.getEnunciado());
        assertEquals(ID_AREA_MATEMATICAS, actualizado.getIdArea().getId());

        // Verificar persistencia
        Response responseConsulta = get("banco_preguntas/" + idCreado);
        assertEquals(200, responseConsulta.getStatus());

        BancoPregunta consultado = responseConsulta.readEntity(BancoPregunta.class);
        assertEquals("Pregunta actualizada - ¿Cuál es la raíz cuadrada de 16?", consultado.getEnunciado());
        assertEquals(ID_AREA_MATEMATICAS, consultado.getIdArea().getId());
    }

    /**
     * PUT /banco_preguntas/{id} con un id inexistente debe devolver 404.
     */
    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");
        BancoPregunta actualizada = crearPregunta("No importa", ID_AREA_MATEMATICAS);

        Response response = put("banco_preguntas/" + idInexistente, actualizada);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    /**
     * DELETE /banco_preguntas/{id} debe devolver 204 y posteriores GETs deben retornar 404.
     */
    @Test
    void delete_ConIdExistente_DebeRetornar204_YNoEncontrarDespues() {
        // Crear una pregunta temporal
        UUID idCreado = crearPreguntaReal("Pregunta a eliminar", ID_AREA_CIENCIAS);

        // Verificar que existe
        Response responseAntesEliminar = get("banco_preguntas/" + idCreado);
        assertEquals(200, responseAntesEliminar.getStatus());

        // Eliminar
        Response responseDelete = delete("banco_preguntas/" + idCreado);
        assertEquals(204, responseDelete.getStatus(),
                "DELETE debe retornar 204 No Content");

        // Verificar que ya no existe
        Response responseDespuesEliminar = get("banco_preguntas/" + idCreado);
        assertEquals(404, responseDespuesEliminar.getStatus());
        assertNotNull(responseDespuesEliminar.getHeaderString("Not-found-id"));
    }

    /**
     * DELETE /banco_preguntas/{id} con un id inexistente debe devolver 404.
     */
    @Test
    void delete_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");

        Response response = delete("banco_preguntas/" + idInexistente);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    // ========== HELPERS ==========

    /**
     * Construye una entidad BancoPregunta válida con los parámetros dados.
     * No ejecuta el POST, solo prepara el payload.
     */
    private BancoPregunta crearPregunta(String enunciado, UUID idArea) {
        BancoPregunta pregunta = new BancoPregunta();
        pregunta.setEnunciado(enunciado);
        AreasConocimiento area = new AreasConocimiento();
        area.setId(idArea);
        pregunta.setIdArea(area);
        return pregunta;
    }

    /**
     * Construye una entidad BancoPregunta válida y ejecuta el POST al recurso.
     * Devuelve el UUID del recurso creado extraído del header Location.
     * Falla si el POST no retorna 201.
     */
    private UUID crearPreguntaReal(String enunciado, UUID idArea) {
        BancoPregunta pregunta = crearPregunta(enunciado, idArea);

        Response responseCreacion = post("banco_preguntas", pregunta);
        assertEquals(201, responseCreacion.getStatus(),
                "crearPreguntaReal: POST debe retornar 201");

        String location = responseCreacion.getHeaderString("Location");
        assertNotNull(location, "crearPreguntaReal: Location header no puede ser nulo");

        String idString = location.substring(location.lastIndexOf('/') + 1);
        return UUID.fromString(idString);
    }
}
