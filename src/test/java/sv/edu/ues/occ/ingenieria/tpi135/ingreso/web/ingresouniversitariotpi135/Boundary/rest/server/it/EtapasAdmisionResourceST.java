package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.it;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.EtapasAdmision;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ST para EtapasAdmisionResource.
 * Base: GET/POST/PUT/DELETE /resources/v1/etapas
 * Datos semilla: init.sql tiene 5 etapas.
 * El recurso valida: nombre no vacío Y cantidadPreguntasRequeridas > 0.
 */
public class EtapasAdmisionResourceST extends AbstractResourceST {

    // ID semilla desde init.sql
    private static final UUID ID_ETAPA_INSCRIPCION = UUID.fromString("c1000000-0000-0000-0000-000000000001");

    @Test
    void findRange_ConDatosIniciales_DebeRetornarLista() {
        Response response = get("etapas");

        assertEquals(200, response.getStatus());

        EtapasAdmision[] arreglo = response.readEntity(EtapasAdmision[].class);
        assertNotNull(arreglo);
        assertTrue(arreglo.length >= 3, "Debe haber al menos 3 etapas semilla");

        String totalHeader = response.getHeaderString("Total-records");
        assertNotNull(totalHeader);
        assertTrue(Integer.parseInt(totalHeader) >= 3);
    }

    @Test
    void findById_ConIdExistente_DebeRetornar200() {
        Response response = get("etapas/" + ID_ETAPA_INSCRIPCION);

        assertEquals(200, response.getStatus());

        EtapasAdmision entidad = response.readEntity(EtapasAdmision.class);
        assertNotNull(entidad);
        assertEquals(ID_ETAPA_INSCRIPCION, entidad.getIdEtapaAdmision());
        assertEquals("Etapa Inscripcion", entidad.getNombre());
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");
        Response response = get("etapas/" + idInexistente);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void create_ConEntidadValida_DebeRetornar201_YPermitirConsultar() {
        EtapasAdmision nueva = new EtapasAdmision();
        nueva.setNombre("Etapa ST Test");
        nueva.setDescripcion("Etapa creada por ST");
        nueva.setCantidadPreguntasRequeridas(10); // obligatorio según validación del recurso

        Response responseCreacion = post("etapas", nueva);

        assertEquals(201, responseCreacion.getStatus());
        String location = responseCreacion.getHeaderString("Location");
        assertNotNull(location);

        String idCreadoStr = location.substring(location.lastIndexOf('/') + 1);
        UUID idCreado = UUID.fromString(idCreadoStr);

        Response responseConsulta = get("etapas/" + idCreado);
        assertEquals(200, responseConsulta.getStatus());

        EtapasAdmision creada = responseConsulta.readEntity(EtapasAdmision.class);
        assertEquals(idCreado, creada.getIdEtapaAdmision());
        assertEquals("Etapa ST Test", creada.getNombre());
    }

    @Test
    void create_ConEntidadInvalida_SinNombre_DebeRetornar400() {
        // El recurso valida nombre no vacío → 400 BAD_REQUEST
        EtapasAdmision invalida = new EtapasAdmision();
        invalida.setCantidadPreguntasRequeridas(10);
        // nombre es null

        Response response = post("etapas", invalida);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }

    @Test
    void create_ConEntidadInvalida_SinCantidadPreguntas_DebeRetornar400() {
        // El recurso también valida cantidadPreguntasRequeridas > 0 → 400 BAD_REQUEST
        EtapasAdmision invalida = new EtapasAdmision();
        invalida.setNombre("Etapa Sin Cantidad");
        // cantidadPreguntasRequeridas es null o 0

        Response response = post("etapas", invalida);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }

    @Test
    void update_ConEntidadValida_DebeRetornar200() {
        UUID idCreado = crearEtapaReal("Etapa Temporal Update");

        EtapasAdmision actualizada = new EtapasAdmision();
        actualizada.setNombre("Etapa Modificada ST");
        actualizada.setCantidadPreguntasRequeridas(15);

        Response responseUpdate = put("etapas/" + idCreado, actualizada);
        assertEquals(200, responseUpdate.getStatus());

        EtapasAdmision cuerpo = responseUpdate.readEntity(EtapasAdmision.class);
        assertEquals("Etapa Modificada ST", cuerpo.getNombre());

        Response responseConsulta = get("etapas/" + idCreado);
        assertEquals(200, responseConsulta.getStatus());
        assertEquals("Etapa Modificada ST", responseConsulta.readEntity(EtapasAdmision.class).getNombre());
    }

    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        UUID idInexistente = UUID.fromString("ffffffff-0000-0000-0000-000000000000");
        EtapasAdmision payload = new EtapasAdmision();
        payload.setNombre("Intento de actualización");
        payload.setCantidadPreguntasRequeridas(5);

        Response response = put("etapas/" + idInexistente, payload);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void delete_ConIdExistente_DebeRetornar204_YLuego404() {
        UUID idCreado = crearEtapaReal("Etapa Temporal Delete");

        Response responseDelete = delete("etapas/" + idCreado);
        assertEquals(204, responseDelete.getStatus());

        Response responseConsulta = get("etapas/" + idCreado);
        assertEquals(404, responseConsulta.getStatus());
        assertNotNull(responseConsulta.getHeaderString("Not-found-id"));
    }

    private UUID crearEtapaReal(String nombre) {
        EtapasAdmision nueva = new EtapasAdmision();
        nueva.setNombre(nombre);
        nueva.setCantidadPreguntasRequeridas(5);

        Response responseCreacion = post("etapas", nueva);
        assertEquals(201, responseCreacion.getStatus(), "Helper crearEtapaReal: POST debe retornar 201");

        String location = responseCreacion.getHeaderString("Location");
        return UUID.fromString(location.substring(location.lastIndexOf('/') + 1));
    }
}
