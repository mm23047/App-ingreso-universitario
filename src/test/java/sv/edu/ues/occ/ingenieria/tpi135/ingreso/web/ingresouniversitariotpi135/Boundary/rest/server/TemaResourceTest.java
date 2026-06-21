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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.TemaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoPregunta;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.Tema;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TemaResourceTest {

    @Mock
    private TemaDAO temaDAO;

    @Mock
    private BancoPreguntaDAO bancoPreguntaDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    @InjectMocks
    private TemaResource resource;

    private Tema entidad;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        entidad = new Tema();
        entidad.setIdTema(testId);
        entidad.setNombreTema("Álgebra Lineal");
    }

    // ==================== listTemas (GET /) ====================

    @Test
    void listTemas_ConParametrosValidos_DebeRetornar200ConLista() {
        when(temaDAO.count()).thenReturn(1);
        when(temaDAO.findRange(0, 10)).thenReturn(List.of(entidad));

        Response response = resource.listTemas(0, 10);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals("1", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
        verify(temaDAO).findRange(0, 10);
        verify(temaDAO).count();
    }

    @Test
    void listTemas_ConListaVacia_DebeRetornar200ConTotalCero() {
        when(temaDAO.count()).thenReturn(0);
        when(temaDAO.findRange(0, 10)).thenReturn(Collections.emptyList());

        Response response = resource.listTemas(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void listTemas_ConExcepcionEnDAO_DebeRetornar500() {
        when(temaDAO.count()).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.listTemas(0, 10);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== getTema (GET /{idTema}) ====================

    @Test
    void getTema_ConIdExistente_DebeRetornar200() {
        when(temaDAO.leer(testId)).thenReturn(entidad);

        Response response = resource.getTema(testId.toString());

        assertEquals(200, response.getStatus());
        assertSame(entidad, response.getEntity());
        verify(temaDAO).leer(testId);
    }

    @Test
    void getTema_ConIdInexistente_DebeRetornar404() {
        when(temaDAO.leer(testId)).thenReturn(null);

        Response response = resource.getTema(testId.toString());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
    }

    @Test
    void getTema_ConUuidInvalido_DebeRetornar400() {
        Response response = resource.getTema("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(temaDAO);
    }

    @Test
    void getTema_ConExcepcionEnDAO_DebeRetornar500() {
        when(temaDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.getTema(testId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== updateTema (PUT /{idTema}) ====================

    @Test
    void updateTema_ConDatosValidos_DebeRetornar200() {
        when(temaDAO.leer(testId)).thenReturn(entidad);
        Tema actualizado = new Tema();
        actualizado.setNombreTema("Álgebra Avanzada");
        when(temaDAO.actualizar(actualizado)).thenReturn(actualizado);

        Response response = resource.updateTema(testId.toString(), actualizado);

        assertEquals(200, response.getStatus());
        assertSame(actualizado, response.getEntity());
        assertEquals(testId, actualizado.getIdTema());
        verify(temaDAO).actualizar(actualizado);
    }

    @Test
    void updateTema_ConIdInexistente_DebeRetornar404() {
        when(temaDAO.leer(testId)).thenReturn(null);

        Response response = resource.updateTema(testId.toString(), entidad);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
        verify(temaDAO, never()).actualizar(any());
    }

    @Test
    void updateTema_ConUuidInvalido_DebeRetornar409() {
        Response response = resource.updateTema("no-es-uuid", entidad);

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
        verifyNoInteractions(temaDAO);
    }

    @Test
    void updateTema_ConIllegalArgumentEnActualizar_DebeRetornar409() {
        when(temaDAO.leer(testId)).thenReturn(entidad);
        Tema conCiclo = new Tema();
        conCiclo.setNombreTema("Tema con ciclo");
        when(temaDAO.actualizar(conCiclo))
                .thenThrow(new IllegalArgumentException("Ciclo detectado en jerarquía"));

        Response response = resource.updateTema(testId.toString(), conCiclo);

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
    }

    @Test
    void updateTema_ConExcepcionEnLeer_DebeRetornar500() {
        when(temaDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.updateTema(testId.toString(), entidad);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    @Test
    void updateTema_ConExcepcionEnActualizar_DebeRetornar500() {
        when(temaDAO.leer(testId)).thenReturn(entidad);
        Tema datos = new Tema();
        datos.setNombreTema("Nuevo nombre");
        when(temaDAO.actualizar(datos)).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.updateTema(testId.toString(), datos);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== deleteTema (DELETE /{idTema}) ====================

    @Test
    void deleteTema_SinPreguntasNiHijos_DebeRetornar204() {
        when(temaDAO.leer(testId)).thenReturn(entidad);
        when(bancoPreguntaDAO.findByTema(testId)).thenReturn(Collections.emptyList());
        when(temaDAO.findByTemaPadre(testId)).thenReturn(Collections.emptyList());

        Response response = resource.deleteTema(testId.toString());

        assertEquals(204, response.getStatus());
        verify(temaDAO).eliminar(entidad);
    }

    @Test
    void deleteTema_ConPreguntasAsociadas_DebeRetornar409() {
        when(temaDAO.leer(testId)).thenReturn(entidad);
        BancoPregunta pregunta = new BancoPregunta();
        when(bancoPreguntaDAO.findByTema(testId)).thenReturn(List.of(pregunta));

        Response response = resource.deleteTema(testId.toString());

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
        verify(temaDAO, never()).eliminar(any());
    }

    @Test
    void deleteTema_ConSubtemasAsociados_DebeRetornar409() {
        when(temaDAO.leer(testId)).thenReturn(entidad);
        when(bancoPreguntaDAO.findByTema(testId)).thenReturn(Collections.emptyList());
        Tema hijo = new Tema();
        hijo.setNombreTema("Subtema");
        when(temaDAO.findByTemaPadre(testId)).thenReturn(List.of(hijo));

        Response response = resource.deleteTema(testId.toString());

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
        verify(temaDAO, never()).eliminar(any());
    }

    @Test
    void deleteTema_ConIdInexistente_DebeRetornar404() {
        when(temaDAO.leer(testId)).thenReturn(null);

        Response response = resource.deleteTema(testId.toString());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
        verify(temaDAO, never()).eliminar(any());
    }

    @Test
    void deleteTema_ConUuidInvalido_DebeRetornar500() {
        Response response = resource.deleteTema("no-es-uuid");

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
        verifyNoInteractions(temaDAO);
    }

    @Test
    void deleteTema_ConExcepcionEnDAO_DebeRetornar500() {
        when(temaDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.deleteTema(testId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== getPreguntasByTema (GET /{idTema}/preguntas) ====================

    @Test
    void getPreguntasByTema_ConTemaExistenteConPreguntas_DebeRetornar200() {
        when(temaDAO.leer(testId)).thenReturn(entidad);
        BancoPregunta p1 = new BancoPregunta();
        p1.setIdBancoPregunta(UUID.randomUUID());
        BancoPregunta p2 = new BancoPregunta();
        p2.setIdBancoPregunta(UUID.randomUUID());
        when(bancoPreguntaDAO.findByTema(testId)).thenReturn(List.of(p1, p2));

        Response response = resource.getPreguntasByTema(testId.toString(), 0, 50);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals("2", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void getPreguntasByTema_ConTemaExistenteSinPreguntas_DebeRetornar200ConListaVacia() {
        when(temaDAO.leer(testId)).thenReturn(entidad);
        when(bancoPreguntaDAO.findByTema(testId)).thenReturn(Collections.emptyList());

        Response response = resource.getPreguntasByTema(testId.toString(), 0, 50);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void getPreguntasByTema_ConPaginacion_DebeRetornarSubLista() {
        when(temaDAO.leer(testId)).thenReturn(entidad);
        BancoPregunta p1 = new BancoPregunta();
        BancoPregunta p2 = new BancoPregunta();
        BancoPregunta p3 = new BancoPregunta();
        when(bancoPreguntaDAO.findByTema(testId)).thenReturn(List.of(p1, p2, p3));

        Response response = resource.getPreguntasByTema(testId.toString(), 1, 1);

        assertEquals(200, response.getStatus());
        List<?> resultado = (List<?>) response.getEntity();
        assertEquals(1, resultado.size());
        assertEquals("3", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void getPreguntasByTema_ConTemaInexistente_DebeRetornar404() {
        when(temaDAO.leer(testId)).thenReturn(null);

        Response response = resource.getPreguntasByTema(testId.toString(), 0, 50);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
        verifyNoInteractions(bancoPreguntaDAO);
    }

    @Test
    void getPreguntasByTema_ConUuidInvalido_DebeRetornar400() {
        Response response = resource.getPreguntasByTema("no-es-uuid", 0, 50);

        assertEquals(400, response.getStatus());
        verifyNoInteractions(temaDAO, bancoPreguntaDAO);
    }

    @Test
    void getPreguntasByTema_ConExcepcionEnDAO_DebeRetornar500() {
        when(temaDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.getPreguntasByTema(testId.toString(), 0, 50);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== createPreguntaInTema (POST /{idTema}/preguntas) ====================

    @Test
    void createPreguntaInTema_ConDatosValidos_DebeRetornar201() {
        BancoPregunta pregunta = new BancoPregunta();
        pregunta.setEnunciado("¿Cuánto es 2+2?");

        when(temaDAO.leer(testId)).thenReturn(entidad);
        doAnswer(inv -> {
            BancoPregunta p = inv.getArgument(0);
            p.setIdBancoPregunta(UUID.randomUUID());
            return null;
        }).when(bancoPreguntaDAO).crear(any(BancoPregunta.class));

        when(uriInfo.getBaseUriBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build(any())).thenReturn(URI.create("http://localhost/preguntas/" + UUID.randomUUID()));

        Response response = resource.createPreguntaInTema(testId.toString(), pregunta, uriInfo);

        assertEquals(201, response.getStatus());
        assertNotNull(response.getEntity());
        verify(bancoPreguntaDAO).crear(pregunta);
        assertSame(entidad, pregunta.getTema());
    }

    @Test
    void createPreguntaInTema_ConPreguntaNula_DebeRetornar400() {
        Response response = resource.createPreguntaInTema(testId.toString(), null, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.MISSING_PARAMETER));
        verifyNoInteractions(temaDAO, bancoPreguntaDAO);
    }

    @Test
    void createPreguntaInTema_SinEnunciado_DebeRetornar400() {
        BancoPregunta sinEnunciado = new BancoPregunta();

        Response response = resource.createPreguntaInTema(testId.toString(), sinEnunciado, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.MISSING_PARAMETER));
        verifyNoInteractions(temaDAO, bancoPreguntaDAO);
    }

    @Test
    void createPreguntaInTema_ConEnunciadoEnBlanco_DebeRetornar400() {
        BancoPregunta conBlancos = new BancoPregunta();
        conBlancos.setEnunciado("   ");

        Response response = resource.createPreguntaInTema(testId.toString(), conBlancos, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.MISSING_PARAMETER));
        verifyNoInteractions(temaDAO, bancoPreguntaDAO);
    }

    @Test
    void createPreguntaInTema_ConTemaInexistente_DebeRetornar404() {
        BancoPregunta pregunta = new BancoPregunta();
        pregunta.setEnunciado("¿Pregunta válida?");
        when(temaDAO.leer(testId)).thenReturn(null);

        Response response = resource.createPreguntaInTema(testId.toString(), pregunta, uriInfo);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
        verify(bancoPreguntaDAO, never()).crear(any());
    }

    @Test
    void createPreguntaInTema_ConUuidTemaInvalido_DebeRetornar409() {
        BancoPregunta pregunta = new BancoPregunta();
        pregunta.setEnunciado("¿Pregunta válida?");

        Response response = resource.createPreguntaInTema("no-es-uuid", pregunta, uriInfo);

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
        verifyNoInteractions(temaDAO, bancoPreguntaDAO);
    }

    @Test
    void createPreguntaInTema_ConIllegalArgumentEnDAO_DebeRetornar409() {
        BancoPregunta pregunta = new BancoPregunta();
        pregunta.setEnunciado("¿Pregunta duplicada?");
        when(temaDAO.leer(testId)).thenReturn(entidad);
        doThrow(new IllegalArgumentException("Ya existe una pregunta con ese enunciado"))
                .when(bancoPreguntaDAO).crear(any());

        Response response = resource.createPreguntaInTema(testId.toString(), pregunta, uriInfo);

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
    }

    @Test
    void createPreguntaInTema_ConExcepcionEnDAO_DebeRetornar500() {
        BancoPregunta pregunta = new BancoPregunta();
        pregunta.setEnunciado("¿Pregunta válida?");
        when(temaDAO.leer(testId)).thenReturn(entidad);
        doThrow(new RuntimeException("Error de BD"))
                .when(bancoPreguntaDAO).crear(any());

        Response response = resource.createPreguntaInTema(testId.toString(), pregunta, uriInfo);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }
}
