package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.it;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ExamenesRealizado;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integracion REST para el recurso ExamenesRealizadoResource.
 *
 * Este recurso es de solo lectura y expone:
 * - GET /examenes_realizados (paginado, con filtros opcionales por aspiranteId y pruebaId).
 * - GET /examenes_realizados/{id} para consultar un examen realizado especifico.
 *
 * Las pruebas validan que:
 * - Se puedan recuperar los examenes iniciales sembrados por ingresoTPI135_init.sql.
 * - Los filtros por aspiranteId y pruebaId funcionen de acuerdo a las relaciones de la BD.
 * - Se manejen correctamente los codigos 200, 404 y 422 en los casos mas relevantes.
 */
public class ExamenesRealizadoResourceST extends AbstractResourceST {

    // UUIDs tomados del init.sql (mismos que en ExamenesRealizadoDAOIT y entidades relacionadas)
    private static final UUID ID_EXAMEN_1      = UUID.fromString("0d000000-0000-0000-0000-000000000001");
    private static final UUID ID_EXAMEN_2      = UUID.fromString("0d000000-0000-0000-0000-000000000002");
    private static final UUID ID_ASIGNACION_1  = UUID.fromString("0c000000-0000-0000-0000-000000000001");
    private static final UUID ID_ASPIRANTE_1   = UUID.fromString("e1000000-0000-0000-0000-000000000001");
    private static final UUID ID_INSCRIPCION_2 = UUID.fromString("09000000-0000-0000-0000-000000000002");
    private static final UUID ID_PRUEBA_1      = UUID.fromString("d1000000-0000-0000-0000-000000000001");
    private static final UUID ID_ETAPA_1       = UUID.fromString("c1000000-0000-0000-0000-000000000001");

    /**
     * GET /examenes_realizados/{id} con un id existente debe devolver 200
     * y reflejar los datos basicos del examen configurados en init.sql.
     */
    @Test
    void findById_ConIdExistente_DebeRetornar200() {
        Response response = get("examenes_realizados/" + ID_EXAMEN_1);

        assertEquals(200, response.getStatus());

        ExamenesRealizado entidad = response.readEntity(ExamenesRealizado.class);
        assertNotNull(entidad);
        assertEquals(ID_EXAMEN_1, entidad.getId());
        assertNotNull(entidad.getIdAsignacion());
        assertEquals(ID_ASIGNACION_1, entidad.getIdAsignacion().getId());
        assertNotNull(entidad.getIdEtapa());
        assertEquals(ID_ETAPA_1, entidad.getIdEtapa().getId());
        assertNotNull(entidad.getPuntajeFinal());
        assertEquals(8.50d, entidad.getPuntajeFinal().doubleValue(), 0.001d);
    }

    /**
     * GET /examenes_realizados/{id} con un id inexistente debe devolver 404
     * y el header Not-found-id.
     */
    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");

        Response response = get("examenes_realizados/" + idInexistente);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    /**
     * GET /examenes_realizados sin filtros debe usar el comportamiento paginado heredado
     * de AbstractResource, devolviendo al menos los 2 examenes iniciales y el header Total-records.
     */
    @Test
    void findRange_SinFiltros_DebeRetornarListaYPaginacion() {
        Response response = get("examenes_realizados?first=0&max=10");

        assertEquals(200, response.getStatus());

        ExamenesRealizado[] arreglo = response.readEntity(ExamenesRealizado[].class);
        assertNotNull(arreglo);
        assertTrue(arreglo.length > 0);

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader);
        int total = Integer.parseInt(totalHeader);
        assertTrue(total >= 2);
    }

    /**
     * GET /examenes_realizados?aspiranteId=... con un aspiranteId valido debe
     * devolver los examenes asociados a ese aspirante.
     */
    @Test
    void findRange_ConAspiranteIdValido_DebeRetornarListaFiltrada() {
        Response response = get("examenes_realizados?aspiranteId=" + ID_ASPIRANTE_1);

        assertEquals(200, response.getStatus());

        ExamenesRealizado[] arreglo = response.readEntity(ExamenesRealizado[].class);
        assertNotNull(arreglo);
        assertTrue(arreglo.length >= 1);

        // Con los datos iniciales del init.sql, el aspirante 1 solo tiene
        // asociado el examen con ID_EXAMEN_1, de modo que el filtro por
        // aspiranteId debe devolver precisamente ese examen.
        for (ExamenesRealizado examen : arreglo) {
            assertEquals(ID_EXAMEN_1, examen.getId());
        }
    }

    /**
     * GET /examenes_realizados?aspiranteId=... con un aspiranteId con formato
     * invalido debe devolver 422 y el header Missing-parameter.
     */
    @Test
    void findRange_ConAspiranteIdInvalido_DebeRetornar422() {
        Response response = get("examenes_realizados?aspiranteId=no-es-uuid");

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }

    /**
     * GET /examenes_realizados?pruebaId=... con un pruebaId valido debe devolver
     * los examenes asociados a esa prueba.
     */
    @Test
    void findRange_ConPruebaIdValido_DebeRetornarListaFiltrada() {
        Response response = get("examenes_realizados?pruebaId=" + ID_PRUEBA_1);

        assertEquals(200, response.getStatus());

        ExamenesRealizado[] arreglo = response.readEntity(ExamenesRealizado[].class);
        assertNotNull(arreglo);
        assertTrue(arreglo.length >= 1);

        // Todos los examenes devueltos deben corresponder a la prueba 1
        for (ExamenesRealizado examen : arreglo) {
            assertNotNull(examen.getIdClave());
            assertNotNull(examen.getIdClave().getIdPrueba());
            assertEquals(ID_PRUEBA_1, examen.getIdClave().getIdPrueba().getId());
        }
    }

    /**
     * GET /examenes_realizados?pruebaId=... con un pruebaId con formato invalido
     * debe devolver 422 y el header Missing-parameter.
     */
    @Test
    void findRange_ConPruebaIdInvalido_DebeRetornar422() {
        Response response = get("examenes_realizados?pruebaId=no-es-uuid");

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }

    /**
     * POST /examenes_realizados/{id}/calificar debe recalcular el puntaje final
     * usando respuestas del examen y marcar la inscripción asociada como CALIFICADO.
     */
    @Test
    void calificar_ConIdExistente_DebeRecalcularPuntajeYMarcarInscripcion() {
        Response responseCalificacion = targetDe("examenes_realizados/" + ID_EXAMEN_2 + "/calificar")
                .request()
                .post(Entity.text(""));

        assertEquals(200, responseCalificacion.getStatus());
        ExamenesRealizado calificado = responseCalificacion.readEntity(ExamenesRealizado.class);
        assertNotNull(calificado);
        assertEquals(ID_EXAMEN_2, calificado.getId());
        assertNotNull(calificado.getPuntajeFinal());
        assertEquals(0, new BigDecimal("5.00").compareTo(calificado.getPuntajeFinal()));

        Response responseExamen = get("examenes_realizados/" + ID_EXAMEN_2);
        assertEquals(200, responseExamen.getStatus());
        ExamenesRealizado examenPersistido = responseExamen.readEntity(ExamenesRealizado.class);
        assertNotNull(examenPersistido);
        assertEquals(0, new BigDecimal("5.00").compareTo(examenPersistido.getPuntajeFinal()));

        Response responseInscripcion = get("inscripciones_prueba/" + ID_INSCRIPCION_2);
        assertEquals(200, responseInscripcion.getStatus());
        InscripcionesPrueba inscripcion = responseInscripcion.readEntity(InscripcionesPrueba.class);
        assertNotNull(inscripcion);
        assertEquals("CALIFICADO", inscripcion.getEstado());
    }
}
