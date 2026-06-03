package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.BancoPreguntaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PreguntaOpcionDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoRespuesta;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntaOpcion;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para BancoPreguntaResource.
 * NOTA: BancoPreguntaResource NO expone un endpoint POST al raíz (/preguntas).
 * Las preguntas se crean bajo un área mediante el endpoint de AreasConocimientoResource.
 */
@ExtendWith(MockitoExtension.class)
class BancoPreguntaResourceTest {

    @Mock
    private BancoPreguntaDAO bancoPreguntaDAO;

    @Mock
    private PreguntaOpcionDAO preguntaOpcionDAO;

    @InjectMocks
    private BancoPreguntaResource resource;

    private BancoPregunta entidad;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        entidad = new BancoPregunta();
        entidad.setIdBancoPregunta(testId);
        entidad.setEnunciado("¿Cuánto es 2+2?");
    }

    // ==================== listPreguntas (GET /) ====================

    @Test
    void findRange_ConParametrosValidos_DebeRetornar200ConLista() {
        when(bancoPreguntaDAO.count()).thenReturn(1);
        when(bancoPreguntaDAO.findRange(0, 10)).thenReturn(List.of(entidad));

        Response response = resource.listPreguntas(0, 10);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals("1", response.getHeaderString("Total-records"));
        verify(bancoPreguntaDAO).count();
        verify(bancoPreguntaDAO).findRange(0, 10);
    }

    @Test
    void findRange_ConListaVacia_DebeRetornar200ConListaVacia() {
        when(bancoPreguntaDAO.count()).thenReturn(0);
        when(bancoPreguntaDAO.findRange(0, 10)).thenReturn(Collections.emptyList());

        Response response = resource.listPreguntas(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString("Total-records"));
    }

    @Test
    void findRange_ConExcepcionEnDAO_DebeRetornar500() {
        when(bancoPreguntaDAO.count()).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.listPreguntas(0, 10);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== getPregunta (GET /{idPregunta}) ====================

    @Test
    void findById_ConIdExistente_DebeRetornar200ConEntidad() {
        when(bancoPreguntaDAO.leer(testId)).thenReturn(entidad);

        Response response = resource.getPregunta(testId.toString());

        assertEquals(200, response.getStatus());
        BancoPregunta resultado = (BancoPregunta) response.getEntity();
        assertEquals(testId, resultado.getIdBancoPregunta());
        verify(bancoPreguntaDAO).leer(testId);
    }

    @Test
    void findById_ConIdInexistente_DebeRetornar404() {
        when(bancoPreguntaDAO.leer(testId)).thenReturn(null);

        Response response = resource.getPregunta(testId.toString());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void findById_ConIdFormatoInvalido_DebeRetornar400() {
        Response response = resource.getPregunta("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(bancoPreguntaDAO);
    }

    @Test
    void findById_ConExcepcionEnDAO_DebeRetornar500() {
        when(bancoPreguntaDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.getPregunta(testId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== updatePregunta (PUT /{idPregunta}) ====================

    @Test
    void update_ConIdYEntidadValidos_DebeRetornar200() {
        when(bancoPreguntaDAO.leer(testId)).thenReturn(entidad);
        BancoPregunta actualizada = new BancoPregunta();
        actualizada.setEnunciado("¿Cuánto es 3+3?");

        Response response = resource.updatePregunta(testId.toString(), actualizada);

        assertEquals(200, response.getStatus());
        verify(bancoPreguntaDAO).actualizar(actualizada);
    }

    @Test
    void update_ConIdFormatoInvalido_DebeRetornar409() {
        // updatePregunta captura IllegalArgumentException (UUID inválido) → CONFLICT 409
        Response response = resource.updatePregunta("no-es-uuid", entidad);

        assertEquals(409, response.getStatus());
        verifyNoInteractions(bancoPreguntaDAO);
    }

    @Test
    void update_ConIdInexistente_DebeRetornar404() {
        when(bancoPreguntaDAO.leer(testId)).thenReturn(null);

        Response response = resource.updatePregunta(testId.toString(), entidad);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void update_ConExcepcionEnDAO_DebeRetornar500() {
        when(bancoPreguntaDAO.leer(testId)).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.updatePregunta(testId.toString(), entidad);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== deletePregunta (DELETE /{idPregunta}) ====================

    @Test
    void delete_ConIdExistente_SinOpciones_DebeRetornar204() {
        when(bancoPreguntaDAO.leer(testId)).thenReturn(entidad);
        when(preguntaOpcionDAO.findByPregunta(testId)).thenReturn(Collections.emptyList());

        Response response = resource.deletePregunta(testId.toString());

        assertEquals(204, response.getStatus());
        verify(bancoPreguntaDAO).eliminar(entidad);
    }

    @Test
    void delete_ConIdExistente_ConOpciones_DebeRetornar409() {
        when(bancoPreguntaDAO.leer(testId)).thenReturn(entidad);
        when(preguntaOpcionDAO.findByPregunta(testId)).thenReturn(List.of(
            new sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntaOpcion()
        ));

        Response response = resource.deletePregunta(testId.toString());

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString("Conflict-reason"));
        verify(bancoPreguntaDAO, never()).eliminar(any());
    }

    @Test
    void delete_ConIdFormatoInvalido_DebeRetornar500() {
        // UUID.fromString("no-es-uuid") lanza IAE capturada por el handler de Exception → 500
        Response response = resource.deletePregunta("no-es-uuid");

        assertEquals(500, response.getStatus());
        verifyNoInteractions(bancoPreguntaDAO);
    }

    @Test
    void delete_ConIdInexistente_DebeRetornar404() {
        when(bancoPreguntaDAO.leer(testId)).thenReturn(null);

        Response response = resource.deletePregunta(testId.toString());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString("Not-found-id"));
    }

    @Test
    void delete_ConExcepcionEnDAO_DebeRetornar500() {
        when(bancoPreguntaDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.deletePregunta(testId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString("Server-exception"));
    }

    // ==================== createOpcionInPregunta (POST /{idPregunta}/opciones) ====================

    @Mock private UriInfo uriInfo;
    @Mock private UriBuilder uriBuilder;

    @Test
    void createOpcion_ConOpcionValida_DebeRetornar201() {
        UUID opcionId = UUID.randomUUID();
        BancoRespuesta respuesta = new BancoRespuesta();
        respuesta.setIdBancoRespuesta(UUID.randomUUID());
        PreguntaOpcion opcion = new PreguntaOpcion();
        opcion.setIdRespuestaGlobal(respuesta);

        when(bancoPreguntaDAO.leer(testId)).thenReturn(entidad);
        doAnswer(inv -> {
            PreguntaOpcion op = inv.getArgument(0);
            op.setIdPreguntaOpcion(opcionId);
            return null;
        }).when(preguntaOpcionDAO).crear(any(PreguntaOpcion.class));
        when(uriInfo.getBaseUriBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build(any())).thenReturn(URI.create("http://localhost/opciones/" + opcionId));

        Response response = resource.createOpcionInPregunta(testId.toString(), opcion, uriInfo);

        assertEquals(201, response.getStatus());
        verify(preguntaOpcionDAO).crear(opcion);
        assertEquals(entidad, opcion.getBancoPregunta());
    }

    @Test
    void createOpcion_ConOpcionNula_DebeRetornar400() {
        Response response = resource.createOpcionInPregunta(testId.toString(), null, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(preguntaOpcionDAO);
    }

    @Test
    void createOpcion_SinRespuestaGlobal_DebeRetornar400() {
        PreguntaOpcion opcionSinRespuesta = new PreguntaOpcion();

        Response response = resource.createOpcionInPregunta(testId.toString(), opcionSinRespuesta, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString("Missing-parameter"));
        verifyNoInteractions(bancoPreguntaDAO, preguntaOpcionDAO);
    }

    @Test
    void createOpcion_ConPreguntaInexistente_DebeRetornar404() {
        BancoRespuesta respuesta = new BancoRespuesta();
        respuesta.setIdBancoRespuesta(UUID.randomUUID());
        PreguntaOpcion opcion = new PreguntaOpcion();
        opcion.setIdRespuestaGlobal(respuesta);

        when(bancoPreguntaDAO.leer(testId)).thenReturn(null);

        Response response = resource.createOpcionInPregunta(testId.toString(), opcion, uriInfo);

        assertEquals(404, response.getStatus());
        verifyNoInteractions(preguntaOpcionDAO);
    }
}
