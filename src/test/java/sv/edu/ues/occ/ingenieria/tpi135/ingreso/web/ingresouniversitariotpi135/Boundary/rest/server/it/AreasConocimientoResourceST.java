package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.it;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.AreasConocimiento;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ST para AreasConocimientoResource.
 * Base: GET/POST/PUT/DELETE /resources/v1/areas
 * Datos semilla: init.sql tiene 3 áreas (Matemática, Lenguaje, Ciencias Naturales).
 */
public class AreasConocimientoResourceST extends AbstractResourceST {

    // IDs desde init.sql
    private static final UUID ID_AREA_MATEMATICA = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Test
    void findRange_ConDatosIniciales_DebeRetornarLista() {
        Response response = get("areas");

        assertEquals(200, response.getStatus());

        AreasConocimiento[] arreglo = response.readEntity(AreasConocimiento[].class);
        assertNotNull(arreglo);
        assertTrue(arreglo.length >= 3, "Debe haber al menos 3 áreas semilla");

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader);
        assertTrue(Integer.parseInt(totalHeader) >= 3);
    }

    @Test
    void findById_ConIdExistente_DebeRetornar200() {
        Response response = get("areas/" + ID_AREA_MATEMATICA);

        assertEquals(200, response.getStatus());

        AreasConocimiento entidad = response.readEntity(AreasConocimiento.class);
        assertNotNull(entidad);
        assertEquals(ID_AREA_MATEMATICA, entidad.getIdAreaConocimiento());
        assertEquals("Matemática", entidad.getNombreArea());
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");

        Response response = get("areas/" + idInexistente);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void create_ConEntidadValida_DebeRetornar201_YPermitirConsultar() {
        AreasConocimiento nueva = new AreasConocimiento();
        nueva.setNombreArea("Humanidades");

        Response responseCreacion = post("areas", nueva);

        assertEquals(201, responseCreacion.getStatus());
        String location = responseCreacion.getHeaderString("Location");
        assertNotNull(location);

        String idString = location.substring(location.lastIndexOf('/') + 1);
        UUID idCreado = UUID.fromString(idString);

        Response responseConsulta = get("areas/" + idCreado);
        assertEquals(200, responseConsulta.getStatus());

        AreasConocimiento creada = responseConsulta.readEntity(AreasConocimiento.class);
        assertEquals(idCreado, creada.getIdAreaConocimiento());
        assertEquals("Humanidades", creada.getNombreArea());
    }

    @Test
    void create_ConEntidadInvalida_SinNombre_DebeRetornar400() {
        // El recurso valida que nombreArea no sea nulo/blank → 400 BAD_REQUEST
        AreasConocimiento invalida = new AreasConocimiento();
        // nombreArea es null → debe retornar 400

        Response response = post("areas", invalida);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }

    @Test
    void update_ConEntidadValida_DebeRetornar200() {
        UUID idCreado = crearAreaReal("Área Temporal Update");

        AreasConocimiento actualizada = new AreasConocimiento();
        actualizada.setNombreArea("Área Modificada ST");

        Response responseUpdate = put("areas/" + idCreado, actualizada);
        assertEquals(200, responseUpdate.getStatus());

        AreasConocimiento cuerpo = responseUpdate.readEntity(AreasConocimiento.class);
        assertEquals("Área Modificada ST", cuerpo.getNombreArea());

        Response responseConsulta = get("areas/" + idCreado);
        assertEquals(200, responseConsulta.getStatus());
        assertEquals("Área Modificada ST", responseConsulta.readEntity(AreasConocimiento.class).getNombreArea());
    }

    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");
        AreasConocimiento payload = new AreasConocimiento();
        payload.setNombreArea("Intento de actualización");

        Response response = put("areas/" + idInexistente, payload);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void delete_ConIdExistente_DebeRetornar204_YLuego404() {
        // Las áreas semilla tienen temas → 409 Conflict. Creamos una nueva sin temas.
        UUID idCreado = crearAreaReal("Área Temporal Delete");

        Response responseDelete = delete("areas/" + idCreado);
        assertEquals(204, responseDelete.getStatus());

        Response responseConsulta = get("areas/" + idCreado);
        assertEquals(404, responseConsulta.getStatus());
        assertNotNull(responseConsulta.getHeaderString("Not-found-id"));
    }

    private UUID crearAreaReal(String nombreArea) {
        AreasConocimiento nueva = new AreasConocimiento();
        nueva.setNombreArea(nombreArea);

        Response responseCreacion = post("areas", nueva);
        assertEquals(201, responseCreacion.getStatus(), "Helper crearAreaReal: POST debe retornar 201");

        String location = responseCreacion.getHeaderString("Location");
        return UUID.fromString(location.substring(location.lastIndexOf('/') + 1));
    }
}
