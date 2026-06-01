package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.it;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.CatalogoCarrera;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ST para CatalogoCarreraResource.
 * Base: GET/POST/PUT/DELETE /resources/v1/carreras
 * Datos semilla: init.sql tiene 7 carreras.
 */
public class CatalogoCarreraResourceST extends AbstractResourceST {

    // ID semilla: Ingeniería en Ciencias de la Computación
    private static final String ID_CARRERA_ICS = "ICS";

    @Test
    void findRange_ConDatosIniciales_DebeRetornarLista() {
        Response response = get("carreras");

        assertEquals(200, response.getStatus());

        CatalogoCarrera[] arreglo = response.readEntity(CatalogoCarrera[].class);
        assertNotNull(arreglo);
        assertTrue(arreglo.length >= 4, "Debe haber al menos 4 carreras semilla");

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader);
        assertTrue(Integer.parseInt(totalHeader) >= 4);
    }

    @Test
    void findById_ConIdExistente_DebeRetornar200() {
        Response response = get("carreras/" + ID_CARRERA_ICS);

        assertEquals(200, response.getStatus());

        CatalogoCarrera entidad = response.readEntity(CatalogoCarrera.class);
        assertNotNull(entidad);
        assertEquals(ID_CARRERA_ICS, entidad.getIdCarrera());
        assertEquals("Ingeniería en Ciencias de la Computación", entidad.getNombreCatalogoCarrera());
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        String idInexistente = "XYZ";
        Response response = get("carreras/" + idInexistente);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void create_ConEntidadValida_DebeRetornar201_YPermitirConsultar() {
        CatalogoCarrera nueva = new CatalogoCarrera();
        nueva.setIdCarrera("IND");
        nueva.setNombreCatalogoCarrera("Ingeniería Industrial");

        Response responseCreacion = post("carreras", nueva);

        assertEquals(201, responseCreacion.getStatus());
        String location = responseCreacion.getHeaderString("Location");
        assertNotNull(location);

        String idCreado = location.substring(location.lastIndexOf('/') + 1);
        assertEquals("IND", idCreado);

        Response responseConsulta = get("carreras/" + idCreado);
        assertEquals(200, responseConsulta.getStatus());

        CatalogoCarrera creada = responseConsulta.readEntity(CatalogoCarrera.class);
        assertEquals("IND", creada.getIdCarrera());
        assertEquals("Ingeniería Industrial", creada.getNombreCatalogoCarrera());
    }

    @Test
    void create_ConEntidadInvalida_SinIdCarrera_DebeRetornar400() {
        // El recurso valida que idCarrera no sea nulo/blank → 400 BAD_REQUEST
        CatalogoCarrera invalida = new CatalogoCarrera();
        // Sin idCarrera

        Response response = post("carreras", invalida);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }

    @Test
    void update_ConEntidadValida_DebeRetornar200() {
        String idCreado = crearCarreraReal("TMP", "Carrera Temporal Update");

        CatalogoCarrera actualizada = new CatalogoCarrera();
        actualizada.setNombreCatalogoCarrera("Carrera Modificada ST");

        Response responseUpdate = put("carreras/" + idCreado, actualizada);
        assertEquals(200, responseUpdate.getStatus());

        CatalogoCarrera cuerpo = responseUpdate.readEntity(CatalogoCarrera.class);
        assertEquals("Carrera Modificada ST", cuerpo.getNombreCatalogoCarrera());

        Response responseConsulta = get("carreras/" + idCreado);
        assertEquals(200, responseConsulta.getStatus());
        assertEquals("Carrera Modificada ST", responseConsulta.readEntity(CatalogoCarrera.class).getNombreCatalogoCarrera());
    }

    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        String idInexistente = "ERR";
        CatalogoCarrera payload = new CatalogoCarrera();
        payload.setNombreCatalogoCarrera("Intento de actualización");

        Response response = put("carreras/" + idInexistente, payload);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void delete_ConIdExistente_DebeRetornar204_YLuego404() {
        String idCreado = crearCarreraReal("DEL", "Carrera Temporal Delete");

        Response responseDelete = delete("carreras/" + idCreado);
        assertEquals(204, responseDelete.getStatus());

        Response responseConsulta = get("carreras/" + idCreado);
        assertEquals(404, responseConsulta.getStatus());
        assertNotNull(responseConsulta.getHeaderString("Not-found-id"));
    }

    private String crearCarreraReal(String idCarrera, String nombre) {
        CatalogoCarrera nueva = new CatalogoCarrera();
        nueva.setIdCarrera(idCarrera);
        nueva.setNombreCatalogoCarrera(nombre);

        Response responseCreacion = post("carreras", nueva);
        assertEquals(201, responseCreacion.getStatus(), "Helper crearCarreraReal: POST debe retornar 201");

        String location = responseCreacion.getHeaderString("Location");
        return location.substring(location.lastIndexOf('/') + 1);
    }
}
