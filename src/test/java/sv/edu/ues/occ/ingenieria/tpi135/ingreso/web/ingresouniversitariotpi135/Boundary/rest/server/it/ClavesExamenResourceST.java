package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.it;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.ClavesExamen;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración REST para el recurso ClavesExamanResource.
 * 
 * Valida el contrato HTTP de los endpoints de claves de examen,
 * incluyendo filtros por prueba, validaciones de FK, y persistencia.
 * Recurso de acceso restringido (sensible) para administradores.
 */
public class ClavesExamenResourceST extends AbstractResourceST {

    // UUIDs de claves del init.sql
    private static final UUID ID_CLAVE_A = UUID.fromString("08000000-0000-0000-0000-000000000001");
    private static final UUID ID_CLAVE_B = UUID.fromString("08000000-0000-0000-0000-000000000002");

    // UUIDs de pruebas del init.sql
    private static final UUID ID_PRUEBA_1 = UUID.fromString("d1000000-0000-0000-0000-000000000001");
    private static final UUID ID_PRUEBA_2 = UUID.fromString("d1000000-0000-0000-0000-000000000002");

    /**
     * GET /resources/v1/claves_examen debe retornar al menos las 2 claves iniciales.
     */
    @Test
    void findRange_ConDatosIniciales_DebeRetornarLista() {
        Response response = get("claves_examen");

        assertEquals(200, response.getStatus());

        ClavesExamen[] arreglo = response.readEntity(ClavesExamen[].class);
        assertNotNull(arreglo);
        assertTrue(arreglo.length >= 2, "Debe haber al menos 2 claves iniciales");

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader);
        int total = Integer.parseInt(totalHeader);
        assertTrue(total >= 2);

        // Verificar que está Clave A
        boolean encontroClaveA = false;
        for (ClavesExamen clave : arreglo) {
            if (ID_CLAVE_A.equals(clave.getIdClaveExaman())) {
                encontroClaveA = true;
                assertEquals("Clave A", clave.getNombreClave());
                assertNotNull(clave.getIdPrueba());
                assertEquals(ID_PRUEBA_1, clave.getIdPrueba().getIdPruebaAdmision());
                break;
            }
        }
        assertTrue(encontroClaveA, "Debe encontrar Clave A");
    }

    /**
     * GET /resources/v1/claves_examen?first=0&max=1 debe retornar máximo 1 registro.
     */
    @Test
    void findRange_ConPaginacion_DebeRetornarDatosLimitados() {
        Response response = get("claves_examen?first=0&max=1");

        assertEquals(200, response.getStatus());

        ClavesExamen[] arreglo = response.readEntity(ClavesExamen[].class);
        assertNotNull(arreglo);
        assertEquals(1, arreglo.length, "Debe retornar exactamente 1 registro");

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader);
        int total = Integer.parseInt(totalHeader);
        assertTrue(total >= 2);
    }

    /**
     * GET /resources/v1/claves_examen/{id} con un id existente debe retornar 200.
     */
    @Test
    void findById_ConIdExistente_DebeRetornar200() {
        Response response = get("claves_examen/" + ID_CLAVE_A);

        assertEquals(200, response.getStatus());

        ClavesExamen entidad = response.readEntity(ClavesExamen.class);
        assertNotNull(entidad);
        assertEquals(ID_CLAVE_A, entidad.getIdClaveExaman());
        assertEquals("Clave A", entidad.getNombreClave());
        assertNotNull(entidad.getIdPrueba());
        assertEquals(ID_PRUEBA_1, entidad.getIdPrueba().getIdPruebaAdmision());
    }

    /**
     * GET /resources/v1/claves_examen/{id} con un id inexistente debe retornar 404.
     */
    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");

        Response response = get("claves_examen/" + idInexistente);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    /**
     * GET /resources/v1/claves_examen/no-es-uuid debe retornar 404.
     */
    @Test
    void findById_ConFormatoIdInvalido_DebeRetornar404() {
        Response response = get("claves_examen/no-es-uuid");

        assertEquals(404, response.getStatus());
    }

    /**
     * GET /resources/v1/claves_examen?idPrueba={id} debe retornar claves de esa prueba.
     */
    @Test
    void findRange_ConFiltroPrueba_DebeRetornarDeLaPrueba() {
        Response response = get("claves_examen?idPrueba=" + ID_PRUEBA_1);

        assertEquals(200, response.getStatus());

        ClavesExamen[] arreglo = response.readEntity(ClavesExamen[].class);
        assertNotNull(arreglo);
        assertTrue(arreglo.length >= 2, "Prueba 1 debe tener al menos 2 claves");

        // Verificar que al menos una clave pertenece a Prueba 1
        boolean encontroDeLaPrueba = false;
        for (ClavesExamen clave : arreglo) {
            assertNotNull(clave.getIdPrueba());
            if (ID_PRUEBA_1.equals(clave.getIdPrueba().getIdPruebaAdmision())) {
                encontroDeLaPrueba = true;
                break;
            }
        }
        assertTrue(encontroDeLaPrueba);
    }

    /**
     * POST /resources/v1/claves_examen con una entidad válida debe retornar 201.
     */
    @Test
    void create_ConEntidadValida_DebeRetornar201_YPermitirConsultar() {
        ClavesExamen nueva = crearClave(ID_PRUEBA_2, "Clave Test");

        Response responseCreacion = post("claves_examen", nueva);

        assertEquals(201, responseCreacion.getStatus());
        String location = responseCreacion.getHeaderString("Location");
        assertNotNull(location);

        String idString = location.substring(location.lastIndexOf('/') + 1);
        UUID idCreado = UUID.fromString(idString);

        Response responseConsulta = get("claves_examen/" + idCreado);
        assertEquals(200, responseConsulta.getStatus());

        ClavesExamen creado = responseConsulta.readEntity(ClavesExamen.class);
        assertNotNull(creado);
        assertEquals(idCreado, creado.getIdClaveExaman());
        assertEquals("Clave Test", creado.getNombreClave());
        assertEquals(ID_PRUEBA_2, creado.getIdPrueba().getIdPruebaAdmision());
    }

    /**
     * POST /resources/v1/claves_examen con una entidad inválida (sin prueba) debe retornar 422.
     */
    @Test
    void create_ConEntidadInvalida_SinPrueba_DebeRetornar422() {
        ClavesExamen nueva = new ClavesExamen();
        nueva.setNombreClave("Clave sin prueba");
        // Falta: idPrueba

        Response response = post("claves_examen", nueva);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }

    /**
     * POST /resources/v1/claves_examen con una entidad inválida (sin nombre) debe retornar 422.
     */
    @Test
    void create_ConEntidadInvalida_SinNombre_DebeRetornar422() {
        ClavesExamen nueva = new ClavesExamen();
        PruebasAdmision prueba = new PruebasAdmision();
        prueba.setIdPruebaAdmision(ID_PRUEBA_1);
        nueva.setIdPrueba(prueba);
        // Falta: nombreClave

        Response response = post("claves_examen", nueva);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }

    /**
     * PUT /resources/v1/claves_examen/{id} con datos válidos debe retornar 200.
     */
    @Test
    void update_ConEntidadValida_DebeRetornar200() {
        UUID idCreado = crearClaveReal(ID_PRUEBA_1, "Clave Original");

        ClavesExamen actualizada = crearClave(ID_PRUEBA_1, "Clave Actualizada");

        Response responsePut = put("claves_examen/" + idCreado, actualizada);

        assertEquals(200, responsePut.getStatus());

        ClavesExamen actualizado = responsePut.readEntity(ClavesExamen.class);
        assertNotNull(actualizado);
        assertEquals(idCreado, actualizado.getIdClaveExaman());
        assertEquals("Clave Actualizada", actualizado.getNombreClave());

        // Verificar persistencia
        Response responseConsulta = get("claves_examen/" + idCreado);
        assertEquals(200, responseConsulta.getStatus());

        ClavesExamen consultado = responseConsulta.readEntity(ClavesExamen.class);
        assertEquals("Clave Actualizada", consultado.getNombreClave());
    }

    /**
     * PUT /resources/v1/claves_examen/{id} con un id inexistente debe retornar 404.
     */
    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");
        ClavesExamen actualizada = crearClave(ID_PRUEBA_1, "No importa");

        Response response = put("claves_examen/" + idInexistente, actualizada);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    /**
     * DELETE /resources/v1/claves_examen/{id} debe retornar 204 y posteriores GETs deben retornar 404.
     */
    @Test
    void delete_ConIdExistente_DebeRetornar204_YNoEncontrarDespues() {
        UUID idCreado = crearClaveReal(ID_PRUEBA_1, "Clave a eliminar");

        Response responseAntesEliminar = get("claves_examen/" + idCreado);
        assertEquals(200, responseAntesEliminar.getStatus());

        Response responseDelete = delete("claves_examen/" + idCreado);
        assertEquals(204, responseDelete.getStatus());

        Response responseDespuesEliminar = get("claves_examen/" + idCreado);
        assertEquals(404, responseDespuesEliminar.getStatus());
        assertNotNull(responseDespuesEliminar.getHeaderString("Not-found-id"));
    }

    /**
     * DELETE /resources/v1/claves_examen/{id} con un id inexistente debe retornar 404.
     */
    @Test
    void delete_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");

        Response response = delete("claves_examen/" + idInexistente);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    // ========== HELPERS ==========

    /**
     * Construye una entidad ClavesExaman válida con los parámetros dados.
     * No ejecuta el POST, solo prepara el payload.
     */
    private ClavesExamen crearClave(UUID idPrueba, String nombreClave) {
        ClavesExamen clave = new ClavesExamen();
        PruebasAdmision prueba = new PruebasAdmision();
        prueba.setIdPruebaAdmision(idPrueba);
        clave.setIdPrueba(prueba);
        clave.setNombreClave(nombreClave);
        return clave;
    }

    /**
     * Construye una entidad ClavesExaman válida y ejecuta el POST al recurso.
     * Devuelve el UUID del recurso creado extraído del header Location.
     * Falla si el POST no retorna 201.
     */
    private UUID crearClaveReal(UUID idPrueba, String nombreClave) {
        ClavesExamen clave = crearClave(idPrueba, nombreClave);

        Response responseCreacion = post("claves_examen", clave);
        assertEquals(201, responseCreacion.getStatus(),
                "crearClaveReal: POST debe retornar 201");

        String location = responseCreacion.getHeaderString("Location");
        assertNotNull(location);

        String idString = location.substring(location.lastIndexOf('/') + 1);
        return UUID.fromString(idString);
    }
}
