package sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Boundary.rest.server;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Control.PreguntaOpcionDAO;
import sv.edu.ues.occ.ingenieria.tpi135.ingreso.web.ingresouniversitariotpi135.Entity.PreguntaOpcion;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PreguntaOpcionResourceTest {

    @Mock
    private PreguntaOpcionDAO preguntaOpcionDAO;

    @InjectMocks
    private PreguntaOpcionResource resource;

    private PreguntaOpcion entidad;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        entidad = new PreguntaOpcion();
        entidad.setIdPreguntaOpcion(testId);
        entidad.setEsCorrecta(false);
    }

    // ==================== getOpcion (GET /{idOpcion}) ====================

    @Test
    void getOpcion_ConIdExistente_DebeRetornar200() {
        when(preguntaOpcionDAO.leer(testId)).thenReturn(entidad);

        Response response = resource.getOpcion(testId.toString());

        assertEquals(200, response.getStatus());
        assertSame(entidad, response.getEntity());
        verify(preguntaOpcionDAO).leer(testId);
    }

    @Test
    void getOpcion_ConIdInexistente_DebeRetornar404() {
        when(preguntaOpcionDAO.leer(testId)).thenReturn(null);

        Response response = resource.getOpcion(testId.toString());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
    }

    @Test
    void getOpcion_ConUuidInvalido_DebeRetornar400() {
        Response response = resource.getOpcion("no-es-uuid");

        assertEquals(400, response.getStatus());
        verifyNoInteractions(preguntaOpcionDAO);
    }

    @Test
    void getOpcion_ConExcepcionEnDAO_DebeRetornar500() {
        when(preguntaOpcionDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.getOpcion(testId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== updateOpcion (PUT /{idOpcion}) ====================

    @Test
    void updateOpcion_ConDatosValidos_DebeRetornar200() {
        when(preguntaOpcionDAO.leer(testId)).thenReturn(entidad);
        PreguntaOpcion actualizada = new PreguntaOpcion();
        actualizada.setEsCorrecta(true);
        when(preguntaOpcionDAO.actualizar(actualizada)).thenReturn(actualizada);

        Response response = resource.updateOpcion(testId.toString(), actualizada);

        assertEquals(200, response.getStatus());
        assertSame(actualizada, response.getEntity());
        assertEquals(testId, actualizada.getIdPreguntaOpcion());
        verify(preguntaOpcionDAO).actualizar(actualizada);
    }

    @Test
    void updateOpcion_ConIdInexistente_DebeRetornar404() {
        when(preguntaOpcionDAO.leer(testId)).thenReturn(null);

        Response response = resource.updateOpcion(testId.toString(), new PreguntaOpcion());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
        verify(preguntaOpcionDAO, never()).actualizar(any());
    }

    @Test
    void updateOpcion_ConUuidInvalido_DebeRetornar409() {
        Response response = resource.updateOpcion("no-es-uuid", entidad);

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
        verifyNoInteractions(preguntaOpcionDAO);
    }

    @Test
    void updateOpcion_ConIllegalArgumentEnActualizar_DebeRetornar409() {
        when(preguntaOpcionDAO.leer(testId)).thenReturn(entidad);
        PreguntaOpcion datos = new PreguntaOpcion();
        when(preguntaOpcionDAO.actualizar(datos))
                .thenThrow(new IllegalArgumentException("Datos inválidos"));

        Response response = resource.updateOpcion(testId.toString(), datos);

        assertEquals(409, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.CONFLICT_REASON));
    }

    @Test
    void updateOpcion_ConExcepcionEnLeer_DebeRetornar500() {
        when(preguntaOpcionDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.updateOpcion(testId.toString(), entidad);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    @Test
    void updateOpcion_ConExcepcionEnActualizar_DebeRetornar500() {
        when(preguntaOpcionDAO.leer(testId)).thenReturn(entidad);
        PreguntaOpcion datos = new PreguntaOpcion();
        when(preguntaOpcionDAO.actualizar(datos)).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.updateOpcion(testId.toString(), datos);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }

    // ==================== deleteOpcion (DELETE /{idOpcion}) ====================

    @Test
    void deleteOpcion_ConIdExistente_DebeRetornar204() {
        when(preguntaOpcionDAO.leer(testId)).thenReturn(entidad);

        Response response = resource.deleteOpcion(testId.toString());

        assertEquals(204, response.getStatus());
        verify(preguntaOpcionDAO).eliminar(entidad);
    }

    @Test
    void deleteOpcion_ConIdInexistente_DebeRetornar404() {
        when(preguntaOpcionDAO.leer(testId)).thenReturn(null);

        Response response = resource.deleteOpcion(testId.toString());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
        verify(preguntaOpcionDAO, never()).eliminar(any());
    }

    @Test
    void deleteOpcion_ConUuidInvalido_DebeRetornar500() {
        Response response = resource.deleteOpcion("no-es-uuid");

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
        verifyNoInteractions(preguntaOpcionDAO);
    }

    @Test
    void deleteOpcion_ConExcepcionEnDAO_DebeRetornar500() {
        when(preguntaOpcionDAO.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.deleteOpcion(testId.toString());

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }
}
