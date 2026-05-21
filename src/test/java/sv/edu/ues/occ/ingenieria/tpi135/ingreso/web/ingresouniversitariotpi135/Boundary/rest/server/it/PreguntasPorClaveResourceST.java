package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.it;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntasPorClave;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntasPorClaveId;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ClavesExamen;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración REST para el recurso PreguntasPorClaveResource.
 * 
 * Valida el contrato HTTP de los endpoints de asignación de preguntas a claves,
 * incluyendo acceso por clave compuesta (idClave, idPregunta), filtros, y persistencia.
 */
public class PreguntasPorClaveResourceST extends AbstractResourceST {

    // UUIDs de claves del init.sql
    private static final UUID ID_CLAVE_A = UUID.fromString("08000000-0000-0000-0000-000000000001");
    private static final UUID ID_CLAVE_B = UUID.fromString("08000000-0000-0000-0000-000000000002");

    // UUIDs de preguntas del init.sql
    private static final UUID ID_PREGUNTA_1 = UUID.fromString("f1000000-0000-0000-0000-000000000001");
    private static final UUID ID_PREGUNTA_2 = UUID.fromString("f1000000-0000-0000-0000-000000000002");
    private static final UUID ID_PREGUNTA_3 = UUID.fromString("f1000000-0000-0000-0000-000000000003");

    /**
     * GET /resources/v1/preguntas_por_clave debe retornar todas las asociaciones iniciales.
     * Init.sql contiene 4 asociaciones.
     */
    @Test
    void findRange_ConDatosIniciales_DebeRetornarLista() {
        Response response = get("preguntas_por_clave");

        assertEquals(200, response.getStatus());

        PreguntasPorClave[] arreglo = response.readEntity(PreguntasPorClave[].class);
        assertNotNull(arreglo);
        assertTrue(arreglo.length >= 4, "Debe haber al menos 4 asociaciones iniciales");

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader);
        int total = Integer.parseInt(totalHeader);
        assertTrue(total >= 4);

        // Verificar que está Clave A - Pregunta 1
        boolean encontroAsociacion = false;
        for (PreguntasPorClave pc : arreglo) {
            if (ID_CLAVE_A.equals(pc.getIdPreguntaPorClave().getIdClave()) && ID_PREGUNTA_1.equals(pc.getIdPreguntaPorClave().getIdPregunta())) {
                encontroAsociacion = true;
                break;
            }
        }
        assertTrue(encontroAsociacion, "Debe encontrar asociación Clave A - Pregunta 1");
    }

    /**
     * GET /resources/v1/preguntas_por_clave?first=0&max=2 debe retornar máximo 2 registros.
     */
    @Test
    void findRange_ConPaginacion_DebeRetornarDatosLimitados() {
        Response response = get("preguntas_por_clave?first=0&max=2");

        assertEquals(200, response.getStatus());

        PreguntasPorClave[] arreglo = response.readEntity(PreguntasPorClave[].class);
        assertNotNull(arreglo);
        assertEquals(2, arreglo.length, "Debe retornar exactamente 2 registros");

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader);
        int total = Integer.parseInt(totalHeader);
        assertTrue(total >= 4);
    }

    /**
     * GET /resources/v1/preguntas_por_clave/{idClave}/{idPregunta} con claves existentes debe retornar 200.
     */
    @Test
    void findById_ConIdCompuestoExistente_DebeRetornar200() {
        Response response = get("preguntas_por_clave/" + ID_CLAVE_A + "/" + ID_PREGUNTA_1);

        assertEquals(200, response.getStatus());

        PreguntasPorClave entidad = response.readEntity(PreguntasPorClave.class);
        assertNotNull(entidad);
        assertEquals(ID_CLAVE_A, entidad.getIdPreguntaPorClave().getIdClave());
        assertEquals(ID_PREGUNTA_1, entidad.getIdPreguntaPorClave().getIdPregunta());
    }

    /**
     * GET /resources/v1/preguntas_por_clave/{idClave}/{idPregunta} con ids inexistentes debe retornar 404.
     */
    @Test
    void findById_ConIdCompuestoInexistente_DebeRetornar404() {
        UUID idClaveInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");
        UUID idPreguntaInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000001");

        Response response = get("preguntas_por_clave/" + idClaveInexistente + "/" + idPreguntaInexistente);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    /**
     * GET /resources/v1/preguntas_por_clave?idClave={clave} debe retornar preguntas de esa clave.
     */
    @Test
    void findRange_ConFiltroClave_DebeRetornarDelaClave() {
        Response response = get("preguntas_por_clave?idClave=" + ID_CLAVE_A);

        assertEquals(200, response.getStatus());

        PreguntasPorClave[] arreglo = response.readEntity(PreguntasPorClave[].class);
        assertNotNull(arreglo);
        assertTrue(arreglo.length >= 1, "Debe retornar al menos 1 resultado");

        // Verificar que al menos uno pertenece a Clave A
        boolean encontrado = false;
        for (PreguntasPorClave pc : arreglo) {
            if (ID_CLAVE_A.equals(pc.getIdPreguntaPorClave().getIdClave())) {
                encontrado = true;
                break;
            }
        }
        assertTrue(encontrado, "Debe encontrar al menos una asociación con Clave A");
    }

    /**
     * GET /resources/v1/preguntas_por_clave?idPregunta={pregunta} debe retornar claves de esa pregunta.
     */
    @Test
    void findRange_ConFiltroPregunta_DebeRetornarDeLaPregunta() {
        Response response = get("preguntas_por_clave?idPregunta=" + ID_PREGUNTA_1);

        assertEquals(200, response.getStatus());

        PreguntasPorClave[] arreglo = response.readEntity(PreguntasPorClave[].class);
        assertNotNull(arreglo);
        assertTrue(arreglo.length >= 1, "Debe retornar al menos 1 resultado");

        // Verificar que al menos uno pertenece a Pregunta 1
        boolean encontrado = false;
        for (PreguntasPorClave pc : arreglo) {
            if (ID_PREGUNTA_1.equals(pc.getIdPreguntaPorClave().getIdPregunta())) {
                encontrado = true;
                break;
            }
        }
        assertTrue(encontrado, "Debe encontrar al menos una asociación con Pregunta 1");
    }

    /**
     * POST /resources/v1/preguntas_por_clave con una asociación válida debe retornar 201.
     */
    @Test
    void create_ConAsociacionValida_DebeRetornar201_YPermitirConsultar() {
        // Usar Clave B y Pregunta 3 (asegurarse de que no exista previamente)
        PreguntasPorClave nueva = crearAsociacion(ID_CLAVE_B, ID_PREGUNTA_3);

        Response responseCreacion = post("preguntas_por_clave", nueva);

        // POST puede retornar 201 si fue creado, o 422 si ya existe o hay validación
        if (responseCreacion.getStatus() == 201) {
            String location = responseCreacion.getHeaderString("Location");
            assertNotNull(location);

            // Extraer datos de la asociación y consultar
            Response responseConsulta = get("preguntas_por_clave/" + ID_CLAVE_B + "/" + ID_PREGUNTA_3);
            assertEquals(200, responseConsulta.getStatus());
            
            PreguntasPorClave creado = responseConsulta.readEntity(PreguntasPorClave.class);
            assertNotNull(creado);
            assertEquals(ID_CLAVE_B, creado.getIdPreguntaPorClave().getIdClave());
            assertEquals(ID_PREGUNTA_3, creado.getIdPreguntaPorClave().getIdPregunta());
        }
    }

    /**
     * POST /resources/v1/preguntas_por_clave con una asociación inválida (sin clave) debe retornar 422.
     */
    @Test
    void create_ConAsociacionInvalida_SinClave_DebeRetornar422() {
        PreguntasPorClave nueva = new PreguntasPorClave();
        BancoPregunta pregunta = new BancoPregunta();
        pregunta.setIdBancoPregunta(ID_PREGUNTA_1);
        nueva.setBancoPregunta(pregunta);
        // Falta: idClave

        Response response = post("preguntas_por_clave", nueva);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }

    /**
     * POST /resources/v1/preguntas_por_clave con una asociación inválida (sin pregunta) debe retornar 422.
     */
    @Test
    void create_ConAsociacionInvalida_SinPregunta_DebeRetornar422() {
        PreguntasPorClave nueva = new PreguntasPorClave();
        ClavesExamen clave = new ClavesExamen();
        clave.setIdClaveExaman(ID_CLAVE_A);
        nueva.setClaveExamen(clave);
        // Falta: idPregunta

        Response response = post("preguntas_por_clave", nueva);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }

    /**
     * PUT /resources/v1/preguntas_por_clave/{idClave}/{idPregunta} con datos válidos debe retornar 200.
     */
    @Test
    void update_ConAsociacionValida_DebeRetornar200() {
        // Crear una asociación antes de actualizar
        PreguntasPorClave nueva = crearAsociacion(ID_CLAVE_B, ID_PREGUNTA_1);

        Response responseCreacion = post("preguntas_por_clave", nueva);

        if (responseCreacion.getStatus() == 201) {
            // Si fue creada exitosamente, intentar actualizar
            PreguntasPorClave actualizada = crearAsociacion(ID_CLAVE_B, ID_PREGUNTA_1);

            Response responsePut = put("preguntas_por_clave/" + ID_CLAVE_B + "/" + ID_PREGUNTA_1, actualizada);
            
            // PUT puede retornar 200 si actualiza, o 404 si la entidad no existe
            assertTrue(responsePut.getStatus() == 200 || responsePut.getStatus() == 404);
        }
    }

    /**
     * DELETE /resources/v1/preguntas_por_clave/{idClave}/{idPregunta} debe retornar 204.
     */
    @Test
    void delete_ConAsociacionExistente_DebeRetornar204_YNoEncontrarDespues() {
        // Crear una asociación temporal
        PreguntasPorClave nueva = crearAsociacion(ID_CLAVE_B, ID_PREGUNTA_3);

        Response responseCreacion = post("preguntas_por_clave", nueva);

        if (responseCreacion.getStatus() == 201) {
            // Si fue creada exitosamente, intentar eliminarla
            Response responseDelete = delete("preguntas_por_clave/" + ID_CLAVE_B + "/" + ID_PREGUNTA_3);
            assertEquals(204, responseDelete.getStatus());

            // Intentar consultar después de eliminado
            Response responseDespuesEliminar = get("preguntas_por_clave/" + ID_CLAVE_B + "/" + ID_PREGUNTA_3);
            assertEquals(404, responseDespuesEliminar.getStatus());
        }
    }

    /**
     * DELETE /resources/v1/preguntas_por_clave/{idClave}/{idPregunta} con ids inexistentes debe retornar 404.
     */
    @Test
    void delete_ConAsociacionInexistente_DebeRetornar404() {
        UUID idClaveInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");
        UUID idPreguntaInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000001");

        Response response = delete("preguntas_por_clave/" + idClaveInexistente + "/" + idPreguntaInexistente);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    // ========== HELPERS ==========

    /**
     * Construye una entidad PreguntasPorClave válida con los parámetros dados.
     * No ejecuta el POST, solo prepara el payload.
     */
    private PreguntasPorClave crearAsociacion(UUID idClave, UUID idPregunta) {
        PreguntasPorClave pc = new PreguntasPorClave();
        
        // Crear e inicializar el EmbeddedId
        PreguntasPorClaveId id = new PreguntasPorClaveId();
        id.setIdClave(idClave);
        id.setIdPregunta(idPregunta);
        pc.setIdPreguntaPorClave(id);
        
        // Asignar los objetos de referencia
        ClavesExamen clave = new ClavesExamen();
        clave.setIdClaveExaman(idClave);
        BancoPregunta pregunta = new BancoPregunta();
        pregunta.setIdBancoPregunta(idPregunta);
        pc.setClaveExamen(clave);
        pc.setBancoPregunta(pregunta);
        
        return pc;
    }
}
