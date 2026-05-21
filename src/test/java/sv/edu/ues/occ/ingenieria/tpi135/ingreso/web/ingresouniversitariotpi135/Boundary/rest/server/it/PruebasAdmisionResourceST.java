package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.it;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración REST para el recurso PruebasAdmisionResource.
 * 
 * Estas pruebas validan el contrato HTTP de los endpoints de pruebas de admisión,
 * incluyendo códigos de estado, headers y cuerpos JSON, con integración real
 * contra la base de datos inicializada por ingresoTPI135_init.sql.
 */
public class PruebasAdmisionResourceST extends AbstractResourceST {

    // UUIDs tomados del init.sql
    private static final UUID ID_PRUEBA_1 = UUID.fromString("d1000000-0000-0000-0000-000000000001");
    private static final UUID ID_PRUEBA_2 = UUID.fromString("d1000000-0000-0000-0000-000000000002");

    /**
     * GET /pruebas_admision debe devolver al menos las 2 pruebas iniciales
     * con un header Total-records válido.
     */
    @Test
    void findRange_ConDatosIniciales_DebeRetornarLista() {
        Response response = get("pruebas_admision");

        assertEquals(200, response.getStatus());

        PruebasAdmision[] arreglo = response.readEntity(PruebasAdmision[].class);
        assertNotNull(arreglo);
        assertTrue(arreglo.length >= 2, "Debe haber al menos 2 pruebas iniciales");

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader);
        int total = Integer.parseInt(totalHeader);
        assertTrue(total >= 2);

        // Verificar que al menos está Prueba 1 con datos válidos
        boolean encontroPrueba1 = false;
        for (PruebasAdmision prueba : arreglo) {
            if (ID_PRUEBA_1.equals(prueba.getIdPruebaAdmision())) {
                encontroPrueba1 = true;
                assertNotNull(prueba.getNombrePrueba(), "Prueba 1 debe tener nombre");
                assertEquals(2026, prueba.getAnio());
                assertTrue(prueba.getActiva());
                break;
            }
        }
        assertTrue(encontroPrueba1, "Debe encontrar prueba 1");
    }

    /**
     * GET /pruebas_admision?first=0&max=1 debe devolver máximo 1 registro
     * y un header Total-records coherente.
     */
    @Test
    void findRange_ConPaginacion_DebeRetornarDatosLimitados() {
        Response response = get("pruebas_admision?first=0&max=1");

        assertEquals(200, response.getStatus());

        PruebasAdmision[] arreglo = response.readEntity(PruebasAdmision[].class);
        assertNotNull(arreglo);
        assertEquals(1, arreglo.length, "Debe retornar exactamente 1 registro");

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader);
        int total = Integer.parseInt(totalHeader);
        assertTrue(total >= 2, "Total debe ser al menos 2");
    }

    /**
     * GET /pruebas_admision/{id} con un id existente debe devolver 200
     * y los datos correctos.
     */
    @Test
    void findById_ConIdExistente_DebeRetornar200() {
        Response response = get("pruebas_admision/" + ID_PRUEBA_1);

        assertEquals(200, response.getStatus());

        PruebasAdmision entidad = response.readEntity(PruebasAdmision.class);
        assertNotNull(entidad);
        assertEquals(ID_PRUEBA_1, entidad.getIdPruebaAdmision());
        assertEquals("Prueba de Admisión 2026 - Ciclo 01", entidad.getNombrePrueba());
        assertEquals(2026, entidad.getAnio());
        assertTrue(entidad.getActiva());
    }

    /**
     * GET /pruebas_admision/{id} con un id inexistente debe devolver 404
     * y header Not-found-id.
     */
    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");

        Response response = get("pruebas_admision/" + idInexistente);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"),
                "Debe tener header Not-found-id");
    }

    /**
     * GET /pruebas_admision/no-es-uuid debe devolver 404
     * (JAX-RS rechaza el formato).
     */
    @Test
    void findById_ConFormatoIdInvalido_DebeRetornar404() {
        Response response = get("pruebas_admision/no-es-uuid");

        assertEquals(404, response.getStatus());
    }

    /**
     * POST /pruebas_admision con una entidad válida debe devolver 201
     * y permitir consultar el recurso creado.
     */
    @Test
    void create_ConEntidadValida_DebeRetornar201_YPermitirConsultar() {
        PruebasAdmision nueva = crearPrueba("Prueba Test 2027", 2027, true);

        Response responseCreacion = post("pruebas_admision", nueva);

        assertEquals(201, responseCreacion.getStatus(),
                "POST debe retornar 201 Created");
        String location = responseCreacion.getHeaderString("Location");
        assertNotNull(location, "Debe tener header Location");

        String idString = location.substring(location.lastIndexOf('/') + 1);
        UUID idCreado = UUID.fromString(idString);

        // Verificar que se puede consultar el recurso creado
        Response responseConsulta = get("pruebas_admision/" + idCreado);
        assertEquals(200, responseConsulta.getStatus());

        PruebasAdmision creado = responseConsulta.readEntity(PruebasAdmision.class);
        assertNotNull(creado);
        assertEquals(idCreado, creado.getIdPruebaAdmision());
        assertEquals("Prueba Test 2027", creado.getNombrePrueba());
        assertEquals(2027, creado.getAnio());
        assertTrue(creado.getActiva());
    }

    /**
        * POST /pruebas_admision con una entidad inválida (sin nombre)
        * debe devolver 422.
     */
    @Test
    void create_ConEntidadInvalida_SinNombre_DebeRetornar422() {
        PruebasAdmision nueva = new PruebasAdmision();
        nueva.setAnio(2027);
        nueva.setActiva(true);
        // Falta: nombrePrueba

        Response response = post("pruebas_admision", nueva);

        assertEquals(422, response.getStatus(),
            "POST con entidad inválida debe retornar 422");
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }

    /**
        * POST /pruebas_admision con una entidad inválida (sin año)
        * debe devolver 422.
     */
    @Test
    void create_ConEntidadInvalida_SinAnio_DebeRetornar422() {
        PruebasAdmision nueva = new PruebasAdmision();
        nueva.setNombrePrueba("Prueba sin año");
        nueva.setActiva(true);
        // Falta: anio

        Response response = post("pruebas_admision", nueva);

        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }

    /**
     * PUT /pruebas_admision/{id} con datos válidos debe devolver 200
     * y reflejar los cambios.
     */
    @Test
    void update_ConEntidadValida_DebeRetornar200() {
        // Crear primero una prueba temporal
        UUID idCreado = crearPruebaReal("Prueba para actualizar", 2027, false);

        // Construir payload actualizado
        PruebasAdmision actualizada = crearPrueba("Prueba actualizada", 2027, true);

        Response responsePut = put("pruebas_admision/" + idCreado, actualizada);

        assertEquals(200, responsePut.getStatus(),
                "PUT debe retornar 200 OK");

        PruebasAdmision actualizado = responsePut.readEntity(PruebasAdmision.class);
        assertNotNull(actualizado);
        assertEquals(idCreado, actualizado.getIdPruebaAdmision());
        assertEquals("Prueba actualizada", actualizado.getNombrePrueba());
        assertTrue(actualizado.getActiva());

        // Verificar persistencia
        Response responseConsulta = get("pruebas_admision/" + idCreado);
        assertEquals(200, responseConsulta.getStatus());

        PruebasAdmision consultado = responseConsulta.readEntity(PruebasAdmision.class);
        assertEquals("Prueba actualizada", consultado.getNombrePrueba());
        assertTrue(consultado.getActiva());
    }

    /**
     * PUT /pruebas_admision/{id} con un id inexistente debe devolver 404
     * y header Not-found-id.
     */
    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");
        PruebasAdmision actualizada = crearPrueba("No importa", 2027, true);

        Response response = put("pruebas_admision/" + idInexistente, actualizada);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    /**
     * DELETE /pruebas_admision/{id} debe devolver 204
     * y posteriores GETs deben retornar 404.
     */
    @Test
    void delete_ConIdExistente_DebeRetornar204_YNoEncontrarDespues() {
        // Crear una prueba temporal
        UUID idCreado = crearPruebaReal("Prueba a eliminar", 2027, true);

        // Verificar que existe
        Response responseAntesEliminar = get("pruebas_admision/" + idCreado);
        assertEquals(200, responseAntesEliminar.getStatus());

        // Eliminar
        Response responseDelete = delete("pruebas_admision/" + idCreado);
        assertEquals(204, responseDelete.getStatus(),
                "DELETE debe retornar 204 No Content");

        // Verificar que ya no existe
        Response responseDespuesEliminar = get("pruebas_admision/" + idCreado);
        assertEquals(404, responseDespuesEliminar.getStatus());
        assertNotNull(responseDespuesEliminar.getHeaderString("Not-found-id"));
    }

    /**
     * DELETE /pruebas_admision/{id} con un id inexistente debe devolver 404.
     */
    @Test
    void delete_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");

        Response response = delete("pruebas_admision/" + idInexistente);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    // ========== HELPERS ==========

    /**
     * Construye una entidad PruebasAdmision válida con los parámetros dados.
     * No ejecuta el POST, solo prepara el payload.
     */
    private PruebasAdmision crearPrueba(String nombrePrueba, Integer anio, Boolean activa) {
        PruebasAdmision prueba = new PruebasAdmision();
        prueba.setNombrePrueba(nombrePrueba);
        prueba.setAnio(anio);
        prueba.setActiva(activa);
        return prueba;
    }

    /**
     * Construye una entidad PruebasAdmision válida y ejecuta el POST al recurso.
     * Devuelve el UUID del recurso creado extraído del header Location.
     * Falla si el POST no retorna 201.
     */
    private UUID crearPruebaReal(String nombrePrueba, Integer anio, Boolean activa) {
        PruebasAdmision prueba = crearPrueba(nombrePrueba, anio, activa);

        Response responseCreacion = post("pruebas_admision", prueba);
        assertEquals(201, responseCreacion.getStatus(),
                "crearPruebaReal: POST debe retornar 201");

        String location = responseCreacion.getHeaderString("Location");
        assertNotNull(location, "crearPruebaReal: Location header no puede ser nulo");

        String idString = location.substring(location.lastIndexOf('/') + 1);
        return UUID.fromString(idString);
    }
}
