package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.it;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PruebasAdmision;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ST para PruebasAdmisionResource.
 * Base: GET/POST/PUT/DELETE /resources/v1/pruebas_admision
 * Datos semilla: init.sql tiene 3 pruebas.
 */
public class PruebasAdmisionResourceST extends AbstractResourceST {

    // UUIDs desde init.sql
    private static final UUID ID_PRUEBA_1 = UUID.fromString("d1000000-0000-0000-0000-000000000001");
    private static final UUID ID_PRUEBA_2 = UUID.fromString("d1000000-0000-0000-0000-000000000002");

    @Test
    void findRange_ConDatosIniciales_DebeRetornarLista() {
        Response response = get("pruebas_admision");

        assertEquals(200, response.getStatus());

        PruebasAdmision[] arreglo = response.readEntity(PruebasAdmision[].class);
        assertNotNull(arreglo);
        assertTrue(arreglo.length >= 2, "Debe haber al menos 2 pruebas semilla");

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader);
        assertTrue(Integer.parseInt(totalHeader) >= 2);

        // Verificar que está Prueba Test A con datos correctos
        boolean encontroPrueba1 = false;
        for (PruebasAdmision prueba : arreglo) {
            if (ID_PRUEBA_1.equals(prueba.getIdPruebaAdmision())) {
                encontroPrueba1 = true;
                assertNotNull(prueba.getNombrePrueba());
                assertEquals(2024, prueba.getAnio());
                assertTrue(prueba.getActiva());
                break;
            }
        }
        assertTrue(encontroPrueba1, "Debe encontrar Prueba Test A");
    }

    @Test
    void findRange_ConPaginacion_DebeRetornarDatosLimitados() {
        Response response = get("pruebas_admision?first=0&max=1");

        assertEquals(200, response.getStatus());

        PruebasAdmision[] arreglo = response.readEntity(PruebasAdmision[].class);
        assertNotNull(arreglo);
        assertEquals(1, arreglo.length, "Debe retornar exactamente 1 registro");

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader);
        assertTrue(Integer.parseInt(totalHeader) >= 2);
    }

    @Test
    void findById_ConIdExistente_DebeRetornar200() {
        Response response = get("pruebas_admision/" + ID_PRUEBA_1);

        assertEquals(200, response.getStatus());

        PruebasAdmision entidad = response.readEntity(PruebasAdmision.class);
        assertNotNull(entidad);
        assertEquals(ID_PRUEBA_1, entidad.getIdPruebaAdmision());
        assertEquals("Prueba Test A", entidad.getNombrePrueba());
        assertEquals(2024, entidad.getAnio());
        assertTrue(entidad.getActiva());
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");

        Response response = get("pruebas_admision/" + idInexistente);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void findById_ConFormatoIdInvalido_DebeRetornar400() {
        // El recurso captura IllegalArgumentException → 400 BAD_REQUEST
        Response response = get("pruebas_admision/no-es-uuid");

        assertEquals(400, response.getStatus());
    }

    @Test
    void create_ConEntidadValida_DebeRetornar201_YPermitirConsultar() {
        PruebasAdmision nueva = crearPrueba("Prueba ST Test 2027", 2027, true);

        Response responseCreacion = post("pruebas_admision", nueva);

        assertEquals(201, responseCreacion.getStatus());
        String location = responseCreacion.getHeaderString("Location");
        assertNotNull(location);

        String idString = location.substring(location.lastIndexOf('/') + 1);
        UUID idCreado = UUID.fromString(idString);

        Response responseConsulta = get("pruebas_admision/" + idCreado);
        assertEquals(200, responseConsulta.getStatus());

        PruebasAdmision creada = responseConsulta.readEntity(PruebasAdmision.class);
        assertEquals(idCreado, creada.getIdPruebaAdmision());
        assertEquals("Prueba ST Test 2027", creada.getNombrePrueba());
        assertEquals(2027, creada.getAnio());
        assertTrue(creada.getActiva());
    }

    @Test
    void create_ConEntidadInvalida_SinNombre_DebeRetornar400() {
        // El recurso valida nombrePrueba no vacío → 400 BAD_REQUEST
        PruebasAdmision invalida = new PruebasAdmision();
        invalida.setAnio(2027);
        invalida.setActiva(true);
        // Falta: nombrePrueba

        Response response = post("pruebas_admision", invalida);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }

    @Test
    void update_ConEntidadValida_DebeRetornar200() {
        UUID idCreado = crearPruebaReal("Prueba ST para Update", 2028, false);

        PruebasAdmision actualizada = crearPrueba("Prueba ST Actualizada", 2028, true);

        Response responsePut = put("pruebas_admision/" + idCreado, actualizada);

        assertEquals(200, responsePut.getStatus());

        PruebasAdmision actualizado = responsePut.readEntity(PruebasAdmision.class);
        assertNotNull(actualizado);
        assertEquals(idCreado, actualizado.getIdPruebaAdmision());
        assertEquals("Prueba ST Actualizada", actualizado.getNombrePrueba());
        assertTrue(actualizado.getActiva());

        Response responseConsulta = get("pruebas_admision/" + idCreado);
        assertEquals(200, responseConsulta.getStatus());
        assertEquals("Prueba ST Actualizada", responseConsulta.readEntity(PruebasAdmision.class).getNombrePrueba());
    }

    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");
        PruebasAdmision actualizada = crearPrueba("No importa", 2027, true);

        Response response = put("pruebas_admision/" + idInexistente, actualizada);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void delete_ConIdExistente_DebeRetornar204_YNoEncontrarDespues() {
        UUID idCreado = crearPruebaReal("Prueba ST para Delete", 2029, true);

        Response responseAntesEliminar = get("pruebas_admision/" + idCreado);
        assertEquals(200, responseAntesEliminar.getStatus());

        Response responseDelete = delete("pruebas_admision/" + idCreado);
        assertEquals(204, responseDelete.getStatus());

        Response responseDespuesEliminar = get("pruebas_admision/" + idCreado);
        assertEquals(404, responseDespuesEliminar.getStatus());
        assertNotNull(responseDespuesEliminar.getHeaderString("Not-found-id"));
    }

    @Test
    void delete_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");

        Response response = delete("pruebas_admision/" + idInexistente);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    private PruebasAdmision crearPrueba(String nombre, Integer anio, Boolean activa) {
        PruebasAdmision prueba = new PruebasAdmision();
        prueba.setNombrePrueba(nombre);
        prueba.setAnio(anio);
        prueba.setActiva(activa);
        return prueba;
    }

    private UUID crearPruebaReal(String nombre, Integer anio, Boolean activa) {
        PruebasAdmision prueba = crearPrueba(nombre, anio, activa);

        Response responseCreacion = post("pruebas_admision", prueba);
        assertEquals(201, responseCreacion.getStatus(), "Helper crearPruebaReal: POST debe retornar 201");

        String location = responseCreacion.getHeaderString("Location");
        assertNotNull(location);
        return UUID.fromString(location.substring(location.lastIndexOf('/') + 1));
    }
}
