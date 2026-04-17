package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.it;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AsignacionesAulaPupitre;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AulasExaman;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.InscripcionesPrueba;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integracion REST para el recurso AsignacionesAulaPupitreResource.
 *
 * Estas pruebas ejercitan:
 * - Consulta por id de una asignacion existente e inexistente.
 * - Listado paginado sin filtros.
 * - Filtros por inscripcionId y aspiranteId.
 * - Creacion, actualizacion y eliminacion de asignaciones respetando las FKs
 *   hacia inscripciones_prueba y aulas_examen iniciales definidas en ingresoTPI135_init.sql.
 */
public class AsignacionesAulaPupitreResourceST extends AbstractResourceIT {

    // UUIDs tomados del init.sql (mismos que en AsignacionesAulaPupitreDAOIT y entidades relacionadas)
    private static final UUID ID_ASIGNACION_1  = UUID.fromString("0c000000-0000-0000-0000-000000000001");
    private static final UUID ID_INSCRIPCION_1 = UUID.fromString("09000000-0000-0000-0000-000000000001");
    private static final UUID ID_ASPIRANTE_1   = UUID.fromString("e1000000-0000-0000-0000-000000000001");
    private static final UUID ID_AULA_1        = UUID.fromString("0a000000-0000-0000-0000-000000000001");

    /**
     * GET /asignaciones_aula_pupitre/{id} con un id existente debe devolver 200
     * y reflejar los datos basicos de la asignacion configurados en init.sql.
     */
    @Test
    void findById_ConIdExistente_DebeRetornar200() {
        Response response = get("asignaciones_aula_pupitre/" + ID_ASIGNACION_1);

        assertEquals(200, response.getStatus());

        AsignacionesAulaPupitre entidad = response.readEntity(AsignacionesAulaPupitre.class);
        assertNotNull(entidad);
        assertEquals(ID_ASIGNACION_1, entidad.getId());
        assertNotNull(entidad.getIdInscripcion());
        assertEquals(ID_INSCRIPCION_1, entidad.getIdInscripcion().getId());
        assertNotNull(entidad.getIdAula());
        assertEquals(ID_AULA_1, entidad.getIdAula().getId());
        assertNotNull(entidad.getPupitre());
    }

    /**
     * GET /asignaciones_aula_pupitre/{id} con un id inexistente debe devolver 404
     * y el header Not-found-id.
     */
    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");

        Response response = get("asignaciones_aula_pupitre/" + idInexistente);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    /**
     * GET /asignaciones_aula_pupitre sin filtros debe usar el comportamiento paginado heredado
     * de AbstractResource, devolviendo al menos las 2 asignaciones iniciales y el header Total-records.
     */
    @Test
    void findRange_SinFiltros_DebeRetornarListaYPaginacion() {
        Response response = get("asignaciones_aula_pupitre?first=0&max=10");

        assertEquals(200, response.getStatus());

        AsignacionesAulaPupitre[] arreglo = response.readEntity(AsignacionesAulaPupitre[].class);
        assertNotNull(arreglo);
        assertTrue(arreglo.length >= 2);

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader);
        int total = Integer.parseInt(totalHeader);
        assertTrue(total >= 2);
    }

    /**
     * GET /asignaciones_aula_pupitre?inscripcionId=... con un inscripcionId valido
     * debe devolver las asignaciones asociadas a esa inscripcion.
     */
    @Test
    void findRange_ConInscripcionIdValido_DebeRetornarListaFiltrada() {
        Response response = get("asignaciones_aula_pupitre?inscripcionId=" + ID_INSCRIPCION_1);

        assertEquals(200, response.getStatus());

        AsignacionesAulaPupitre[] arreglo = response.readEntity(AsignacionesAulaPupitre[].class);
        assertNotNull(arreglo);
        assertTrue(arreglo.length >= 1);

        for (AsignacionesAulaPupitre a : arreglo) {
            assertNotNull(a.getIdInscripcion());
            assertEquals(ID_INSCRIPCION_1, a.getIdInscripcion().getId());
        }
    }

    /**
     * GET /asignaciones_aula_pupitre?inscripcionId=... con un valor de formato invalido
     * debe devolver 422 y el header Missing-parameter.
     */
    @Test
    void findRange_ConInscripcionIdInvalido_DebeRetornar422() {
        Response response = get("asignaciones_aula_pupitre?inscripcionId=no-es-uuid");

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }

    /**
     * GET /asignaciones_aula_pupitre?aspiranteId=... con un aspiranteId valido
     * debe devolver las asignaciones asociadas a inscripciones de ese aspirante.
     */
    @Test
    void findRange_ConAspiranteIdValido_DebeRetornarListaFiltrada() {
        Response response = get("asignaciones_aula_pupitre?aspiranteId=" + ID_ASPIRANTE_1);

        assertEquals(200, response.getStatus());

        AsignacionesAulaPupitre[] arreglo = response.readEntity(AsignacionesAulaPupitre[].class);
        assertNotNull(arreglo);
        assertTrue(arreglo.length >= 1);

        for (AsignacionesAulaPupitre a : arreglo) {
            assertNotNull(a.getIdInscripcion());
            // La relación con el aspirante se valida indirectamente a través de la inscripción,
            // que en los datos iniciales está asociada de forma única al aspirante 1.
            assertEquals(ID_INSCRIPCION_1, a.getIdInscripcion().getId());
        }
    }

    /**
     * GET /asignaciones_aula_pupitre?aspiranteId=... con un valor de formato invalido
     * debe devolver 422 y el header Missing-parameter.
     */
    @Test
    void findRange_ConAspiranteIdInvalido_DebeRetornar422() {
        Response response = get("asignaciones_aula_pupitre?aspiranteId=no-es-uuid");

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }

    /**
     * POST /asignaciones_aula_pupitre con una entidad valida debe devolver 201 y
     * permitir consultar luego el recurso creado.
     */
    @Test
    void create_ConEntidadValida_DebeRetornar201_YPermitirConsultar() {
        AsignacionesAulaPupitre nueva = crearAsignacion(ID_INSCRIPCION_1, ID_AULA_1, "Z-99");

        Response responseCreacion = post("asignaciones_aula_pupitre", nueva);

        assertEquals(201, responseCreacion.getStatus());
        String location = responseCreacion.getHeaderString("Location");
        assertNotNull(location);

        String idString = location.substring(location.lastIndexOf('/') + 1);
        UUID idCreado = UUID.fromString(idString);

        Response responseConsulta = get("asignaciones_aula_pupitre/" + idCreado);
        assertEquals(200, responseConsulta.getStatus());

        AsignacionesAulaPupitre creada = responseConsulta.readEntity(AsignacionesAulaPupitre.class);
        assertNotNull(creada);
        assertEquals(idCreado, creada.getId());
        assertNotNull(creada.getIdInscripcion());
        assertEquals(ID_INSCRIPCION_1, creada.getIdInscripcion().getId());
        assertNotNull(creada.getIdAula());
        assertEquals(ID_AULA_1, creada.getIdAula().getId());
        assertEquals("Z-99", creada.getPupitre());
    }

    /**
     * POST /asignaciones_aula_pupitre con una entidad invalida (sin inscripcion)
     * debe devolver 422 y el header Missing-parameter.
     */
    @Test
    void create_ConEntidadInvalida_SinInscripcion_DebeRetornar422() {
        AsignacionesAulaPupitre nueva = new AsignacionesAulaPupitre();

        AulasExaman aula = new AulasExaman();
        aula.setId(ID_AULA_1);
        nueva.setIdAula(aula);
        nueva.setPupitre("X-01");

        Response response = post("asignaciones_aula_pupitre", nueva);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }

    /**
     * PUT /asignaciones_aula_pupitre/{id} con datos validos debe devolver 200 y
     * reflejar los cambios, que ademas deben persistir al consultarse nuevamente.
     */
    @Test
    void update_ConEntidadValida_DebeRetornar200_YPersistirCambios() {
        // Crear primero una asignacion temporal
        UUID idCreado = crearAsignacionReal(ID_INSCRIPCION_1, ID_AULA_1, "Y-01");

        // Construir payload actualizado
        AsignacionesAulaPupitre actualizada = crearAsignacion(ID_INSCRIPCION_1, ID_AULA_1, "Y-02");

        Response responseUpdate = put("asignaciones_aula_pupitre/" + idCreado, actualizada);

        assertEquals(200, responseUpdate.getStatus());

        AsignacionesAulaPupitre cuerpo = responseUpdate.readEntity(AsignacionesAulaPupitre.class);
        assertNotNull(cuerpo);
        assertEquals(idCreado, cuerpo.getId());
        assertEquals("Y-02", cuerpo.getPupitre());

        // Verificar persistencia via GET
        Response responseConsulta = get("asignaciones_aula_pupitre/" + idCreado);
        assertEquals(200, responseConsulta.getStatus());
        AsignacionesAulaPupitre verificada = responseConsulta.readEntity(AsignacionesAulaPupitre.class);
        assertEquals("Y-02", verificada.getPupitre());
    }

    /**
     * PUT /asignaciones_aula_pupitre/{id} con un id inexistente debe devolver 404
     * y el header Not-found-id, sin crear ni modificar registros.
     */
    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");

        AsignacionesAulaPupitre payload = crearAsignacion(ID_INSCRIPCION_1, ID_AULA_1, "Y-99");

        Response response = put("asignaciones_aula_pupitre/" + idInexistente, payload);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    /**
     * DELETE /asignaciones_aula_pupitre/{id} con un id existente debe devolver 204
     * y luego 404 al consultar la asignacion eliminada.
     */
    @Test
    void delete_ConIdExistente_DebeRetornar204_YLuego404() {
        // Crear asignacion temporal
        UUID idCreado = crearAsignacionReal(ID_INSCRIPCION_1, ID_AULA_1, "W-01");

        Response responseDelete = delete("asignaciones_aula_pupitre/" + idCreado);
        assertEquals(204, responseDelete.getStatus());

        Response responseConsulta = get("asignaciones_aula_pupitre/" + idCreado);
        assertEquals(404, responseConsulta.getStatus());
        assertNotNull(responseConsulta.getHeaderString("Not-found-id"));
    }

    /**
     * DELETE /asignaciones_aula_pupitre/{id} con un id inexistente debe devolver 404
     * y el header Not-found-id.
     */
    @Test
    void delete_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");

        Response response = delete("asignaciones_aula_pupitre/" + idInexistente);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    /**
     * Construye una AsignacionesAulaPupitre coherente a partir de los ids de
     * inscripcion y aula, y el valor de pupitre deseado.
     */
    private AsignacionesAulaPupitre crearAsignacion(UUID idInscripcion, UUID idAula, String pupitre) {
        AsignacionesAulaPupitre asignacion = new AsignacionesAulaPupitre();

        InscripcionesPrueba inscripcion = new InscripcionesPrueba();
        inscripcion.setId(idInscripcion);
        asignacion.setIdInscripcion(inscripcion);

        AulasExaman aula = new AulasExaman();
        aula.setId(idAula);
        asignacion.setIdAula(aula);

        asignacion.setPupitre(pupitre);
        return asignacion;
    }

    /**
     * Crea realmente una asignacion via el recurso REST de asignaciones,
     * reutilizado por pruebas que solo necesitan una asignacion existente
     * como precondicion. Encapsula el POST y la extraccion del UUID desde
     * el header Location.
     */
    private UUID crearAsignacionReal(UUID idInscripcion, UUID idAula, String pupitre) {
        AsignacionesAulaPupitre nueva = crearAsignacion(idInscripcion, idAula, pupitre);

        Response responseCreacion = post("asignaciones_aula_pupitre", nueva);
        assertEquals(201, responseCreacion.getStatus());
        String location = responseCreacion.getHeaderString("Location");
        assertNotNull(location);

        String idString = location.substring(location.lastIndexOf('/') + 1);
        return UUID.fromString(idString);
    }
}
