package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server.it;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntaOpcion;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoRespuesta;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class OpcionesRespuestaResourceST extends AbstractResourceST {

    private static final UUID ID_OPCION_1 = UUID.fromString("0b000000-0000-0000-0000-000000000001");
    private static final UUID ID_OPCION_2 = UUID.fromString("0b000000-0000-0000-0000-000000000002");
    private static final UUID ID_PREGUNTA_1 = UUID.fromString("f1000000-0000-0000-0000-000000000001");
    private static final UUID ID_PREGUNTA_2 = UUID.fromString("f1000000-0000-0000-0000-000000000002");

    @Test
    void findRange_ConDatosIniciales_DebeRetornarLista() {
        Response response = get("opciones_respuesta");
        assertEquals(200, response.getStatus());
        PreguntaOpcion[] arreglo = response.readEntity(PreguntaOpcion[].class);
        assertNotNull(arreglo);
    }

    @Test
    void findById_ConIdExistente_DebeRetornar200() {
        Response response = get("opciones_respuesta/" + ID_OPCION_1);
        assertEquals(200, response.getStatus());
        PreguntaOpcion entidad = response.readEntity(PreguntaOpcion.class);
        assertNotNull(entidad);
        assertEquals(ID_OPCION_1, entidad.getId());
        assertNotNull(entidad.getIdPregunta());
    }

    @Test
    void create_ConEntidadValida_DebeRetornar201_YPermitirConsultar() {
        PreguntaOpcion nueva = crearOpcion(ID_PREGUNTA_2, true);
        Response responseCreacion = post("opciones_respuesta", nueva);
        assertEquals(201, responseCreacion.getStatus());
        String location = responseCreacion.getHeaderString("Location");
        assertNotNull(location);
        String idString = location.substring(location.lastIndexOf('/') + 1);
        UUID idCreado = UUID.fromString(idString);
        Response responseConsulta = get("opciones_respuesta/" + idCreado);
        assertEquals(200, responseConsulta.getStatus());
        PreguntaOpcion creado = responseConsulta.readEntity(PreguntaOpcion.class);
        assertNotNull(creado);
        assertEquals(idCreado, creado.getId());
    }

    @Test
    void create_ConEntidadInvalida_SinPregunta_DebeRetornar422() {
        PreguntaOpcion nueva = new PreguntaOpcion();
        nueva.setIdRespuestaGlobal(new BancoRespuesta());
        nueva.setEsCorrecta(false);
        Response response = post("opciones_respuesta", nueva);
        assertEquals(422, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
    }

    private PreguntaOpcion crearOpcion(UUID idPregunta, Boolean esCorrecta) {
        PreguntaOpcion opcion = new PreguntaOpcion();
        BancoPregunta pregunta = new BancoPregunta();
        pregunta.setId(idPregunta);
        opcion.setIdPregunta(pregunta);
        BancoRespuesta br = new BancoRespuesta();
        opcion.setIdRespuestaGlobal(br);
        opcion.setEsCorrecta(esCorrecta);
        return opcion;
    }
}
