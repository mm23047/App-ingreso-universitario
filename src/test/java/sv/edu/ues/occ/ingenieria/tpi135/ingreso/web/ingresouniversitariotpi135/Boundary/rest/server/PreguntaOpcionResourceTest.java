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

    @Mock private PreguntaOpcionDAO dao;
    @InjectMocks private PreguntaOpcionResource resource;

    private PreguntaOpcion testOpcion;
    private String idOpcionStr;

    @BeforeEach
    void setUp() {
        testOpcion = new PreguntaOpcion();
        testOpcion.setIdPreguntaOpcion(UUID.randomUUID());
        testOpcion.setEsCorrecta(false);
        idOpcionStr = testOpcion.getIdPreguntaOpcion().toString();
    }

    @Test
    void updateOpcion_ConDatosValidos_Retorna200() {
        PreguntaOpcion payload = new PreguntaOpcion();
        when(dao.leer(testOpcion.getIdPreguntaOpcion())).thenReturn(testOpcion);

        Response response = resource.updateOpcion(idOpcionStr, payload);

        assertEquals(200, response.getStatus());
        verify(dao).actualizar(payload);
    }

    @Test
    void updateOpcion_ConIdInexistente_Retorna404() {
        when(dao.leer(any())).thenReturn(null);

        Response response = resource.updateOpcion(idOpcionStr, new PreguntaOpcion());

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
        verify(dao, never()).actualizar(any());
    }

    @Test
    void deleteOpcion_ConIdExistente_Retorna204() {
        when(dao.leer(testOpcion.getIdPreguntaOpcion())).thenReturn(testOpcion);

        Response response = resource.deleteOpcion(idOpcionStr);

        assertEquals(204, response.getStatus());
        verify(dao).eliminar(testOpcion);
    }

    @Test
    void deleteOpcion_ConIdInexistente_Retorna404() {
        when(dao.leer(any())).thenReturn(null);

        Response response = resource.deleteOpcion(idOpcionStr);

        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.NOT_FOUND_ID));
        verify(dao, never()).eliminar(any());
    }

    @Test
    void deleteOpcion_ConExcepcionEnDAO_Retorna500() {
        when(dao.leer(any())).thenThrow(new RuntimeException("Error de BD"));

        Response response = resource.deleteOpcion(idOpcionStr);

        assertEquals(500, response.getStatus());
        assertNotNull(response.getHeaderString(RestHeaders.SERVER_EXCEPTION));
    }
}
