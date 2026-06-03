package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.it;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ST para BancoPreguntaResource.
 * Base: GET/PUT/DELETE /resources/v1/preguntas
 * Nota: No existe POST /preguntas en el recurso; los tests CRUD usan solo preguntas semilla.
 * Datos semilla (init.sql): 4 preguntas.
 */
public class BancoPreguntaResourceST extends AbstractResourceST {

    // UUIDs de preguntas desde init.sql
    private static final UUID ID_PREGUNTA_1 = UUID.fromString("f1000000-0000-0000-0000-000000000001");
    private static final UUID ID_PREGUNTA_2 = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID ID_PREGUNTA_3 = UUID.fromString("f1000000-0000-0000-0000-000000000003");

    @Test
    void findRange_ConDatosIniciales_DebeRetornarListaPreguntas() {
        Response response = get("preguntas");

        assertEquals(200, response.getStatus());

        BancoPregunta[] arreglo = response.readEntity(BancoPregunta[].class);
        assertNotNull(arreglo);
        assertTrue(arreglo.length >= 4, "Debe haber al menos 4 preguntas semilla");

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader);
        assertTrue(Integer.parseInt(totalHeader) >= 4);

        // Verificar que están las preguntas conocidas por ID
        boolean encontroPregunta1 = false, encontroPregunta2 = false, encontroPregunta3 = false;
        for (BancoPregunta pregunta : arreglo) {
            if (ID_PREGUNTA_1.equals(pregunta.getIdBancoPregunta())) encontroPregunta1 = true;
            if (ID_PREGUNTA_2.equals(pregunta.getIdBancoPregunta())) encontroPregunta2 = true;
            if (ID_PREGUNTA_3.equals(pregunta.getIdBancoPregunta())) encontroPregunta3 = true;
        }
        assertTrue(encontroPregunta1, "Debe encontrar pregunta f1000000...001");
        assertTrue(encontroPregunta2, "Debe encontrar pregunta 55555555...5555");
        assertTrue(encontroPregunta3, "Debe encontrar pregunta f1000000...003");
    }

    @Test
    void findRange_ConPaginacion_DebeRetornarDatosLimitados() {
        Response response = get("preguntas?first=0&max=2");

        assertEquals(200, response.getStatus());

        BancoPregunta[] arreglo = response.readEntity(BancoPregunta[].class);
        assertNotNull(arreglo);
        assertEquals(2, arreglo.length, "Debe retornar exactamente 2 registros");

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader);
        assertTrue(Integer.parseInt(totalHeader) >= 4);
    }

    @Test
    void findById_ConIdExistente_DebeRetornar200() {
        Response response = get("preguntas/" + ID_PREGUNTA_1);

        assertEquals(200, response.getStatus());

        BancoPregunta entidad = response.readEntity(BancoPregunta.class);
        assertNotNull(entidad);
        assertEquals(ID_PREGUNTA_1, entidad.getIdBancoPregunta());
        assertEquals("¿Cuánto es la raíz cuadrada de 16?", entidad.getEnunciado());
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");

        Response response = get("preguntas/" + idInexistente);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void findById_ConFormatoIdInvalido_DebeRetornar400() {
        // El recurso captura IllegalArgumentException → 400 BAD_REQUEST
        Response response = get("preguntas/no-es-uuid");

        assertEquals(400, response.getStatus());
    }

    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        // No necesita un recurso existente: la validación de existencia es primero
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");
        BancoPregunta payload = new BancoPregunta();
        payload.setEnunciado("Pregunta inexistente");

        Response response = put("preguntas/" + idInexistente, payload);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void delete_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");

        Response response = delete("preguntas/" + idInexistente);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }
}
