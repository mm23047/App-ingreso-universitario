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
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.BancoRespuestaDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PreguntaOpcionDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.BancoRespuesta;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BancoRespuestaResourceTest {

    @Mock
    private BancoRespuestaDAO bancoRespuestaDAO;

    @Mock
    private PreguntaOpcionDAO preguntaOpcionDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    @InjectMocks
    private BancoRespuestaResource resource;

    private BancoRespuesta entidad;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        entidad = new BancoRespuesta();
        entidad.setIdBancoRespuesta(testId);
        entidad.setTextoRespuesta("Respuesta de ejemplo");
    }

    // ==================== listRespuestas (GET /) ====================

    @Test
    void listRespuestas_ConParametrosValidos_DebeRetornar200ConLista() {
        when(bancoRespuestaDAO.findRange(0, 10)).thenReturn(List.of(entidad));
        when(bancoRespuestaDAO.count()).thenReturn(1);

        Response response = resource.listRespuestas(0, 10);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals("1", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
        verify(bancoRespuestaDAO).findRange(0, 10);
        verify(bancoRespuestaDAO).count();
    }

    @Test
    void listRespuestas_ConListaVacia_DebeRetornar200ConTotalCero() {
        when(bancoRespuestaDAO.findRange(0, 10)).thenReturn(Collections.emptyList());
        when(bancoRespuestaDAO.count()).thenReturn(0);

        Response response = resource.listRespuestas(0, 10);

        assertEquals(200, response.getStatus());
        assertEquals("0", response.getHeaderString(RestHeaders.TOTAL_RECORDS));
    }

    @Test
    void listRespuestas_ConExcepcionEnDAO_DebeRetornar500() {
        when(bancoRespuestaDAO.findRange(anyInt(), anyInt())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.listRespuestas(0, 10);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== createRespuestaGlobal (POST /) ====================

    @Test
    void createRespuestaGlobal_ConDatosValidos_DebeRetornar201() {
        BancoRespuesta nueva = new BancoRespuesta();
        nueva.setTextoRespuesta("Verdadero");

        doAnswer(inv -> {
            BancoRespuesta r = inv.getArgument(0);
            r.setIdBancoRespuesta(UUID.randomUUID());
            return null;
        }).when(bancoRespuestaDAO).crear(any(BancoRespuesta.class));

        when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create("http://localhost/respuestas_globales/1"));

        Response response = resource.createRespuestaGlobal(nueva, uriInfo);

        assertEquals(201, response.getStatus());
        assertNotNull(response.getEntity());
        assertNull(nueva.getAreaConocimiento());
        verify(bancoRespuestaDAO).crear(nueva);
    }

    @Test
    void createRespuestaGlobal_ConEntidadNula_DebeRetornar400() {
        Response response = resource.createRespuestaGlobal(null, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.MISSING_PARAMETER));
        verifyNoInteractions(bancoRespuestaDAO);
    }

    @Test
    void createRespuestaGlobal_SinTextoRespuesta_DebeRetornar400() {
        BancoRespuesta sinTexto = new BancoRespuesta();

        Response response = resource.createRespuestaGlobal(sinTexto, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.MISSING_PARAMETER));
        verifyNoInteractions(bancoRespuestaDAO);
    }

    @Test
    void createRespuestaGlobal_ConTextoEnBlanco_DebeRetornar400() {
        BancoRespuesta conBlancos = new BancoRespuesta();
        conBlancos.setTextoRespuesta("   ");

        Response response = resource.createRespuestaGlobal(conBlancos, uriInfo);

        assertEquals(400, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.MISSING_PARAMETER));
        verifyNoInteractions(bancoRespuestaDAO);
    }

    @Test
    void createRespuestaGlobal_ConTextoDuplicadoEnDAO_DebeRetornar409() {
        BancoRespuesta nueva = new BancoRespuesta();
        nueva.setTextoRespuesta("Verdadero");
        doThrow(new IllegalArgumentException("Ya existe una respuesta con ese texto"))
                .when(bancoRespuestaDAO).crear(any());

        Response response = resource.createRespuestaGlobal(nueva, uriInfo);

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
    }

    @Test
    void createRespuestaGlobal_ConExcepcionEnDAO_DebeRetornar500() {
        BancoRespuesta nueva = new BancoRespuesta();
        nueva.setTextoRespuesta("Verdadero");
        doThrow(new RuntimeException("Error de BD"))
                .when(bancoRespuestaDAO).crear(any());

        Response response = resource.createRespuestaGlobal(nueva, uriInfo);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== getRespuesta (GET /{idRespuesta}) ====================

    @Test
    void getRespuesta_ConIdExistente_DebeRetornar200() {
        when(bancoRespuestaDAO.leer(testId)).thenReturn(entidad);

        Response response = resource.getRespuesta(testId.toString());

        assertEquals(200, response.getStatus());
        assertSame(entidad, response.getEntity());
        verify(bancoRespuestaDAO).leer(testId);
    }

    @Test
    void getRespuesta_ConIdInexistente_DebeRetornar404() {
        when(bancoRespuestaDAO.leer(testId)).thenReturn(null);

        Response response = resource.getRespuesta(testId.toString());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
    }

    @Test
    void getRespuesta_ConUuidInvalido_DebeRetornar400() {
        Response response = resource.getRespuesta("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(bancoRespuestaDAO);
    }

    @Test
    void getRespuesta_ConExcepcionEnDAO_DebeRetornar500() {
        when(bancoRespuestaDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.getRespuesta(testId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== updateRespuesta (PUT /{idRespuesta}) ====================

    @Test
    void updateRespuesta_ConDatosValidos_DebeRetornar200() {
        when(bancoRespuestaDAO.leer(testId)).thenReturn(entidad);
        BancoRespuesta actualizada = new BancoRespuesta();
        actualizada.setTextoRespuesta("Texto actualizado");
        when(bancoRespuestaDAO.actualizar(actualizada)).thenReturn(actualizada);

        Response response = resource.updateRespuesta(testId.toString(), actualizada);

        assertEquals(200, response.getStatus());
        assertSame(actualizada, response.getEntity());
        assertEquals(testId, actualizada.getIdBancoRespuesta());
        verify(bancoRespuestaDAO).actualizar(actualizada);
    }

    @Test
    void updateRespuesta_ConIdInexistente_DebeRetornar404() {
        when(bancoRespuestaDAO.leer(testId)).thenReturn(null);

        Response response = resource.updateRespuesta(testId.toString(), entidad);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
        verify(bancoRespuestaDAO, never()).actualizar(any());
    }

    @Test
    void updateRespuesta_ConUuidInvalido_DebeRetornar409() {
        Response response = resource.updateRespuesta("no-es-uuid", entidad);

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
        verifyNoInteractions(bancoRespuestaDAO);
    }

    @Test
    void updateRespuesta_ConIllegalArgumentEnActualizar_DebeRetornar409() {
        when(bancoRespuestaDAO.leer(testId)).thenReturn(entidad);
        BancoRespuesta datos = new BancoRespuesta();
        datos.setTextoRespuesta("Duplicado");
        when(bancoRespuestaDAO.actualizar(datos))
                .thenThrow(new IllegalArgumentException("Ya existe una respuesta con ese texto"));

        Response response = resource.updateRespuesta(testId.toString(), datos);

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
    }

    @Test
    void updateRespuesta_ConExcepcionEnLeer_DebeRetornar500() {
        when(bancoRespuestaDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.updateRespuesta(testId.toString(), entidad);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    @Test
    void updateRespuesta_ConExcepcionEnActualizar_DebeRetornar500() {
        when(bancoRespuestaDAO.leer(testId)).thenReturn(entidad);
        BancoRespuesta datos = new BancoRespuesta();
        datos.setTextoRespuesta("Nuevo texto");
        when(bancoRespuestaDAO.actualizar(datos))
                .thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.updateRespuesta(testId.toString(), datos);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== deleteRespuesta (DELETE /{idRespuesta}) ====================

    @Test
    void deleteRespuesta_ConIdExistente_DebeRetornar204() {
        when(bancoRespuestaDAO.leer(testId)).thenReturn(entidad);

        Response response = resource.deleteRespuesta(testId.toString());

        assertEquals(204, response.getStatus());
        verify(bancoRespuestaDAO).eliminar(entidad);
    }

    @Test
    void deleteRespuesta_ConIdInexistente_DebeRetornar404() {
        when(bancoRespuestaDAO.leer(testId)).thenReturn(null);

        Response response = resource.deleteRespuesta(testId.toString());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
        verify(bancoRespuestaDAO, never()).eliminar(any());
    }

    @Test
    void deleteRespuesta_ConUuidInvalido_DebeRetornar500() {
        Response response = resource.deleteRespuesta("no-es-uuid");

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
        verifyNoInteractions(bancoRespuestaDAO);
    }

    @Test
    void deleteRespuesta_ConExcepcionEnDAO_DebeRetornar500() {
        when(bancoRespuestaDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.deleteRespuesta(testId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }
}
