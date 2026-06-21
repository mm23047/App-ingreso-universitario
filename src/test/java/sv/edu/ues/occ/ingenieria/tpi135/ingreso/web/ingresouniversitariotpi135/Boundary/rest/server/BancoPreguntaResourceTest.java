package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
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

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BancoPreguntaResourceTest {

    @Mock
    private BancoPreguntaDAO bancoPreguntaDAO;

    @Mock
    private PreguntaOpcionDAO preguntaOpcionDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

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
    void listPreguntas_ConParametrosValidos_DebeRetornar200ConLista() {
        when(bancoPreguntaDAO.count()).thenReturn(1);
        when(bancoPreguntaDAO.findRange(0, 10)).thenReturn(List.of(entidad));

        Response response = resource.listPreguntas(0, 10);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals("1", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
        verify(bancoPreguntaDAO).findRange(0, 10);
        verify(bancoPreguntaDAO).count();
    }

    @Test
    void listPreguntas_ConListaVacia_DebeRetornar200ConTotalCero() {
        when(bancoPreguntaDAO.count()).thenReturn(0);
        when(bancoPreguntaDAO.findRange(0, 10)).thenReturn(Collections.emptyList());

        Response response = resource.listPreguntas(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void listPreguntas_ConExcepcionEnDAO_DebeRetornar500() {
        when(bancoPreguntaDAO.count()).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.listPreguntas(0, 10);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== getPregunta (GET /{idPregunta}) ====================

    @Test
    void getPregunta_ConIdExistente_DebeRetornar200() {
        when(bancoPreguntaDAO.leer(testId)).thenReturn(entidad);

        Response response = resource.getPregunta(testId.toString());

        assertEquals(200, response.getStatus());
        assertSame(entidad, response.getEntity());
        verify(bancoPreguntaDAO).leer(testId);
    }

    @Test
    void getPregunta_ConIdInexistente_DebeRetornar404() {
        when(bancoPreguntaDAO.leer(testId)).thenReturn(null);

        Response response = resource.getPregunta(testId.toString());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
    }

    @Test
    void getPregunta_ConUuidInvalido_DebeRetornar400() {
        Response response = resource.getPregunta("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(bancoPreguntaDAO);
    }

    @Test
    void getPregunta_ConExcepcionEnDAO_DebeRetornar500() {
        when(bancoPreguntaDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.getPregunta(testId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== updatePregunta (PUT /{idPregunta}) ====================

    @Test
    void updatePregunta_ConDatosValidos_DebeRetornar200() {
        when(bancoPreguntaDAO.leer(testId)).thenReturn(entidad);
        BancoPregunta actualizada = new BancoPregunta();
        actualizada.setEnunciado("¿Cuánto es 3+3?");
        when(bancoPreguntaDAO.actualizar(actualizada)).thenReturn(actualizada);

        Response response = resource.updatePregunta(testId.toString(), actualizada);

        assertEquals(200, response.getStatus());
        assertSame(actualizada, response.getEntity());
        assertEquals(testId, actualizada.getIdBancoPregunta());
        verify(bancoPreguntaDAO).actualizar(actualizada);
    }

    @Test
    void updatePregunta_ConIdInexistente_DebeRetornar404() {
        when(bancoPreguntaDAO.leer(testId)).thenReturn(null);

        Response response = resource.updatePregunta(testId.toString(), entidad);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
        verify(bancoPreguntaDAO, never()).actualizar(any());
    }

    @Test
    void updatePregunta_ConUuidInvalido_DebeRetornar409() {
        Response response = resource.updatePregunta("no-es-uuid", entidad);

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
        verifyNoInteractions(bancoPreguntaDAO);
    }

    @Test
    void updatePregunta_ConIllegalArgumentEnActualizar_DebeRetornar409() {
        when(bancoPreguntaDAO.leer(testId)).thenReturn(entidad);
        BancoPregunta datos = new BancoPregunta();
        datos.setEnunciado("Duplicada");
        when(bancoPreguntaDAO.actualizar(datos))
                .thenThrow(new IllegalArgumentException("Ya existe una pregunta con ese enunciado"));

        Response response = resource.updatePregunta(testId.toString(), datos);

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
    }

    @Test
    void updatePregunta_ConExcepcionEnLeer_DebeRetornar500() {
        when(bancoPreguntaDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.updatePregunta(testId.toString(), entidad);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    @Test
    void updatePregunta_ConExcepcionEnActualizar_DebeRetornar500() {
        when(bancoPreguntaDAO.leer(testId)).thenReturn(entidad);
        BancoPregunta datos = new BancoPregunta();
        datos.setEnunciado("Nuevo enunciado");
        when(bancoPreguntaDAO.actualizar(datos)).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.updatePregunta(testId.toString(), datos);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== deletePregunta (DELETE /{idPregunta}) ====================

    @Test
    void deletePregunta_SinOpciones_DebeRetornar204() {
        when(bancoPreguntaDAO.leer(testId)).thenReturn(entidad);
        when(preguntaOpcionDAO.findByPregunta(testId)).thenReturn(Collections.emptyList());

        Response response = resource.deletePregunta(testId.toString());

        assertEquals(204, response.getStatus());
        verify(bancoPreguntaDAO).eliminar(entidad);
    }

    @Test
    void deletePregunta_ConOpcionesAsociadas_DebeRetornar409() {
        when(bancoPreguntaDAO.leer(testId)).thenReturn(entidad);
        when(preguntaOpcionDAO.findByPregunta(testId)).thenReturn(List.of(new PreguntaOpcion()));

        Response response = resource.deletePregunta(testId.toString());

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
        verify(bancoPreguntaDAO, never()).eliminar(any());
    }

    @Test
    void deletePregunta_ConIdInexistente_DebeRetornar404() {
        when(bancoPreguntaDAO.leer(testId)).thenReturn(null);

        Response response = resource.deletePregunta(testId.toString());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
    }

    @Test
    void deletePregunta_ConUuidInvalido_DebeRetornar500() {
        Response response = resource.deletePregunta("no-es-uuid");

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
        verifyNoInteractions(bancoPreguntaDAO);
    }

    @Test
    void deletePregunta_ConExcepcionEnDAO_DebeRetornar500() {
        when(bancoPreguntaDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.deletePregunta(testId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== getOpcionesOfPregunta (GET /{idPregunta}/opciones) ====================

    @Test
    void getOpciones_ConPreguntaExistenteConOpciones_DebeRetornar200() {
        when(bancoPreguntaDAO.leer(testId)).thenReturn(entidad);
        PreguntaOpcion op1 = new PreguntaOpcion();
        op1.setIdPreguntaOpcion(UUID.randomUUID());
        PreguntaOpcion op2 = new PreguntaOpcion();
        op2.setIdPreguntaOpcion(UUID.randomUUID());
        when(preguntaOpcionDAO.findByPregunta(testId)).thenReturn(List.of(op1, op2));

        Response response = resource.getOpcionesOfPregunta(testId.toString(), 0, 50);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals("2", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void getOpciones_ConPreguntaExistenteSinOpciones_DebeRetornar200ConListaVacia() {
        when(bancoPreguntaDAO.leer(testId)).thenReturn(entidad);
        when(preguntaOpcionDAO.findByPregunta(testId)).thenReturn(Collections.emptyList());

        Response response = resource.getOpcionesOfPregunta(testId.toString(), 0, 50);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void getOpciones_ConPaginacion_DebeRetornarSubLista() {
        when(bancoPreguntaDAO.leer(testId)).thenReturn(entidad);
        PreguntaOpcion op1 = new PreguntaOpcion();
        PreguntaOpcion op2 = new PreguntaOpcion();
        PreguntaOpcion op3 = new PreguntaOpcion();
        when(preguntaOpcionDAO.findByPregunta(testId)).thenReturn(List.of(op1, op2, op3));

        Response response = resource.getOpcionesOfPregunta(testId.toString(), 1, 1);

        assertEquals(200, response.getStatus());
        List<?> resultado = (List<?>) response.getEntity();
        assertEquals(1, resultado.size());
        assertEquals("3", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void getOpciones_ConPreguntaInexistente_DebeRetornar404() {
        when(bancoPreguntaDAO.leer(testId)).thenReturn(null);

        Response response = resource.getOpcionesOfPregunta(testId.toString(), 0, 50);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
        verifyNoInteractions(preguntaOpcionDAO);
    }

    @Test
    void getOpciones_ConUuidInvalido_DebeRetornar400() {
        Response response = resource.getOpcionesOfPregunta("no-es-uuid", 0, 50);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(bancoPreguntaDAO, preguntaOpcionDAO);
    }

    @Test
    void getOpciones_ConExcepcionEnDAO_DebeRetornar500() {
        when(bancoPreguntaDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.getOpcionesOfPregunta(testId.toString(), 0, 50);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== createOpcionInPregunta (POST /{idPregunta}/opciones) ====================

    @Test
    void createOpcion_ConDatosValidos_DebeRetornar201() {
        BancoRespuesta respuesta = new BancoRespuesta();
        respuesta.setIdBancoRespuesta(UUID.randomUUID());
        PreguntaOpcion opcion = new PreguntaOpcion();
        opcion.setIdRespuestaGlobal(respuesta);

        when(bancoPreguntaDAO.leer(testId)).thenReturn(entidad);
        doAnswer(inv -> {
            PreguntaOpcion op = inv.getArgument(0);
            op.setIdPreguntaOpcion(UUID.randomUUID());
            return null;
        }).when(preguntaOpcionDAO).crear(any(PreguntaOpcion.class));
        when(uriInfo.getBaseUriBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build(any())).thenReturn(URI.create("http://localhost/opciones/1"));

        Response response = resource.createOpcionInPregunta(testId.toString(), opcion, uriInfo);

        assertEquals(201, response.getStatus());
        assertNotNull(response.getEntity());
        verify(preguntaOpcionDAO).crear(opcion);
        assertSame(entidad, opcion.getBancoPregunta());
    }

    @Test
    void createOpcion_ConOpcionNula_DebeRetornar400() {
        Response response = resource.createOpcionInPregunta(testId.toString(), null, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.MISSING_PARAMETER));
        verifyNoInteractions(bancoPreguntaDAO, preguntaOpcionDAO);
    }

    @Test
    void createOpcion_SinRespuestaGlobal_DebeRetornar400() {
        PreguntaOpcion sinRespuesta = new PreguntaOpcion();

        Response response = resource.createOpcionInPregunta(testId.toString(), sinRespuesta, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.MISSING_PARAMETER));
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
        verify(preguntaOpcionDAO, never()).crear(any());
    }

    @Test
    void createOpcion_ConUuidPreguntaInvalido_DebeRetornar409() {
        BancoRespuesta respuesta = new BancoRespuesta();
        respuesta.setIdBancoRespuesta(UUID.randomUUID());
        PreguntaOpcion opcion = new PreguntaOpcion();
        opcion.setIdRespuestaGlobal(respuesta);

        Response response = resource.createOpcionInPregunta("no-es-uuid", opcion, uriInfo);

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
        verifyNoInteractions(bancoPreguntaDAO, preguntaOpcionDAO);
    }

    @Test
    void createOpcion_ConIllegalArgumentEnDAO_DebeRetornar409() {
        BancoRespuesta respuesta = new BancoRespuesta();
        respuesta.setIdBancoRespuesta(UUID.randomUUID());
        PreguntaOpcion opcion = new PreguntaOpcion();
        opcion.setIdRespuestaGlobal(respuesta);
        when(bancoPreguntaDAO.leer(testId)).thenReturn(entidad);
        doThrow(new IllegalArgumentException("Opción duplicada"))
                .when(preguntaOpcionDAO).crear(any());

        Response response = resource.createOpcionInPregunta(testId.toString(), opcion, uriInfo);

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
    }

    @Test
    void createOpcion_ConExcepcionEnDAO_DebeRetornar500() {
        BancoRespuesta respuesta = new BancoRespuesta();
        respuesta.setIdBancoRespuesta(UUID.randomUUID());
        PreguntaOpcion opcion = new PreguntaOpcion();
        opcion.setIdRespuestaGlobal(respuesta);
        when(bancoPreguntaDAO.leer(testId)).thenReturn(entidad);
        doThrow(new RuntimeException("Error de BD"))
                .when(preguntaOpcionDAO).crear(any());

        Response response = resource.createOpcionInPregunta(testId.toString(), opcion, uriInfo);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }
}
